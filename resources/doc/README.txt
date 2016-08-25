!impexp.name! v!impexp.version!

  This software is free software and is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. 


0. Index
--------

1. License
2. Copyright
3. About
4. System requirements
5. (Un)Installation
6. Running the application
7. Cooperation partners and supporters
8. Active Developers
9. Contact
10. Websites
11. Disclaimer


1. License
----------

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this software except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0


2. Copyright
------------

(C) !vendor.copyright.year!
!vendor.name!
!vendor.organisation!, !vendor.country!
!vendor.homepage!


3. About
--------

The !impexp.name! is a Java based front-end for 
the !3dcitydb.name! version !3dcitydb.version!. It allows for high-performance 
importing and exporting spatial data for a virtual 3D city model.

* Full support for CityGML versions 2.0.0 and 1.0.0
* Support for Oracle Spatial, Oracle Locator, and PostGIS
* Reading/writing CityGML instance documents of arbitrary file size
* Export of KML/COLLADA/glTF models including tiling schemas for 
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

The !impexp.name! comes with both a Graphical
User Interface (GUI) and a Command Line Interface (CLI). The CLI 
allows for employing the tool in batch processing workflows and 
third party applications.


4. System requirements
----------------------

* Java JRE or JDK >= 1.8
* !3dcitydb.name! version !3dcitydb.version! on
  - Oracle Spatial DBMS >= 10G R2 with Spatial or Locator option
  - PostgreSQL DBMS >= 9.1 with PostGIS extension >= 2.0
  
The !impexp.name! can be run on any platform 
providing appropriate Java support. It has been tested on the 
following platforms:
  - Microsoft Windows XP, Vista, Windows 7, 8, 8.1, 10
  - Apple Mac OS X 10.9, 10.11
  - Ubuntu 14, 15


5. (Un)Installation
-------------------

It is recommended to use the universal installer to unpack the 
!impexp.name! application files to your local 
computer. The universal installer will guide you through the steps of the
installation process. Afterwards, you can immediately run the application 
(see next section).

When you successfully install the !impexp.name!,
an uninstaller is automatically generated in the 'uninstaller' subfolder
of the installation folder. Please use this uninstaller in order to remove 
the !impexp.name! application files from your
computer. Alternatively, you can simply delete the entire installation
folder.


6. Running the application
--------------------------

For running the !impexp.name! use one of the 
following options:

a) Recommended:
   Use the provided starter scripts to launch the application. The starter
   scripts can be found in the installation folder. During setup you can
   choose to additionally create shortcuts for the starter scripts on your 
   desktop and in the start menu of your preferred OS.
   
   Depending on the platform, please run one the following starter scripts:
   - 3DCityDB-Importer-Exporter.bat     (Microsoft Windows family)
   - 3DCityDB-Importer-Exporter.sh      (UNIX/Linux family)
   - 3DCityDB-Importer-Exporter.command (Mac OS X) 
   
   On most platforms, double-clicking the starter script or its shortcut
   runs the !impexp.name!. 
   
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
   
       chmod u+x 3DCityDB-Importer-Exporter.sh
     
   Afterwards, simply run the starter script by the following command:
   
       ./3DCityDB-Importer-Exporter.sh
   
b) Alternatively, you can directly run the !impexp.jar.filename!
   from within a shell environment without using the starter scripts. The
   runnable JAR archive can be found in the 'lib' subfolder of the
   installation folder. 

   Open a shell, change to the installation folder and type the following:
   
       java -jar lib/!impexp.jar.filename! [-options]
   
   Make sure to define reasonable values for the available main memory. 
   Otherwise you might quickly run into main memory issues due
   to restrictive JVM default values.
   
   This is also the recommended way to use the CLI version of the program. 
   See the provided starter scripts for further examples. 
   Type 'java -jar lib/!impexp.jar.filename! -help' to get a list of the
   available program arguments.
  
  
7. Cooperation partners and supporters  
--------------------------------------

The !impexp.name! v!impexp.version! has been developed by
and with the support from the following cooperation partners:

* Chair of Geoinformatics, Technical University of Munich
  https://www.gis.bgu.tum.de/
* virtualcitySYSTEMS GmbH, Berlin
  http://www.virtualcitysystems.de/
* M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen
  http://www.moss.de/
  
   
8. Active Developers
--------------------

Claus Nagel <cnagel@virtualcitysystems.de>
Richard Redweik <rredweik@virtualcitysystems.de>
Felix Kunde <felix-kunde@gmx.de>
Zhihang Yao <zhihang.yao@tum.de>
György Hudra <ghudra@moss.de>
Thomas H. Kolbe <thomas.kolbe@tum.de>

Version !impexp.version! is based on earlier versions of the
!impexp.name!. Please refer to the !3dcitydb.name!
documentation for the list of all contributors to previous versions. 


9. Contact
----------

cnagel@virtualcitysystems.de
thomas.kolbe@tum.de


10. Websites
------------

Official !3dcitydb.name! website: 
!3dcitydb.homepage!

Related websites:
!3dcitydb.git!
!vendor.homepage!
http://www.citygml.org/
http://www.citygmlwiki.org/
http://www.opengeospatial.org/standards/citygml


11. Disclaimer
--------------

THIS SOFTWARE IS PROVIDED BY THE CHAIR OF GEOINFORMATION FROM TU MUNICH
(TUMGI) "AS IS" AND "WITH ALL FAULTS." 
TUMGI MAKES NO REPRESENTATIONS OR WARRANTIES OF ANY KIND CONCERNING THE 
QUALITY, SAFETY OR SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR 
IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.

TUMGI MAKES NO REPRESENTATIONS OR WARRANTIES AS TO THE TRUTH, ACCURACY OR 
COMPLETENESS OF ANY STATEMENTS, INFORMATION OR MATERIALS CONCERNING THE 
SOFTWARE THAT IS CONTAINED ON AND WITHIN ANY OF THE WEBSITES OWNED AND 
OPERATED BY TUMGI.

IN NO EVENT WILL TUMGI BE LIABLE FOR ANY INDIRECT, PUNITIVE, SPECIAL, 
INCIDENTAL OR CONSEQUENTIAL DAMAGES HOWEVER THEY MAY ARISE AND EVEN IF 
TUMGI HAVE BEEN PREVIOUSLY ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.