package back.backend.domain.profile.service;

import back.backend.domain.profile.dto.ProfileResponse;
import back.backend.domain.profile.dto.ProfileUpsertRequest;
import back.backend.domain.profile.entity.Profile;
import back.backend.domain.profile.repository.ProfileRepository;
import back.backend.domain.skill.entity.UserSkill;
import back.backend.domain.skill.repository.UserSkillRepository;
import back.backend.domain.user.entity.User;
import back.backend.domain.user.repository.UserRepository;
import back.backend.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserSkillRepository userSkillRepository;

    public ProfileService(UserRepository userRepository, ProfileRepository profileRepository,
                          UserSkillRepository userSkillRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.userSkillRepository = userSkillRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PROFILE_NOT_FOUND", "프로필을 등록해 주세요."));
        return ProfileResponse.of(profile, userSkillRepository.findAllByUserIdOrderByIdAsc(userId));
    }

    @Transactional
    public ProfileResponse upsert(Long userId, ProfileUpsertRequest request) {
        validateUniqueSkills(request.skills());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        Profile profile = profileRepository.findByUserId(userId)
                .map(existing -> {
                    existing.update(request.desiredJob().trim(), request.careerLevel(), request.desiredLocation().trim());
                    return existing;
                })
                .orElseGet(() -> Profile.create(user, request.desiredJob().trim(), request.careerLevel(), request.desiredLocation().trim()));
        profileRepository.save(profile);

        userSkillRepository.deleteAllByUserId(userId);
        userSkillRepository.flush();
        List<UserSkill> skills = request.skills().stream()
                .map(skill -> UserSkill.create(user, skill.name().trim(), skill.level()))
                .toList();
        userSkillRepository.saveAll(skills);
        return ProfileResponse.of(profile, skills);
    }

    private static void validateUniqueSkills(List<ProfileUpsertRequest.SkillRequest> skills) {
        Set<String> names = new HashSet<>();
        boolean duplicate = skills.stream()
                .map(skill -> skill.name().trim().toLowerCase(Locale.ROOT))
                .anyMatch(name -> !names.add(name));
        if (duplicate) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DUPLICATE_SKILL", "중복된 기술은 등록할 수 없습니다.");
        }
    }
}
