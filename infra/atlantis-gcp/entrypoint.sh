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
assert_present PORT

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

export ATLANTIS_GH_USER="$(get_secret ATLANTIS_GH_USER)"
export ATLANTIS_GH_TOKEN="$(get_secret ATLANTIS_GH_TOKEN)"
export ATLANTIS_GH_WEBHOOK_SECRET="$(get_secret ATLANTIS_WEBHOOK_SECRET)"
export ATLANTIS_PORT=$PORT
exec /usr/local/bin/docker-entrypoint.sh $@
