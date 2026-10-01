package back.backend.domain.jobposting.source.saramin;

@FunctionalInterface
public interface SaraminApiClient {

    String fetchPage(int start, int count);
}
