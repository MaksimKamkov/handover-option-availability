ARG RUNTIME_IMAGE

FROM ${RUNTIME_IMAGE}
ARG PACKAGE_NAME
COPY .entrypoint.sh /tmp/
RUN tr -d '\r' < /tmp/.entrypoint.sh > /entrypoint.sh && \
    chmod 0755 /entrypoint.sh
COPY handover-option-availability-app/target/${PACKAGE_NAME}.jar /app/
ENV PACKAGE_NAME ${PACKAGE_NAME}

ENTRYPOINT ["/entrypoint.sh"]
