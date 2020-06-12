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
	|| true # don't fail pipeline

REG_NAME="eu.gcr.io/quizzestutor/atlantis-gcp"
VERSION="v0.13.0" # TODO: pass and user in Dockerfile (currently works bad in podman)

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
$DOCKER pull $REG_NAME || true

# Build GCP-specialized atlantis image
$DOCKER build $(tags_as_options) .

# Publish GCP-specialized image to Container Registry
for tag in "${TAGS[@]}"; do
	$DOCKER push "$tag" $PUSH_OPTIONS
done
