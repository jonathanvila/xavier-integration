package org.jboss.xavier.integrations.route;

import org.apache.camel.builder.RouteBuilder;
import org.jboss.xavier.Application;
import org.jboss.xavier.analytics.pojo.output.AnalysisModel;
import org.jboss.xavier.analytics.pojo.output.InitialSavingsEstimationReportModel;
import org.jboss.xavier.analytics.pojo.output.workload.inventory.WorkloadInventoryReportModel;
import org.jboss.xavier.integrations.jpa.repository.AnalysisRepository;
import org.jboss.xavier.integrations.jpa.repository.InitialSavingsEstimationReportRepository;
import org.jboss.xavier.integrations.jpa.repository.WorkloadInventoryReportRepository;
import org.jboss.xavier.integrations.jpa.service.AnalysisService;
import org.jboss.xavier.integrations.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Routes_ModelIntegrityTest extends XavierCamelTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private WorkloadInventoryReportRepository workloadInventoryReportRepository;

    @Autowired
    private InitialSavingsEstimationReportRepository initialSavingsEstimationReportRepository;

    @Value("${camel.component.servlet.mapping.context-path}")
    String camel_context;

    @Before
    public void setup() {
        camel_context = camel_context.substring(0, camel_context.indexOf("*"));
    }

    @Test
    public void setInitialSavingsEstimationReportModel_Test() throws Exception {
        //Given
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("rest:post:/create-analysis-test")
                        .routeId("create-analysis-test")
                        .to("direct:check-authenticated-request")
                        .process(exchange -> {
                            AnalysisModel analysisModel = analysisService.buildAndSave(
                                    "reportName",
                                    "reportDescription",
                                    "payloadName",
                                    "mrizzi@redhat.com"
                            );

                            // First insert
                            InitialSavingsEstimationReportModel initialSavingsEstimationReportModel1 = new InitialSavingsEstimationReportModel();
                            analysisService.setInitialSavingsEstimationReportModel(initialSavingsEstimationReportModel1, analysisModel.getId());

                            // Second insert
                            InitialSavingsEstimationReportModel initialSavingsEstimationReportModel2 = new InitialSavingsEstimationReportModel();
                            analysisService.setInitialSavingsEstimationReportModel(initialSavingsEstimationReportModel2, analysisModel.getId());

                            List<AnalysisModel> all = analysisRepository.findAll();
                            assertThat(all.get(0).getInitialSavingsEstimationReportModel()).isNotNull();

                            List<InitialSavingsEstimationReportModel> initialSavingsEstimationReportModels = initialSavingsEstimationReportRepository.findByAnalysisIdEquals(analysisModel.getId());
                            assertThat(initialSavingsEstimationReportModels.size()).isEqualTo(1);
                        });
            }
        });

        //When
        camelContext.start();
        TestUtil.startUsernameRoutes(camelContext);
        camelContext.startRoute("create-analysis-test");
        camelContext.startRoute("reports-get-all");

        HttpHeaders headers = new HttpHeaders();
        headers.set(TestUtil.HEADER_RH_IDENTITY, TestUtil.getBase64RHIdentity());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response1 = restTemplate.exchange(camel_context + "/create-analysis-test/", HttpMethod.POST, entity, String.class);

        // Then
        assertThat(response1.getStatusCodeValue()).isEqualTo(200);
        camelContext.stop();
    }

    @Test
    public void addWorkloadInventoryReportModels_test() throws Exception {
        //Given
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("rest:post:/create-analysis-test")
                        .routeId("create-analysis-test")
                        .to("direct:check-authenticated-request")
                        .process(exchange -> {
                            AnalysisModel analysisModel = analysisService.buildAndSave(
                                    "reportName",
                                    "reportDescription",
                                    "payloadName",
                                    "mrizzi@redhat.com"
                            );

                            // First insert
                            List<WorkloadInventoryReportModel> workloadInventoryReportModels1 = Arrays.asList(new WorkloadInventoryReportModel(), new WorkloadInventoryReportModel());
                            analysisService.addWorkloadInventoryReportModels(workloadInventoryReportModels1, analysisModel.getId());

                            // Second insert
                            List<WorkloadInventoryReportModel> workloadInventoryReportModels2 = Arrays.asList(new WorkloadInventoryReportModel(), new WorkloadInventoryReportModel());
                            analysisService.addWorkloadInventoryReportModels(workloadInventoryReportModels2, analysisModel.getId());

                            List<AnalysisModel> all = analysisRepository.findAll();
                            assertThat(all.get(0).getWorkloadInventoryReportModels()).isNotNull();

                            List<WorkloadInventoryReportModel> workloadInventoryReportModels = workloadInventoryReportRepository.findByAnalysisIdEquals(analysisModel.getId());
                            assertThat(workloadInventoryReportModels.size()).isEqualTo(2);
                        });
            }
        });

        //When
        camelContext.start();
        TestUtil.startUsernameRoutes(camelContext);
        camelContext.startRoute("create-analysis-test");
        camelContext.startRoute("reports-get-all");

        HttpHeaders headers = new HttpHeaders();
        headers.set(TestUtil.HEADER_RH_IDENTITY, TestUtil.getBase64RHIdentity());
        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<String> response1 = restTemplate.exchange(camel_context + "/create-analysis-test/", HttpMethod.POST, entity, String.class);

        // Then
        assertThat(response1.getStatusCodeValue()).isEqualTo(200);
        camelContext.stop();
    }

}
