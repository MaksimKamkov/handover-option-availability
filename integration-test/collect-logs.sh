#!/usr/bin/env bash
set -x

mkdir -p ${CI_PROJECT_DIR}/integration-test/logs && cd ${CI_PROJECT_DIR}/integration-test/logs
docker-compose logs --no-color --timestamps ${CI_PROJECT_NAME} > ${CI_PROJECT_NAME}.log
docker-compose logs --no-color --timestamps wiremock > wiremock.log