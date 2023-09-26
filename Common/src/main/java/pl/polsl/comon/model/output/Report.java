package pl.polsl.comon.model.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Date;

@Data
public class Report {
    @JsonProperty("sector")
    private String sector;

    @JsonProperty("soil_moisture")
    private Double humidity;
    @JsonProperty("lux")
    private Double lux;
    @JsonProperty("temperature")
    private Double temperature;
    @JsonProperty("soil_moisture")
    private Double soilMoisture;



    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("delivery_at")
    private Date deliveryAt;

    @JsonProperty("saved_at")
    private Date savedAt;

}
