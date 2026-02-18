package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.jooq.tables.pojos.StudentRecords;
import lombok.Getter;

@Getter
public class StudentRecordResponse {

    private final Long id;
    private final String title;

    public StudentRecordResponse(StudentRecords record) {
        this.id = record.getId();
        this.title = record.getTitle();
    }
}
