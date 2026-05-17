-- 1. 테이블 생성 (기본키 포함)

CREATE TABLE users (
                       id BIGSERIAL NOT NULL,
                       password VARCHAR(100) NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NULL,
                       phone VARCHAR(50) NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'MEMBER', -- ENUM 대신 VARCHAR + CHECK 제약 조건 활용
                       generation INT NULL,
                       CONSTRAINT PK_USERS PRIMARY KEY (id),
                       CONSTRAINT CHK_USERS_ROLE CHECK (role IN ('ADMIN', 'MEMBER'))
);

CREATE TABLE study_session (
                               id BIGSERIAL NOT NULL,
                               created_by BIGINT NOT NULL,
                               generation INT NULL,
                               week BIGINT NOT NULL,
                               session_date DATE NOT NULL,
                               day_part VARCHAR(10) NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               host_name VARCHAR(100) NOT NULL,
                               status VARCHAR(30) NOT NULL DEFAULT 'BEFORE_SESSION',
                               description VARCHAR(1000) NULL,
                               session_material_name VARCHAR(255) NULL,
                               session_material_url VARCHAR(1000) NULL,
                               assignment_name VARCHAR(255) NULL,
                               assignment_url VARCHAR(1000) NULL,
                               recording_url VARCHAR(1000) NULL,
                               recording_password VARCHAR(60) NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT PK_STUDY_SESSION PRIMARY KEY (id),
                               CONSTRAINT CHK_STUDY_SESSION_DAY_PART CHECK (day_part IN ('AM', 'PM')),
                               CONSTRAINT CHK_STUDY_SESSION_STATUS CHECK (status IN ('BEFORE_SESSION', 'IN_SESSION', 'AFTER_SESSION'))
);

CREATE TABLE assignment (
                            id SERIAL NOT NULL,
                            title VARCHAR(255) NOT NULL,
                            week VARCHAR(255) NULL,
                            session_date DATE NULL,
                            CONSTRAINT PK_ASSIGNMENT PRIMARY KEY (id)
);

CREATE TABLE assignment_item (
                                 id SERIAL NOT NULL,
                                 user_id BIGINT NOT NULL, -- FK 대상이므로 SERIAL에서 INT로 수정
                                 assignment_id INT NOT NULL, -- FK 대상이므로 SERIAL에서 INT로 수정
                                 submitted VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
                                 CONSTRAINT PK_ASSIGNMENT_ITEM PRIMARY KEY (id),
                                 CONSTRAINT CHK_ASSIGNMENT_ITEM_SUBMITTED CHECK (submitted IN ('SUCCESS', 'INSUFFICIENT', 'FAILURE'))
);

CREATE TABLE attendance_code (
                                 id SERIAL NOT NULL,
                                 attendance_date VARCHAR(255) NULL,
                                 attendance_order VARCHAR(255) NULL,
                                 code VARCHAR(20) NOT NULL,
                                 is_expired BOOLEAN NOT NULL, -- TINYINT(1)에서 BOOLEAN으로 수정
                                 Field3 VARCHAR(255) NULL,
                                 CONSTRAINT PK_ATTENDANCE_CODE PRIMARY KEY (id)
);

CREATE TABLE attendance (
                            id SERIAL NOT NULL,
                            attendance_code_id INT NOT NULL,
                            user_id BIGINT NOT NULL,
                            status BOOLEAN NOT NULL, -- TINYINT(1)에서 BOOLEAN으로 수정
                            CONSTRAINT PK_ATTENDANCE PRIMARY KEY (id)
);

CREATE TABLE question (
                          id BIGSERIAL NOT NULL,
                          session_id BIGINT NOT NULL,
                          user_id BIGINT NOT NULL,
                          content VARCHAR(1000) NOT NULL,
                          image_url VARCHAR(1000) NULL,
                          is_resolved BOOLEAN NOT NULL, -- TINYINT(1)에서 BOOLEAN으로 수정
                          like_count INT NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP NULL,
                          CONSTRAINT PK_QUESTION PRIMARY KEY (id)
);

CREATE TABLE question_comment (
                                  id BIGSERIAL NOT NULL,
                                  question_id BIGINT NOT NULL,
                                  user_id BIGINT NOT NULL,
                                  parent_comment_id BIGINT NULL,
                                  content VARCHAR(1000) NOT NULL,
                                  image_url VARCHAR(1000) NULL,
                                  created_at TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  deleted_at TIMESTAMP NULL,
                                  CONSTRAINT PK_QUESTION_COMMENT PRIMARY KEY (id)
);

CREATE TABLE question_anonymous_identity (
                                             id BIGSERIAL NOT NULL,
                                             user_id BIGINT NOT NULL,
                                             question_id BIGINT NOT NULL,
                                             anonymous_no INT NOT NULL DEFAULT 1,
                                             created_at TIMESTAMP NOT NULL,
                                             CONSTRAINT PK_QUESTION_ANONYMOUS_IDENTITY PRIMARY KEY (id)
);

CREATE TABLE question_like (
                               id BIGSERIAL NOT NULL, -- PK 타입 매칭을 위해 BIGSERIAL 수정
                               question_id BIGINT NOT NULL,
                               user_id BIGINT NOT NULL,
                               created_at TIMESTAMP NOT NULL,
                               CONSTRAINT PK_QUESTION_LIKE PRIMARY KEY (id)
);

CREATE TABLE understanding_check (
                                     id BIGSERIAL NOT NULL, -- BIGINT PK용 BIGSERIAL 수정
                                     session_id BIGINT NOT NULL,
                                     created_by BIGINT NOT NULL,
                                     title VARCHAR(255) NOT NULL,
                                     description VARCHAR(255) NULL,
                                     created_at TIMESTAMP NOT NULL,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT PK_UNDERSTANDING_CHECK PRIMARY KEY (id)
);

CREATE TABLE understanding_response (
                                        id BIGSERIAL NOT NULL,
                                        check_id BIGINT NOT NULL,
                                        user_id BIGINT NOT NULL,
                                        choice VARCHAR(20) NOT NULL,
                                        created_at TIMESTAMP NOT NULL,
                                        updated_at TIMESTAMP NOT NULL,
                                        CONSTRAINT PK_UNDERSTANDING_RESPONSE PRIMARY KEY (id),
                                        CONSTRAINT CHK_UNDERSTANDING_RESPONSE_CHOICE CHECK (choice IN ('UNDERSTOOD', 'NOT_UNDERSTOOD'))
);

CREATE TABLE deposit (
                         id SERIAL NOT NULL,
                         user_id BIGINT NOT NULL,
                         amount INT NOT NULL,
                         descent_assignment INT NOT NULL,
                         descent_attendance INT NOT NULL,
                         ascent_defence INT NOT NULL,
                         CONSTRAINT PK_DEPOSIT PRIMARY KEY (id)
);

-- 2. 코멘트(주석) 설정
COMMENT ON COLUMN attendance_code.attendance_order IS '1, 2, 3';