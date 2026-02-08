# REST API 명세서

## 🌐 공통 사항

### Base URL
```
https://your-domain.com/api
```

### 인증
대부분의 API는 JWT 기반 인증이 필요합니다.

**Header**
```
Authorization: Bearer {accessToken}
```

### Content-Type
```
Content-Type: application/json
```

### 공통 에러 응답 형식

```json
{
  "code": "ERROR_CODE",
  "message": "상세 에러 메시지"
}
```

**공통 HTTP 상태 코드**
- `200 OK`: 요청 성공
- `201 Created`: 리소스 생성 성공
- `400 Bad Request`: 잘못된 요청
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스 없음
- `409 Conflict`: 리소스 충돌
- `500 Internal Server Error`: 서버 오류

---

## 1. 인증 (Authentication)

### 1-1. 이메일 인증 번호 요청

대학 이메일로 OTP 인증 번호를 발송합니다.

**Endpoint**
```
POST /api/auth/email/verify
```

**Request Body**
```json
{
  "email": "student@university.ac.kr"
}
```

**Response**
```json
{
  "message": "인증 번호가 이메일로 전송되었습니다.",
  "expiresIn": 180
}
```

**Error Cases**
- `400 Bad Request`: 지원하지 않는 이메일 형식입니다.
- `409 Conflict`: 이미 가입된 이메일입니다.

---

### 1-2. 인증 번호 확인

사용자가 입력한 OTP 번호를 검증합니다.

**Endpoint**
```
POST /api/auth/email/confirm
```

**Request Body**
```json
{
  "email": "student@university.ac.kr",
  "code": "123456"
}
```

**Response**
```json
{
  "verified": true,
  "message": "인증이 완료되었습니다."
}
```

**Error Cases**
- `400 Bad Request`: 인증 번호가 일치하지 않거나 만료되었습니다.

---

### 1-3. 회원가입 완료

약관 동의 및 인증 완료 후 최종 회원 정보를 등록합니다.

**Endpoint**
```
POST /api/auth/signup
```

**Request Body**
```json
{
  "email": "student@university.ac.kr",
  "password": "SecurePassword123!",
  "name": "홍길동",
  "marketingAgreement": true
}
```

**Response**
```json
{
  "userId": 1,
  "email": "student@university.ac.kr",
  "name": "홍길동",
  "createdAt": "2024-05-20T10:00:00Z"
}
```

**Error Cases**
- `400 Bad Request`: 비밀번호 정책 미달 또는 필수 약관 미동의.

**비밀번호 정책**
- 최소 8자 이상
- 영문 대소문자, 숫자, 특수문자 조합

---

### 1-4. 로그인

이메일과 비밀번호로 로그인합니다.

**Endpoint**
```
POST /api/auth/login
```

**Request Body**
```json
{
  "email": "student@gmail.com",
  "password": "SecurePassword123!"
}
```

**Response**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "student@gmail.com",
    "name": "홍길동"
  }
}
```

**Error Cases**
- `401 Unauthorized`: 이메일 또는 비밀번호가 일치하지 않습니다.

**토큰 정보**
- Access Token 유효기간: 1시간
- Refresh Token 유효기간: 14일

---

### 1-5. 액세스 토큰 갱신

Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.

**Endpoint**
```
POST /api/auth/refresh
```

**Request Body**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Cases**
- `401 Unauthorized`: Refresh Token이 만료되었거나 유효하지 않습니다. 다시 로그인해야 합니다.

---

### 1-6. 로그아웃

서버 측 Redis에서 Refresh Token을 삭제하여 즉시 무효화합니다.

**Endpoint**
```
POST /api/auth/logout
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Request Body**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response**
```json
{
  "message": "로그아웃되었습니다."
}
```

---

## 2. 생활기록부 관리 (Student Records)

### 2-1. S3 Presigned URL 발급

클라이언트가 S3에 PDF를 직접 업로드하기 위한 임시 보안 URL을 발급합니다.

**Endpoint**
```
GET /api/records/presigned-url
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Query Parameters**
- `fileName` (required): 업로드할 파일명 (예: `my_record.pdf`)

**Example Request**
```
GET /api/records/presigned-url?fileName=my_record.pdf
```

**Response**
```json
{
  "presignedUrl": "https://s3.amazonaws.com/bucket/users/1/records/uuid_filename.pdf?...",
  "s3Key": "users/1/records/uuid_filename.pdf",
  "expiresIn": 300
}
```

**Error Cases**
- `401 Unauthorized`: 인증되지 않은 사용자입니다.
- `400 Bad Request`: 파일 확장자가 PDF가 아닙니다.

**사용 흐름**
1. 이 API로 Presigned URL 받기
2. 클라이언트에서 해당 URL로 직접 PUT 요청하여 파일 업로드
3. 업로드 성공 후 다음 API (2-2)로 메타데이터 저장

---

### 2-2. 생기부 목록 조회

로그인한 사용자가 등록한 모든 생활기록부의 목록을 조회합니다.

**Endpoint**
```
GET /api/records
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Response**
```json
[
  {
    "id": 10,
    "title": "2025학년도 수시 대비 생기부"
  }
]
```

**Error Cases**
- `401 Unauthorized`: 인증되지 않은 사용자입니다.

---

### 2-3. 특정 생기부 상세 데이터 조회

특정 생활기록부의 상세 정보와 해당 생기부를 기반으로 생성된 **모든 질문 세트(대학/전공별 묶음) 목록**을 함께 조회합니다. 생기부 관리 페이지 하단의 '생성된 질문 기록' 섹션을 구성하는 데 사용됩니다.

**Endpoint**
```
GET /api/records/{recordId}
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `recordId`: 생기부 ID

**Response**
```json
{
  "id": 10,
  "title": "2025학년도 수시 대비 생기부",
  "status": "READY",
  "createdAt": "2024-05-20T10:00:00Z",
  "questionSets": [
    {
      "id": 1,
      "title": "한양대"
    },
    {
      "id": 2,
      "title": "건국대"
    }
  ]
}
```

**Error Cases**
- **401:** 인증되지 않은 사용자입니다.
- **403**: 해당 `recordId`가 로그인한 사용자의 소유가 아닌 경우.
- **404**: 존재하지 않는 생기부 ID를 조회한 경우.

---

### 2-4. 생기부 삭제

등록된 생기부 정보와 S3의 실제 파일을 삭제합니다.

**Endpoint**
```
DELETE /api/records/{recordId}
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `recordId`: 생기부 ID

**Response**
```json
{
  "message": "생기부가 삭제되었습니다."
}
```

**Error Cases**
- `401 Unauthorized`: 인증되지 않은 사용자입니다.
- `403 Forbidden`: 본인의 생기부만 삭제할 수 있습니다.
- `404 Not Found`: 존재하지 않는 생기부 ID입니다.

**주의사항**
- S3의 실제 파일도 함께 삭제됩니다.

---

## 3. 면접 질문 및 보관함 (Questions & Bookmarks)

### 3-1. 생성된 질문 목록 조회

특정 질문 세트(대학/전공별 묶음)에 포함된 영역별 예상 질문들을 조회합니다. 질문 생성 완료 후 결과 화면이나 면접 준비 화면에서 사용됩니다.

**Endpoint**
```
GET /api/question-sets/{setId}/questions
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `setId`: 질문 세트 ID

**Query Parameters** (선택)
- `category`: 카테고리 필터 (예: `인성`, `전공적합성`)
- `difficulty`: 난이도 필터 (`BASIC`, `DEEP`)

**Example Request**
```
GET /api/question-sets/1/questions?category=인성&difficulty=BASIC
```

**Response**
```json
[
  {
    "questionId": 101,
    "category": "인성",
    "content": "동아리 활동 중 갈등을 해결한 구체적인 사례를 말씀해 주세요.",
    "difficulty": "BASIC",
    "isBookmarked": true,
    "modelAnswer": "저는 2학년 로봇 동아리 활동 당시..."
  },
  {
    "questionId": 102,
    "category": "전공적합성",
    "content": "지원하신 학과와 관련하여 가장 깊이 있게 탐구한 개념은 무엇인가요?",
    "difficulty": "DEEP",
    "isBookmarked": false,
    "modelAnswer": null
  }
]
```

**Error Cases**
- **403 Forbidden**: 타인의 질문 세트를 조회하려고 하는 경우.
- **404 Not Found**: 존재하지 않는 `setId`이거나, 아직 질문 생성이 완료되지 않은 경우.

---

### 3-2. 질문 즐겨찾기 등록/해제

특정 질문을 '내 질문 보관함'에 추가하거나 제거합니다.

**Endpoint**
```
POST /api/bookmarks
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Request Body**
```json
{
  "questionId": 101
}
```

**Response**
```json
{
  "questionId": 101,
  "isBookmarked": true
}
```

**동작 방식**
- 즐겨찾기가 없으면 추가 (`isBookmarked: true`)
- 이미 즐겨찾기되어 있으면 제거 (`isBookmarked: false`)

**Error Cases**
- `404 Not Found`: 존재하지 않는 질문 ID입니다.

---

### 3-3. 즐겨찾기 질문 목록 조회

사용자가 즐겨찾기한 모든 질문을 모아서 조회합니다.

**Endpoint**
```
GET /api/bookmarks
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Response**
```json
[
  {
    "bookmarkId": 50,
    "questionId": 101,
    "recordTitle": "2025학년도 수시 생기부",
    "category": "인성",
    "content": "동아리 활동 중 갈등을 해결한 사례...",
    "difficulty": "BASIC",
    "createdAt": "2024-05-21T15:30:00Z"
  },
  {
    "bookmarkId": 51,
    "questionId": 105,
    "recordTitle": "2025학년도 수시 생기부",
    "category": "전공적합성",
    "content": "컴퓨터 과학에서 가장 흥미로운 분야는...",
    "difficulty": "DEEP",
    "createdAt": "2024-05-21T16:00:00Z"
  }
]
```

---

## 4. AI 면접 연습 (Interview Sessions)

> **주의**: LangGraph에 대한 학습이 진행 중이므로 변경 가능성이 높습니다.

### 4-1. 면접 세션 생성

선택한 생기부를 기반으로 새로운 면접 세션을 생성합니다.

**Endpoint**
```
POST /api/interviews
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Request Body**
```json
{
  "recordId": 10,
  "intensity": "DEEP",
  "mode": "TEXT"
}
```

**Request Fields**
- `recordId`: 면접 기반이 될 생기부 ID
- `intensity`: 면접 난이도 (`BASIC`, `DEEP`)
- `mode`: 면접 방식 (`TEXT`: 텍스트, `VOICE`: 음성)

**Response**
```json
{
  "sessionId": "int_777",
  "threadId": "thread_abc_123",
  "firstMessage": "반갑습니다. 지금부터 생활기록부를 기반으로 면접을 시작하겠습니다.",
  "limitTimeSeconds": 900
}
```

**Error Cases**
- `404 Not Found`: 등록된 생기부가 없습니다.
- `400 Bad Request`: 생기부 분석이 완료되지 않았습니다.

---

### 4-2. 실시간 대화 및 답변 전송

사용자의 답변(텍스트/STT 결과)을 전송하고 AI 면접관의 다음 반응을 수신합니다.

**Endpoint**
```
POST /api/interviews/{sessionId}/chat
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `sessionId`: 면접 세션 ID

**Request Body**
```json
{
  "message": "네, 저는 고등학교 시절 프로젝트 리더로서..."
}
```

**Response Type**
```
Content-Type: text/event-stream
```

**Response Stream Example**
```
data: {"type": "thinking", "message": "답변을 분석 중입니다..."}

data: {"type": "feedback", "score": 85, "comment": "구체적인 사례를 잘 들었습니다."}

data: {"type": "question", "content": "그 경험에서 가장 어려웠던 점은 무엇이었나요?"}

data: {"type": "end"}
```

**Error Cases**
- `408 Request Timeout`: 답변 제한 시간이 초과되었습니다.
- `404 Not Found`: 존재하지 않는 세션입니다.

---

### 4-3. 면접 종료 요청 및 리포트 생성

사용자가 종료 버튼을 누르거나 제한 시간이 종료되었을 때 호출됩니다.

**Endpoint**
```
POST /api/interviews/{sessionId}/complete
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `sessionId`: 면접 세션 ID

**Response**
```json
{
  "message": "면접이 성공적으로 종료되었습니다. 결과 분석을 진행합니다.",
  "status": "ANALYZING"
}
```

**Error Cases**
- `404 Not Found`: 해당 면접 세션을 찾을 수 없습니다.

**분석 프로세스**
1. LangGraph가 전체 대화 흐름 요약
2. 종합 분석 리포트 생성
3. `final_report` (JSONB) 필드에 저장
4. 상태를 `COMPLETED`로 변경

---

### 4-4. 면접 결과 리포트 조회

면접 완료 후 생성된 종합 점수 및 영역별 피드백 리포트를 조회합니다.

**Endpoint**
```
GET /api/interviews/{sessionId}/results
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `sessionId`: 면접 세션 ID

**Response**
```json
{
  "sessionId": "int_777",
  "totalScore": 88,
  "categoryScores": {
    "전공적합성": 90,
    "인성": 85,
    "의사소통": 89
  },
  "feedback": {
    "strengths": [
      "경험의 구체성이 뛰어남",
      "논리적 흐름"
    ],
    "weaknesses": [
      "말 끝맺음이 다소 불분명함"
    ],
    "improvementPoints": "답변 시 두괄식 구성을 더 활용해보세요."
  },
  "logs": [
    {
      "q": "동아리 활동 중 갈등을 해결한 사례를 말씀해 주세요.",
      "a": "네, 저는 2학년 로봇 동아리 활동 당시...",
      "score": 90,
      "feedback": "구체적인 사례 제시가 우수합니다."
    },
    {
      "q": "지원 학과와 관련하여 가장 깊이 탐구한 개념은?",
      "a": "저는 알고리즘 최적화에 대해...",
      "score": 85,
      "feedback": "전공 이해도는 좋으나 답변 구조 개선 필요"
    }
  ],
  "metadata": {
    "duration": 780,
    "totalQuestions": 8,
    "avgResponseTime": 45,
    "completedAt": "2024-05-20T11:15:00Z"
  }
}
```

**Error Cases**
- `202 Accepted`: 아직 리포트가 생성 중입니다. (잠시 후 다시 시도)
- `404 Not Found`: 결과 데이터가 존재하지 않습니다.

---

## 5. 공지사항 및 FAQ

### 5-1. 공지사항 목록 조회

**Endpoint**
```
GET /api/notices
```

**Query Parameters** (선택)
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 10)

**Response**
```json
{
  "content": [
    {
      "id": 1,
      "title": "서비스 정기 점검 안내",
      "isPinned": true,
      "createdAt": "2024-05-20T10:00:00Z"
    },
    {
      "id": 2,
      "title": "새로운 기능 업데이트",
      "isPinned": false,
      "createdAt": "2024-05-19T15:00:00Z"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "currentPage": 0
}
```

---

### 5-2. 공지사항 상세 조회

**Endpoint**
```
GET /api/notices/{id}
```

**Path Parameters**
- `id`: 공지사항 ID

**Response**
```json
{
  "id": 1,
  "title": "서비스 정기 점검 안내",
  "content": "2024년 5월 21일 오전 2시부터 6시까지 정기 점검이 진행됩니다...",
  "isPinned": true,
  "createdAt": "2024-05-20T10:00:00Z",
  "updatedAt": "2024-05-20T10:00:00Z"
}
```

**Error Cases**
- `404 Not Found`: 해당 공지글이 삭제되었거나 존재하지 않습니다.

---

### 5-3. FAQ 목록 조회

**Endpoint**
```
GET /api/faqs
```

**Query Parameters** (선택)
- `category`: 카테고리 필터 (예: `사용법`, `결제`)

**Response**
```json
[
  {
    "id": 1,
    "category": "사용법",
    "question": "생기부는 어떻게 업로드하나요?",
    "answer": "마이페이지 > 생기부 관리에서 PDF 파일을 업로드할 수 있습니다...",
    "displayOrder": 1
  },
  {
    "id": 2,
    "category": "결제",
    "question": "결제는 어떤 방식으로 가능한가요?",
    "answer": "신용카드, 체크카드, 카카오페이 등 다양한 결제 수단을 지원합니다...",
    "displayOrder": 2
  }
]
```

---

## 6. 관리자 API (Admin)

> Thymeleaf 기반 서버 사이드 렌더링으로 구현되고 보안상 제공하지 않습니다.
 
 
---

## 7. 마이페이지

### 7-1. 대시보드

**Endpoint**
```
GET /api/users/me/dashboard
```

**Response**
```json
{
  "userName": "길동",
  "registDate": "20260118",  
  "questionBookmarkCnt": 24,
  "interviewSessionCnt": 3,
  "interviewResponseAvg": 0
}
```

---

### 7-2. 계정정보

**Endpoint**
```
GET /api/users/me/accountInfo
```

**Response**
```json
{
  "userName": "길동",
  "registDate": "20260118",  
  "email": "Honggildong@Example.Com"
}
```

---

### 7-3. 설정

**Endpoint**
```
GET /api/users/me/setting
```

**Response**
```json
{
  "responseAutoSave": true
}
```

---

### 7-4. 비밀번호 변경

현재 비밀번호를 확인한 후 새 비밀번호로 변경합니다.

**Endpoint**
```
PATCH /api/users/me/password
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Request Body**
```json
{
  "currentPassword": "CurrentPassword123!",
  "newPassword": "NewPassword456!"
}
```

**Response**
```json
{
  "message": "비밀번호가 변경되었습니다."
}
```

**Error Cases**
- `400 Bad Request`: 새 비밀번호가 정책에 맞지 않습니다.
- `401 Unauthorized`: 현재 비밀번호가 일치하지 않습니다.

**비밀번호 정책**
- 최소 8자 이상
- 영문 대소문자, 숫자, 특수문자 조합

---

### 7-5. 회원탈퇴

본인 확인 후 계정을 삭제합니다. 관련된 모든 데이터(생기부, 질문, 면접 기록 등)가 함께 삭제됩니다.

**Endpoint**
```
DELETE /api/users/me
```

**Headers**
```
Authorization: Bearer {accessToken}
```

**Request Body**
```json
{
  "password": "CurrentPassword123!"
}
```

**Response**
```json
{
  "message": "회원 탈퇴가 완료되었습니다."
}
```

**Error Cases**
- `401 Unauthorized`: 비밀번호가 일치하지 않습니다.

**주의사항**
- 탈퇴 시 S3에 저장된 생기부 PDF 파일도 함께 삭제됩니다.
- Redis에 저장된 인증 토큰도 삭제됩니다.
- 삭제된 데이터는 복구할 수 없습니다. 