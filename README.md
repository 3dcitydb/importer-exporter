3D City Database Importer/Exporter
==================================

The 3D City Database Importer/Exporter is a Java based front-end for the [3D City Database](https://github.com/3dcitydb/3dcitydb). It allows for high-performance loading and extracting 3D city model data.

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
* XML validation of CityGML instance documents
* Multithreaded programming facilitating high-performance CityGML 
  processing

The 3D City Database Importer/Exporter comes with both a Graphical User Interface (GUI) and a Command Line Interface (CLI). The CLI 
allows for employing the tool in batch processing workflows and third party applications.

License
-------
The 3D City Database Importer/Exporter is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0). See the `LICENSE` file for more details.

Note that releases of the software before version 3.3.0 continue to be licensed under GNU LGPL 3.0. To request a previous release of the 3D City Database Importer/Exporter under Apache License 2.0 create a GitHub issue.

Latest release
--------------
The latest stable release of the 3D City Database Importer/Exporter is 3.2.0.

Download a Java-based executable installer for the software [here](https://github.com/3dcitydb/importer-exporter/releases/download/v3.2.0/3DCityDB-Importer-Exporter-3.2-Setup.jar). Previous releases are available from the [releases section](https://github.com/3dcitydb/importer-exporter/releases).

System requirements
-------------------
* Java JRE or JDK >= 1.8
* [3D City Databse](https://github.com/3dcitydb/3dcitydb) on
  - Oracle DBMS >= 10G R2 with Spatial or Locator option
  - PostgreSQL DBMS >= 9.1 with PostGIS extension >= 2.0
  
The 3D City Database Importer/Exporter can be run on any platform providing appropriate Java support. 

Documentation
-------------
A complete and comprehensive documentation on the 3D City Database and the Importer/Exporter tool is installed with the software and is available [online](http://www.3dcitydb.org/3dcitydb/documentation/).

Contributing
------------
* To file bugs found in the software create a GitHub issue.
* To contribute code for fixing filed issues create a pull request with the issue id.
* To propose a new feature create a GitHub issue and open a discussion.

Cooperation partners and supporters  
-----------------------------------

The 3D City Database Importer/Exporter has been developed by and with the support from the following cooperation partners:

* [Chair of Geoinformatics, Technical University of Munich](https://www.gis.bgu.tum.de/)
* [virtualcitySYSTEMS GmbH, Berlin](http://www.virtualcitysystems.de/)
* [M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen](http://www.moss.de/)

More information
----------------
[OGC CityGML](http://www.opengeospatial.org/standards/citygml) is an open data model and XML-based format for the storage and exchange of semantic 3D city models. It is an application schema for the [Geography Markup Language version 3.1.1 (GML3)](http://www.opengeospatial.org/standards/gml), the extendible international standard for spatial data exchange issued by the Open Geospatial Consortium (OGC) and the ISO TC211. The aim of the development of CityGML is to reach a common definition of the basic entities, attributes, and relations of a 3D city model.

CityGML is an international OGC standard and can be used free of charge.
