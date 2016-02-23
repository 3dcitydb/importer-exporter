virtualcityDATABASE Importer/Exporter v2.1.1


0. Index
--------

1. License
2. Copyright
3. About
4. System requirements
5. (Un)Installation
6. Running the application
7. Developers
8. Contact
9. Websites
10. Disclaimer


1. License
----------

The virtualcityDATABASE Importer/Exporter is a modified
and branded version of the free and open source software
3D City Database Importer/Exporter.

This software is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.


2. Copyright
------------

(C) 2013 - 2016
virtualcitySYSTEMS GmbH
Tauentzienstraße 7 b/c
10789 Berlin, Germany
All rights reserved.


3. About
--------

The virtualcityDATABASE Importer/Exporter is a Java based front-end for 
the virtualcityDATABASE version 2.1. It allows for high-performance 
importing and exporting spatial data for a virtual 3D city model.

* Full support for CityGML versions 2.0.0 and 1.0.0
* Support for Oracle Spatial, Oracle Locator, and PostGIS
* Reading/writing CityGML instance documents of arbitrary file size
* Export of KML/COLLADA models including tiling schemas for 
  visualization and interactive exploration of large city models
  in Digital Earth Browsers, 3D GIS, and computer graphics software
* Generic KML information balloons
* Export of thematic object data into tables. Supported data formats are
  CSV, Microsoft Excel, and direct upload into Google Spreadsheets
* Resolving and preservation of forward and backwards XLinks in 
  CityGML datasets
* Full support of 3D Coordinate Reference Systems (CRS) and 3D 
  coordinate transformations; support for user-defined CRS 
* Coordinate transformations for CityGML exports
* Map window for graphical selection of bounding boxes
* XML validation of CityGML instance documents
* Multithreaded programming facilitating high-performance CityGML 
  processing
* Proxy support for HTTP, HTTPS, and SOCKS protocols

The virtualcityDATABASE Importer/Exporter comes with both a Graphical
User Interface (GUI) and a Command Line Interface (CLI). The CLI 
allows for employing the tool in batch processing workflows and 
third party applications.


4. System requirements
----------------------

* Java JRE or JDK >= 1.7
* virtualcityDATABASE version 2.1 on
  - Oracle Spatial DBMS >= 10G R2 with Spatial or Locator option
  - PostgreSQL DBMS >= 9.1 with PostGIS extension >= 2.0
  
The virtualcityDATABASE Importer/Exporter can be run on any platform 
providing appropriate Java support. It has been tested on the 
following platforms:
  - Microsoft Windows XP, Vista, Windows 7, 8, 8.1
  - Apple Mac OS X 10.9
  - Ubuntu 14


5. (Un)Installation
-------------------

It is recommended to use the universal installer to unpack the 
virtualcityDATABASE Importer/Exporter application files to your local 
computer. The universal installer will guide you through the steps of the
installation process. Afterwards, you can immediately run the application 
(see next section).

When you successfully install the virtualcityDATABASE Importer/Exporter,
an uninstaller is automatically generated in the 'uninstaller' subfolder
of the installation folder. Please use this uninstaller in order to remove 
the virtualcityDATABASE Importer/Exporter application files from your
computer. Alternatively, you can simply delete the entire installation
folder.


6. Running the application
--------------------------

For running the virtualcityDATABASE Importer/Exporter use one of the 
following options:

a) Recommended:
   Use the provided starter scripts to launch the application. The starter
   scripts can be found in the installation folder. During setup you can
   choose to additionally create shortcuts for the starter scripts on your 
   desktop and in the start menu of your preferred OS.
   
   Depending on the platform, please run one the following starter scripts:
   - virtualcityDATABASE-Importer-Exporter.bat (Microsoft Windows family)
   - virtualcityDATABASE-Importer-Exporter.sh  (UNIX/Linux family) 
   - virtualcityDATABASE-Importer-Exporter.command (Mac OS X) 
   
   On most platforms, double-clicking the starter script or its shortcut
   runs the virtualcityDATABASE Importer/Exporter. 
   
   PLEASE NOTE:
   The starter scripts override default settings of the Java Virtual 
   Machine (JVM). Most importantly, the maximum available main memory
   is specified through the -Xmx parameter of the JVM. The values have been
   chosen to be reasonable for most platforms. Please edit the starter
   scripts in case you need to adapt these default values (e.g., in order
   to increase the available main memory).  
   
   For some UNIX/Linux distributions, you will have to run the starter 
   script from within a shell environment. Please open your favorite shell
   and first check whether execution rights are correctly set for the
   starter script.
   
   Change to the installation folder and enter the following to make the
   starter script executable for the owner of the file:
   
       chmod u+x virtualcityDATABASE-Importer-Exporter.sh
     
   Afterwards, simply run the starter script by the following command:
   
       ./virtualcityDATABASE-Importer-Exporter.sh
   
b) Alternatively, you can directly run the virtualcityDATABASE-impexp.jar
   from within a shell environment without using the starter scripts. The
   runnable JAR archive can be found in the 'lib' subfolder of the
   installation folder. 

   Open a shell, change to the installation folder and type the following:
   
       java -jar lib/virtualcityDATABASE-impexp.jar [-options]
   
   Make sure to define reasonable values for the available main memory. 
   Otherwise you might quickly run into main memory issues due
   to restrictive JVM default values.
   
   This is also the recommended way to use the CLI version of the program. 
   See the provided starter scripts for further examples. 
   Type 'java -jar lib/virtualcityDATABASE-impexp.jar -help' to get a list
   of the available program arguments.
  
   
7. Developers
-------------

Claus Nagel <cnagel@virtualcitysystems.de>
Richard Redweik <rredweik@virtualcitysystems.de>


8. Contact
----------

cnagel@virtualcitysystems.de
rredweik@virtualcitysystems.de


9. Websites
------------

Official virtualcityDATABASE Importer/Exporter website: 
http://www.virtualcitysystems.de//en/products/virtualcitydatabase

Related websites:
http://www.3dcitydb.org/
http://www.citygml.org/
http://www.citygmlwiki.org/
http://www.opengeospatial.org/standards/citygml


10. Disclaimer
--------------

THIS SOFTWARE IS PROVIDED BY virtualcitySYSTEMS GmbH "AS IS" AND "WITH ALL 
FAULTS." virtualcitySYSTEMS GmbH MAKES NO REPRESENTATIONS OR WARRANTIES OF 
ANY KIND CONCERNING THE QUALITY, SAFETY OR SUITABILITY OF THE SOFTWARE,
EITHER EXPRESSED OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED 
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR 
NON-INFRINGEMENT.

virtualcitySYSTEMS GmbH MAKES NO REPRESENTATIONS OR WARRANTIES AS TO THE
TRUTH, ACCURACY OR COMPLETENESS OF ANY STATEMENTS, INFORMATION OR MATERIALS
CONCERNING THE SOFTWARE THAT IS CONTAINED ON AND WITHIN ANY OF THE 
WEBSITES OWNED AND OPERATED BY virtualcitySYSTEMS GmbH.

IN NO EVENT WILL virtualcitySYSTEMS GmbH BE LIABLE FOR ANY INDIRECT, 
PUNITIVE, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES HOWEVER THEY MAY
ARISE AND EVEN IF virtualcitySYSTEMS GmbH HAVE BEEN PREVIOUSLY ADVISED OF
THE POSSIBILITY OF SUCH DAMAGES.