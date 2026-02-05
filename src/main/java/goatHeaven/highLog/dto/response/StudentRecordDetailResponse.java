package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.domain.StudentRecord;
import goatHeaven.highLog.domain.QuestionSet;
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

    public StudentRecordDetailResponse(StudentRecord record) {
        this.id = record.getId();
        this.title = record.getTitle();
        this.status = record.getStatus().name();
        this.createdAt = record.getCreatedAt();
        this.questionSets = record.getQuestionSets().stream()
                .map(QuestionSetSummary::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class QuestionSetSummary {
        private final Long id;
        private final String title;

        public QuestionSetSummary(QuestionSet questionSet) {
            this.id = questionSet.getId();
            this.title = questionSet.getTitle();
        }
    }
}
