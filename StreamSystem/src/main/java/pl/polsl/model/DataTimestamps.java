package pl.polsl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DataTimestamps {
    private LocalDateTime dateOfCreation;
    private LocalDateTime arrivedTime;
}
