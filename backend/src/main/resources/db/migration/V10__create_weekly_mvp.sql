CREATE TABLE weekly_mvp (
    id BIGINT NOT NULL,
    week1_mvp VARCHAR(100),
    week2_mvp VARCHAR(100),
    week3_mvp VARCHAR(100),
    week4_mvp VARCHAR(100),
    week5_mvp VARCHAR(100),
    challenge_mvp VARCHAR(100),
    updated_at TIMESTAMP,
    CONSTRAINT pk_weekly_mvp PRIMARY KEY (id)
);

-- 단일 row(고정 id=1)로만 운영되는 명예의 전당 데이터, 미리 한 행을 만들어둠
INSERT INTO weekly_mvp (id, updated_at) VALUES (1, CURRENT_TIMESTAMP);