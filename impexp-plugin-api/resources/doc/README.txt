@name@

Version @version@ (@date@)

  This software is free software and is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. 


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

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this software except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0


2. Copyright
------------

(C) @vendorCopyright@
@vendorName@
@vendorOrganisation@, @vendorCountry@
@vendorWebsiteUrl@


3. About
--------

The @name@ is a Java class library
and API facilitating the development of extensions for the
@impexpFullName@ software.


4. Requirements
---------------

Plugins developed with this version of the API should only be used 
with version @version@ of the @impexpFullName@.
Although we try hard to keep the API as stable as possible, we cannot
guarantee that there will be no changes between different versions of
the @impexpFullName@. Please always check the changelog.


5. How to use
-------------

a) Make sure you are using Java 8 or higher.
b) Put the "@jar@" on your classpath.
c) Implement the "@pluginApiService@" interface.
d) Implement further extension interfaces required by your plugin.
e) Compile your plugin sources and create a JAR package.
f) Create a "META-INF/services/@pluginApiService@" file
   within the JAR (the file must be UTF-8 encoded).
g) On separate lines within the file, list the fully qualified binary 
   names of your concrete implementation(s) of the
   "@pluginApiService@" interface.
h) Put the JAR into the "@pluginsDir@" directory within the installation
   directory of the @impexpFullName@.

Steps e) to g) can usually be automated using an IDE.

Please consult the documentation of the @impexpFullName@
for a comprehensive description of the plugin API and its usage.

The plugin support of the @impexpFullName@ is implemented
on top of the Service Provider API introduced in Java SE 6. More details
about this API can be, for example, found here:
http://java.sun.com/developer/technicalArticles/javase/extensible/index.html

   
6. Developers
-------------

Claus Nagel <cnagel@virtualcitysystems.de>


7. Contact
----------

cnagel@virtualcitysystems.de
thomas.kolbe@tum.de


8. Websites
-----------

Official @citydbName@ website: 
@citydbWebsiteUrl@

Related websites:
@citydbVcsUrl@
@vendorWebsiteUrl@
http://www.citygml.org/
http://www.citygmlwiki.org/
http://www.opengeospatial.org/standards/citygml


9. Disclaimer
-------------

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