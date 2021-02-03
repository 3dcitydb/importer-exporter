# 3DCityDB Importer/Exporter Dockerfile #######################################
#   Official website    https://www.3dcitydb.net
#   GitHub              https://github.com/3dcitydb/importer-exporter
###############################################################################

# Fetch & build stage #########################################################
# Base image
ARG buildstage_tag='11.0.9-slim'
FROM openjdk:${buildstage_tag} AS buildstage

WORKDIR /build_tmp

# Copy source code
COPY . ./

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
ARG runtimestage_tag='11.0.9-jre-slim'
FROM openjdk:11-jre-slim AS runtimestage
COPY --from=buildstage /impexp /impexp

WORKDIR /impexp

RUN set -x && \
  mkdir -p /share/config /share/data && \
  chmod -v u+x /impexp/bin/* /impexp/contribs/collada2gltf/COLLADA2GLTF*linux/COLLADA2GLTF-bin && \
  ln -vs /impexp/bin/impexp /usr/bin/impexp

ENTRYPOINT [ "bin/impexp-entrypoint.sh" ]

# Labels ######################################################################
LABEL maintainer="Bruno Willenborg"
LABEL maintainer.email="b.willenborg(at)tum.de"
LABEL maintainer.organization="Chair of Geoinformatics, Technical University of Munich (TUM)"
LABEL source.repo="https://github.com/tum-gis/3dcitydb-importer-exporter-docker"
