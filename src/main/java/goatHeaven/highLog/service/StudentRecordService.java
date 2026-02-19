package goatHeaven.highLog.service;

import goatHeaven.highLog.dto.response.StudentRecordPageResponse;
import goatHeaven.highLog.jooq.tables.pojos.StudentRecords;
import goatHeaven.highLog.jooq.tables.pojos.QuestionSets;
import goatHeaven.highLog.dto.response.StudentRecordResponse;
import goatHeaven.highLog.dto.response.StudentRecordDetailResponse;
import goatHeaven.highLog.exception.CustomException;
import goatHeaven.highLog.exception.ErrorCode;
import goatHeaven.highLog.repository.StudentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentRecordService {

    private final StudentRecordRepository studentRecordRepository;
    private final S3Service s3Service;

    public List<StudentRecordResponse> getRecords(Long userId) {
        return studentRecordRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(StudentRecordResponse::new)
                .toList();
    }

    public StudentRecordPageResponse getRecordsWithPagination(Long userId, int page) {
        // 페이지당 7개
        int pageSize = 7;

        // 페이지 번호 검증 (1 이상)
        if (page < 1) {
            page = 1;
        }

        // 전체 개수 조회
        long totalCount = studentRecordRepository.countByUserId(userId);

        // 전체 페이지 수 계산
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        if (totalCount == 0) {
            totalPages = 1;
        }

        // 페이지 번호가 전체 페이지 수를 초과하면 마지막 페이지로
        if (page > totalPages) {
            page = totalPages;
        }

        // 오프셋 계산
        int offset = (page - 1) * pageSize;

        // 생기부 조회
        List<StudentRecordResponse> records =
                studentRecordRepository.findByUserIdOrderByCreatedAtDescWithPagination(userId, offset, pageSize);

        return StudentRecordPageResponse.of(records, totalPages, page, totalCount);
    }

    public StudentRecordDetailResponse getRecord(Long recordId, Long userId) {
        StudentRecords record = studentRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        // 본인의 생기부만 조회 가능
        if (!record.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        List<QuestionSets> questionSets = studentRecordRepository.findQuestionSetsByRecordId(recordId);

        return new StudentRecordDetailResponse(record, questionSets);
    }

    @Transactional
    public void deleteRecord(Long recordId, Long userId) {
        StudentRecords record = studentRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        // 본인의 생기부만 삭제 가능
        if (!record.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // S3에서 파일 삭제
        if (record.getS3Key() != null) {
            s3Service.deleteFile(record.getS3Key());
        }

        // Questions → QuestionSets → StudentRecord 순서로 삭제
        studentRecordRepository.deleteAllByStudentRecordId(recordId);
    }
}
