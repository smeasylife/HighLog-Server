package goatHeaven.highLog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudentRecordPageResponse {
    private List<StudentRecordResponse> records;
    private int totalPages;
    private int currentPage;
    private long totalCount;

    public static StudentRecordPageResponse of(
            List<StudentRecordResponse> records,
            int totalPages,
            int currentPage,
            long totalCount
    ) {
        return new StudentRecordPageResponse(records, totalPages, currentPage, totalCount);
    }
}
