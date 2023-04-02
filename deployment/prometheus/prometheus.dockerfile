FROM prom/prometheus

COPY ./deployment/prometheus/prometheus.yml /etc/prometheus/prometheus.yml