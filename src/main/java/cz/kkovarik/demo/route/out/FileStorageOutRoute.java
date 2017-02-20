package cz.kkovarik.demo.route.out;

import cz.kkovarik.demo.domain.FileStoragePayloadInfo;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;


/**
 * @author <a href="mailto:kovarikkarel@gmail.com">Karel Kovarik</a>
 */
@Component
public class FileStorageOutRoute extends RouteBuilder {
    public static final String ROUTE_ID = "FILE-STORAGE-OUT-ROUTE";
    public static final String URI_SAVE_TO_FILE = "direct:" + ROUTE_ID;

    public static final String FILE_NAME_PROPERTY = ROUTE_ID + "-fileName";

    static final String TO_FILE_NODE_ID = ROUTE_ID + "-TO-FILE";

    @Override
    public void configure() throws Exception {

        from(URI_SAVE_TO_FILE).routeId(ROUTE_ID)
                .convertBodyTo(FileStoragePayloadInfo.class)
                .process(exchange -> {
                    exchange.getIn().setHeader(Exchange.FILE_NAME, exchange.getProperty(FILE_NAME_PROPERTY));
                    exchange.getIn().setBody(((FileStoragePayloadInfo) exchange.getIn().getBody()).getContent());
                })
                .to("file:storage").id(TO_FILE_NODE_ID);
    }
}
