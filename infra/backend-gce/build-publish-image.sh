#!/bin/sh

set -e # exit on first error

PUSH_OPTIONS=""
DOCKER=docker

# If using podman, make the necessary tweaks to push docker-format manifests
# Cloud Run is picky about this unfortunately
command -v podman &>/dev/null \
	&& DOCKER=podman \
	&& PUSH_OPTIONS="--remove-signatures" \
	&& export BUILDAH_FORMAT=docker \
	&& echo "using podman for builds"

REG_NAME="eu.gcr.io/quizzestutor/backend-gce"
VERSION="$(git rev-parse HEAD)"

# compute image tags
TAGS=(
	"$REG_NAME:$VERSION"
	"$REG_NAME:last"
)
BRANCH_LIST="$(git branch --points-at HEAD -a --format "%(refname:short)")"
(grep "^origin/master$" <<<$BRANCH_LIST) && TAGS+="$REG_NAME:stable"
(grep "^origin/develop$" <<<$BRANCH_LIST) && TAGS+="$REG_NAME:stable"

function tags_as_options() {
	for tag in $TAGS; do
		echo -n " -t $tag"
	done
}

# Ensure GCR credentials are available
gcloud auth configure-docker

# Build "regular" backend image
pushd ../../backend
$DOCKER build -t "quizzestutor-backend" .
popd

# Build GCE-specialized backend image (requires the former)
$DOCKER build $(tags_as_options) .

# Publish GCE-specialized image to Container Registry
for tag in $TAGS; do
	$DOCKER push "$tag" $PUSH_OPTIONS
done
