package back.backend.domain.user.entity;

import back.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    protected User() {
    }

    public static User createLocal(String name) {
        User user = new User();
        user.name = name;
        return user;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
