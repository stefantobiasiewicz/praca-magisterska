package pl.polsl.comon.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.polsl.comon.entites.ReportEntity;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    List<ReportEntity> getAllByContext(String context);
    List<ReportEntity> getAllByContextAndSector(String context, String sector);

    @Transactional
    void deleteAllByContext(String context);

}
