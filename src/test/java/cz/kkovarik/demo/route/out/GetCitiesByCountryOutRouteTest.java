package cz.kkovarik.demo.route.out;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import cz.kkovarik.demo.CamelDemoApplication;
import cz.kkovarik.demo.dto.GetCitiesRequestDto;
import cz.kkovarik.demo.in.weather.model.GetCitiesByCountryResponse;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
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
public class GetCitiesByCountryOutRouteTest {

    @Autowired
    private CamelContext context;

    @Autowired
    private ProducerTemplate template;

    @EndpointInject(uri = "mock:webServiceMock")
    private MockEndpoint webserviceMock;

    @EndpointInject(uri = "mock:resultMock")
    private MockEndpoint resultMock;

    @Before
    public void setupMocks() throws Exception {
        context.getRouteDefinition(GetCitiesByCountryOutRoute.ROUTE_ID)
                .adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById(GetCitiesByCountryOutRoute.WEBSERVICE_NODE_ID).replace().to(webserviceMock);
                weaveAddLast().to(resultMock);
            }
        });
        // mock web service response
        webserviceMock.whenAnyExchangeReceived(exchange ->
                exchange.getOut().setBody(webserviceResultXML()));
    }

    @Test
    public void test_ok() throws Exception {
        webserviceMock.setExpectedCount(1);
        resultMock.setExpectedCount(1);

        final GetCitiesRequestDto requestDto = new GetCitiesRequestDto();
        requestDto.setCountry("Germany");
        requestDto.setFileName("test.txt");

        template.sendBody(GetCitiesByCountryOutRoute.URI_GET_CITIES_BY_COUNTRY, requestDto);

        webserviceMock.assertIsSatisfied();
        resultMock.assertIsSatisfied();

        // request
        final String request = webserviceMock.getReceivedExchanges().get(0).getIn().getMandatoryBody(String.class);
        assertXMLEqual("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<GetCitiesByCountry xmlns=\"http://www.webserviceX.NET\">\n"
                + "    <CountryName>Germany</CountryName>\n"
                + "</GetCitiesByCountry>\n", request);

        // response
        final GetCitiesByCountryResponse response =
                resultMock.getReceivedExchanges().get(0).getIn().getMandatoryBody(GetCitiesByCountryResponse.class);
        assertThat(response.getGetCitiesByCountryResult(), notNullValue());
    }

    private static String webserviceResultXML() {
        return "<GetCitiesByCountryResponse xmlns=\"http://www.webserviceX.NET\">\n"
                + "  <GetCitiesByCountryResult><![CDATA[<NewDataSet>\n"
                + "  <Table>\n"
                + "    <Country>Slovakia</Country>\n"
                + "    <City>Kosice</City>\n"
                + "  </Table>\n"
                + "  <Table>\n"
                + "    <Country>Slovakia</Country>\n"
                + "    <City>Piestany</City>\n"
                + "  </Table>\n"
                + "  <Table>\n"
                + "    <Country>Slovakia</Country>\n"
                + "    <City>Sliac</City>\n"
                + "  </Table>\n"
                + "  <Table>\n"
                + "    <Country>Slovakia</Country>\n"
                + "    <City>Bratislava Ivanka</City>\n"
                + "  </Table>\n"
                + "  <Table>\n"
                + "    <Country>Slovakia</Country>\n"
                + "    <City>Kamenica Nad Cirochou</City>\n"
                + "  </Table>\n"
                + "  <Table>\n"
                + "    <Country>Slovakia</Country>\n"
                + "    <City>Lucenec</City>\n"
                + "  </Table>\n"
                + "  <Table>\n"
                + "    <Country>Slovakia</Country>\n"
                + "    <City>Poprad / Tatry</City>\n"
                + "  </Table>\n"
                + "</NewDataSet>]]></GetCitiesByCountryResult>\n"
                + "</GetCitiesByCountryResponse>\n\n";
    }
}