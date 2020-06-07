#!/bin/sh

set -e # exit on first error

DOCKER=docker
command -v podman && ! command -v docker && DOCKER=podman

REG_NAME="eu.gcr.io/quizzestutor/frontend-gc"
VERSION="$(git rev-parse HEAD)"

# TODO: pass this variables in here in a better way
FENIX_CLIENT_ID="${FENIX_CLIENT_ID:-1695915081466032}"
FRONTEND_BASE_URL="${FRONTEND_BASE_URL:-https://quiztutor.breda.pt}"
BACKEND_BASE_URL="${BACKEND_BASE_URL:-https://backend.quiztutor.breda.pt}"

# Ensure GCR credentials are available
gcloud auth configure-docker

# Build "regular" frontend image
pushd ../../frontend
$DOCKER build --build-arg NODE_ENV=production --build-arg FENIX_CLIENT_ID=$FENIX_CLIENT_ID --build-arg FRONTEND_BASE_URL=$FRONTEND_BASE_URL --build-arg BACKEND_BASE_URL=$BACKEND_BASE_URL -t "quizzestutor-frontend" .
popd

# Build Google Cloud-specialized fronend image (requires the former)
$DOCKER build -t "$REG_NAME:$VERSION" -t "$REG_NAME:last" .

# Publish Google Cloud-specialized image to Container Registry
$DOCKER push "$REG_NAME:last"
$DOCKER push "$REG_NAME:$VERSION"

BRANCH_LIST="$(git branch --points-at HEAD -a --format "%(refname:short)")"
if echo $BRANCH_LIST | grep "^origin/master$"; then
	$DOCKER tag "$REG_NAME:$VERSION" "$REG_NAME:stable"
	$DOCKER push "$REG_NAME:stable"
fi
if echo $BRANCH_LIST | grep "^origin/develop$"; then
	$DOCKER tag "$REG_NAME:$VERSION" "$REG_NAME:staging"
	$DOCKER push "$REG_NAME:staging"
fi
