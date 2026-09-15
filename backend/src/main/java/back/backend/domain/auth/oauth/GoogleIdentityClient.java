package back.backend.domain.auth.oauth;

public interface GoogleIdentityClient {

    GoogleIdentity exchange(String authorizationCode, String redirectUri);
}
