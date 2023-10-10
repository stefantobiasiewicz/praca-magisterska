package pl.polsl.comon.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import pl.polsl.comon.entites.ReportEntity;
import pl.polsl.comon.entites.WindowStatisticsEntity;

import java.util.List;

public interface WindowStatisticsRepository extends JpaRepository<WindowStatisticsEntity, Long> {
    List<WindowStatisticsEntity> getAllByContext(String context);
    @Transactional
    void deleteAllByContext(String context);
}
