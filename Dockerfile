# 3DCityDB Importer/Exporter Dockerfile #######################################
#   Official website    https://www.3dcitydb.org
#   GitHub              https://github.com/3dcitydb/importer-exporter
###############################################################################

# Fetch & build stage #########################################################
# ARGS
ARG BUILDER_IMAGE_TAG='11.0.12-jdk-slim'
ARG RUNTIME_IMAGE_TAG='11.0.12-jre-slim'

# Base image
FROM openjdk:${BUILDER_IMAGE_TAG} AS builder

# Copy source code
WORKDIR /build
COPY . /build

# Build
RUN chmod u+x ./gradlew && ./gradlew installDockerDist

# Runtime stage ###############################################################
# Base image
FROM openjdk:${RUNTIME_IMAGE_TAG} AS runtime

# Copy from builder
COPY --from=builder /build/impexp-client-cli/build/install/3DCityDB-Importer-Exporter-Docker /opt/impexp

# Run as non-root user
RUN groupadd --gid 1000 -r impexp && \
    useradd --uid 1000 --gid 1000 -d /data -m -r --no-log-init impexp

# Put start script in path and set permissions
RUN ln -sf /opt/impexp/impexp /usr/local/bin/ && \
    chmod a+x /opt/impexp/contribs/collada2gltf/*linux*/COLLADA2GLTF-bin

WORKDIR /data
USER 1000

ENTRYPOINT ["impexp"]
CMD ["--help"]

# Labels ######################################################################
LABEL maintainer="Bruno Willenborg"
LABEL maintainer.email="b.willenborg(at)tum.de"
LABEL maintainer.organization="Chair of Geoinformatics, Technical University of Munich (TUM)"
LABEL source.repo="https://github.com/3dcitydb/importer-exporter"