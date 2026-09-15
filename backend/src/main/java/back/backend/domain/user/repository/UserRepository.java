package back.backend.domain.user.repository;

import back.backend.domain.user.entity.User;
import back.backend.domain.user.entity.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
