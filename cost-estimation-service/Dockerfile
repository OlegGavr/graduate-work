ARG builder_image=project-planning/builder

# Lifehack COPY --from= doesn't allow variables
FROM $builder_image as builder


FROM bellsoft/liberica-openjre-alpine:17

ENV MODULE=cost-estimation-service/cost-estimation-service-impl
ENV APP_HOME /app

WORKDIR $APP_HOME

COPY --from=builder /workspace/build/$MODULE/build/libs/*.jar /app/app.jar

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c"]

CMD ["exec java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 $JAVA_OPTS -jar app.jar"]