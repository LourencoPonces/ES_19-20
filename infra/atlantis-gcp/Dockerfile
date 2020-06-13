# An Atlantis container with tweaks for running in GCP
# A fancy way of saying it loads some configuration from Secret Manager
# and some of our own configuration

FROM runatlantis/atlantis:v0.13.0

RUN apk add --no-cache curl jq && rm -rf /tmp/*

RUN mkdir -p /data
COPY entrypoint.sh /entrypoint.sh

ENV PORT=8080

COPY repo_config.yaml /
ENV ATLANTIS_REPO_CONFIG /repo_config.yaml

ENTRYPOINT [ "/entrypoint.sh" ]
CMD [ "server" ]
