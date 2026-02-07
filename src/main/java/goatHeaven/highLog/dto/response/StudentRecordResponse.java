package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.domain.StudentRecord;
import lombok.Getter;

@Getter
public class StudentRecordResponse {

    private final Long id;
    private final String title;

    public StudentRecordResponse(StudentRecord record) {
        this.id = record.getId();
        this.title = record.getTitle();
    }
}
