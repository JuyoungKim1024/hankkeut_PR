package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.dto.JobPostingDetailResponse;
import back.backend.domain.jobposting.dto.JobPostingPageResponse;
import back.backend.domain.jobposting.dto.JobPostingSort;
import back.backend.domain.jobposting.entity.JobPosting;
import back.backend.domain.jobposting.entity.JobPostingStatus;
import back.backend.domain.jobposting.repository.JobPostingRepository;
import back.backend.global.exception.ApiException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class JobPostingQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final JobPostingRepository jobPostingRepository;

    public JobPostingQueryService(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    @Transactional(readOnly = true)
    public JobPostingPageResponse findAll(String query,
                                          String location,
                                          JobPostingStatus status,
                                          JobPostingSort sort,
                                          int page,
                                          int size) {
        validatePage(page, size);
        List<Specification<JobPosting>> filters = new ArrayList<>();
        filters.add(hasStatus(status));
        if (StringUtils.hasText(query)) {
            filters.add(containsQuery(query.trim()));
        }
        if (StringUtils.hasText(location)) {
            filters.add(containsLocation(location.trim()));
        }

        PageRequest pageable = PageRequest.of(page, size, sort(sort));
        return JobPostingPageResponse.from(
                jobPostingRepository.findAll(Specification.allOf(filters), pageable)
        );
    }

    @Transactional(readOnly = true)
    public JobPostingDetailResponse findById(long jobId) {
        JobPosting posting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "JOB_NOT_FOUND", "채용공고를 찾을 수 없습니다."));
        return JobPostingDetailResponse.from(posting);
    }

    private Specification<JobPosting> hasStatus(JobPostingStatus status) {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    private Specification<JobPosting> containsQuery(String query) {
        String keyword = "%" + query.toLowerCase(Locale.ROOT) + "%";
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("companyName")), keyword),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), keyword),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("jobCategory")), keyword),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), keyword)
        );
    }

    private Specification<JobPosting> containsLocation(String location) {
        String keyword = "%" + location.toLowerCase(Locale.ROOT) + "%";
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), keyword);
    }

    private Sort sort(JobPostingSort sort) {
        if (sort == JobPostingSort.DEADLINE) {
            return Sort.by(Sort.Order.asc("expiredAt"), Sort.Order.desc("id"));
        }
        return Sort.by(Sort.Order.desc("postedAt"), Sort.Order.desc("id"));
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PAGE",
                    "페이지는 0 이상, 크기는 1 이상 100 이하여야 합니다."
            );
        }
    }
}
