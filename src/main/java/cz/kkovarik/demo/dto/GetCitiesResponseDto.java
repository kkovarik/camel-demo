package cz.kkovarik.demo.dto;

/**
 * @author <a href="mailto:kovarikkarel@gmail.com">Karel Kovarik</a>
 */
public class GetCitiesResponseDto {

    private final String listOfCities;

    GetCitiesResponseDto(String listOfCities) {
        this.listOfCities = listOfCities;
    }

    public String getListOfCities() {
        return listOfCities;
    }

    public static final class Builder {

        private String listOfCities;

        public Builder withListOfCities(String listOfCities) {
            this.listOfCities = listOfCities;
            return this;
        }

        public GetCitiesResponseDto build() {
            return new GetCitiesResponseDto(listOfCities);
        }
    }
}
