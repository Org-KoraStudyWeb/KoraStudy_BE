package korastudy.be.entity.User;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import korastudy.be.entity.Enum.RoleName;
import lombok.*;

import java.util.LinkedHashSet;
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
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RoleName roleName;

    @JsonBackReference
    @ManyToMany(mappedBy = "roles")
    private Set<Account> accounts = new LinkedHashSet<>();
}
