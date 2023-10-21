package pl.polsl.comon.entites;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@IdClass(WindowStatisticsEntity.class)
public class WindowStatisticsEntity implements Serializable {
    @Id
    private Long id;
    @Id
    private String context;
    private LocalDateTime catchTime;

//    data for QoS adn QoD
    private Long humidityDataLost;
    private Long lightDataLost;
    private Long tempDataLost;

    private Long BufferSize;


    private Long windowProcessTime;
}
