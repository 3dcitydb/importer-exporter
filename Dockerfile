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
  mv impexp-client-cli/build/install/3DCityDB-Importer-Exporter-Docker/* /impexp

# Cleanup dist
RUN set -x && \
  rm -rf rm -rf /build_tmp /impexp/license /impexp/**/*.md /impexp/**/*.txt

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
  chmod -v a+x /impexp/impexp \
    /impexp/contribs/collada2gltf/COLLADA2GLTF*linux/COLLADA2GLTF-bin

ENV PATH=/impexp:$PATH

ENTRYPOINT [ "impexp" ]
CMD [ "help" ]

# Labels ######################################################################
LABEL maintainer="Bruno Willenborg"
LABEL maintainer.email="b.willenborg(at)tum.de"
LABEL maintainer.organization="Chair of Geoinformatics, Technical University of Munich (TUM)"
LABEL source.repo="https://github.com/3dcitydb/importer-exporter"
