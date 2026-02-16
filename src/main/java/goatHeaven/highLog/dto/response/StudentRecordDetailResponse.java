package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.jooq.tables.pojos.StudentRecords;
import goatHeaven.highLog.jooq.tables.pojos.QuestionSets;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class StudentRecordDetailResponse {

    private final Long id;
    private final String title;
    private final String status;
    private final LocalDateTime createdAt;
    private final List<QuestionSetSummary> questionSets;

    public StudentRecordDetailResponse(StudentRecords record, List<QuestionSets> questionSets) {
        this.id = record.getId();
        this.title = record.getTitle();
        this.status = record.getStatus();
        this.createdAt = record.getCreatedAt();
        this.questionSets = questionSets.stream()
                .map(QuestionSetSummary::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class QuestionSetSummary {
        private final Long id;
        private final String title;

        public QuestionSetSummary(QuestionSets questionSet) {
            this.id = questionSet.getId();
            this.title = questionSet.getTitle();
        }
    }
}
