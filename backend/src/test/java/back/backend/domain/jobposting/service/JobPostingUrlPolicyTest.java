package back.backend.domain.jobposting.service;

import back.backend.global.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobPostingUrlPolicyTest {

    private final JobPostingUrlPolicy policy = new JobPostingUrlPolicy("jobs.example.com, careers.example.org");

    @Test
    @DisplayName("t1 허용된 호스트의 HTTPS URL은 fragment를 제거하고 정규화한다")
    void t1_allowedHttpsUrlIsNormalized() {
        URI result = policy.validate("https://jobs.example.com/openings/../jobs/123#detail");

        assertThat(result.toString()).isEqualTo("https://jobs.example.com/jobs/123");
    }

    @Test
    @DisplayName("t2 허용 목록에 없는 호스트는 거부한다")
    void t2_unknownHostIsRejected() {
        assertThatThrownBy(() -> policy.validate("https://unknown.example/jobs/1"))
                .isInstanceOf(ApiException.class)
                .hasMessage("허용된 채용공고 사이트가 아닙니다.");
    }

    @Test
    @DisplayName("t3 인증정보나 임의 포트가 포함된 URL은 거부한다")
    void t3_credentialsAndCustomPortsAreRejected() {
        assertThatThrownBy(() -> policy.validate("https://user@jobs.example.com/jobs/1"))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> policy.validate("https://jobs.example.com:8443/jobs/1"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("t4 잡코리아 공고 URL의 추적 query는 제거한다")
    void t4_jobKoreaTrackingQueryIsRemoved() {
        JobPostingUrlPolicy jobKoreaPolicy = new JobPostingUrlPolicy("www.jobkorea.co.kr");

        URI result = jobKoreaPolicy.validate(
                "https://www.jobkorea.co.kr/Recruit/GI_Read/48861911?Oem_Code=C1&rPageCode=TL");

        assertThat(result.toString())
                .isEqualTo("https://www.jobkorea.co.kr/Recruit/GI_Read/48861911");
    }

    @Test
    @DisplayName("t5 허용 호스트가 비어 있으면 공개 URL도 가져오기 대상으로 쓰지 않는다")
    void t5_emptyAllowlistRejectsUrl() {
        assertThatThrownBy(() -> new JobPostingUrlPolicy("").validate("https://jobs.example.com/jobs/1"))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.code()).isEqualTo("JOB_SOURCE_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("t6 설정에 있어도 루프백·사설·공유 주소로 연결하지 않는다")
    void t6_rejectsPrivateResolvedAddresses() {
        for (String host : new String[]{"127.0.0.1", "10.0.0.2", "169.254.169.254", "100.64.0.1", "[::1]", "[fc00::1]"}) {
            URI uri = URI.create("https://" + host + "/job");
            assertThatThrownBy(() -> policy.requirePublicAddress(uri))
                    .isInstanceOfSatisfying(ApiException.class,
                            exception -> assertThat(exception.code()).isEqualTo("JOB_SOURCE_NOT_ALLOWED"));
        }
    }
}
