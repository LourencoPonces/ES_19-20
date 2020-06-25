#!/bin/sh

set -e # fail fast

# check that required env vars exist
assert_present() {
	local var="$1"
	if eval 'test -z "${'"$var"'}"'; then
		echo "Variable $var not set"
		exit 1
	fi
}
assert_present SVC_ACCOUNT
assert_present USERASSETS_BUCKET
assert_present EXPORTS_BUCKET
assert_present IMPORTS_BUCKET

assert_present SPRING_DATASOURCE_USERNAME
assert_present SPRING_DATASOURCE_URL
assert_present FIGURES_DIR
assert_present EXPORT_DIR
assert_present LOAD_DIR
assert_present CALLBACK_URL
assert_present CORS_ALLOWED_ORIGINS

get_token() {
	curl "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/$SVC_ACCOUNT/token" -H "Metadata-Flavor: Google" \
		| jq -r .access_token
}

TOKEN="$(get_token)"

get_secret() {
	local key="$1"

	curl "https://secretmanager.googleapis.com/v1/projects/quizzestutor/secrets/$key/versions/latest:access" -H "Authorization: Bearer $TOKEN" \
		| jq -r .payload.data \
		| base64 -d
}

gcsfuse $USERASSETS_BUCKET /data/userassets
gcsfuse $EXPORTS_BUCKET /data/exports
gcsfuse $IMPORTS_BUCKET /data/imports

export SPRING_DATASOURCE_PASSWORD="$(get_secret BACKEND_DB_PASSWORD)"
export AUTH_SECRET="$(get_secret BACKEND_AUTH_SECRET)"
export OAUTH_CONSUMER_KEY="$(get_secret FENIX_ID)"
export OAUTH_CONSUMER_SECRET="$(get_secret FENIX_SECRET)"

exec java -Djava.security.egd=file:/dev/urandom -jar ./quizzes-tutor-backend-0.0.1-SNAPSHOT.jar
