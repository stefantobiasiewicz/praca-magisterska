package pl.polsl.comon.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Experiment {
    @JsonProperty("test")
    private String test;
}
