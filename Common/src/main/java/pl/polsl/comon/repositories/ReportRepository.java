package pl.polsl.comon.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.polsl.comon.entites.ReportEntity;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    List<ReportEntity> getAllByContext(String context);
    List<ReportEntity> getAllByContextAndSector(String context, String sector);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReportEntity WHERE context LIKE :context")
    void deleteAllByContext(@Param("context") String context);
}
