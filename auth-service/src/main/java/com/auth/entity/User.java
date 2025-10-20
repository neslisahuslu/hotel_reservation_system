package com.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

        @Id
        @GeneratedValue
        @Column(name= "id")
        private UUID id;

        @Column(name = "username", nullable = false, length = 50)
        private String username;

        @JsonIgnore
        @Column(name = "password", nullable = false)
        private String password;

        @Builder.Default
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(
                name = "user_roles",
                joinColumns = @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_roles_user")),
                inverseJoinColumns = @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_roles_role")),
                indexes = {
                        @Index(name = "idx_user_roles_user", columnList = "user_id"),
                        @Index(name = "idx_user_roles_role", columnList = "role_id")
                }
        )
        private Set<Role> roles = new HashSet<>();

        // --- Spring Security implementasyonu ---
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
                return roles.stream()
                        .map(role -> (GrantedAuthority) () -> "ROLE_" + role.getName())
                        .collect(Collectors.toSet());
        }

        @Override public boolean isAccountNonExpired() { return true; }
        @Override public boolean isAccountNonLocked() { return true; }
        @Override public boolean isCredentialsNonExpired() { return true; }
        @Override public boolean isEnabled() { return true; }
}
