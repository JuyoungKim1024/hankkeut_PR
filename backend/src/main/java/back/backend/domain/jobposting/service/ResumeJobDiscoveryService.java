package back.backend.domain.jobposting.service;

import back.backend.domain.resume.repository.ResumeExperienceRepository;
import back.backend.domain.resume.repository.ResumeRepository;
import back.backend.domain.user.service.LocalUserService;
import back.backend.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeJobDiscoveryService {
    private static final int MAX_BYTES = 12 * 1024 * 1024;
    private final ResumeRepository resumes;
    private final ResumeExperienceRepository experiences;
    private final LocalUserService users;
    private final ResumeJobRecommendations recommendations;
    private final RestClient client;
    private final JsonMapper json = JsonMapper.builder().build();

    public ResumeJobDiscoveryService(ResumeRepository resumes, ResumeExperienceRepository experiences,
                                    LocalUserService users, RuleBasedSkillExtractor extractor) {
        this.resumes = resumes;
        this.experiences = experiences;
        this.users = users;
        this.recommendations = new ResumeJobRecommendations(extractor);
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4))
                .followRedirects(HttpClient.Redirect.NEVER).build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(12));
        this.client = RestClient.builder().requestFactory(factory)
                .baseUrl("https://boards-api.greenhouse.io").build();
    }

    public Results discover(long resumeId, String board) {
        if (!board.matches("[a-zA-Z0-9_-]{1,80}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_JOB_BOARD", "채용 보드 이름만 입력해 주세요.");
        }
        var resume = users.find().flatMap(user -> resumes.findByIdAndUserId(resumeId, user.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RESUME_NOT_FOUND", "이력서를 찾을 수 없습니다."));
        String text = resume.getExtractedText();
        String confirmed = experiences.findAllByResumeIdOrderById(resumeId).stream()
                .map(item -> item.content().skills()).collect(Collectors.joining("\n"));
        List<String> detected = recommendations.detectedSkills(text, confirmed);
        if (detected.isEmpty()) return new Results(detected, List.of());

        try {
            return client.get().uri("/v1/boards/{board}/jobs?content=true", board)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().value() == 404) {
                            throw new ApiException(HttpStatus.NOT_FOUND, "JOB_BOARD_NOT_FOUND", "회사 채용 보드를 찾을 수 없습니다.");
                        }
                        if (!response.getStatusCode().is2xxSuccessful()) throw unavailable();
                        byte[] body = response.getBody().readNBytes(MAX_BYTES + 1);
                        if (body.length > MAX_BYTES) throw unavailable();
                        var payload = json.readTree(body);
                        if (payload == null || !payload.path("jobs").isArray()) throw unavailable();
                        return new Results(detected, recommendations.rank(payload, text, confirmed));
                    });
        } catch (RestClientException | JacksonException failure) {
            throw unavailable();
        }
    }

    private static ApiException unavailable() {
        return new ApiException(HttpStatus.BAD_GATEWAY, "JOB_BOARD_UNAVAILABLE", "회사 공고를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
    }

    public record Results(List<String> detectedSkills, List<ResumeJobRecommendations.Recommendation> items) {}
}
