3D City Database version 2.0.6-postgis

  The 3D City Database version 2.0.6-postgis is free software and comes 
  WITHOUT ANY WARRANTY. See the DISCLAIMER at the end of this document
  for more details. 


0. Index
--------

1. License
2. Copyright
3. About
4. System requirements
5. Database setup
6. Database deletion
7. Documentation
8. Cooperation partners and supporters
9. Developers
10. Contact
11. Websites
12. Disclaimer


1. License
----------

The 3D City Database version 2.0.6-postgis is free software under the GNU Lesser 
General Public License Version 3.0. See the file LICENSE for more details. 
For a copy of the GNU Lesser General Public License see the files 
COPYING and COPYING.LESSER or visit http://www.gnu.org/licenses/.


2. Copyright
------------

(c) 2007 - 2012
Institute for Geodesy and Geoinformation Science (IGG)
Technische Universitaet Berlin, Germany
http://www.igg.tu-berlin.de/


3. About
--------

The 3D City Database is a free 3D geo database to store, represent, and
manage virtual 3D city models on top of a standard spatial relational
database. The database model contains semantically rich, hierarchically
structured, multi-scale urban objects facilitating complex GIS modeling and
analysis tasks, far beyond visualization. The schema of the 3D City Database
is based on the City Geography Markup Language (CityGML), an international
standard for representing and exchanging virtual 3D city models issued
by the Open Geospatial Consortium (OGC).

The 3D City Database has been realized as an PostgreSQL / PostGIS relational
database schema, supporting following key features:

    * Complex thematic modelling
    * Five different Levels of Detail (LODs)
    * Appearance data
    * Digital terrain models (DTMs)
    * Representation of generic and prototypical 3D objects
    * Free, also recursive aggregation of geo objects
    * Flexible 3D geometries
    * Aerial photographs

The 3D City Database is shipped as a collection of SQL scripts which allow
for creating and dropping database instances.

The 3D City Database was implemented on behalf of the Berliner
Senatsverwaltung fuer Wirtschaft, Arbeit und Frauen and the Berlin Partner GmbH.
The development is continuing the work of the Institute for Cartography and
Geoinformation (IKG), University of Bonn. Please find the previous version of the
database at http://www.3dcitydb.net/.


4. System requirements
----------------------

* PostgreSQL >= 8.4
* PostGIS >= 2.0


5. Database setup
-----------------

To create a new database instance of the 3D City Database use the psql-console
to call the SQL script "CREATE_DB.sql" which can be found in the top-level SQL 
folder of the distribution package. This script will start the setup procedure 
and invoke further scripts in the background. 

It command can look like this:
psql -h your_host_address -p 5432 -U your_username -d name_of_database 
-f "CREATE_DB.sql"

Note: 
AN EMPTY POSTGIS-DATABASE HAS TO BE CREATED FIRST! THE SPECIFIED DATABASE
MUST ALREADY EXIST ON THE POSTGRESQL DBMS SERVER!

It also possible to fasten the setup by editing the batchfile CREATE_DB.bat in
a way that it suits to the system environment. 

Settings for batchfiles:

set PGPORT  =  5432 (this is the default port)
set PGHOST  =  your_host_address ('localhost' on local machine)
set PGUSER  =  your_username ('postgres' is the default superuser)
set CITYDB  =  name of the already existing empty PostGIS-database  
set PGBIN   =  path_to_psql.exe (e.g. 'C:\PostgreSQL\9.1\bin' or 
			   'C:\pgAdmin III\1.14')

When executed the user is asked for his PostgreSQL login-password.

The setup procedure requires two mandatory user inputs:
1) Spatial Reference Identifier for newly created geometry objects (SRID),
2) corresponding GML conformant URN encoding for gml:srsName attributes

e.g. for Berlin these parameters are:
Please enter a valid SRID (e.g. 4326 for WGS84): 3068
Please enter the corresponding SRSName to be used in GML exports (e.g. 
urn:ogc:def:crs:EPSG:4326 for WGS84): urn:ogc:def:crs,crs:EPSG:6.12:3068,
crs:EPSG:6.12:5783

Values for WGS84 are suggested in brackets BUT these are nor default-values!
If no numeric value is set for the SRID-variable spatial columns can't be 
created. To change the reference system afterwards the function 
util_change_db_srid found in the geodb_pkg.schema can be used. To avoid any
errors, loss of data or long waiting times this function should be executed
on an empty database. 


6. Database deletion
--------------------

To drop an existing database instance of the 3D City Database call the SQL script
"DROP_DB.sql" which can be found in the top-level SQL folder of the 
distribution package. The batchfile DROP_DB.bat can also be used in the same
manner like CREATE_DB.bat. Note that DROP_DB only clears the primarily created
PostGIS-database from the relational schema of the 3D City Database. The database
itself is not dropped.


7. Documentation
----------------

A comprehensive documentation on the 3D City Database version 2.0.6 and on its
porting to PostGIS can be found on the project's website at 
http://www.igg.tu-berlin.de/software/. 


8. Cooperation partners and supporters  
--------------------------------------

The development of the 3D City Database version 2.0.6 has been 
financially supported by the following cooperation partners:

* Business Location Center, Berlin 
  http://www.businesslocationcenter.de/
* virtualcitySYSTEMS GmbH, Berlin
  http://www.virtualcitysystems.de/
* Berlin Senate of Business, Technology and Women
  http://www.berlin.de/sen/wtf/

  
9. Developers
-------------

Claus Nagel <claus.nagel@tu-berlin.de>
Javier Herreruela <javier.herreruela@tu-berlin.de>
Felix Kunde <felix-kunde@gmx.de>
Alexandra Lorenz <lorenz@tu-berlin.de>
Gerhard Koenig <gerhard.koenig@tu-berlin.de>
Thomas H. Kolbe <thomas.kolbe@tu-berlin.de>


10. Contact
----------

claus.nagel@tu-berlin.de
javier.herreruela@tu-berlin.de
felix-kunde@gmx.de
lorenz@tu-berlin.de
gerhard.koenig@tu-berlin.de
thomas.kolbe@tu-berlin.de


11. Websites
------------

Official 3D City Database website: 
http://www.3dcitydb.net/

Related websites:
http://www.igg.tu-berlin.de/
http://www.gis.tu-berlin.de/software
http://www.citygml.org/
http://www.citygmlwiki.org/
http://www.opengeospatial.org/standards/citygml


12. Disclaimer
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