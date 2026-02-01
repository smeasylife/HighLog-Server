package goatHeaven.highLog.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "AUTH001", "지원하지 않는 이메일 형식입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH002", "이미 가입된 이메일입니다."),
    INVALID_OTP(HttpStatus.BAD_REQUEST, "AUTH003", "인증 번호가 일치하지 않거나 만료되었습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH004", "이메일 인증이 완료되지 않았습니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "AUTH005", "비밀번호는 8자 이상, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH006", "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH007", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH008", "만료된 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH009", "토큰을 찾을 수 없습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER001", "사용자를 찾을 수 없습니다."),

    // Record
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "RECORD001", "해당 생기부를 찾을 수 없습니다."),
    RECORD_NOT_READY(HttpStatus.BAD_REQUEST, "RECORD002", "생기부 분석이 완료되지 않았습니다."),
    RECORD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "RECORD003", "해당 생기부에 접근 권한이 없습니다."),

    // Question
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION001", "해당 질문을 찾을 수 없습니다."),

    // Student Record
    RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "RECORD001", "생기부를 찾을 수 없습니다."),
    RECORD_NOT_READY(HttpStatus.BAD_REQUEST, "RECORD002", "생기부 분석이 완료되지 않았습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "RECORD003", "접근 권한이 없습니다."),

    // Question
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION001", "질문을 찾을 수 없습니다."),
    QUESTIONS_ALREADY_EXIST(HttpStatus.CONFLICT, "QUESTION002", "이미 질문이 생성되었습니다."),

    // AI Service
    AI_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI001", "AI 서비스 오류가 발생했습니다."),

    // S3
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "S3001", "PDF 파일만 업로드할 수 있습니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "S3002", "잘못된 파일명입니다."),
    S3_PRESIGNED_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3003", "Presigned URL 생성에 실패했습니다."),
    S3_FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3004", "S3 파일 삭제에 실패했습니다."),

    // Notice
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE001", "해당 공지사항을 찾을 수 없습니다."),
    ADMIN_ACCESS_DENIED(HttpStatus.FORBIDDEN, "NOTICE002", "관리자 권한이 필요합니다."),

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON002", "잘못된 입력값입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
