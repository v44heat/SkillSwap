

CREATE TABLE IF NOT EXISTS users (
    id             BIGSERIAL    PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    department     VARCHAR(150),
    year_of_study  VARCHAR(50),
    student_id     VARCHAR(50),
    bio            TEXT,
    role           VARCHAR(20)  NOT NULL DEFAULT 'STUDENT'
                   CHECK (role IN ('STUDENT','ADMIN')),
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                   CHECK (status IN ('ACTIVE','SUSPENDED')),
    average_rating NUMERIC(3,2) DEFAULT 0.00,
    total_sessions INT          DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS skill_listings (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title            VARCHAR(200) NOT NULL,
    description      TEXT         NOT NULL,
    category         VARCHAR(30)  NOT NULL
                     CHECK (category IN ('PROGRAMMING','DESIGN','MATHEMATICS','LANGUAGES','BUSINESS','SCIENCE','ARTS','OTHER')),
    level            VARCHAR(20)  NOT NULL DEFAULT 'INTERMEDIATE'
                     CHECK (level IN ('BEGINNER','INTERMEDIATE','ADVANCED')),
    session_duration VARCHAR(50),
    availability     VARCHAR(100),
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    is_flagged       BOOLEAN      NOT NULL DEFAULT FALSE,
    flag_reason      TEXT,
    total_sessions   INT          DEFAULT 0,
    average_rating   NUMERIC(3,2) DEFAULT 0.00,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS session_requests (
    id                BIGSERIAL    PRIMARY KEY,
    skill_listing_id  BIGINT       NOT NULL REFERENCES skill_listings(id) ON DELETE CASCADE,
    requester_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    teacher_id        BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                      CHECK (status IN ('PENDING','ACCEPTED','DECLINED','WITHDRAWN')),
    proposed_datetime TIMESTAMPTZ  NOT NULL,
    duration          VARCHAR(50),
    focus_message     TEXT         NOT NULL,
    meeting_format    VARCHAR(30)  NOT NULL DEFAULT 'FLEXIBLE'
                      CHECK (meeting_format IN ('IN_PERSON','ONLINE_GOOGLE_MEET','ONLINE_ZOOM','FLEXIBLE')),
    decline_reason    TEXT,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT no_self_request CHECK (requester_id <> teacher_id)
);

CREATE TABLE IF NOT EXISTS sessions (
    id               BIGSERIAL   PRIMARY KEY,
    request_id       BIGINT      NOT NULL UNIQUE REFERENCES session_requests(id) ON DELETE CASCADE,
    skill_listing_id BIGINT      NOT NULL REFERENCES skill_listings(id) ON DELETE CASCADE,
    teacher_id       BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    learner_id       BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    scheduled_at     TIMESTAMPTZ NOT NULL,
    duration         VARCHAR(50),
    meeting_format   VARCHAR(30) NOT NULL DEFAULT 'FLEXIBLE'
                     CHECK (meeting_format IN ('IN_PERSON','ONLINE_GOOGLE_MEET','ONLINE_ZOOM','FLEXIBLE')),
    status           VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'
                     CHECK (status IN ('CONFIRMED','COMPLETED','CANCELLED')),
    cancel_reason    TEXT,
    completed_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS feedback (
    id               BIGSERIAL   PRIMARY KEY,
    session_id       BIGINT      NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    reviewer_id      BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reviewee_id      BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    skill_listing_id BIGINT      NOT NULL REFERENCES skill_listings(id) ON DELETE CASCADE,
    overall_rating   INT         NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    review_text      TEXT,
    teacher_reply    TEXT,
    is_reported      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT no_self_review         CHECK (reviewer_id <> reviewee_id),
    CONSTRAINT one_review_per_session UNIQUE (session_id, reviewer_id)
);

-- ── INDEXES ──────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_users_email           ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status          ON users(status);
CREATE INDEX IF NOT EXISTS idx_skill_listings_user   ON skill_listings(user_id);
CREATE INDEX IF NOT EXISTS idx_skill_listings_cat    ON skill_listings(category);
CREATE INDEX IF NOT EXISTS idx_skill_listings_active ON skill_listings(is_active, is_flagged);
CREATE INDEX IF NOT EXISTS idx_requests_requester    ON session_requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_requests_teacher      ON session_requests(teacher_id);
CREATE INDEX IF NOT EXISTS idx_requests_status       ON session_requests(status);
CREATE INDEX IF NOT EXISTS idx_sessions_teacher      ON sessions(teacher_id);
CREATE INDEX IF NOT EXISTS idx_sessions_learner      ON sessions(learner_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status       ON sessions(status);
CREATE INDEX IF NOT EXISTS idx_sessions_scheduled    ON sessions(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_feedback_session      ON feedback(session_id);
CREATE INDEX IF NOT EXISTS idx_feedback_reviewee     ON feedback(reviewee_id);

-- ── TRIGGERS ─────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
  CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TRIGGER trg_skill_listings_updated_at
    BEFORE UPDATE ON skill_listings FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TRIGGER trg_session_requests_updated_at
    BEFORE UPDATE ON session_requests FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TRIGGER trg_sessions_updated_at
    BEFORE UPDATE ON sessions FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TRIGGER trg_feedback_updated_at
    BEFORE UPDATE ON feedback FOR EACH ROW EXECUTE FUNCTION set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Recalculate skill_listings.average_rating after feedback changes
CREATE OR REPLACE FUNCTION recalc_listing_rating()
RETURNS TRIGGER AS $$
DECLARE target_listing_id BIGINT;
BEGIN
  target_listing_id := COALESCE(NEW.skill_listing_id, OLD.skill_listing_id);
  UPDATE skill_listings
  SET average_rating = COALESCE((
    SELECT ROUND(AVG(overall_rating)::NUMERIC, 2)
    FROM feedback WHERE skill_listing_id = target_listing_id
  ), 0.00)
  WHERE id = target_listing_id;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
  CREATE TRIGGER trg_recalc_listing_rating
    AFTER INSERT OR UPDATE OR DELETE ON feedback
    FOR EACH ROW EXECUTE FUNCTION recalc_listing_rating();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Recalculate users.average_rating (as teacher/reviewee) after feedback changes
CREATE OR REPLACE FUNCTION recalc_user_rating()
RETURNS TRIGGER AS $$
DECLARE target_user_id BIGINT;
BEGIN
  target_user_id := COALESCE(NEW.reviewee_id, OLD.reviewee_id);
  UPDATE users
  SET average_rating = COALESCE((
    SELECT ROUND(AVG(overall_rating)::NUMERIC, 2)
    FROM feedback WHERE reviewee_id = target_user_id
  ), 0.00)
  WHERE id = target_user_id;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
  CREATE TRIGGER trg_recalc_user_rating
    AFTER INSERT OR UPDATE OR DELETE ON feedback
    FOR EACH ROW EXECUTE FUNCTION recalc_user_rating();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- Increment total_sessions counters when a session is completed
CREATE OR REPLACE FUNCTION on_session_completed()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.status = 'COMPLETED' AND OLD.status <> 'COMPLETED' THEN
    UPDATE skill_listings SET total_sessions = total_sessions + 1 WHERE id = NEW.skill_listing_id;
    UPDATE users SET total_sessions = total_sessions + 1 WHERE id = NEW.teacher_id;
    UPDATE users SET total_sessions = total_sessions + 1 WHERE id = NEW.learner_id;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
  CREATE TRIGGER trg_session_completed
    AFTER UPDATE ON sessions
    FOR EACH ROW EXECUTE FUNCTION on_session_completed();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ── SEED: Default admin ───────────────────────────────────────
-- password is: Admin@1234
INSERT INTO users (email, password_hash, first_name, last_name, role, status)
VALUES (
  'admin@skillswap.ac.ke',
  '$2a$12$6C5ssrwUYP8811ki/eY9pOdYzJ3z/HWCl7p0696Z967Z7gfz0RKgu',
  'System', 'Admin', 'ADMIN', 'ACTIVE'
) ON CONFLICT (email) DO NOTHING;
