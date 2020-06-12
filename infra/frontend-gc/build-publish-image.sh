#!/usr/bin/env bash

set -e # exit on first error

PUSH_OPTIONS=""
#DOCKER=docker

# If using podman, make the necessary tweaks to push docker-format manifests
# Cloud Run is picky about this unfortunately
command -v podman &>/dev/null && test -z $DOCKER \
	&& DOCKER=podman \
	&& PUSH_OPTIONS="--remove-signatures" \
	&& export BUILDAH_FORMAT=docker \
	&& echo "using podman for builds" \
	|| DOCKER=docker

REG_NAME="eu.gcr.io/quizzestutor/frontend-gc"
VERSION="$(git rev-parse HEAD)"

FENIX_CLIENT_ID="$(gcloud secrets versions access --secret FENIX_ID latest)"

pushd .. >/dev/null
FRONTEND_BASE_URL="${FRONTEND_BASE_URL:-$(terraform output frontend_base_url)}"
BACKEND_BASE_URL="${BACKEND_BASE_URL:-$(terraform output backend_base_url)}"
popd >/dev/null

# compute image tags
TAGS=(
	"$REG_NAME:$VERSION"
	"$REG_NAME:latest"
)

function tags_as_options() {
	for tag in "${TAGS[@]}"; do
		echo -n " -t $tag"
	done
}

# Ensure GCR credentials are available
gcloud auth configure-docker

# Pull previous build (acts as cache)
$DOCKER pull $REG_NAME

# Build "regular" frontend image
pushd ../../frontend
$DOCKER build --build-arg NODE_ENV=production --build-arg FENIX_CLIENT_ID=$FENIX_CLIENT_ID --build-arg FRONTEND_BASE_URL=$FRONTEND_BASE_URL --build-arg BACKEND_BASE_URL=$BACKEND_BASE_URL -t "quizzestutor-frontend" .
popd

# Build Google Cloud-specialized fronend image (requires the former)
$DOCKER build $(tags_as_options) .

# Publish Google Cloud-specialized image to Container Registry
for tag in "${TAGS[@]}"; do
	$DOCKER push "$tag" $PUSH_OPTIONS
done
