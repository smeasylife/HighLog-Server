# Database Schema Documentation

## 📋 개요

이 문서는 AI 면접 연습 플랫폼의 PostgreSQL 데이터베이스 스키마를 상세히 설명합니다.

## 🗂️ 테이블 구조

### 1. users (사용자)

사용자 기본 정보를 저장하는 테이블입니다.

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    university VARCHAR(100) NOT NULL,
    marketing_agreement BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
```

**컬럼 설명**
- `id`: 사용자 고유 식별자
- `email`: 대학 이메일 (인증 필수, 유니크)
- `password`: BCrypt 암호화된 비밀번호
- `name`: 사용자 이름
- `university`: 소속 대학 (이메일 도메인에서 추출)
- `marketing_agreement`: 마케팅 수신 동의 여부
- `created_at`: 계정 생성 시간
- `updated_at`: 정보 수정 시간

### 2. student_records (생활기록부)

사용자가 업로드한 생활기록부 정보를 관리합니다.

```sql
CREATE TABLE student_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    s3_key VARCHAR(512) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_records_user_id ON student_records(user_id);
CREATE INDEX idx_records_status ON student_records(status);
```

**컬럼 설명**
- `id`: 생기부 고유 식별자
- `user_id`: 소유자 사용자 ID
- `title`: 생기부 제목 (사용자 입력)
- `filename`: 업로드된 원본 파일명
- `s3_key`: S3 저장 경로 (`users/{userId}/records/{uuid}_filename.pdf`)
- `status`: 분석 상태 (`PENDING`, `ANALYZING`, `READY`, `FAILED`)
- `created_at`: 생기부 등록 시간

### 3. question_sets (질문 세트)

사용자가 "질문 생성하기"를 누를 때마다 생성되는 질문 세트입니다. 대학, 전공, 전형 정보가 이 테이블에 저장됩니다.

```sql
CREATE TABLE question_sets (
    id BIGSERIAL PRIMARY KEY,
    record_id BIGINT NOT NULL,
    target_school VARCHAR(100) NOT NULL,
    target_major VARCHAR(100) NOT NULL,
    interview_type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_set_record FOREIGN KEY (record_id) REFERENCES student_records(id) ON DELETE CASCADE
);

CREATE INDEX idx_qsets_record_id ON question_sets(record_id);
```

**컬럼 설명**
- `id`: 질문 세트 고유 식별자
- `record_id`: 연결된 생기부 ID
- `target_school`: 목표 대학 (예: "한양대")
- `target_major`: 목표 전공 (예: "컴퓨터학부")
- `interview_type`: 면접 전형 타입 (예: "학생부종합")
- `title`: 질문 세트 제목
- `created_at`: 질문 세트 생성 시간

### 4. questions (면접 질문)

AI가 생성한 면접 예상 질문을 저장합니다. 각 질문은 특정 질문 세트에 속합니다.

```sql
CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    set_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    is_bookmarked BOOLEAN DEFAULT FALSE,
    model_answer TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_question_set FOREIGN KEY (set_id) REFERENCES question_sets(id) ON DELETE CASCADE
);

CREATE INDEX idx_questions_set_id ON questions(set_id);
CREATE INDEX idx_questions_category ON questions(category);
CREATE INDEX idx_questions_difficulty ON questions(difficulty);
```

**컬럼 설명**
- `id`: 질문 고유 식별자
- `set_id`: 소속된 질문 세트 ID
- `category`: 질문 카테고리 (인성, 전공적합성, 의사소통 등)
- `content`: 질문 내용
- `difficulty`: 난이도 (`기본`, `압박`, `압박`)
- `model_answer`: AI가 생성한 모범 답안 (선택적)
- `created_at`: 질문 생성 시간

### 5. notices (공지사항)

관리자가 작성하는 공지사항입니다.

```sql
CREATE TABLE notices (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_pinned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notices_pinned ON notices(is_pinned);
CREATE INDEX idx_notices_created_at ON notices(created_at DESC);
```

**컬럼 설명**
- `id`: 공지사항 고유 식별자
- `title`: 공지 제목
- `content`: 공지 내용
- `is_pinned`: 상단 고정 여부
- `created_at`: 작성 시간
- `updated_at`: 수정 시간

### 6. faqs (자주 묻는 질문)

FAQ 목록을 관리합니다.

```sql
CREATE TABLE faqs (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    question VARCHAR(255) NOT NULL,
    answer TEXT NOT NULL,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_faqs_category ON faqs(category);
CREATE INDEX idx_faqs_order ON faqs(display_order);
```

**컬럼 설명**
- `id`: FAQ 고유 식별자
- `category`: FAQ 카테고리 (사용법, 결제, 기술 지원 등)
- `question`: 질문 내용
- `answer`: 답변 내용
- `display_order`: 표시 순서 (낮을수록 먼저 표시)
- `created_at`: 작성 시간
