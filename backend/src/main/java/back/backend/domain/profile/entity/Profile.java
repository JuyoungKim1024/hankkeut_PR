package back.backend.domain.profile.entity;

import back.backend.domain.user.entity.User;
import back.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile")
public class Profile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "desired_job", nullable = false, length = 100)
    private String desiredJob;

    @Enumerated(EnumType.STRING)
    @Column(name = "career_level", nullable = false, length = 30)
    private CareerLevel careerLevel;

    @Column(name = "desired_location", nullable = false, length = 100)
    private String desiredLocation;

    protected Profile() {
    }

    public static Profile create(User user, String desiredJob, CareerLevel careerLevel, String desiredLocation) {
        Profile profile = new Profile();
        profile.user = user;
        profile.update(desiredJob, careerLevel, desiredLocation);
        return profile;
    }

    public void update(String desiredJob, CareerLevel careerLevel, String desiredLocation) {
        this.desiredJob = desiredJob;
        this.careerLevel = careerLevel;
        this.desiredLocation = desiredLocation;
    }

    public String getDesiredJob() { return desiredJob; }
    public CareerLevel getCareerLevel() { return careerLevel; }
    public String getDesiredLocation() { return desiredLocation; }
}
