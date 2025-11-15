# --- Build Stage ---
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# 1) Gradle wrapper 먼저 복사 + 실행권한 부여 (캐시용)
COPY gradlew ./gradlew
COPY gradle ./gradle
RUN chmod +x gradlew

# 2) settings/build 파일만 먼저 복사 (캐시용)
COPY settings.gradle ./settings.gradle
COPY build.gradle ./build.gradle

# 의존성 캐시
RUN ./gradlew --no-daemon dependencies || true

# 3) 실제 소스 복사
COPY . .

# 4) bootJar 빌드 (테스트는 스킵)
RUN ./gradlew clean bootJar -x test --no-daemon

# --- Runtime Stage ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# spring 유저 생성
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

# 항상 app.jar 이름으로 빌드되게 gradle에서 설정해놨으니 그대로 사용
COPY --from=builder /app/build/libs/app.jar app.jar

# ✅ Docker에서는 docker 프로필로 작동
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
