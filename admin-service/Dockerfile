ARG builder_image=project-planning/builder

# Lifehack COPY --from= doesn't allow variables
FROM $builder_image as builder

FROM bellsoft/liberica-openjre-alpine:17

ENV MODULE=admin-service
ENV APP_HOME /app

WORKDIR $APP_HOME

COPY --from=builder /workspace/build/$MODULE/build/libs/*.jar /app/app.jar

ENV JAVA_OPTS=""


ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar" ]