package cz.kkovarik.demo.route.out;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import cz.kkovarik.demo.CamelDemoApplication;
import cz.kkovarik.demo.in.weather.model.GetCitiesByCountryResponse;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * @author <a href="mailto:kovarikkarel@gmail.com">Karel Kovarik</a>
 */
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = {
        CamelDemoApplication.class
})
public class FileStorageOutRouteTest {

    @Autowired
    private CamelContext context;

    @Autowired
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:file")
    private MockEndpoint fileMock;

    @Before
    public void setupMocks() throws Exception {
        context.getRouteDefinition(FileStorageOutRoute.ROUTE_ID)
                .adviceWith(context, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        weaveById(FileStorageOutRoute.TO_FILE_NODE_ID).replace().to(fileMock);
                    }
                });
    }

    @Test
    public void test_SaveGetCitiesByCountry() throws Exception {

        fileMock.setExpectedMessageCount(1);

        Exchange exchange = new DefaultExchange(context);
        exchange.setProperty(FileStorageOutRoute.FILE_NAME_PROPERTY, "testovaci.txt");
        GetCitiesByCountryResponse response = new GetCitiesByCountryResponse();
        response.setGetCitiesByCountryResult("data-to-write");
        exchange.getIn().setBody(response);

        final Exchange result = template.send(FileStorageOutRoute.URI_SAVE_TO_FILE, exchange);

        fileMock.assertIsSatisfied();

        assertThat(result.getIn().getHeader(Exchange.FILE_NAME, String.class), is("testovaci.txt"));
        assertThat(result.getIn().getMandatoryBody(String.class), is("data-to-write"));
    }

}