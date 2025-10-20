package com.auth.entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role  {

    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    @GeneratedValue
    private UUID id;

    @Column(name = "name")
    private String name;
}
