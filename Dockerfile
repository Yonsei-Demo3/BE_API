FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# 1) Gradle wrapper 먼저 복사 + 실행권한 부여 (캐시용)
COPY gradlew gradlew
COPY gradle gradle
RUN chmod +x gradlew

# 2) settings/build 파일만 먼저 복사 (캐시용)
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle

RUN ./gradlew --no-daemon dependencies || true

COPY . .

RUN ./gradlew clean bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring

# 항상 app.jar이므로 고정
COPY --from=builder /app/build/libs/app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
