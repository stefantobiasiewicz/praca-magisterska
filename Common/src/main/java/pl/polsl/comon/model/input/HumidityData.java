package pl.polsl.comon.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


import java.time.LocalDateTime;

@Data
public class HumidityData {
    @JsonProperty("sector")
    private String sector;
    @JsonProperty("humidity")
    private Double humidity;
    @JsonProperty("battery")
    private LocalDateTime dateOfCreation;
}