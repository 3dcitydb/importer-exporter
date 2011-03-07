@ECHO OFF
REM Example shell script to execute the 3D city database import/export tool
REM The tool can be started using a graphical user interface (default) or
REM as a shell version (using -shell switch, e.g., for batch processing)
REM Use 'impexp.jar -help' to get a list of available options

REM Uncomment the following line to use this script as execution wrapper, e.g., from external programs
REM Command line arguments are passed to the jar file
REM java -jar -Xms128m -Xmx768m impexp.jar %*

REM The following command executes the GUI version 
java -jar -Xms128m -Xmx768m impexp.jar