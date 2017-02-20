package cz.kkovarik.demo.converter;

import cz.kkovarik.demo.domain.FileStoragePayloadInfo;
import cz.kkovarik.demo.in.weather.model.GetCitiesByCountryResponse;
import cz.kkovarik.demo.route.out.FileStorageOutRoute;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.springframework.util.Assert;


/**
 * @author <a href="mailto:kovarikkarel@gmail.com">Karel Kovarik</a>
 */
@Converter
public class FileStorageConverter {

    @Converter
    public static FileStoragePayloadInfo toFileStoragePayload(GetCitiesByCountryResponse response, Exchange ex) {
        String fileName = ex.getProperty(FileStorageOutRoute.FILE_NAME_PROPERTY, String.class);
        Assert.hasText(fileName, "the fileName must not be empty");
        return new FileStoragePayloadInfo(fileName, response.getGetCitiesByCountryResult());
    }

}
