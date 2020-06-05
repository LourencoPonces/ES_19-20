#!/bin/sh

set -e # exit on first error

DOCKER=docker
command -v podman && ! command -v docker && DOCKER=podman

REG_NAME="eu.gcr.io/quizzestutor/backend-gce"
VERSION="$(git rev-parse HEAD)"

# Build "regular" backend image
pushd ../../backend
$DOCKER build -t "quizzestutor-backend:$VERSION" .
popd


# Build GCE-specialized backend image (requires the former)
$DOCKER build -t "$REG_NAME:$VERSION" -t "$REG_NAME:latest" .

# Publish GCE-specialized image to Container Registry
gcloud auth configure-docker
$DOCKER push "$REG_NAME:latest"
$DOCKER push "$REG_NAME:$VERSION"
