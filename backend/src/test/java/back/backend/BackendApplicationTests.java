package back.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @Test
    @DisplayName("t1 애플리케이션 컨텍스트를 로드한다")
    void t1_contextLoads() {
    }

}
