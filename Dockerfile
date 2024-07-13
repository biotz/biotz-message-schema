FROM debian:bookworm-slim

ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# hadolint ignore=DL3022
COPY --from=eclipse-temurin:21-jdk $JAVA_HOME $JAVA_HOME

ENV LEIN_VERSION=2.10.0
ENV CLJ_KONDO_VERSION=2023.12.15
ENV BINARIES_INSTALL_PATH=/usr/local/bin

RUN set -eux; \
    apt-get update && \
    apt-get install -y --no-install-recommends \
    ca-certificates=20230311 runit=2.1.2-54 \
    curl=7.88.1-10+deb12u6 unzip=6.0-28 && \
    useradd --home-dir /home/hop --create-home --shell /bin/bash --user-group hop && \
    curl -sL -o "${BINARIES_INSTALL_PATH}/lein" "https://codeberg.org/leiningen/leiningen/raw/tag/${LEIN_VERSION}/bin/lein-pkg" && \
    chmod 755 "${BINARIES_INSTALL_PATH}/lein" && \
    mkdir -p /usr/share/java && \
    curl -sL -o "/usr/share/java/leiningen-$LEIN_VERSION-standalone.jar" "https://codeberg.org/leiningen/leiningen/releases/download/$LEIN_VERSION/leiningen-$LEIN_VERSION-standalone.jar" && \
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
