# The backend container, with tweaks for running in GCE
# A fancy way of saying it mounts storage buckets before starting the server
FROM quizzestutor-backend

# temporarily set user back to root to install stuff
USER root

RUN apt-get update \
	&& apt-get install -y gnupg2 lsb-release \
	&& echo "deb http://packages.cloud.google.com/apt gcsfuse-$(lsb_release -c -s) main" > /etc/apt/sources.list.d/gcsfuse.list \
	&& curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add - \
	&& apt-get update \
	&& apt-get install -y --no-install-recommends gcsfuse curl jq \
	&& apt-get purge -y lsb-release gnupg2 \
	&& apt-get autoremove --purge -y \
	&& apt-get upgrade -y \
	&& rm -rf /var/lib/apt/lists/*

RUN mkdir -p /data/userassets /data/exports /data/imports \
	&& chown quizzestutor /data/*

COPY entrypoint.sh ./entrypoint.sh

# go back to unprivileged user
USER quizzestutor

CMD [ "./entrypoint.sh" ]
