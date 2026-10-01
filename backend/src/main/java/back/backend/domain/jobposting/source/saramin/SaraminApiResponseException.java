package back.backend.domain.jobposting.source.saramin;

public class SaraminApiResponseException extends RuntimeException {

    public SaraminApiResponseException(String code) {
        super("사람인 API 응답 오류: code=" + code);
    }
}
