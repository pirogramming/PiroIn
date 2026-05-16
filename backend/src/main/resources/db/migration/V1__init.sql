-- 1. ENUM 타입을 대체할 임시 타입 정의 (혹은 테이블 생성시 CHECK 제약조건 사용 가능)
CREATE TYPE submission_status AS ENUM ('SUCCESS', 'INSUFFICIENT', 'FAILURE');
CREATE TYPE session_status AS ENUM ('BEFORE_SESSION', 'IN_SESSION', 'AFTER_SESSION');
CREATE TYPE day_part_type AS ENUM ('AM', 'PM');
CREATE TYPE choice_type AS ENUM ('UNDERSTOOD', 'NOT_UNDERSTOOD');
CREATE TYPE role_type AS ENUM ('ADMIN', 'MEMBER');

-- 2. 테이블 생성 (백틱 제거, TINYINT -> SMALLINT/BOOLEAN 변경)
CREATE TABLE assignment (
                            id SERIAL NOT NULL,
                            date_id INT NOT NULL, -- SERIAL은 PK용이므로 FK가 될 곳은 INT로 변경
                            title VARCHAR(255) NOT NULL,
                            content VARCHAR(255) NULL
);

CREATE TABLE understanding_response (
                                        id BIGINT NOT NULL,
                                        check_id BIGINT NOT NULL,
                                        user_id BIGINT NOT NULL,
                                        choice choice_type NOT NULL,
                                        created_at TIMESTAMP NOT NULL
);

CREATE TABLE users (
                       id SERIAL NOT NULL,
                       password VARCHAR(100) NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NULL,
                       phone VARCHAR(50) NULL,
                       role role_type NOT NULL DEFAULT 'MEMBER',
                       generation INT NULL
);

CREATE TABLE assignment_item (
                                 id SERIAL NOT NULL,
                                 user_id INT NOT NULL,
                                 assignment_id INT NOT NULL,
                                 submitted submission_status NOT NULL DEFAULT 'SUCCESS'
);

CREATE TABLE attendance_code (
                                 id SERIAL NOT NULL,
                                 date_id INT NOT NULL,
                                 attendance_order SMALLINT NULL, -- TINYINT를 SMALLINT로 변경
                                 code VARCHAR(20) NOT NULL,
                                 is_expired BOOLEAN NOT NULL, -- TINYINT(1)을 BOOLEAN으로 변경
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE question_like (
                               id BIGINT NOT NULL,
                               question_id BIGINT NOT NULL,
                               user_id BIGINT NOT NULL,
                               created_at TIMESTAMP NOT NULL
);

CREATE TABLE question_anonymous_identity (
                                             id SERIAL NOT NULL,
                                             user_id INT NOT NULL,
                                             question_id INT NOT NULL,
                                             anonymous_no INT NOT NULL DEFAULT 1,
                                             created_at TIMESTAMP NOT NULL
);

CREATE TABLE deposit (
                         id SERIAL NOT NULL,
                         user_id INT NOT NULL,
                         amount INT NOT NULL,
                         descent_assignment INT NOT NULL,
                         descent_attendance INT NOT NULL,
                         ascent_defence INT NOT NULL
);

CREATE TABLE understanding_check (
                                     id BIGINT NOT NULL,
                                     session_id BIGINT NOT NULL,
                                     created_by BIGINT NOT NULL,
                                     title VARCHAR(255) NOT NULL,
                                     description VARCHAR(255) NULL,
                                     created_at TIMESTAMP NOT NULL,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE date (
                      id SERIAL NOT NULL,
                      date date NULL
);

CREATE TABLE question_comment (
                                  id SERIAL NOT NULL,
                                  question_id INT NOT NULL,
                                  user_id INT NOT NULL,
                                  parent_comment_id INT NULL,
                                  content VARCHAR(1000) NOT NULL,
                                  image_url VARCHAR(1000) NULL,
                                  created_at TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  deleted_at TIMESTAMP NULL
);

CREATE TABLE study_session (
                               id SERIAL NOT NULL,
                               date_id INT NOT NULL,
                               created_by INT NOT NULL,
                               generation INT NULL,
                               week INT NOT NULL,
                               day_part day_part_type NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               host_name VARCHAR(100) NOT NULL,
                               description VARCHAR(1000) NULL,
                               session_material_name VARCHAR(255) NULL,
                               status session_status NOT NULL DEFAULT 'BEFORE_SESSION',
                               session_material_url VARCHAR(1000) NULL,
                               assignment_name VARCHAR(255) NULL,
                               assignment_url VARCHAR(1000) NULL,
                               recording_url VARCHAR(1000) NULL,
                               recording_password VARCHAR(60) NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance (
                            id SERIAL NOT NULL,
                            attendance_code_id INT NOT NULL,
                            user_id INT NOT NULL,
                            status BOOLEAN NOT NULL
);

CREATE TABLE question (
                          id SERIAL NOT NULL,
                          session_id INT NOT NULL,
                          user_id INT NOT NULL,
                          content VARCHAR(1000) NOT NULL,
                          image_url VARCHAR(1000) NULL,
                          is_resolved BOOLEAN NOT NULL,
                          like_count INT NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP NULL
);

-- 제약 조건 추가
ALTER TABLE assignment ADD CONSTRAINT PK_ASSIGNMENT PRIMARY KEY (id);
ALTER TABLE understanding_response ADD CONSTRAINT PK_UNDERSTANDING_RESPONSE PRIMARY KEY (id);
ALTER TABLE users ADD CONSTRAINT PK_USERS PRIMARY KEY (id);
ALTER TABLE assignment_item ADD CONSTRAINT PK_ASSIGNMENT_ITEM PRIMARY KEY (id);
ALTER TABLE attendance_code ADD CONSTRAINT PK_ATTENDANCE_CODE PRIMARY KEY (id);
ALTER TABLE question_like ADD CONSTRAINT PK_QUESTION_LIKE PRIMARY KEY (id);
ALTER TABLE question_anonymous_identity ADD CONSTRAINT PK_QUESTION_ANONYMOUS_IDENTITY PRIMARY KEY (id);
ALTER TABLE deposit ADD CONSTRAINT PK_DEPOSIT PRIMARY KEY (id);
ALTER TABLE understanding_check ADD CONSTRAINT PK_UNDERSTANDING_CHECK PRIMARY KEY (id);
ALTER TABLE date ADD CONSTRAINT PK_DATE PRIMARY KEY (id);
ALTER TABLE question_comment ADD CONSTRAINT PK_QUESTION_COMMENT PRIMARY KEY (id);
ALTER TABLE study_session ADD CONSTRAINT PK_STUDY_SESSION PRIMARY KEY (id);
ALTER TABLE attendance ADD CONSTRAINT PK_ATTENDANCE PRIMARY KEY (id);
ALTER TABLE question ADD CONSTRAINT PK_QUESTION PRIMARY KEY (id);