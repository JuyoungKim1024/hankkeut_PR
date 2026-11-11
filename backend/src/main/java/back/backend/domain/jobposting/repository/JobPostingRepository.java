package back.backend.domain.jobposting.repository;

import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.entity.JobSource;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long>, JpaSpecificationExecutor<JobPosting> {

    Optional<JobPosting> findBySourceAndExternalId(JobSource source, String externalId);

    @Query("select posting.id from JobPosting posting where posting.source = :source and posting.externalId = :externalId")
    Optional<Long> findIdBySourceAndExternalId(@Param("source") JobSource source, @Param("externalId") String externalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select posting from JobPosting posting where posting.id = :id")
    Optional<JobPosting> findByIdForUpdate(@Param("id") long id);

    List<JobPosting> findAllBySourceAndStatus(JobSource source, JobPostingStatus status);
}
