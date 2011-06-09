#!/bin/sh
# Example shell script to execute the 3D City Database Importer/Exporter
# The tool can be started using a graphical user interface (default) or
# as a shell version (using -shell switch, e.g., for batch processing)
# Use '3dcitydb-impexp.jar -help' to get a list of available options

# cd to path of the starter script
cd "$( cd "$( dirname "$0" )" && pwd )" > /dev/null

# Make sure JAVA_HOME is set
if [ -n "$JAVA_HOME" ]
then
    JAVA="$JAVA_HOME"/bin/java
else
    JAVA=java
fi

# Uncomment the following line to use this script as execution wrapper, 
# e.g., from external programs. Command line arguments are passed to the 
# jar file.
# exec "$JAVA" -jar -Xms128m -Xmx768m lib/3dcitydb-impexp.jar "$@"

# The following command executes the GUI version 
exec "$JAVA" -jar -Xms128m -Xmx768m lib/3dcitydb-impexp.jar