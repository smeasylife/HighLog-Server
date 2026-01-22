package goatHeaven.highLog.service;

import goatHeaven.highLog.dto.response.QuestionGenerationEvent;
import goatHeaven.highLog.domain.Question;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    /**
     * FastAPI 서버에 질문 생성을 요청하고 SSE 이벤트를 반환합니다.
     *
     * @param recordId 생기부 ID
     * @return SSE 이벤트 스트림
     */
    public Flux<QuestionGenerationEvent> generateQuestions(Long recordId) {
        WebClient webClient = webClientBuilder
                .baseUrl(aiServerUrl)
                .build();

        return webClient.post()
                .uri("/api/v1/questions/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"record_id\": " + recordId + "}")
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::parseSseEvent)
                .doOnNext(event -> log.info("Received AI event: {}", event.getType()))
                .doOnError(error -> log.error("AI service error", error));
    }

    /**
     * 생기부 PDF를 벡터화하고 벡터 DB에 저장합니다.
     *
     * @param recordId 생기부 ID
     * @return 성공 여부
     */
    public boolean vectorizeRecord(Long recordId) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(aiServerUrl)
                    .build();

            Boolean result = webClient.post()
                    .uri("/api/v1/records/vectorize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"record_id\": " + recordId + "}")
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to vectorize record: {}", recordId, e);
            return false;
        }
    }

    private QuestionGenerationEvent parseSseEvent(String data) {
        try {
            JsonNode jsonNode = objectMapper.readTree(data);
            String type = jsonNode.get("type").asText();

            return switch (type) {
                case "progress" -> QuestionGenerationEvent.progress(
                        jsonNode.get("progress").asInt(),
                        jsonNode.get("message").asText()
                );
                case "category_complete" -> {
                    String category = jsonNode.get("category").asText();
                    List<Question> questions = objectMapper.convertValue(
                            jsonNode.get("questions"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Question.class)
                    );
                    yield QuestionGenerationEvent.categoryComplete(category, questions);
                }
                case "complete" -> QuestionGenerationEvent.complete(jsonNode.get("data"));
                case "error" -> QuestionGenerationEvent.error(jsonNode.get("message").asText());
                default -> throw new IllegalArgumentException("Unknown event type: " + type);
            };
        } catch (Exception e) {
            log.error("Failed to parse SSE event: {}", data, e);
            return QuestionGenerationEvent.error("Failed to parse event");
        }
    }
}
