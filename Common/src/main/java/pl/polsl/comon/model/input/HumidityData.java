package pl.polsl.comon.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


import java.time.OffsetDateTime;

@Data
public class HumidityData {
    @JsonProperty("address")
    private String address;
    @JsonProperty("humidity")
    private Double humidity;
    @JsonProperty("battery")
    private OffsetDateTime dateOfCreation;
}