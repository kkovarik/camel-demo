package cz.kkovarik.demo.route.out;

import cz.kkovarik.demo.in.weather.model.GetCitiesByCountry;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.springframework.stereotype.Component;


/**
 * @author <a href="mailto:kovarikkarel@gmail.com">Karel Kovarik</a>
 */
@Component
public class GetCitiesByCountryOutRoute extends RouteBuilder {

    public static final String ROUTE_ID = "GET-CITIES-BY-COUNTRY-SOAP-OUT";
    public static final String URI_GET_CITIES_BY_COUNTRY = "direct:" + ROUTE_ID;
    static final String WEBSERVICE_NODE_ID = ROUTE_ID + "-TO-WEBSERVICE";

    private static final String WEB_SERVICE_URL = "http://www.webservicex.com/globalweather.asmx";
    private static final String SOAP_ACTION = "http://www.webserviceX.NET/GetCitiesByCountry";

    private static final JaxbDataFormat JAXB = new JaxbDataFormat();
    static {
        JAXB.setContextPath("cz.kkovarik.demo.in.weather.model");
    }

    @Override
    public void configure() throws Exception {
        errorHandler(noErrorHandler());

        from(URI_GET_CITIES_BY_COUNTRY)
                .routeId(ROUTE_ID).convertBodyTo(GetCitiesByCountry.class)
                .marshal(JAXB)
                .to("spring-ws:" + WEB_SERVICE_URL + "?soapAction=" + SOAP_ACTION)
                    .id(WEBSERVICE_NODE_ID)
                .unmarshal(JAXB);
    }
}
