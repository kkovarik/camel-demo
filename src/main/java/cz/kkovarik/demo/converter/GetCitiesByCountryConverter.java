package cz.kkovarik.demo.converter;

import cz.kkovarik.demo.dto.GetCitiesRequestDto;
import cz.kkovarik.demo.dto.GetCitiesResponseDto;
import cz.kkovarik.demo.in.weather.model.GetCitiesByCountry;
import cz.kkovarik.demo.in.weather.model.GetCitiesByCountryResponse;
import org.apache.camel.Converter;


/**
 * @author <a href="mailto:kovarikkarel@gmail.com">Karel Kovarik</a>
 */
@Converter
public class GetCitiesByCountryConverter {

    @Converter
    public static GetCitiesByCountry toServiceRequest(GetCitiesRequestDto dto) {
        final GetCitiesByCountry ret = new GetCitiesByCountry();
        ret.setCountryName(dto.getCountry());
        return ret;
    }

    @Converter
    public static GetCitiesResponseDto toResponseDto(GetCitiesByCountryResponse response) {
        return new GetCitiesResponseDto.Builder()
                .withListOfCities(response.getGetCitiesByCountryResult())
                .build();
    }
}
