# Build Stage
FROM gradle:8.10-jdk21 AS builder

WORKDIR /app

# Gradle wrapper 복사 (의존성 캐싱을 위함)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# jOOQ-custom 프로젝트 복사
COPY jOOQ-custom jOOQ-custom

# Gradle wrapper 실행 권한 설정
RUN chmod +x gradlew

# 의존성 다운로드 (캐싱 레이어)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 빌드
RUN ./gradlew clean build -x test --no-daemon

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 빌드된 jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행 계정 생성 (보안)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 포트 노출
EXPOSE 8080

# JVM 옵션 및 실행
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", \
    "app.jar"]
