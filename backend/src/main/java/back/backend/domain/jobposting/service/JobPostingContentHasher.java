package back.backend.domain.jobposting.service;

import back.backend.domain.jobposting.source.CollectedJobPosting;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class JobPostingContentHasher {

    private JobPostingContentHasher() {
    }

    static String hash(CollectedJobPosting posting) {
        String content = Stream.of(
                        posting.companyName(),
                        posting.title(),
                        posting.jobCategory(),
                        posting.career(),
                        posting.location(),
                        posting.description(),
                        posting.qualification(),
                        posting.preference(),
                        posting.originalUrl(),
                        posting.postedAt(),
                        posting.expiredAt(),
                        posting.status())
                .map(value -> value == null ? "<null>" : value.toString())
                .collect(Collectors.joining("\u001F"));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
