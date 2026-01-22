package goatHeaven.highLog.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionGenerationEvent {

    private String type;
    private Integer progress;
    private String message;
    private String category;
    private Object data;

    public static QuestionGenerationEvent progress(int progress, String message) {
        return QuestionGenerationEvent.builder()
                .type("progress")
                .progress(progress)
                .message(message)
                .build();
    }

    public static QuestionGenerationEvent categoryComplete(String category, Object data) {
        return QuestionGenerationEvent.builder()
                .type("category_complete")
                .category(category)
                .data(data)
                .build();
    }

    public static QuestionGenerationEvent complete(Object data) {
        return QuestionGenerationEvent.builder()
                .type("complete")
                .data(data)
                .build();
    }

    public static QuestionGenerationEvent error(String message) {
        return QuestionGenerationEvent.builder()
                .type("error")
                .message(message)
                .build();
    }
}
