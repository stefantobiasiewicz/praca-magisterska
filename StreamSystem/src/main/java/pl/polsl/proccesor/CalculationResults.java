package pl.polsl.proccesor;

import lombok.Data;
import pl.polsl.comon.entites.ReportEntity;

import java.util.List;

@Data
public class CalculationResults {
    private long bufferSize;
    private List<ReportEntity> entities;
}
