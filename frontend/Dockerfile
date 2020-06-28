## Build stage

FROM node:14-alpine as build-stage

WORKDIR /app

COPY package*.json ./
RUN npm install

ARG APP_NAME="Quizzes Tutor"
ARG FENIX_CLIENT_ID
ARG FRONTEND_BASE_URL=http://localhost:8080
ARG BACKEND_BASE_URL=http://localhost:8081
ARG NODE_ENV=development

ENV VUE_APP_NAME="$APP_NAME"
ENV VUE_APP_ROOT_API="$BACKEND_BASE_URL"
ENV VUE_APP_FENIX_CLIENT_ID="$FENIX_CLIENT_ID"
ENV VUE_APP_FENIX_REDIRECT_URI="$FRONTEND_BASE_URL/login"
ENV VUE_APP_FENIX_URL="https://fenix.tecnico.ulisboa.pt/oauth/userdialog?client_id=${VUE_APP_FENIX_CLIENT_ID}&redirect_uri=${VUE_APP_FENIX_REDIRECT_URI}"

COPY *.js ./
COPY *.json ./
COPY src ./src
COPY public ./public

RUN if [ "$NODE_ENV" = "development" ] ; then npm run build:dev ; else npm run build ; fi


## Production stage

FROM nginx:stable-alpine as production-stage
RUN mkdir /app
COPY --from=build-stage /app/dist /app
