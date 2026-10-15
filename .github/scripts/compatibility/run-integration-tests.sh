#!/bin/sh
# Runs a Micro Integrator integration test profile inside an arbitrary Linux container image, so the
# server and the tests run on the OS and JDK under test.
#
# Expected to be started from the repository root (mounted into the container), with the product pack
# already present at distribution/target/wso2mi-<version>.zip and installed into MAVEN_REPO.
#
# Environment:
#   TEST_PROFILE      Maven profile from integration/pom.xml to run (required)
#   JDK_VERSION       JDK feature version, e.g. 17 or 21 (required unless JDK_URL is set)
#   JDK_DISTRIBUTION  temurin | corretto | microsoft (default: temurin)
#   JDK_URL           Direct URL to a JDK .tar.gz; overrides JDK_VERSION/JDK_DISTRIBUTION
#   MAVEN_VERSION     Maven version to install (default: 3.9.9)
#   MAVEN_REPO        Local Maven repository mounted into the container (default: /m2)
#   ENV_REPORT        File to write the detected OS/JDK details to (default: compatibility-env.md)

set -eu

JDK_DISTRIBUTION="${JDK_DISTRIBUTION:-temurin}"
JDK_URL="${JDK_URL:-}"
MAVEN_VERSION="${MAVEN_VERSION:-3.9.9}"
MAVEN_REPO="${MAVEN_REPO:-/m2}"
ENV_REPORT="${ENV_REPORT:-compatibility-env.md}"

log() {
    echo "==> $*"
}

fail() {
    echo "ERROR: $*" >&2
    exit 1
}

[ -n "${TEST_PROFILE:-}" ] || fail "TEST_PROFILE is not set"
[ -n "${JDK_URL}" ] || [ -n "${JDK_VERSION:-}" ] || fail "Either JDK_VERSION or JDK_URL must be set"

# Tools needed to fetch the JDK/Maven and by bin/micro-integrator.sh (ps, awk, expr, grep).
install_packages() {
    missing=""
    command -v curl > /dev/null 2>&1 || missing="$missing curl"
    command -v tar > /dev/null 2>&1 || missing="$missing tar"
    command -v gzip > /dev/null 2>&1 || missing="$missing gzip"
    command -v unzip > /dev/null 2>&1 || missing="$missing unzip"
    command -v ps > /dev/null 2>&1 || missing="$missing ps"
    command -v awk > /dev/null 2>&1 || missing="$missing awk"
    command -v find > /dev/null 2>&1 || missing="$missing find"
    if [ -z "$missing" ]; then
        return
    fi
    log "Installing missing tools:$missing"
    if command -v apt-get > /dev/null 2>&1; then
        export DEBIAN_FRONTEND=noninteractive
        apt-get update -y
        apt-get install -y --no-install-recommends ca-certificates curl tar gzip unzip procps gawk findutils
    elif command -v dnf > /dev/null 2>&1; then
        # Minimal RHEL-family images ship curl-minimal, which conflicts with the full curl package.
        pkgs="tar gzip unzip procps-ng gawk findutils"
        command -v curl > /dev/null 2>&1 || pkgs="$pkgs curl"
        dnf install -y $pkgs
    elif command -v microdnf > /dev/null 2>&1; then
        pkgs="tar gzip unzip procps-ng gawk findutils"
        command -v curl > /dev/null 2>&1 || pkgs="$pkgs curl"
        microdnf install -y $pkgs
    elif command -v yum > /dev/null 2>&1; then
        pkgs="tar gzip unzip procps-ng gawk findutils"
        command -v curl > /dev/null 2>&1 || pkgs="$pkgs curl"
        yum install -y $pkgs
    elif command -v zypper > /dev/null 2>&1; then
        zypper --non-interactive install curl tar gzip unzip procps gawk findutils
    elif command -v apk > /dev/null 2>&1; then
        apk add --no-cache curl tar gzip unzip procps gawk findutils
    else
        fail "Unsupported package manager; install:$missing"
    fi
}

jdk_download_url() {
    if [ -n "$JDK_URL" ]; then
        echo "$JDK_URL"
        return
    fi
    case "$(uname -m)" in
        x86_64 | amd64) arch=x64 ;;
        aarch64 | arm64) arch=aarch64 ;;
        *) fail "Unsupported architecture: $(uname -m)" ;;
    esac
    libc=glibc
    if [ -f /etc/alpine-release ]; then
        libc=musl
    fi
    case "$JDK_DISTRIBUTION" in
        temurin)
            os=linux
            [ "$libc" = musl ] && os=alpine-linux
            echo "https://api.adoptium.net/v3/binary/latest/${JDK_VERSION}/ga/${os}/${arch}/jdk/hotspot/normal/eclipse"
            ;;
        corretto)
            os=linux
            [ "$libc" = musl ] && os=alpine
            echo "https://corretto.aws/downloads/latest/amazon-corretto-${JDK_VERSION}-${arch}-${os}-jdk.tar.gz"
            ;;
        microsoft)
            os=linux
            [ "$libc" = musl ] && os=alpine
            echo "https://aka.ms/download-jdk/microsoft-jdk-${JDK_VERSION}-${os}-${arch}.tar.gz"
            ;;
        *)
            fail "Unsupported JDK_DISTRIBUTION '$JDK_DISTRIBUTION' (use temurin, corretto, microsoft or set JDK_URL)"
            ;;
    esac
}

install_jdk() {
    url="$(jdk_download_url)"
    log "Installing JDK from $url"
    mkdir -p /opt/jdk
    curl -fsSL --retry 3 "$url" | tar -xz -C /opt/jdk --strip-components=1
    JAVA_HOME=/opt/jdk
    export JAVA_HOME
}

install_maven() {
    url="https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
    log "Installing Maven ${MAVEN_VERSION}"
    mkdir -p /opt/maven
    curl -fsSL --retry 3 "$url" | tar -xz -C /opt/maven --strip-components=1
}

write_env_report() {
    os_name="$(. /etc/os-release && echo "${PRETTY_NAME:-$NAME}")"
    # Unset JAVA_TOOL_OPTIONS so the JVM's "Picked up ..." banner is not taken as the version line.
    java_version="$(env -u JAVA_TOOL_OPTIONS "$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)"
    java_vendor="$(env -u JAVA_TOOL_OPTIONS "$JAVA_HOME/bin/java" -XshowSettings:properties -version 2>&1 \
        | awk -F'= ' '/java.vendor =/ {print $2}')"
    {
        echo "| Item | Value |"
        echo "|---|---|"
        echo "| Profile | \`${TEST_PROFILE}\` |"
        echo "| OS | ${os_name} |"
        echo "| Kernel (host) | $(uname -r) |"
        echo "| Architecture | $(uname -m) |"
        echo "| JDK | ${java_version} (${java_vendor}) |"
    } > "$ENV_REPORT"
    cat "$ENV_REPORT"
}

install_packages
install_jdk
install_maven
PATH="$JAVA_HOME/bin:/opt/maven/bin:$PATH"
export PATH

write_env_report
mvn -v

log "Running integration test profile ${TEST_PROFILE}"
mvn -B clean install --file integration/pom.xml -P "$TEST_PROFILE" -fae -Dmaven.repo.local="$MAVEN_REPO"
