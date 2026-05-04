CREATE TYPE role_enum AS ENUM ('ADMIN', 'MEMBER');

CREATE TYPE session_day_part_enum AS ENUM ('AM', 'PM');

CREATE TYPE session_status_enum AS ENUM (
    'BEFORE_SESSION',
    'IN_SESSION',
    'AFTER_SESSION'
);

CREATE TYPE assignment_status_enum AS ENUM (
    'SUCCESS',
    'INSUFFICIENT',
    'FAILURE'
);

CREATE TYPE understanding_choice_enum AS ENUM (
    'UNDERSTOOD',
    'NOT_UNDERSTOOD'
);

CREATE TABLE users (
    id BIGINT NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    role role_enum NOT NULL DEFAULT 'MEMBER',
    generation INT,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE study_session (
    id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    generation INT NOT NULL,
    week BIGINT NOT NULL,
    session_date DATE NOT NULL,
    day_part session_day_part_enum NOT NULL,
    title VARCHAR(255) NOT NULL,
    host_name VARCHAR(100),
    status session_status_enum NOT NULL DEFAULT 'BEFORE_SESSION',
    description TEXT,
    session_material_url TEXT,
    assignment_url TEXT,
    recording_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_study_session PRIMARY KEY (id),
    CONSTRAINT fk_study_session_created_by
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE attendance_code (
    id BIGINT NOT NULL,
    study_session_id BIGINT,
    code VARCHAR(20) NOT NULL,
    is_expired BOOLEAN NOT NULL,
    CONSTRAINT pk_attendance_code PRIMARY KEY (id),
    CONSTRAINT fk_attendance_code_session
        FOREIGN KEY (study_session_id) REFERENCES study_session (id)
);

CREATE TABLE attendance (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    study_session_id BIGINT,
    status BOOLEAN NOT NULL,
    CONSTRAINT pk_attendance PRIMARY KEY (id),
    CONSTRAINT uq_attendance_user_session
        UNIQUE (user_id, study_session_id),
    CONSTRAINT fk_attendance_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_attendance_session
        FOREIGN KEY (study_session_id) REFERENCES study_session (id)
);

CREATE TABLE deposit (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount INT NOT NULL,
    descent_assignment INT NOT NULL,
    descent_attendance INT NOT NULL,
    ascent_defence INT NOT NULL,
    CONSTRAINT pk_deposit PRIMARY KEY (id),
    CONSTRAINT uq_deposit_user
        UNIQUE (user_id),
    CONSTRAINT fk_deposit_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE assignment (
    id BIGINT NOT NULL,
    session_id BIGINT,
    title VARCHAR(255) NOT NULL,
    content VARCHAR(255),
    CONSTRAINT pk_assignment PRIMARY KEY (id),
    CONSTRAINT fk_assignment_session
        FOREIGN KEY (session_id) REFERENCES study_session (id)
);

CREATE TABLE assignment_item (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    assignment_id BIGINT NOT NULL,
    submitted assignment_status_enum NOT NULL DEFAULT 'SUCCESS',
    CONSTRAINT pk_assignment_item PRIMARY KEY (id),
    CONSTRAINT uq_assignment_item_user_assignment
        UNIQUE (user_id, assignment_id),
    CONSTRAINT fk_assignment_item_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_assignment_item_assignment
        FOREIGN KEY (assignment_id) REFERENCES assignment (id)
);

CREATE TABLE question (
    id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    image_url TEXT,
    is_resolved BOOLEAN NOT NULL,
    like_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT pk_question PRIMARY KEY (id),
    CONSTRAINT fk_question_session
        FOREIGN KEY (session_id) REFERENCES study_session (id),
    CONSTRAINT fk_question_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE question_comment (
    id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    image_url TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT pk_question_comment PRIMARY KEY (id),
    CONSTRAINT fk_question_comment_question
        FOREIGN KEY (question_id) REFERENCES question (id),
    CONSTRAINT fk_question_comment_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE question_like (
    id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_question_like PRIMARY KEY (id),
    CONSTRAINT uq_question_like_question_user
        UNIQUE (question_id, user_id),
    CONSTRAINT fk_question_like_question
        FOREIGN KEY (question_id) REFERENCES question (id),
    CONSTRAINT fk_question_like_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE question_anonymous_identity (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    anonymous_no INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_question_anonymous_identity PRIMARY KEY (id),
    CONSTRAINT uq_question_anon_question_user
        UNIQUE (question_id, user_id),
    CONSTRAINT uq_question_anon_question_no
        UNIQUE (question_id, anonymous_no),
    CONSTRAINT fk_question_anon_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_question_anon_question
        FOREIGN KEY (question_id) REFERENCES question (id)
);

CREATE TABLE understanding_check (
    id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_understanding_check PRIMARY KEY (id),
    CONSTRAINT fk_understanding_check_session
        FOREIGN KEY (session_id) REFERENCES study_session (id),
    CONSTRAINT fk_understanding_check_created_by
        FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE understanding_response (
    id BIGINT NOT NULL,
    check_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    choice understanding_choice_enum NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_understanding_response PRIMARY KEY (id),
    CONSTRAINT uq_understanding_response_check_user
        UNIQUE (check_id, user_id),
    CONSTRAINT fk_understanding_response_check
        FOREIGN KEY (check_id) REFERENCES understanding_check (id),
    CONSTRAINT fk_understanding_response_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
