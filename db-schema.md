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
    s3_key VARCHAR(512) NOT NULL,
    target_school VARCHAR(100),
    target_major VARCHAR(100),
    interview_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    analyzed_at TIMESTAMP,
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_records_user_id ON student_records(user_id);
CREATE INDEX idx_records_status ON student_records(status);
```

**컬럼 설명**
- `id`: 생기부 고유 식별자
- `user_id`: 소유자 사용자 ID
- `title`: 생기부 제목 (사용자 입력)
- `s3_key`: S3 저장 경로 (`users/{userId}/records/{uuid}_filename.pdf`)
- `target_school`: 목표 대학
- `target_major`: 목표 전공
- `interview_type`: 면접 전형 타입 (종합전형, 학생부교과 등)
- `status`: 분석 상태 (`PENDING`, `ANALYZING`, `READY`, `FAILED`)
- `created_at`: 생기부 등록 시간
- `analyzed_at`: AI 분석 완료 시간

### 3. questions (면접 질문)

AI가 생성한 면접 예상 질문을 저장합니다.

```sql
CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    record_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    is_bookmarked BOOLEAN DEFAULT FALSE,
    model_answer TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_question_record FOREIGN KEY (record_id) REFERENCES student_records(id) ON DELETE CASCADE
);

CREATE INDEX idx_questions_record_id ON questions(record_id);
CREATE INDEX idx_questions_category ON questions(category);
CREATE INDEX idx_questions_difficulty ON questions(difficulty);
```

**컬럼 설명**
- `id`: 질문 고유 식별자
- `record_id`: 질문이 생성된 생기부 ID
- `category`: 질문 카테고리 (인성, 전공적합성, 의사소통 등)
- `content`: 질문 내용
- `difficulty`: 난이도 (`BASIC`, `DEEP`)
- `model_answer`: AI가 생성한 모범 답안 (선택적)
- `created_at`: 질문 생성 시간


### 4. interview_sessions (면접 세션)

실제 면접 연습 세션 정보를 저장합니다.

```sql
CREATE TABLE interview_sessions (
    id VARCHAR(100) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_id BIGINT NOT NULL,
    thread_id VARCHAR(255) NOT NULL,
    intensity VARCHAR(20) NOT NULL,
    mode VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    interview_logs JSONB,
    final_report JSONB,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    limit_time_seconds INT DEFAULT 900,
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_record FOREIGN KEY (record_id) REFERENCES student_records(id) ON DELETE CASCADE
);

CREATE INDEX idx_sessions_user_id ON interview_sessions(user_id);
CREATE INDEX idx_sessions_thread_id ON interview_sessions(thread_id);
CREATE INDEX idx_sessions_status ON interview_sessions(status);
```

**컬럼 설명**
- `id`: 세션 고유 식별자 (예: `int_777`)
- `user_id`: 면접 참여자 ID
- `record_id`: 면접 기반이 된 생기부 ID
- `thread_id`: LangGraph 대화 스레드 ID (상태 관리용)
- `intensity`: 면접 난이도 (`BASIC`, `DEEP`)
- `mode`: 면접 방식 (`TEXT`, `VOICE`)
- `status`: 세션 상태 (`IN_PROGRESS`, `COMPLETED`, `ANALYZING`)
- `interview_logs`: 실시간 대화 로그 (JSONB 배열)
  ```json
  [
    {
      "timestamp": "2024-05-20T10:30:00Z",
      "type": "question",
      "content": "동아리 활동 중...",
      "speaker": "AI"
    },
    {
      "timestamp": "2024-05-20T10:30:45Z",
      "type": "answer",
      "content": "네, 저는...",
      "speaker": "USER",
      "score": 85
    }
  ]
  ```
- `final_report`: 면접 종료 후 종합 리포트 (JSONB)
  ```json
  {
    "totalScore": 88,
    "categoryScores": {
      "전공적합성": 90,
      "인성": 85,
      "의사소통": 89
    },
    "feedback": {
      "strengths": ["구체적인 경험", "논리적 흐름"],
      "weaknesses": ["말 끝맺음 불분명"],
      "improvementPoints": "두괄식 구성 활용"
    }
  }
  ```
- `started_at`: 면접 시작 시간
- `completed_at`: 면접 종료 시간
- `limit_time_seconds`: 제한 시간 (기본 15분)

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

### 7. LangGraph Checkpoints (자동 생성)

LangGraph가 자동으로 생성하고 관리하는 테이블입니다. 면접 대화의 상태를 저장하여 중단 후 재개를 가능하게 합니다.

```sql
-- LangGraph가 자동으로 생성하는 테이블 구조 (참고용)
CREATE TABLE checkpoints (
    thread_id VARCHAR(255) NOT NULL,
    checkpoint_id VARCHAR(255) NOT NULL,
    parent_id VARCHAR(255),
    checkpoint JSONB NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (thread_id, checkpoint_id)
);
```

**특징**
- Spring Boot에서 직접 접근하지 않음
- Python AI 서비스에서 LangGraph가 독립적으로 관리
- 면접 세션의 `thread_id`와 연결됨

## 🔗 테이블 관계도

```
users (1) ────< (N) student_records
  │                       │
  │                       └──< (N) questions
  │                                   
  │                                   
  │
  └────< (N) interview_sessions
              │
              └─── (관계 없음) checkpoints (thread_id 연결)
```

## 📊 JSONB 활용

### interview_logs 예시

```json
[
  {
    "timestamp": "2024-05-20T10:30:00Z",
    "type": "question",
    "content": "동아리 활동 중 갈등을 해결한 구체적인 사례를 말씀해 주세요.",
    "speaker": "AI",
    "questionId": 101
  },
  {
    "timestamp": "2024-05-20T10:30:45Z",
    "type": "answer",
    "content": "네, 저는 2학년 로봇 동아리 활동 당시...",
    "speaker": "USER",
    "score": 85,
    "feedback": "구체적인 사례를 잘 들었습니다."
  },
  {
    "timestamp": "2024-05-20T10:31:30Z",
    "type": "followup",
    "content": "그 경험에서 가장 어려웠던 점은 무엇이었나요?",
    "speaker": "AI"
  }
]
```

### final_report 예시

```json
{
  "totalScore": 88,
  "categoryScores": {
    "전공적합성": 90,
    "인성": 85,
    "의사소통": 89
  },
  "feedback": {
    "strengths": [
      "경험의 구체성이 뛰어남",
      "논리적인 답변 흐름",
      "전공에 대한 이해도가 높음"
    ],
    "weaknesses": [
      "말 끝맺음이 다소 불분명함",
      "시선 처리 개선 필요"
    ],
    "improvementPoints": "답변 시 두괄식 구성을 더 활용해보세요. 결론을 먼저 말하고 근거를 제시하는 방식이 효과적입니다."
  },
  "detailedScores": [
    {
      "questionId": 101,
      "question": "동아리 활동 중...",
      "answer": "네, 저는...",
      "score": 90,
      "feedback": "구체적인 사례 제시 우수"
    },
    {
      "questionId": 102,
      "question": "지원 학과와...",
      "answer": "저는...",
      "score": 85,
      "feedback": "전공 이해도는 좋으나 답변 구조 개선 필요"
    }
  ],
  "duration": 780,
  "totalQuestions": 8,
  "avgResponseTime": 45
}
```
