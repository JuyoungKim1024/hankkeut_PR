package back.backend.domain.jobposting.source;

import back.backend.domain.jobposting.entity.JobSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JobPostingSourceTest {

    @Test
    @DisplayName("t1 공고 소스는 식별자와 수집 결과를 공통 계약으로 제공한다")
    void t1_sourceProvidesIdentityAndCollectedPostings() {
        JobPostingSource source = new JobPostingSource() {
            @Override
            public JobSource source() {
                return JobSource.MANUAL;
            }

            @Override
            public JobPostingFetchResult fetch() {
                return new JobPostingFetchResult(List.of(), List.of(), true);
            }
        };

        assertThat(source.source()).isEqualTo(JobSource.MANUAL);
        assertThat(source.fetch().postings()).isEmpty();
        assertThat(source.fetch().failures()).isEmpty();
        assertThat(source.fetch().completeSnapshot()).isTrue();
    }
}
