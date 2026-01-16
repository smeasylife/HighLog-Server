# AI 면접 연습 플랫폼 - 프로젝트 문서

## 📋 프로젝트 개요

이 프로젝트는 고등학생 및 대학 입시 준비생을 위한 생활기록부 기반 AI 면접 연습 플랫폼입니다. 사용자의 생활기록부를 분석하여 맞춤형 면접 질문을 생성하고, 실시간 AI 면접관과의 대화를 통해 면접 연습을 제공합니다.

## 🏗️ 시스템 아키텍처

### 1. High Level Design

#### 1.1 시스템 구성

- **Frontend**: React (TypeScript)
- **Main Backend**: Spring Boot (Java)
- **AI Engine**: Python (FastAPI + LangGraph)
- **Database**: PostgreSQL
- **Storage**: AWS S3
- **Cache**: Redis

#### 1.2 아키텍처 다이어그램

```
┌─────────────┐
│   React     │ (Frontend)
│ TypeScript  │
└──────┬──────┘
       │ REST API
       ▼
┌──────────────┐      ┌──────────────┐
│ Spring Boot  │◄────►│    Redis     │
│   (Java)     │      │ (JWT, Cache) │
└──────┬───────┘      └──────────────┘
       │
       ├──────► AWS S3 (PDF Storage)
       │
       ├──────► PostgreSQL (Main DB + LangGraph State)
       │
       ▼
┌──────────────┐
│    Python    │
│FastAPI+Graph │
│  (AI Engine) │
└──────────────┘
```

### 2. 데이터 저장소 설계

#### 2.1 PostgreSQL (통합 데이터베이스)

**Core Data (전통적 RDB 방식)**
- 사용자 정보 (users)
- 생활기록부 메타데이터 (student_records)
- 마스터 질문 풀 (questions)
- 즐겨찾기 (bookmarks)

**Interview States (LangGraph 자동 생성)**
- LangGraph의 `checkpoints` 테이블을 통한 대화 상태 실시간 저장
- thread_id 기반 상태 관리로 중단된 면접 재개 가능

**Interview Logs & Reports**
- 면접 중 발생하는 답변(STT 포함)을 JSONB 타입으로 저장
- 최종 피드백 리포트도 JSONB로 유연한 구조 확보

#### 2.2 Redis (In-Memory Cache)

**Authentication**
- JWT Refresh Token 관리
- 토큰 블랙리스트 관리 (로그아웃)

**Rate Limiting**
- API 호출 횟수 제한
- AI API(OpenAI 등) 비용 및 트래픽 관리

**Session Management**
- OTP 인증 번호 저장 (3분 TTL)

#### 2.3 AWS S3 (Object Storage)

**Source Files**
- 사용자 업로드 생활기록부 PDF 원본
- 경로 구조: `users/{userId}/records/{uuid}_filename.pdf`

## 🔄 주요 워크플로우

### 3.1 S3 Presigned URL 기반 생기부 업로드

보안과 성능을 고려한 직접 업로드 방식:

1. **Request URL**: 프론트엔드가 백엔드(Spring Boot)에 업로드 권한 요청
2. **Issue URL**: 백엔드가 AWS SDK를 통해 5분간 유효한 Presigned URL 발급
3. **Direct Upload**: 프론트엔드가 해당 URL을 사용하여 S3에 직접 PDF 업로드
4. **Notify**: 업로드 성공 후 프론트엔드가 백엔드에 '분석 시작' 요청 및 데이터 정합성 확인

**장점**
- 백엔드 서버를 거치지 않아 네트워크 트래픽 감소
- 대용량 파일 업로드 시 서버 부하 최소화
- 클라이언트가 직접 S3와 통신하여 업로드 속도 향상

### 3.2 LangGraph 기반 실시간 AI 면접 흐름

상태 관리가 가능한 AI 면접 시스템:

1. **Session Initialize**
    - 면접 시작 시 Spring Boot가 Python AI 서비스에 세션 생성 요청
    - PostgreSQL에서 사용자의 생기부 기반 질문 스냅샷 로드
    - LangGraph가 `thread_id` 할당 및 초기 상태 저장

2. **Stateful Conversation**
    - 사용자가 답변(텍스트/음성)을 보내면 LangGraph가 현재 대화 맥락(`thread_id`) 확인
    - 대화 중간에 끊겨도 PostgreSQL Checkpointer를 통해 이전 시점부터 면접 재개
    - AI가 사용자의 답변을 평가하고 다음 질문 생성

3. **Real-time Updates**
    - 모든 대화 로그는 즉시 PostgreSQL `interview_sessions` 테이블의 `interview_logs` (JSONB) 필드에 업데이트
    - 답변별 점수 및 피드백도 실시간 저장

4. **Final Evaluation**
    - 면접 종료 버튼 클릭 또는 시간 종료 시 LangGraph가 전체 대화 흐름 분석
    - 종합 리포트 생성하여 `final_report` (JSONB) 필드에 저장
    - 영역별 점수, 강점/약점, 개선 포인트 등 상세 피드백 제공

**LangGraph의 장점**
- 복잡한 대화 흐름을 그래프 구조로 관리
- 체크포인트 기능으로 언제든 이전 상태로 복구 가능
- 멀티턴 대화에서 컨텍스트 유지 및 일관성 확보

## 📦 주요 기능

### 4.1 인증 및 회원 관리

- 대학 이메일 기반 OTP 인증
- JWT 기반 인증 (Access Token + Refresh Token)
- Redis를 통한 토큰 관리 및 로그아웃 처리

### 4.2 생활기록부 관리

- PDF 파일 업로드 (S3 Presigned URL 방식)
- 생기부 메타데이터 관리 (목표 학교, 전공, 전형 타입)
- AI 기반 생기부 분석 및 예상 질문 생성
- 생기부별 상태 추적 (PENDING, READY, FAILED)

### 4.3 면접 질문 관리

- 생기부 분석 기반 맞춤형 질문 생성
- 질문 난이도 분류 (BASIC, DEEP)
- 카테고리별 질문 분류 (인성, 전공적합성, 의사소통 등)
- 질문 즐겨찾기 (내 질문 보관함)
- 모범 답안 제공

### 4.4 AI 면접 연습

- 실시간 AI 면접관과의 대화
- 텍스트 및 음성(STT) 답변 지원
- 답변별 즉각적인 피드백
- 면접 진행 상태 자동 저장 (중단 후 재개 가능)
- 제한 시간 관리 (기본 15분)

### 4.5 면접 결과 분석

- 종합 점수 및 영역별 점수 제공
- 강점 및 약점 분석
- 구체적인 개선 포인트 제안
- 질문별 답변 로그 및 점수 기록

### 4.6 관리자 기능

- Thymeleaf 기반 관리자 페이지
- 공지사항 CRUD
- FAQ 관리

## 🔒 보안 고려사항

### 5.1 인증 보안

- 대학 이메일 인증으로 사용자 신원 확인
- 비밀번호 암호화 저장 (BCrypt)
- JWT 토큰 기반 stateless 인증
- Refresh Token은 Redis에 저장하여 서버 측 무효화 가능

### 5.2 파일 업로드 보안

- Presigned URL 방식으로 직접 업로드
- URL 유효 시간 제한 (5분)
- PDF 파일만 허용
- 파일명 UUID 처리로 충돌 방지

### 5.3 API 보안

- Rate Limiting으로 과도한 요청 차단
- CORS 설정
- 입력 값 검증 및 sanitization

## 📊 데이터 흐름 예시

### 플로우 1: 생기부 업로드 및 예시 질문 생성

1. 사용자가 생기부 업로드 요청
↓
2. Spring Boot가 S3 Presigned URL 발급
   ↓
3. 프론트엔드가 S3에 직접 PDF 업로드
   ↓
4. 업로드 완료 후 Spring Boot에 분석 요청
   ↓
5. Spring Boot가 Python AI 서비스 호출
   ↓
6. AI가 PDF 파싱 및 분석하여 예시 질문 생성
   ↓
7. 생성된 질문들을 PostgreSQL에 저장 (questions 테이블)
   ↓
8. 사용자가 생기부 목록 및 생성된 예시 질문 조회

**용도**: 사용자가 자신의 생기부 기반으로 어떤 질문이 나올 수 있는지 미리 확인하고, 즐겨찾기하여 준비할 수 있습니다.

---

### 플로우 2: 실시간 AI 면접 진행

1. 사용자가 면접할 생기부 선택 및 면접 시작 요청
↓
2. Spring Boot가 해당 생기부 정보를 Python AI 서비스에 전달
   ↓
3. LangGraph가 thread_id 할당 및 면접 세션 초기화
   ↓
4. AI가 생기부를 실시간으로 분석하여 첫 질문 생성
   ↓
5. 실시간 대화 진행 (사용자 답변 ↔ AI 질문/피드백)
   ↓
6. 모든 대화 상태는 PostgreSQL checkpoints에 저장
   ↓
7. 답변별 로그는 interview_sessions의 interview_logs (JSONB)에 저장 ↓
8. 면접 종료 시 LangGraph가 전체 리포트 생성
   ↓
9. 결과를 final_report (JSONB)에 저장
   ↓
10. 프론트엔드가 면접 결과 조회 및 표시`

**용도**: 사용자가 실제 면접처럼 AI와 실시간 대화를 진행하며, 즉각적인 피드백을 받고 최종 평가 리포트를 확인합니다.

---

### 두 플로우의 차이점

| 구분 | 플로우 1: 예시 질문 생성 | 플로우 2: 실시간 AI 면접 |
| --- | --- | --- |
| **목적** | 미리 예상 질문 확인 | 실제 면접 연습 |
| **AI 동작** | 일회성 질문 생성 | 실시간 대화형 면접 |
| **저장 위치** | questions 테이블 | interview_sessions 테이블 |
| **상태 관리** | 필요 없음 | LangGraph checkpoints 사용 |
| **사용자 경험** | 정적 질문 리스트 조회 | 동적 대화 및 즉각 피드백 |

## 🔧 개발 환경 설정

### 필수 요구사항

- Java 17+
- Python 3.10+
- PostgreSQL 14+
- Redis 7+
- Node.js 18+ (Frontend)
- AWS Account (S3 사용)


## 📝 추가 문서

- **db-schema.md**: 데이터베이스 스키마 상세 설계
- **api-spec.md**: REST API 명세서
