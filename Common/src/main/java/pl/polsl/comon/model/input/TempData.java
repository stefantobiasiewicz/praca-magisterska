package pl.polsl.comon.model.input;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;



@Data
public class TempData {
    @JsonProperty("address")
    private String address;
    @JsonProperty("temperature")
    private Double temperature;
    @JsonProperty("timestamp")
    private OffsetDateTime dateOfCreation;
}

