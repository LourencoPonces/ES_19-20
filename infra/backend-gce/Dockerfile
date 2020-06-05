# The backend container, with tweaks for running in GCE
# A fancy way of saying it mounts storage buckets before starting the server
FROM quizzestutor-backend

RUN apt-get update \
	&& apt-get install -y gnupg2 lsb-release \
	&& echo "deb http://packages.cloud.google.com/apt gcsfuse-$(lsb_release -c -s) main" > /etc/apt/sources.list.d/gcsfuse.list \
	&& curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add - \
	&& apt-get update \
	&& apt-get install -y --no-install-recommends gcsfuse \
	&& apt-get purge -y lsb-release gnupg2 \
	&& apt-get autoremove --purge -y \
	&& apt-get upgrade -y \
	&& rm -rf /var/lib/apt/lists/*

RUN useradd --system --shell /usr/bin/nologin --no-log-init quizzestutor \
	&& mkdir -p /data/{userassets,exports,imports} \
	&& chown quizzestutor /data/*

USER quizzestutor

COPY entrypoint.sh ./entrypoint.sh

EXPOSE 8080

ENTRYPOINT [ "./entrypoint.sh" ]
CMD []
