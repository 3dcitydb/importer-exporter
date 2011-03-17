!app.title! v!app.version!

  This program is free software and comes WITHOUT ANY WARRANTY. 
  See the file LICENSE for more details. 


0. Index
--------

1. Licence
2. Copyright
3. About
4. System requirements
5. (Un)Installation
6. Running the application
7. Cooperation partners and supporters
8. Developers
9. Contact
10. Websites


1. Licence
----------

The !app.title! is free software under
the GNU Lesser General Public License Version 3.0. See the file LICENSE 
for more details. For a copy of the GNU Lesser General Public License see 
the files COPYING and COPYING.LESSER or visit http://www.gnu.org/licenses/.


2. Copyright
------------

(c) 2007 - 2011
Institute for Geodesy and Geoinformation Science (IGG)
Technische Universitaet Berlin, Germany
!vendor.homepage!


3. About
--------

The !app.title! is a Java based front-end for 
the 3D City Database version 2.0. It allows for high-performance 
importing and exporting spatial data for a virtual 3D city model.

Main characteristics:
* Full support for CityGML version 1.0.0 and 0.4.0
* Reading/writing CityGML instance documents of arbitrary file size
* Multithreaded programming facilitating high-performance CityGML 
  processing
* Support for CityGML appearances such as textures
* Resolving of XLinks
* XML validation of CityGML instance documents
* Matching/merging of building features within the database

The !app.title! is shipped with both a Graphical
User Interface (GUI) for end-user interaction and a Command Line 
Interface (CLI). The latter one allows for employing the tool in batch 
processing workflows or embedding its functionality into third party
programs.     


4. System requirements
----------------------

* Java JRE or JDK >= 1.6.0_05
* 3D City Database version 2.0.5 on an Oracle Spatial DBMS >= 10G R2
  (version 2.0.5 is mandatory for matching/merging functionality)
  
The !app.title! can be run on any platform 
providing appropriate Java support. It has been tested on the 
following platforms:
  - Microsoft Windows XP, Vista, Windows 7
  - Apple Mac OS X 10.6
  - Ubuntu 9, 10


5. (Un)Installation
-------------------

It is recommended to use the universal installer to unpack the 
!app.title! application files to your local 
computer. The universal installer will guide you through the steps of the
installation process. Afterwards, you can immediately run the application 
(see next section).

When you successfully install the !app.title!,
an uninstaller is automatically generated in the 'uninstaller' subfolder
of the installation folder. Please use this uninstaller in order to remove 
the !app.title! application files from your
computer. Alternatively, you can simply delete the entire installation
folder.


6. Running the application
--------------------------

For running the !app.title! use one of the 
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
   
b) Alternatively, you can directly run the !app.jar!
   from within a shell environment without using the starter scripts. The
   runnable JAR archive can be found in the 'lib' subfolder of the
   installation folder. 

   Open a shell and type the following: 
       java -jar !app.jar! [-options]
   
   Make sure to define the memory limits for the Java heap space. 
   Otherwise you might quickly run into heap space memory lacks due
   to restrictive JVM default values.
   
   This is the recommended way to use the CLI version of the program. 
   See the provided starter scripts for further examples. 
   Type 'java -jar !app.jar! -help' to get a list of supported 
   command line parameters.
  
  
7. Cooperation partners and supporters  
--------------------------------------

The development of the !app.title! has been 
financially supported by the following cooperation partners:

* Business Location Center, Berlin 
  (http://www.businesslocationcenter.de)
* virtualcitySYSTEMS GmbH, Berlin
  (www.virtualcitysystems.de)
* Berlin Senate of Business, Technology and Women
  (http://www.berlin.de/sen/wtf/)
  
   
8. Developers
-------------

Claus Nagel <claus.nagel@tu-berlin.de>
Javier Herreruela <javier.herreruela@tu-berlin.de>
Alexandra Lorenz <lorenz@tu-berlin.de>
Gerhard Koenig <gerhard.koenig@tu-berlin.de>
Thomas H. Kolbe <thomas.kolbe@tu-berlin.de>


9. Contact
----------

claus.nagel@tu-berlin.de
javier.herreruela@tu-berlin.de


10. Websites
-----------

Official !app.title! website: 
!app.homepage!

Related websites:
!vendor.homepage!
http://www.citygml.org/
http://www.citygmlwiki.org/
http://www.opengeospatial.org/standards/citygml