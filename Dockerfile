FROM debian:trixie-slim

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# hadolint ignore=DL3022
COPY --from=eclipse-temurin:21-jdk $JAVA_HOME $JAVA_HOME

ENV CLJ_KONDO_VERSION=2026.05.25
ENV BINARIES_INSTALL_PATH=/usr/local/bin

RUN set -eux; \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get upgrade --yes && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    leiningen=2.10.0-5 \
    ca-certificates=20250419 \
    runit=2.2.0-3 \
    curl=8.14.1-2+deb13u4 \
    unzip=6.0-29 && \
    useradd --home-dir /home/hop --create-home --shell /bin/bash --user-group hop && \
    curl -sL -o /tmp/clj-kondo.zip "https://github.com/clj-kondo/clj-kondo/releases/download/v${CLJ_KONDO_VERSION}/clj-kondo-${CLJ_KONDO_VERSION}-linux-amd64.zip" && \
    unzip /tmp/clj-kondo.zip clj-kondo -d "${BINARIES_INSTALL_PATH}" && \
    rm -f /tmp/clj-kondo.zip && \
    chmod 755 "${BINARIES_INSTALL_PATH}/clj-kondo" && \
    apt-get -y purge curl unzip && \
    apt-get -y autoremove && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY docker/run-as-user.sh "${BINARIES_INSTALL_PATH}"

WORKDIR /app

ENTRYPOINT ["run-as-user.sh"]
