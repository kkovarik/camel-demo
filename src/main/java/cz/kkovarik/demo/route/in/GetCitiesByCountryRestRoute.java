package cz.kkovarik.demo.route.in;

import cz.kkovarik.demo.dto.ExceptionInfoDto;
import cz.kkovarik.demo.dto.GetCitiesRequestDto;
import cz.kkovarik.demo.dto.GetCitiesResponseDto;
import cz.kkovarik.demo.route.out.FileStorageOutRoute;
import cz.kkovarik.demo.route.out.GetCitiesByCountryOutRoute;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.support.ExpressionAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


/**
 * @author <a href="mailto:kovarikkarel@gmail.com">Karel Kovarik</a>
 */
@Component
public class GetCitiesByCountryRestRoute extends RouteBuilder {

    private static final String ROUTE_ID_PREFIX = "GET-CITIES-BY-COUNTRY";
    static final String REST_ROUTE_ID = ROUTE_ID_PREFIX + "-REST";
    static final String SAVE_TO_FILE_ROUTE_ID = ROUTE_ID_PREFIX  + "-SAVE-TO-FILE";
    static final String SAVE_TO_FILE_NODE_ID = SAVE_TO_FILE_ROUTE_ID + "-TO-NODE";
    static final String CALL_WEB_SERVICE_ROUTE_ID = ROUTE_ID_PREFIX + "-WEB-SERVICE";
    static final String CALL_WEB_SERVICE_NODE_ID = CALL_WEB_SERVICE_ROUTE_ID + "-TO-NODE";
    private static final String URI_SAVE_TO_FILE = "direct:" + SAVE_TO_FILE_ROUTE_ID;
    private static final String URI_CALL_WEB_SERVICE = "direct:" + CALL_WEB_SERVICE_ROUTE_ID;


    private static final String COUNTRIES_URI = "/countries";
    private static final String CITIES_URI_SUFFIX = "/cities";
    private static final String ORIGINAL_REQUEST_PROPERTY = CALL_WEB_SERVICE_ROUTE_ID + "-originalRequest";

    @Override
    public void configure() throws Exception {
        restConfiguration().bindingMode(RestBindingMode.json);
        errorHandler(noErrorHandler());

        // GET /countries/{countryName}/cities?fileName=output.txt
        rest(COUNTRIES_URI).produces("application/json")
                .get("/{countryName}" + CITIES_URI_SUFFIX).outType(GetCitiesResponseDto.class)
                .route().routeId(REST_ROUTE_ID)
                // error handling, just wrap to dto
                .onException(Exception.class)
                    .handled(Boolean.TRUE)
                    .process(exchange -> exchange.getIn().setBody(
                            convertToExceptionInfoDto(
                                    exchange.getIn().getMessageId(),
                                    exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)
                    )))
                    .end()
                // transform to request dto
                .transform(new ExpressionAdapter() {
                    @Override
                    public Object evaluate(Exchange exchange) {
                        //TODO (kkovarik, 20.2.2017, TASK) ugly, could it be simpler???
                        final String countryName = exchange.getIn().getHeader("countryName", String.class);
                        final String fileName = exchange.getIn().getHeader("fileName", String.class);
                        final GetCitiesRequestDto requestDto = new GetCitiesRequestDto();
                        requestDto.setCountry(countryName);
                        requestDto.setFileName(fileName);
                        return requestDto;
                    }
                })
                .to(URI_CALL_WEB_SERVICE);

        from(URI_CALL_WEB_SERVICE).routeId(CALL_WEB_SERVICE_ROUTE_ID)
                // save original request
                .setProperty(ORIGINAL_REQUEST_PROPERTY, body())
                // call out route to webservice
                .to(GetCitiesByCountryOutRoute.URI_GET_CITIES_BY_COUNTRY)
                    .id(CALL_WEB_SERVICE_NODE_ID)
                // save to file if requested
                .wireTap(URI_SAVE_TO_FILE)
                .convertBodyTo(GetCitiesResponseDto.class);

        from(URI_SAVE_TO_FILE).routeId(SAVE_TO_FILE_ROUTE_ID)
                // only if fileName is in request, do continue
                .filter(exchange -> getOriginalRequest(exchange).getFileName().isPresent())
                .process(exchange -> {
                    final GetCitiesRequestDto dto = getOriginalRequest(exchange);
                    exchange.setProperty(FileStorageOutRoute.FILE_NAME_PROPERTY, dto.getFileName()
                            .orElseThrow(() -> new IllegalStateException("Filename must not be empty!")));
                })
                .to(FileStorageOutRoute.URI_SAVE_TO_FILE).id(SAVE_TO_FILE_NODE_ID);

    }

    private static GetCitiesRequestDto getOriginalRequest(final Exchange exchange) {
        final GetCitiesRequestDto ret = exchange.getProperty(ORIGINAL_REQUEST_PROPERTY, GetCitiesRequestDto.class);
        Assert.notNull(ret, "GetCitiesRequest in property must not be null.");
        return ret;
    }

    private static ExceptionInfoDto convertToExceptionInfoDto(final String messageId, final Throwable t) {
        final ExceptionInfoDto ret = new ExceptionInfoDto();
        ret.setMessageId(messageId);
        ret.setMessage(t.getMessage());
        return ret;
    }
}
