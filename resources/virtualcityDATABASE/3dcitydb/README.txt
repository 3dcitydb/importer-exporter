virtualcityDATABASE v2.1


0. Index
--------

1. License
2. Copyright
3. About
4. System requirements
5. Database setup
6. Database deletion
7. Documentation
8. Developers
9. Contact
10. Websites
11. Disclaimer


1. License
----------

The virtualcityDATABASE is a modified and branded version of the
free and open source software 3D City Database.

This software is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.


2. Copyright
------------

(C) 2013 - 2016
virtualcitySYSTEMS GmbH
Tauentzienstraße 7 b/c
10789 Berlin, Germany
http://www.virtualcitysystems.de/


3. About
--------

The virtualcityDATABASE is a free 3D geo database to store, represent, and
manage virtual 3D city models on top of a standard spatial relational
database. The database model contains semantically rich, hierarchically
structured, multi-scale urban objects facilitating complex GIS modeling and
analysis tasks, far beyond visualization. The schema of the virtualcityDATABASE
is based on the City Geography Markup Language (CityGML), an international
standard for representing and exchanging virtual 3D city models issued
by the Open Geospatial Consortium (OGC).

The virtualcityDATABASE has been realized as Oracle Spatial/Locator and
PostgreSQL/PostGIS database schema, supporting following key features:

    * Full support for CityGML versions 2.0.0 and 1.0.0
    * Complex thematic modelling
    * Five different Levels of Detail (LODs)
    * Appearance information (textures and materials)
    * Digital terrain models (DTMs)
    * Representation of generic and prototypical 3D objects
    * Free, also recursive aggregation of geo objects
    * Flexible 3D geometries
        
The virtualcityDATABASE enhances the 3D City Database
with advanced data management functionality.

The virtualcityDATABASE comes as a collection of SQL scripts that allow
for creating and dropping database instances.


4. System requirements
----------------------

* Oracle DBMS >= 10g R2 with Spatial or Locator option
* PostgreSQL DBMS >= 9.1 with PostGIS extension >= 2.0


5. Database setup
-----------------

To create a new database instance of the virtualcityDATABASE, simply execute
the SQL script "CREATE_DB.sql", which is available for both Oracle and
PostgreSQL and can be found in the respective subfolder of the distribution
package. The script will start the setup procedure and invoke additional
scripts in the background.

The execution of the script substantially differs for Oracle and PostgreSQL.
Please refer to the documentation of the virtualcityDATABASE for a
comprehensive step-by-step installation guide.

The setup procedure requires the following mandatory user inputs:
1) Spatial Reference Identifier for newly created geometry objects (SRID),
2) Corresponding GML conformant URN encoding for gml:srsName attributes.

For Oracle, two additional inputs are required:
3) Decision whether the database instance should be versioning enabled,
4) Whether the Oracle DBMS runs with Locator or Spatial option.


6. Database deletion
--------------------

To drop an existing database instance of the virtualcityDATABASE, simply execute
the SQL script "DROP_DB.sql" for your database system (Oracle or PostgreSQL).
Please refer to the PDF documentation of the 3D City Database for a
comprehensive step-by-step guide.


7. Documentation
----------------

A comprehensive documentation on the virtualcityDATABASE is provided in the
distribution package.


8. Developers
-------------

Claus Nagel <cnagel@virtualcitysystems.de>
Felix Kunde <fkunde@virtualcitysystems.de>


9. Contact
----------

cnagel@virtualcitysystems.de
fkunde@virtualcitysystems.de


10. Websites
------------

Official virtualcityDATABASE website: 
http://www.virtualcitysystems.de/

Related websites:
http://www.3dcitydb.org/
http://www.citygml.org/
http://www.citygmlwiki.org/
http://www.opengeospatial.org/standards/citygml


11. Disclaimer
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