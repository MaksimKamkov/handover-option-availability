#!/usr/bin/env bash
let timeout=300
printf  "\nWaiting timeout: ${timeout} seconds\n\n"

# Check wiremock container status
#
let elapsed=0
WIREMOCK_REQUEST="curl -s -o /dev/null -w "%{http_code}" localhost:52250/__admin/settings"
while [ "$(eval "${WIREMOCK_REQUEST}")" != "200" ] && [ ${elapsed} -lt ${timeout} ]; do
    printf  "\nWaiting for wiremock container up\n"
    sleep 1
    let elapsed++
done
if [ "$(eval "${WIREMOCK_REQUEST}")" == "200" ]; then
    ${WIREMOCK_REQUEST}
    printf  "\n\nService wiremock container is up and running\n"
else
    printf  "\nWaiting for wiremock container up timed out\n"
    exit 1
fi

# Check application status
#
let elapsed=0
APP_REQUEST="curl -s localhost:8180/actuator/health | jq .status"
while [ "$(eval "${APP_REQUEST}")" != '"UP"' ] && [ ${elapsed} -lt ${timeout} ]; do
    printf  "\nWaiting for ${CI_PROJECT_NAME} up\n"
    sleep 1
    let elapsed++
done
if [ "$(eval "${APP_REQUEST}")" == '"UP"' ]; then
    ${APP_REQUEST}
    printf  "\nService ${CI_PROJECT_NAME} is up and running\n"
else
    printf  "\nWaiting for ${CI_PROJECT_NAME} up timed out\n"
    exit 1
fi
