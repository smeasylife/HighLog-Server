package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.domain.StudentRecord;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StudentRecordResponse {

    private final Long id;
    private final String title;
    private final String targetSchool;
    private final String targetMajor;
    private final String interviewType;
    private final String status;
    private final LocalDateTime createdAt;

    public StudentRecordResponse(StudentRecord record) {
        this.id = record.getId();
        this.title = record.getTitle();
        this.targetSchool = record.getTargetSchool();
        this.targetMajor = record.getTargetMajor();
        this.interviewType = record.getInterviewType();
        this.status = record.getStatus().name();
        this.createdAt = record.getCreatedAt();
    }
}
