#!/bin/sh
# Example shell script to execute the 3D city database import/export tool
# The tool can be started using a graphical user interface (default) or
# as a shell version (using -shell switch, e.g., for batch processing)
# Use 'impexp.jar -help' to get a list of available options

# Uncomment the following line to use this script as execution wrapper, e.g., from external programs
# Command line arguments are passed to the jar file
# java -jar -Xms128m -Xmx768m impexp.jar $@

# The following command executes the GUI version 
java -jar -Xms128m -Xmx768m impexp.jar
