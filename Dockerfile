# ============================================================
# Render uchun Dockerfile — Spring Boot + Gradle
# ============================================================
# Java 21 ishlatamiz — Render va ko'p image'larda Java 25 yo'q
# Agar build.gradle da Java 25 bo'lsa, Docker build da 21 ga o'tkazamiz
# ============================================================

# 1-qatlam: Build
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Gradle wrapper va build fayllarini nusxalash
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Java 25 → 21 (Render uchun — Java 25 image da yo'q)
RUN sed -i 's/JavaLanguageVersion.of(25)/JavaLanguageVersion.of(21)/g' build.gradle

# Dependencies cache — o'zgarmasa qayta yuklanmaydi
RUN ./gradlew dependencies --no-daemon || true

# Source code
COPY src src

# JAR yaratish (testlarsiz — tezroq)
RUN ./gradlew bootJar --no-daemon -x test

# 2-qatlam: Run (kichikroq image)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Build qatlamidan JAR ni olish
COPY --from=build /app/build/libs/*.jar app.jar

# Render PORT env dan o'qiydi (default 8080)
ENV PORT=8080
EXPOSE 8080

# Java xotira — 512 MB da ishlashi uchun
ENV JAVA_OPTS="-Xmx256m -Xms128m"

# server.port — Render PORT ni beradi, ilova shu portda tinglaydi
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
