-- =========================================
-- Flyway Migration: Create users, roles, user_roles tables
-- =========================================

CREATE TABLE roles (
                       id UUID PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL
);

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id UUID NOT NULL,
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                            CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
                            CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user ON user_roles (user_id);
CREATE INDEX idx_user_roles_role ON user_roles (role_id);

-- =========================================
-- Flyway Migration: Insert default admin & user
-- =========================================

-- Insert roles
INSERT INTO roles (id, name)
VALUES
    ('4485326f-c45c-4657-8c60-f7f3c0af4327', 'ADMIN'),
    ('f054d4c0-2e77-4b6d-aa2a-79b3fa0a4f01', 'USER')
ON CONFLICT (id) DO NOTHING;


INSERT INTO users (id, username, password)
VALUES
    ('16f8442c-c043-453a-a32a-8ed13802659c', 'admin', '$2a$10$MFzrDN50rpFIkLAWGrYPQOZPY3.yH35eK4yuJ/kY51k.mC1lCVXzu'),
    ('f3eca345-438f-49dc-bc1c-ebd1d6c95a63', 'user', '$2a$10$x2S4SEXVb/Vrk6QChI0UQ.VxjudVmrnuCjlOPhlgjj.o1g42MDwre')
ON CONFLICT (id) DO NOTHING;

-- Link users to roles
INSERT INTO user_roles (user_id, role_id)
VALUES
    ('16f8442c-c043-453a-a32a-8ed13802659c', '4485326f-c45c-4657-8c60-f7f3c0af4327'),
    ('f3eca345-438f-49dc-bc1c-ebd1d6c95a63', 'f054d4c0-2e77-4b6d-aa2a-79b3fa0a4f01')
ON CONFLICT DO NOTHING;

