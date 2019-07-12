package org.jboss.xavier.integrations.migrationanalytics.output;

import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.apache.commons.io.IOUtils;
import org.jboss.xavier.analytics.pojo.input.UploadFormInputDataModel;
import org.jboss.xavier.integrations.Application;
import org.jboss.xavier.integrations.migrationanalytics.business.Calculator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CamelSpringBootRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = {Application.class})
@UseAdviceWith // Disables automatic start of Camel context
@ActiveProfiles("test")
public class ParamsCalculatorTest {
    @Inject 
    private Calculator reportCalculator;

    @Test
    public void analyticsCalculator_calculate_CloudFormsModelWith32coresAnd16cpupercoreGiven_ShouldReturn1HostAndTotalDiskSpace() throws IOException {
        // Given
        String filename = "cloudforms-export-v1.json";
        String customerid = "CIDE9988";

        String cloudFormJSON = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(filename), "UTF-8");

        Integer hypervisor = 1;
        Long totaldiskspace = 2470679937024L;

        Integer sourceproductindicator = 1;
        Double year1hypervisorpercentage = 10D;
        Double year2hypervisorpercentage = 20D;
        Double year3hypervisorpercentage = 30D;
        Double growthratepercentage = 7D;
        UploadFormInputDataModel expectedFormInputDataModel = new UploadFormInputDataModel(customerid, filename, hypervisor, totaldiskspace, sourceproductindicator, year1hypervisorpercentage, year2hypervisorpercentage, year3hypervisorpercentage, growthratepercentage);

        Map<String, Object> headers = new HashMap<>();
        headers.put("filename", filename);
        headers.put("customerid", customerid);
        headers.put("sourceproductindicator", sourceproductindicator);
        headers.put("year1hypervisorpercentage", year1hypervisorpercentage);
        headers.put("year2hypervisorpercentage", year2hypervisorpercentage);
        headers.put("year3hypervisorpercentage", year3hypervisorpercentage);
        headers.put("growthratepercentage", growthratepercentage);
        
        // When
        UploadFormInputDataModel inputDataModelCalculated = reportCalculator.calculate(cloudFormJSON, headers);

        // Then
        assertThat(inputDataModelCalculated).isEqualToComparingFieldByFieldRecursively(expectedFormInputDataModel);
    }
}