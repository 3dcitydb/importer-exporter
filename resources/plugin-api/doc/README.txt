!impexp.plugin.api.name! v!impexp.plugin.api.version!

  This library is free software and comes WITHOUT ANY WARRANTY.
  See the DISCLAIMER at the end of this document for more details.  


0. Index
--------

1. License
2. Copyright
3. About
4. Requirements
5. How to use
6. Developers
7. Contact
8. Websites
9. Disclaimer


1. License
----------

The !impexp.plugin.api.name! is free software under
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

The !impexp.plugin.api.name! is a Java class library
and API facilitating the development of own extensions for the
!impexp.name! program.


4. Requirements
---------------

Plugins developed with this version of the API should only be used 
with version !impexp.version! of the !impexp.name!.
Although we try hard to keep the API as stable as possible, we cannot
guarantee that there will be no changes between different versions of
the !impexp.name!. Please always check the changelog.


5. How to use
-------------

a) Make sure you are using a Java JDK >= 1.6.0_05.
b) Put the "!impexp.plugin.api.jar.filename!" in your classpath.
c) Implement the "!impexp.plugin.api.maininterface!" interface.
d) Implement further extension interfaces required by your plugin.
e) Compile your plugin sources and create a JAR package.
f) Create a "META-INF/services/!impexp.plugin.api.maininterface!" file
   within the JAR (the file must be UTF-8 encoded).
g) On separate lines within the file, list the fully qualified binary 
   names of your concrete implementation(s) of the
   "!impexp.plugin.api.maininterface!" interface.
h) Put the JAR into the "!dir.dest.plugins.relative!" subfolder within the installation
   folder of the !impexp.name!.

Steps e) to g) can usually be automated using an IDE.

Please consult the documentation of the !impexp.name!
for a comprehensive description of the plugin API and its usage.

The plugin support of the !impexp.name! is implemented
on top of the Service Provider API introduced in Java SE 6. More details
about this API can be, for example, found here:
http://java.sun.com/developer/technicalArticles/javase/extensible/index.html

   
6. Developers
-------------

Claus Nagel <claus.nagel@tu-berlin.de>


7. Contact
----------

claus.nagel@tu-berlin.de


8. Websites
-----------

Official !impexp.plugin.api.name! website: 
!impexp.plugin.api.homepage!

Related websites:
!vendor.homepage!
http://www.citygml.org/
http://www.citygmlwiki.org/
http://www.opengeospatial.org/standards/citygml


9. Disclaimer
-------------

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