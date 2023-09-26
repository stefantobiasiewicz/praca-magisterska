package pl.polsl.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Sector {
    @JsonProperty("name")
    private String name;
    @JsonProperty("addresses")
    private List<String> addresses;
    @JsonProperty("interval")
    private Double interval;
    @JsonProperty("noise")
    private Double noise;
}