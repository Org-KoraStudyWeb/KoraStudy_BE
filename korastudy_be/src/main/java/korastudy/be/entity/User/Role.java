package korastudy.be.entity.User;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // ví dụ: "ROLE_ADMIN", "ROLE_USER"

    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<Account> accounts;
}
