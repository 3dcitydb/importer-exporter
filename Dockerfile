# 3DCityDB Importer/Exporter Dockerfile #######################################
#   Official website    https://www.3dcitydb.net
#   GitHub              https://github.com/3dcitydb/importer-exporter
###############################################################################

# Fetch & build stage #########################################################
# ARGS
ARG BUILDER_IMAGE_TAG='11.0.11-jdk-slim'
ARG RUNTIME_IMAGE_TAG='11.0.11-jre-slim'

# Base image
FROM openjdk:${BUILDER_IMAGE_TAG} AS builder

# Copy source code
WORKDIR /build_tmp
COPY . ./

# Install git, wget
RUN set -x && \
  apt-get update && \
  apt-get install -y --no-install-recommends git && \
  rm -rf /var/lib/apt/lists/*

# Build
RUN set -x && \
  chmod u+x ./gradlew && ./gradlew installDockerDist

# Move dist to /impexp
RUN set -x && \
  mkdir -p /impexp && \
  mv impexp-client/build/install/3DCityDB-Importer-Exporter-Docker/* /impexp && \
  mv docker-scripts/impexp-entrypoint.sh /impexp/bin

# Cleanup dist
RUN set -x && \
  rm -rf /build_tmp /impexp/contribs/collada2gltf/*osx* /impexp/contribs/collada2gltf/*windows* \
    /impexp/license /impexp/**/*.md /impexp/**/*.txt /impexp/**/*.bat \
    /var/lib/apt/lists/*

# Runtime stage ###############################################################
# Base image
FROM openjdk:${RUNTIME_IMAGE_TAG} AS runtime

# Run as non-root user
RUN set -x && \
  groupadd --gid 1000 impexp && \
  useradd --uid 1000 --gid 1000 impexp

USER impexp

# copy from builder
WORKDIR /impexp
COPY --chown=impexp:impexp --from=builder /impexp .

# Set permissions
RUN set -x && \
  chmod -v a+x /impexp/bin/* \
    /impexp/contribs/collada2gltf/COLLADA2GLTF*linux/COLLADA2GLTF-bin

ENV PATH=/impexp/bin:$PATH

ENTRYPOINT [ "impexp-entrypoint.sh" ]

# Labels ######################################################################
LABEL maintainer="Bruno Willenborg"
LABEL maintainer.email="b.willenborg(at)tum.de"
LABEL maintainer.organization="Chair of Geoinformatics, Technical University of Munich (TUM)"
LABEL source.repo="https://github.com/tum-gis/3dcitydb-importer-exporter-docker"
