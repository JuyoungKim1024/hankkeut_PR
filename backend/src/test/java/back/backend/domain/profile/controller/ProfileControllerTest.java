package back.backend.domain.profile.controller;

import back.backend.domain.auth.repository.RefreshTokenRepository;
import back.backend.domain.profile.repository.ProfileRepository;
import back.backend.domain.skill.repository.UserSkillRepository;
import back.backend.domain.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserSkillRepository userSkillRepository;
    @Autowired ProfileRepository profileRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userSkillRepository.deleteAll();
        profileRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("t1 인증 사용자가 프로필과 기술을 저장하면 다시 조회할 수 있다")
    void t1_authenticatedUserCanSaveAndReadProfile() throws Exception {
        String accessToken = signupAndGetAccessToken();
        String request = """
                {
                  "desiredJob": "백엔드 개발자",
                  "careerLevel": "ENTRY",
                  "desiredLocation": "서울",
                  "skills": [
                    {"name": "Java", "level": "INTERMEDIATE"},
                    {"name": "Spring Boot", "level": "BEGINNER"}
                  ]
                }
                """;

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.desiredJob").value("백엔드 개발자"))
                .andExpect(jsonPath("$.data.skills.length()").value(2));

        mockMvc.perform(get("/api/profile").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.careerLevel").value("ENTRY"))
                .andExpect(jsonPath("$.data.skills[0].name").value("Java"));
    }

    @Test
    @DisplayName("t2 중복 기술을 저장하면 400을 반환한다")
    void t2_duplicateSkillReturnsBadRequest() throws Exception {
        String accessToken = signupAndGetAccessToken();
        String request = """
                {
                  "desiredJob": "백엔드 개발자",
                  "careerLevel": "ENTRY",
                  "desiredLocation": "서울",
                  "skills": [
                    {"name": "Java", "level": "BEGINNER"},
                    {"name": "java", "level": "ADVANCED"}
                  ]
                }
                """;

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_SKILL"));
    }

    @Test
    @DisplayName("t3 인증 없이 프로필을 조회하면 401을 반환한다")
    void t3_unauthenticatedRequestReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    private String signupAndGetAccessToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"profile@example.com\",\"password\":\"password1\",\"name\":\"민준\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }
}
