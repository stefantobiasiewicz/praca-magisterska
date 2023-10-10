package pl.polsl.comon.entites;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
public class WindowStatisticsEntity {
    @Id
    private Long id;
    private String context;
    private LocalDateTime catchTime;

//    data for QoS adn QoD
    private Long meanAgeOfInfo;
    private Long humidityDataLost;
    private Long lightDataLost;
    private Long tempDataLost;

    private Long humidityBuffSize;
    private Long lightBuffSize;
    private Long tempBuffSize;


    private Long windowProcessTime;
}
