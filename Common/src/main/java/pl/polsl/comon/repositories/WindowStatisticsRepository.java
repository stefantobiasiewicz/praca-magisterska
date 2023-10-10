package pl.polsl.comon.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.polsl.comon.entites.WindowStatisticsEntity;

public interface WindowStatisticsRepository extends JpaRepository<WindowStatisticsEntity, Long> {

}
