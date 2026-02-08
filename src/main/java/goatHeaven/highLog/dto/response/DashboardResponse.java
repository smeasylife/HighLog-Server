package goatHeaven.highLog.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponse {

    private String userName;
    private String registDate;
    private Integer questionBookmarkCnt;
    private Integer interviewSessionCnt;
    private Integer interviewResponseAvg;

    public static DashboardResponse of(String userName, String registDate, int questionBookmarkCnt) {
        return DashboardResponse.builder()
                .userName(userName)
                .registDate(registDate)
                .questionBookmarkCnt(questionBookmarkCnt)
                .interviewSessionCnt(null)
                .interviewResponseAvg(null)
                .build();
    }
}
