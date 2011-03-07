#!/bin/sh
# Example shell script to execute the !app.title!
# The tool can be started using a graphical user interface (default) or
# as a shell version (using -shell switch, e.g., for batch processing)
# Use '!app.jar! -help' to get a list of available options

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
# exec "$JAVA" -jar -Xms128m -Xmx768m lib/!app.jar! "$@"

# The following command executes the GUI version 
exec "$JAVA" -jar -Xms128m -Xmx768m lib/!app.jar!