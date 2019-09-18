package org.jboss.xavier.integrations.route;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.UseAdviceWith;
import org.jboss.xavier.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(CamelSpringBootRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@UseAdviceWith // Disables automatic start of Camel context
@SpringBootTest(classes = {Application.class})
@ActiveProfiles("test")
public class MainRouteBuilder_DirectAddFileNumberHeaderTest {
    @Autowired
    CamelContext camelContext;

    @Test
    public void mainRouteBuilder_routeDirectAddFileNumberHeader_ContentGiven_ShouldAddHeaderInExchange() throws Exception {
        //Given
        camelContext.setTracing(true);
        camelContext.setAutoStartup(false);
        String fileName = "cloudforms-export-v1-multiple-files.tar";

        //When
        camelContext.start();
        camelContext.startRoute("add-file-number-header");

        Map<String, Object> headers = new HashMap<>();

        Exchange result = camelContext.createProducerTemplate().request("direct:add-file-number-header",  exchange -> {
            exchange.getIn().setBody(getClass().getClassLoader().getResourceAsStream(fileName));
            exchange.getIn().setHeaders(headers);
        });

        //Then
        assertThat(result.getIn().getHeader("analysisPayloadsToBeAnalyzed")).isEqualTo(2);
        camelContext.stop();
    }

}
