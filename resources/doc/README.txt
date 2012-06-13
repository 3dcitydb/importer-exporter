!impexp.name! v!impexp.version!

  This program is free software and comes WITHOUT ANY WARRANTY.
  See the DISCLAIMER at the end of this document for more details.  


0. Index
--------

1. License
2. Copyright
3. About
4. System requirements
5. (Un)Installation
6. Running the application
7. Cooperation partners and supporters
8. Developers
9. Contact
10. Websites
11. Disclaimer


1. License
----------

The !impexp.name! is free software under
the GNU Lesser General Public License Version 3.0. See the file LICENSE 
for more details. For a copy of the GNU Lesser General Public License see 
the files COPYING and COPYING.LESSER or visit http://www.gnu.org/licenses/.


2. Copyright
------------

(c) !vendor.copyright.year!
!vendor.name!
!vendor.organisation!, !vendor.country!
!vendor.homepage!


3. About
--------

The !impexp.name! is a Java based front-end for 
the 3D City Database version 2.0. It allows for high-performance 
importing and exporting spatial data for a virtual 3D city model.

Main characteristics:
* Full support for CityGML version 1.0.0 and 0.4.0
* Full support of 3D CRS and 3D coordinate transformations
* Export of KML/COLLADA models
* Generic KML information balloons
* Reading/writing CityGML instance documents of arbitrary file size
* Resolving of forward and backwards XLinks
* User-defined Coordinate Reference System
* Coordinate transformations for CityGML exports
* Map window for graphical selection of bounding boxes
* XML validation of CityGML instance documents
* Multithreaded programming facilitating high-performance CityGML 
  processing
* Matching/merging of building features within the database
* Proxy support for HTTP, HTTPS, and SOCKS protocols

The !impexp.name! is shipped with both a Graphical
User Interface (GUI) for end-user interaction and a Command Line 
Interface (CLI). The latter one allows for employing the tool in batch 
processing workflows or embedding its functionality into third party
programs.     


4. System requirements
----------------------

* Java JRE or JDK >= 1.6.0_05
* 3D City Database version 2.0.6 on an Oracle Spatial DBMS >= 10G R2
  
The !impexp.name! can be run on any platform 
providing appropriate Java support. It has been tested on the 
following platforms:
  - Microsoft Windows XP, Vista, Windows 7
  - Apple Mac OS X 10.6
  - Ubuntu 9, 10, 11


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
   - 3DCityDB-Importer-Exporter.bat (Microsoft Windows family)
   - 3DCityDB-Importer-Exporter.sh  (UNIX/Linux and derivates, Mac OS X) 
   
   Execute the starter script by simply double-clicking on it. This will 
   work for most platforms.
   
   PLEASE NOTE:
   The starter scripts override default settings of the Java Virtual 
   Machine (JVM). The provided values should be reasonable for most 
   systems. Please edit the starter scripts in case you need to adapt 
   these values (e.g., in order to increase Java heap space size).  
   
   On some UNIX/Linux derivates you will have to run the starter script 
   from within a shell environment. Please open your favorite shell and
   check whether execution permission is set for the starter script.
   
   Change to the installation folder and enter the following to make the
   starter script executable for the owner of the file:
   
       chmod u+x 3DCityDB-Importer-Exporter.sh
     
   Afterwards, simply run the starter script by typing:
   
       ./3DCityDB-Importer-Exporter.sh
   
b) Alternatively, you can directly run the !impexp.jar.filename!
   from within a shell environment without using the starter scripts. The
   runnable JAR archive can be found in the 'lib' subfolder of the
   installation folder. 

   Open a shell and type the following: 
       java -jar !impexp.jar.filename! [-options]
   
   Make sure to define the memory limits for the Java heap space. 
   Otherwise you might quickly run into heap space memory lacks due
   to restrictive JVM default values.
   
   This is the recommended way to use the CLI version of the program. 
   See the provided starter scripts for further examples. 
   Type 'java -jar !impexp.jar.filename! -help' to get a list of supported 
   command line parameters.
  
  
7. Cooperation partners and supporters  
--------------------------------------

The development of the !impexp.name! has been 
financially supported by the following cooperation partners:

* Business Location Center, Berlin 
  http://www.businesslocationcenter.de/
* virtualcitySYSTEMS GmbH, Berlin
  http://www.virtualcitysystems.de/
* Berlin Senate of Business, Technology and Women
  http://www.berlin.de/sen/wtf/
  
   
8. Developers
-------------

Claus Nagel <claus.nagel@tu-berlin.de>
Javier Herreruela <javier.herreruela@tu-berlin.de>
Alexandra Lorenz <lorenz@tu-berlin.de>
Gerhard König <gerhard.koenig@tu-berlin.de>
Thomas H. Kolbe <thomas.kolbe@tu-berlin.de>


9. Contact
----------

claus.nagel@tu-berlin.de
javier.herreruela@tu-berlin.de


10. Websites
------------

Official !impexp.name! website: 
!impexp.homepage!

Related websites:
!vendor.homepage!
http://www.gis.tu-berlin.de/software
http://www.citygml.org/
http://www.citygmlwiki.org/
http://www.opengeospatial.org/standards/citygml


11. Disclaimer
--------------

THIS SOFTWARE IS PROVIDED BY IGG "AS IS" AND "WITH ALL FAULTS." 
IGG MAKES NO REPRESENTATIONS OR WARRANTIES OF ANY KIND CONCERNING THE 
QUALITY, SAFETY OR SUITABILITY OF THE SOFTWARE, EITHER EXPRESSED OR 
IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.

IGG MAKES NO REPRESENTATIONS OR WARRANTIES AS TO THE TRUTH, ACCURACY OR 
COMPLETENESS OF ANY STATEMENTS, INFORMATION OR MATERIALS CONCERNING THE 
SOFTWARE THAT IS CONTAINED ON AND WITHIN ANY OF THE WEBSITES OWNED AND 
OPERATED BY IGG.

IN NO EVENT WILL IGG BE LIABLE FOR ANY INDIRECT, PUNITIVE, SPECIAL, 
INCIDENTAL OR CONSEQUENTIAL DAMAGES HOWEVER THEY MAY ARISE AND EVEN IF 
IGG HAVE BEEN PREVIOUSLY ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.