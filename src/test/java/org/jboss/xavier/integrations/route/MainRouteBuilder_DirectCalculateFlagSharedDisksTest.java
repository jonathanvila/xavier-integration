package org.jboss.xavier.integrations.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.commons.io.IOUtils;
import org.jboss.xavier.analytics.pojo.output.AnalysisModel;
import org.jboss.xavier.analytics.pojo.output.workload.inventory.WorkloadInventoryReportModel;
import org.jboss.xavier.integrations.jpa.service.AnalysisService;
import org.jboss.xavier.integrations.jpa.service.WorkloadInventoryReportService;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MainRouteBuilder_DirectCalculateFlagSharedDisksTest extends XavierCamelTest {
    @Inject
    AnalysisService analysisService;

    @MockBean
    private WorkloadInventoryReportService workloadInventoryReportService;

    @Test
    public void mainRouteBuilder_DirectCalculate_JSONGiven_ShouldCalculateCorrectSharedDiskVMs() throws Exception {
        //Given
        AnalysisModel analysisModel = analysisService.buildAndSave("report name", "report desc", "file name", "user name", "user_account_number");
        Set<String> expectedVmNamesWithSharedDisk = new HashSet<>();
        expectedVmNamesWithSharedDisk.add("dev-windows-server-2008-TEST");
        expectedVmNamesWithSharedDisk.add("james-db-03-copy");
        expectedVmNamesWithSharedDisk.add("dev-windows-server-2008");
        expectedVmNamesWithSharedDisk.add("pemcg-rdm-test");
        String customerId = "CID123";
        String fileName = "cloudforms-export-v1.json";
        Long analysisId = analysisModel.getId();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", fileName);
        metadata.put("org_id", customerId);
        metadata.put(RouteBuilderExceptionHandler.ANALYSIS_ID, analysisId.toString());

        Map<String, Object> headers = new HashMap<>();
        headers.put(RouteBuilderExceptionHandler.MA_METADATA, metadata);
        headers.put(RouteBuilderExceptionHandler.USERNAME, "user name");

        //When
        camelContext.start();
        camelContext.startRoute("flags-shared-disks");
        String body = IOUtils.resourceToString(fileName, StandardCharsets.UTF_8, this.getClass().getClassLoader());

        Exchange result = camelContext.createProducerTemplate().send("direct:flags-shared-disks", exchange -> {
            exchange.getIn().setBody(body);
            exchange.getIn().setHeaders(headers);
        });

        assertThat(result.getIn().getHeader("vmNamesWithSharedDisk", Set.class)).isEqualTo(expectedVmNamesWithSharedDisk);

        camelContext.stop();
    }

    @Test
    public void mainRouteBuilder_DirectCalculate_JSONOnVersion1_0_0Given_ShouldCalculateCorrectSharedDiskVMs() throws Exception {
        //Given
        camelContext.getRouteDefinition("flags-shared-disks").adviceWith(camelContext, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                mockEndpointsAndSkip("direct:vm-workload-inventory");
            }
        });

        AnalysisModel analysisModel = analysisService.buildAndSave("report name", "report desc", "file name", "user name", "user_account_number");
        Set<String> expectedVmNamesWithSharedDisk = new HashSet<>();
        expectedVmNamesWithSharedDisk.add("tomcat");
        expectedVmNamesWithSharedDisk.add("lb");


        String customerId = "CID123";
        String fileName = "cloudforms-export-v1_0_0.json";
        Long analysisId = analysisModel.getId();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", fileName);
        metadata.put("org_id", customerId);
        metadata.put(RouteBuilderExceptionHandler.ANALYSIS_ID, analysisId.toString());

        Map<String, Object> headers = new HashMap<>();
        headers.put(RouteBuilderExceptionHandler.MA_METADATA, metadata);
        headers.put(RouteBuilderExceptionHandler.USERNAME, "user name");

        //When
        camelContext.start();
        camelContext.startRoute("flags-shared-disks");
        String body = IOUtils.resourceToString(fileName, StandardCharsets.UTF_8, this.getClass().getClassLoader());

        //Then
        Exchange result = camelContext.createProducerTemplate().send("direct:flags-shared-disks", exchange -> {
            exchange.getIn().setBody(body);
            exchange.getIn().setHeaders(headers);
        });

        assertThat(result.getIn().getHeader("vmNamesWithSharedDisk")).isEqualTo(expectedVmNamesWithSharedDisk);

        camelContext.stop();
    }

}
