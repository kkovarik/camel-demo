package cz.kkovarik.demo.route.in;


import static com.github.npathai.hamcrestopt.OptionalMatchers.hasValue;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import cz.kkovarik.demo.CamelDemoApplication;
import cz.kkovarik.demo.dto.GetCitiesRequestDto;
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
import org.springframework.test.annotation.DirtiesContext;


/**
 * @author <a href="mailto:kovarikkarel@gmail.com">Karel Kovarik</a>
 */
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = {CamelDemoApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GetCitiesByCountryRestRouteTest {

    private static final String TEST_START_URI = "direct:testStart";

    @Autowired
    private CamelContext context;

    @Autowired
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:getCitiesOut")
    private MockEndpoint mockGetCitiesOut;

    @EndpointInject(uri = "mock:mockFileStorageOut")
    private MockEndpoint mockFileStorageOut;

    @Before
    public void setupMocks() throws Exception {
        context.getRouteDefinition(GetCitiesByCountryRestRoute.REST_ROUTE_ID)
                .adviceWith(context, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        replaceFromWith(TEST_START_URI);
                    }
                });

        context.getRouteDefinition(GetCitiesByCountryRestRoute.CALL_WEB_SERVICE_ROUTE_ID)
                .adviceWith(context, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        weaveById(GetCitiesByCountryRestRoute.CALL_WEB_SERVICE_NODE_ID)
                                .replace().to(mockGetCitiesOut);
                    }
                });

        context.getRouteDefinition(GetCitiesByCountryRestRoute.SAVE_TO_FILE_ROUTE_ID)
                .adviceWith(context, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        weaveById(GetCitiesByCountryRestRoute.SAVE_TO_FILE_NODE_ID).replace().to(mockFileStorageOut);
                    }
                });
    }

    @Test
    public void test_withWriteToFile() throws Exception {

        mockGetCitiesOut.whenAnyExchangeReceived(exchange -> {
            GetCitiesByCountryResponse response = new GetCitiesByCountryResponse();
            response.setGetCitiesByCountryResult("response");
            exchange.getOut().setBody(response);
        });

        mockGetCitiesOut.setExpectedMessageCount(1);
        mockFileStorageOut.setExpectedMessageCount(1);

        Exchange inputExchange = new DefaultExchange(context);
        inputExchange.getIn().setHeader("countryName", "Germany");
        inputExchange.getIn().setHeader("fileName", "output.txt");

        final Exchange result = template.send(TEST_START_URI, inputExchange);

        mockGetCitiesOut.assertIsSatisfied();
        mockFileStorageOut.assertIsSatisfied();

        // verify json response
        final String jsonResponse = result.getOut().getMandatoryBody(String.class);
        assertThat(jsonResponse, hasJsonPath("listOfCities", is("response")));

        // verify request to GetCities out route
        final GetCitiesRequestDto getCitiesRequest =
                mockGetCitiesOut.getReceivedExchanges().get(0).getIn().getMandatoryBody(GetCitiesRequestDto.class);
        assertThat(getCitiesRequest.getCountry(), is("Germany"));
        assertThat(getCitiesRequest.getFileName(), hasValue("output.txt"));

        // verify request to FileStorage out route
        final GetCitiesRequestDto fileStorageRequest =
                mockGetCitiesOut.getReceivedExchanges().get(0).getIn().getMandatoryBody(GetCitiesRequestDto.class);
        assertThat(fileStorageRequest.getCountry(), is("Germany"));
        assertThat(fileStorageRequest.getFileName(), hasValue("output.txt"));
    }

    @Test
    public void test_notWriteToFile() throws Exception {

        mockGetCitiesOut.whenAnyExchangeReceived(exchange -> {
            GetCitiesByCountryResponse response = new GetCitiesByCountryResponse();
            response.setGetCitiesByCountryResult("response");
            exchange.getOut().setBody(response);
        });

        mockGetCitiesOut.setExpectedMessageCount(1);
        mockFileStorageOut.setExpectedMessageCount(0);

        Exchange inputExchange = new DefaultExchange(context);
        inputExchange.getIn().setHeader("countryName", "Germany");
        // no fileName header

        template.send(TEST_START_URI, inputExchange);

        // only endpoint satisfaction
        mockGetCitiesOut.assertIsSatisfied();
        mockFileStorageOut.assertIsSatisfied();
    }

    @Test
    public void test_errorFromSoap() throws Exception {

        mockGetCitiesOut.whenAnyExchangeReceived(exchange -> {
            throw new RuntimeException("simulated soap exception");
        });

        mockGetCitiesOut.setExpectedMessageCount(1);
        mockFileStorageOut.setExpectedMessageCount(0);

        Exchange inputExchange = new DefaultExchange(context);
        inputExchange.getIn().setHeader("countryName", "Germany");
        final Exchange result = template.send(TEST_START_URI, inputExchange);

        mockGetCitiesOut.assertIsSatisfied();
        mockFileStorageOut.assertIsSatisfied();

        // verify error json response
        final String jsonResponse = result.getOut().getMandatoryBody(String.class);
        assertThat(jsonResponse, hasJsonPath("message", is("simulated soap exception")));
        assertThat(jsonResponse, hasJsonPath("messageId", notNullValue()));
    }
}