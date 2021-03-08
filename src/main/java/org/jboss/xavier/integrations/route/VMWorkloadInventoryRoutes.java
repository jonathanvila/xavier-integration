package org.jboss.xavier.integrations.route;

import org.jboss.xavier.analytics.pojo.input.workload.inventory.VMWorkloadInventoryModel;
import org.jboss.xavier.analytics.pojo.output.workload.inventory.WorkloadInventoryReportModel;
import org.jboss.xavier.integrations.jpa.service.WorkloadInventoryReportService;
import org.jboss.xavier.integrations.route.strategy.WorkloadInventoryReportModelAggregationStrategy;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Named
public class VMWorkloadInventoryRoutes extends RouteBuilderExceptionHandler {

    @Inject
    WorkloadInventoryReportService workloadInventoryReportService;

    @Value("${parallel.wir}")
    private boolean parallel;

    @Override
    public void configure() throws Exception {
        super.configure();

        from("direct:calculate-vmworkloadinventory").routeId("calculate-vmworkloadinventory")
            .setHeader("KieSessionId", constant("WorkloadInventoryKSession0"))
            .bean("VMWorkloadInventoryCalculator", "calculate(${body}, ${header.${type:org.jboss.xavier.integrations.route.MainRouteBuilder.MA_METADATA}})", false)
                .process(exchange -> {
                    Set<String> vmNamesWithSharedDisk = exchange.getIn().getHeader("vmNamesWithSharedDisk", Set.class);
                    Collection<VMWorkloadInventoryModel>  allVms = exchange.getIn().getBody(Collection.class);
                    allVms.forEach(vmWorkloadInventoryModel ->
                    {
                        if (vmNamesWithSharedDisk != null && vmNamesWithSharedDisk.contains(vmWorkloadInventoryModel.getVmName())) {
                            vmWorkloadInventoryModel.setHasSharedVmdk(true);
                        }
                    });
                    exchange.getIn().setBody(allVms);
                    exchange.getIn().removeHeader("vmNamesWithSharedDisk");
                    exchange.getIn().removeHeader("originalBody");
                })
                .split(body()).parallelProcessing(parallel).aggregationStrategy(new WorkloadInventoryReportModelAggregationStrategy())
                .setHeader(ANALYSIS_ID, simple("${body." + ANALYSIS_ID + "}", String.class))
                .removeHeader("vmNamesWithSharedDisk")
                .to("direct:vm-workload-inventory")
            .end()
            .process(exchange -> analysisService.addWorkloadInventoryReportModels(exchange.getIn().getBody(List.class),
                    Long.parseLong(exchange.getIn().getHeader(MA_METADATA, Map.class).get(ANALYSIS_ID).toString())));

        from ("direct:vm-workload-inventory").routeId("extract-vmworkloadinventory")
            .transform().method("decisionServerHelper", "generateCommands(${body}, \"GetWorkloadInventoryReports\", ${header.KieSessionId})")
            .to("direct:decisionserver").id("workload-decisionserver")
            .transform().method("decisionServerHelper", "extractWorkloadInventoryReportModel");

        from("direct:flags-shared-disks").routeId("flags-shared-disks")
                .process(exchange -> {
                    String originalBody = exchange.getIn().getBody(String.class);
                    exchange.getIn().setHeader("originalBody", originalBody);
                })
            .bean("flagSharedDisksCalculator", "calculate(${body}, ${header.${type:org.jboss.xavier.integrations.route.MainRouteBuilder.MA_METADATA}})", false)
            .process(exchange -> {
                String originalBody = exchange.getIn().getHeader("originalBody", String.class);
                Set<String> vmNamesWithSharedDisk = exchange.getIn().getBody(Set.class);
                exchange.getIn().setHeader("vmNamesWithSharedDisk", vmNamesWithSharedDisk);
                exchange.getIn().setBody(originalBody);
            }).to("direct:calculate-vmworkloadinventory");


    }
}
