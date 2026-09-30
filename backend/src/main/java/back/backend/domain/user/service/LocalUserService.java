package back.backend.domain.user.service;

import back.backend.domain.user.entity.User;
import back.backend.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocalUserService {

    private static final String DEFAULT_NAME = "로컬 사용자";

    private final UserRepository userRepository;

    public LocalUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User getOrCreate() {
        return userRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> userRepository.save(User.createLocal(DEFAULT_NAME)));
    }
}
