# (Java 17)
FROM eclipse-temurin:17-jdk-jammy

# Gradle 빌드 후 생성되는 jar 위치
ARG JAR_FILE=build/libs/*.jar

# JAR 파일을 컨테이너 내부로 복사
COPY ${JAR_FILE} app.jar

# 실행 명령어 (t2.micro 메모리 부족 방지를 위해 힙 메모리 제한 옵션 추가)
ENTRYPOINT ["java", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]