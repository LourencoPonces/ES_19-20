#!/bin/sh

gcsfuse $USERASSETS_BUCKET /data/userassets
gcsfuse $EXPORTS_BUCKET /data/exports
gcsfuse $IMPORTS_BUCKET /data/imports

exec java -Djava.security.egd=file:/dev/urandom -jar ./quizzes-tutor-backend-0.0.1-SNAPSHOT.jar
