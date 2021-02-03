#!/usr/bin/env sh

# Print commands and their arguments as they are executed
set -e;

# echo version info and maintainer
cat <<EOF


# 3DCityDB ImporterExporter Docker #############################################
################################################################################
# Official repo   github.com/3dcitydb/importer-exporter
# Documentation   3dcitydb-docs.readthedocs.io/en/latest

# Maintainer -------------------------------------------------------------------
  Bruno Willenborg
  Chair of Geoinformatics
  Department of Aerospace and Geodesy
  Technical University of Munich (TUM)
  b.willenborg(at)tum.de
  www.gis.lrg.tum.de
################################################################################

EOF

# Print cmd line passed to container
printf "\nCommand line passed to 3DCityDB ImporterExporter CLI:\n"
echo "  $@"
printf "\n\n"

# Run ImporterExporter CLI
impexp "$@"
