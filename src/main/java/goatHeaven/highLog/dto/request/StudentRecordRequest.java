package goatHeaven.highLog.dto.request;

import lombok.Getter;

@Getter
public class StudentRecordRequest {

    private String title;
    private String s3Key;
    private String targetSchool;
    private String targetMajor;
    private String interviewType;
}
