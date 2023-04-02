ARG builder_image=project-planning/builder

# Lifehack COPY --from= doesn't allow variables
FROM $builder_image as builder

FROM nginx

ENV MODULE=admin-react-service

COPY --from=builder /workspace/build/$MODULE/build /usr/share/nginx/html/
COPY config/nginx/admin-react.conf.template /etc/nginx/templates/

RUN rm /etc/nginx/conf.d/default.conf
