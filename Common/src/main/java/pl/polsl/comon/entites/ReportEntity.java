package pl.polsl.comon.entites;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private Long windowId;
    private String sector;

    @Column(name = "context")
    private String context;
    private LocalDateTime catchTime;

    private Boolean wasTemp;
    private Boolean wasLight;
    private Boolean wasHum;

    private Long meanAgeOfInfo;
}
