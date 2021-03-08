package org.jboss.xavier.integrations.route;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Attachment;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.component.aws.s3.S3Constants;
import org.apache.camel.dataformat.tarfile.TarSplitter;
import org.apache.camel.dataformat.zipfile.ZipSplitter;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.processor.aggregate.GroupedBodyAggregationStrategy;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.jboss.xavier.analytics.pojo.PayloadDownloadLinkModel;
import org.jboss.xavier.analytics.pojo.input.UploadFormInputDataModel;
import org.jboss.xavier.analytics.pojo.output.AnalysisModel;
import org.jboss.xavier.integrations.jpa.service.UserService;
import org.jboss.xavier.integrations.rbac.RBACRouteBuilder;
import org.jboss.xavier.integrations.route.dataformat.CustomizedMultipartDataFormat;
import org.jboss.xavier.integrations.route.model.notification.FilePersistedNotification;
import org.jboss.xavier.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.camel.builder.PredicateBuilder.not;

/**
 * A Camel Java8 DSL Router
 */
@Component
public class MainRouteBuilder extends RouteBuilderExceptionHandler {

    @Value("${thread.concurrentConsumers:5}")
    private int CONCURRENT_CONSUMERS;

    @Value("${insights.upload.host}")
    private String uploadHost;

    @Value("${insights.kafka.host}")
    private String kafkaHost;

    @Value("${insights.upload.mimetype}")
    private String mimeType;

    @Value("${insights.upload.accountnumber}")
    private String accountNumber;

    @Value("#{'${insights.properties}'.split(',')}")
    protected List<String> insightsProperties;

    @Value("${camel.springboot.tracing}")
    private boolean tracingEnabled;

    @Value("${s3.download.link.expiration}")
    private String s3DownloadLinkExpiration;

    @Inject
    private UserService userService;

    private List<Integer> httpSuccessCodes = Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_CREATED, HttpStatus.SC_ACCEPTED, HttpStatus.SC_NO_CONTENT);

    @Inject
    ObjectMapper objectMapper;

    public Processor xRhIdentityHeaderProcessor = exchange -> {
        String xRhIdentityEncoded = exchange.getIn().getHeader(X_RH_IDENTITY, String.class);
        if (xRhIdentityEncoded != null) {
            String xRhIdentityDecoded = new String(Base64.getDecoder().decode(xRhIdentityEncoded));

            JsonNode xRhIdentityJsonNode;
            try {
                xRhIdentityJsonNode = objectMapper.reader().readTree(xRhIdentityDecoded);
            } catch (JsonParseException e) {
                log.error("x-rh-identity could not be parsed to JSON Object");
                return;
            }

            String username = Utils.getFieldValueFromJsonNode(xRhIdentityJsonNode, "identity", "user", "username").map(JsonNode::textValue).orElse(null);
            String userAccountNumber = Utils.getFieldValueFromJsonNode(xRhIdentityJsonNode, "identity", "account_number").map(JsonNode::textValue).orElse(null);

            exchange.getIn().setHeader(USERNAME, username);
            exchange.getIn().setHeader(USER_ACCOUNT_NUMBER, userAccountNumber);
            exchange.getIn().setHeader(X_RH_IDENTITY_JSON_NODE, xRhIdentityJsonNode);
        }
    };

    public void configure() throws Exception {
        super.configure();

        getContext().setTracing(tracingEnabled);

        from("rest:post:/upload?consumes=multipart/form-data")
                .routeId("rest-upload")
                .to("direct:check-authenticated-request")
                .to("direct:fetch-and-process-rbac-user-access")
                // RBAC check start: check min user permissions
                .setHeader(RBACRouteBuilder.RBAC_ENDPOINT_RESOURCE_NAME, constant("payload"))
                .setHeader(RBACRouteBuilder.RBAC_ENDPOINT_RESOURCE_PERMISSION, constant("write"))
                .to("direct:check-rbac-permissions")
                // RBAC check end
                .to("direct:upload");

        from("direct:upload").routeId("direct-upload")
                .unmarshal(new CustomizedMultipartDataFormat())
                .choice()
                    .when(isAllExpectedParamsExist())
                        .split()
                            .attachments()
                            .process(processMultipart())
                            .filter(isFilePart())
                                .to("direct:store")
                            .end()
                        .end()
                    .endChoice()
                    .otherwise()
                      .process(httpError400())
                    .end();

        from("direct:analysis-model").routeId("analysis-model-creation")
                .process(e -> {
                    String userName = e.getIn().getHeader(USERNAME, String.class);
                    String userAccountNumber = e.getIn().getHeader(USER_ACCOUNT_NUMBER, String.class);
                    AnalysisModel analysisModel = analysisService.buildAndSave(
                            (String) e.getIn().getHeader(MA_METADATA, Map.class).get("reportName"),
                            (String) e.getIn().getHeader(MA_METADATA, Map.class).get("reportDescription"),
                            (String) e.getIn().getHeader(Exchange.FILE_NAME),
                            userName,
                            userAccountNumber
                    );
                    e.getIn().getHeader(MA_METADATA, Map.class).put(ANALYSIS_ID, analysisModel.getId().toString());
                });

        from("direct:store").routeId("direct-store")
                .convertBodyTo(byte[].class) // we need this to fully read the stream and close it
                .to("direct:analysis-model")
                .to("direct:insights");

        from("direct:insights").routeId("call-insights-upload-service")
                .process(this::createMultipartToSendToInsights)
                .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
                .setHeader("x-rh-identity", method(MainRouteBuilder.class, "getRHIdentity(${header.x-rh-identity}, ${header.CamelFileName}, ${headers})"))
                .removeHeaders("Camel*")
                .to("http4://" + uploadHost + "/api/ingress/v1/upload")
                .choice()
                    .when(not(isResponseSuccess()))
                        .throwException(org.apache.commons.httpclient.HttpException.class, "Unsuccessful response from Insights Upload Service")
                .end();

        from("kafka:" + kafkaHost + "?topic={{insights.kafka.upload.topic}}&brokers=" + kafkaHost + "&autoOffsetReset=latest&autoCommitEnable=true")
                .routeId("kafka-upload-message")
                .unmarshal().json(JsonLibrary.Jackson, FilePersistedNotification.class)
                .filter(simple("'{{insights.service}}' == ${body.getService}"))
                .to("seda:download-file");

        from("seda:download-file?concurrentConsumers=" + CONCURRENT_CONSUMERS).routeId("download-file")
                .routeId("download-file")
                .setHeader("Exchange.HTTP_URI", simple("${body.url}")).id("setHttpUri")
                .convertBodyTo(FilePersistedNotification.class)
                .setHeader(MA_METADATA, method(MainRouteBuilder.class, "extractMAmetadataHeaderFromIdentity(${body})"))
                .setHeader(X_RH_IDENTITY, simple("${body.b64_identity}"))
                .process(xRhIdentityHeaderProcessor)
                .setBody(constant(""))
                .to("http4:oldhost").id("toOldHost")
                .choice()
                    .when(isResponseSuccess())
                        .removeHeader("Exchange.HTTP_URI")
                        .to("direct:process-file")
                        .log("File ${header.MA_metadata[filename]} success")
                    .otherwise()
                        .throwException(org.apache.commons.httpclient.HttpException.class, "Unsuccessful response from Insights Download Service")
                .end();

        from("direct:process-file").routeId("process-file")
                .convertBodyTo(byte[].class)
                .multicast()
                .to("direct:store-in-s3", "direct:unzip-file");

        from("direct:store-in-s3").routeId("store-in-s3")
                .setHeader(S3Constants.CONTENT_LENGTH, simple("${header.${type:org.apache.camel.Exchange.CONTENT_LENGTH}}"))
                .process(e-> e.getIn().setHeader(S3Constants.KEY, UUID.randomUUID().toString())).id("set-s3-key")
                .setHeader(S3Constants.CONTENT_DISPOSITION, simple("attachment;filename=\"${header.MA_metadata[filename]}\""))
                .to("aws-s3:{{S3_BUCKET}}?amazonS3Client=#s3client&deleteAfterWrite=false").id("s3-call")
                .process(exchange -> analysisService.updatePayloadStorageId(exchange.getIn().getHeader(S3Constants.KEY, String.class),
                        Long.parseLong((String) exchange.getIn().getHeader(MA_METADATA, Map.class).get(ANALYSIS_ID))));

        from("direct:get-s3-payload-link").routeId("get-s3-payload-link")
                .choice()
                    .when(bodyAs(AnalysisModel.class).isNull())
                        .to("direct:request-notfound")
                    .endChoice()
                .end()
                .process(exchange -> {
                    AnalysisModel analysisModel = exchange.getIn().getBody(AnalysisModel.class);

                    String payloadName = analysisModel.getPayloadName();
                    String payloadStorageId = analysisModel.getPayloadStorageId();

                    if (payloadStorageId != null && payloadStorageId.trim().isEmpty()) {
                        payloadStorageId = null;
                    }

                    exchange.getIn().setHeader("AnalysisPayloadName", payloadName);
                    exchange.getIn().setHeader("AnalysisPayloadStorageId", payloadStorageId);
                })
                .choice()
                    .when(header("AnalysisPayloadStorageId").isNull())
                        .process(exchange -> {
                            String fileName = exchange.getIn().getHeader("AnalysisPayloadName", String.class);

                            PayloadDownloadLinkModel payloadDownloadLinkModel = new PayloadDownloadLinkModel(fileName, null);
                            exchange.getIn().setBody(payloadDownloadLinkModel);
                        })
                    .endChoice()
                    .otherwise()
                        .setHeader(S3Constants.KEY, simple("${header.AnalysisPayloadStorageId}"))
                        .setHeader(S3Constants.DOWNLOAD_LINK_EXPIRATION, constant(s3DownloadLinkExpiration))
                        .setHeader(S3Constants.S3_OPERATION, constant("downloadLink"))
                        .to("aws-s3:{{S3_BUCKET}}?amazonS3Client=#s3client").id("aws-s3-get-download-link")
                        .process(exchange -> {
                            String fileName = exchange.getIn().getHeader("AnalysisPayloadName", String.class);
                            String downloadLink = exchange.getIn().getHeader(S3Constants.DOWNLOAD_LINK, String.class);

                            PayloadDownloadLinkModel payloadDownloadLinkModel = new PayloadDownloadLinkModel(fileName, downloadLink);
                            exchange.getIn().setBody(payloadDownloadLinkModel);
                        })
                    .endChoice()
                .end();

        from("direct:unzip-file")
                .routeId("unzip-file")
                .choice()
                    .when(isZippedFile("zip"))
                        .split(new ZipSplitter()).aggregationStrategy(this::calculateICSAggregated)
                        .streaming()
                        .to("direct:calculate")
                    .endChoice()
                    .when(isZippedFile("tar.gz"))
                        .unmarshal().gzip()
                        .split(new TarSplitter()).aggregationStrategy(this::calculateICSAggregated)
                            .streaming()
                            .to("direct:calculate")
                        .end()
                    .endChoice()
                    .otherwise()
                        .to("direct:calculate")
                        .process(exchange -> exchange.getIn().getHeaders().put(UPLOADFORMDATA, exchange.getIn().getBody(UploadFormInputDataModel.class)))
                    .end()
                .end()
                // at this time it's safe to generate the WSR
                .to("direct:send-costsavings")
                .to("direct:calculate-workloadsummaryreportmodel");

        from("direct:calculate").routeId("calculate")
                .convertBodyTo(String.class)
                .multicast().aggregationStrategy(new GroupedBodyAggregationStrategy())
                    .to("direct:calculate-costsavings", "direct:flags-shared-disks" )
                .setBody(e -> e.getIn().getBody(List.class).get(0))
                .end();


        from("direct:send-costsavings").routeId("send-costsavings")
                .setBody(header(UPLOADFORMDATA))
                .to("seda:uploadFormInputDataModel")
                .end();

        from("direct:calculate-costsavings").routeId("calculate-costsavings")
                .transform().method("calculator", "calculate(${body}, ${header.${type:org.jboss.xavier.integrations.route.MainRouteBuilder.MA_METADATA}})")
                .end();

        from("direct:check-authenticated-request")
                .routeId("check-authenticated-request")
                .to("direct:add-username-header")
                .choice()
                    .when(exchange -> {
                        String username = exchange.getIn().getHeader(USERNAME, String.class);
                        return username == null || username.trim().isEmpty();
                    })
                    .to("direct:request-forbidden");

        from("direct:check-authorized-request")
                .routeId("check-authorized-request")
                .choice()
                    .when(exchange -> {
                        String username = (String) exchange.getIn().getHeader(USERNAME);
                        return !userService.isUserAllowedToAdministratorResources(username);
                    })
                    .to("direct:request-forbidden");

        from("direct:add-username-header")
                .routeId("add-username-header")
                .process(xRhIdentityHeaderProcessor);

        from("direct:request-forbidden")
                .routeId("request-forbidden")
                .process(httpError403());

        from("direct:request-notfound")
                .routeId("request-notfound")
                .process(httpError404());
    }

    private Exchange calculateICSAggregated(Exchange old, Exchange neu) {
            UploadFormInputDataModel newObj = (UploadFormInputDataModel) neu.getIn().getBody(List.class).get(0);
            UploadFormInputDataModel oldObj = (old != null) ? old.getIn().getHeader(UPLOADFORMDATA, UploadFormInputDataModel.class) : null;
            newObj.setTotalDiskSpace(newObj.getTotalDiskSpace() + ((oldObj != null) ? oldObj.getTotalDiskSpace() : 0));
            newObj.setHypervisor(newObj.getHypervisor() + ((oldObj != null) ? oldObj.getHypervisor() : 0));
            neu.getIn().getHeaders().put(UPLOADFORMDATA, newObj);
        return neu;
    }

    private Predicate isResponseSuccess() {
        return e -> httpSuccessCodes.contains(e.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
    }

    public Map<String,String> extractMAmetadataHeaderFromIdentity(FilePersistedNotification filePersistedNotification) throws IOException {
        String identity_json = new String(Base64.getDecoder().decode(filePersistedNotification.getB64_identity()));
        JsonNode node= objectMapper.reader().readTree(identity_json);

        Map header = new HashMap<String, String>();
        JsonNode internalNode = node.get("identity").get("internal");
        internalNode.fieldNames().forEachRemaining(field -> header.put(field, internalNode.get(field).asText()));
        return header;
    }

    private Processor httpError400() {
        return exchange -> {
          exchange.getIn().setBody("{ \"error\": \"Bad Request\"}");
          exchange.getIn().setHeader(Exchange.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
          exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
        };
    }

    private Processor httpError403() {
        return exchange -> {
          exchange.getIn().setBody("Forbidden");
          exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_FORBIDDEN);
          exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
        };
    }

    private Processor httpError404() {
        return exchange -> {
            exchange.getIn().setBody("Not found");
            exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpServletResponse.SC_NOT_FOUND);
            exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
        };
    }

    private Predicate isAllExpectedParamsExist() {
        return exchange -> insightsProperties.stream().allMatch(e -> StringUtils.isNoneEmpty((String)(exchange.getIn().getHeader(MA_METADATA, new HashMap<String,Object>(), Map.class)).get(e)));
    }

    private Predicate isFilePart() {
        return exchange -> exchange.getIn().getHeader(CustomizedMultipartDataFormat.CONTENT_DISPOSITION, String.class).contains("filename");
    }

    private void createMultipartToSendToInsights(Exchange exchange) {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipartEntityBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);

        byte[] file = exchange.getIn().getBody(byte[].class);
        multipartEntityBuilder.addPart("file", new ByteArrayBody(file, ContentType.create(mimeType), exchange.getIn().getHeader(Exchange.FILE_NAME, String.class)));
        exchange.getIn().setBody(multipartEntityBuilder.build());
    }

    public String getRHIdentity(String x_rh_identity_base64, String filename, Map<String, Object> headers) throws IOException {
        JsonNode node= objectMapper.reader().readTree(new String(Base64.getDecoder().decode(x_rh_identity_base64)));

        ObjectNode internalNode = (ObjectNode) node.get("identity").get("internal");
        internalNode.put("filename", filename);

        // we add all properties defined on the Insights Properties, that we should have as Headers of the message
        Map<String, String> ma_metadataCasted = (Map<String, String>) headers.get(MA_METADATA);
        insightsProperties.forEach(e -> internalNode.put(e, ma_metadataCasted.get(e)));
        // add the 'analysis_id' value
        String analysisId = ma_metadataCasted.get(ANALYSIS_ID);
        if (analysisId == null)
        {
            throw new IllegalArgumentException("'" + ANALYSIS_ID + "' field not available but it's mandatory");
        }
        internalNode.put(ANALYSIS_ID, analysisId);

        return Base64.getEncoder().encodeToString(node.toString().getBytes(StandardCharsets.UTF_8));
    }

    private Predicate isZippedFile(String extension) {
        return exchange -> {
            String filename = (String) exchange.getIn().getHeader(MA_METADATA, Map.class).get("filename");
            return extension.equalsIgnoreCase(filename.substring(filename.length() - extension.length()));
        };
    }

    private Processor processMultipart() {
        return exchange -> {
            Attachment body = exchange.getIn().getBody(Attachment.class);

            DataHandler dataHandler = body.getDataHandler();

            exchange.getIn().setHeader(Exchange.FILE_NAME, dataHandler.getName());
            exchange.getIn().setHeader(CustomizedMultipartDataFormat.CONTENT_TYPE, dataHandler.getContentType());
            exchange.getIn().setHeader(CustomizedMultipartDataFormat.CONTENT_DISPOSITION, body.getHeader(CustomizedMultipartDataFormat.CONTENT_DISPOSITION));
            exchange.getIn().setBody(dataHandler.getInputStream());
        };
    }
}
