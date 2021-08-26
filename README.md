![Gradle build](https://img.shields.io/github/workflow/status/3dcitydb/importer-exporter/impexp-build?logo=Gradle&logoColor=white&style=flat-square)
[
![Docker build 3dcitydb/impexp:edge](https://img.shields.io/github/workflow/status/3dcitydb/importer-exporter/docker-build-edge?label=debian&style=flat-square&logo=Docker&logoColor=white)
![debian size](https://img.shields.io/docker/image-size/3dcitydb/impexp/edge?label=debian&logo=Docker&logoColor=white&style=flat-square)
![https://hub.docker.com/repository/docker/3dcitydb/impexp](https://img.shields.io/github/workflow/status/3dcitydb/importer-exporter/docker-build-edge-alpine?label=alpine&style=flat-square&logo=Docker&logoColor=white)
![alpine size](https://img.shields.io/docker/image-size/3dcitydb/impexp/edge-alpine?label=alpine&logo=Docker&logoColor=white&style=flat-square)
](https://hub.docker.com/repository/docker/3dcitydb/impexp)

3D City Database Importer/Exporter
==================================

The 3D City Database Importer/Exporter is a Java based client for the [3D City Database](https://github.com/3dcitydb/3dcitydb).
It allows for high-performance loading and extracting 3D city model data.

* Support for CityGML 2.0 and 1.0
* Support for CityJSON 1.0
* Support for CityGML Application Domain Extensions (ADEs) through
  software extensions
* Support for PostgreSQL/PostGIS and Oracle Locator/Spatial
* Reading/writing CityGML instance documents of arbitrary file size
* Export of KML/COLLADA/glTF models including tiling schemas for
  visualization and interactive exploration of large city models
  in Digital Earth Browsers, 3D GIS, and computer graphics software
* Generic KML information balloons
* Export of thematic object data into tables. Supported data formats are
  CSV and Microsoft Excel
* Resolving and preservation of forward and backwards XLinks in
  CityGML datasets
* Full support of 3D Coordinate Reference Systems (CRS) and 3D
  coordinate transformations; support for user-defined CRS
* Coordinate transformations for CityGML/CityJSON exports
* Map window for graphical selection of bounding boxes
* Validation of CityGML/CityJSON instance documents
* Multithreaded programming facilitating high-performance data processing

The 3D City Database Importer/Exporter comes with both a Graphical User Interface (GUI) and a
Command Line Interface (CLI). The CLI allows for employing the tool in batch processing workflows and third party
applications.

License
-------
The 3D City Database Importer/Exporter is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
See the `LICENSE` file for more details.

Note that releases of the software before version 3.3.0 continue to be licensed under GNU LGPL 3.0. To request a
previous release of the 3D City Database Importer/Exporter under Apache License 2.0 create a GitHub issue.

Latest release
--------------
The latest stable release of the 3D City Database Importer/Exporter is 4.3.0.

Download a Java-based executable installer for the software [here](https://github.com/3dcitydb/importer-exporter/releases/download/v4.3.0/3DCityDB-Importer-Exporter-4.3.0-Setup.jar).
Previous releases are available from the [releases section](https://github.com/3dcitydb/importer-exporter/releases).

System requirements
-------------------
* Java JRE or JDK >= 8
* [3D City Database](https://github.com/3dcitydb/3dcitydb) on
  - PostgreSQL DBMS >= 9.6 with PostGIS extension >= 2.3
  - Oracle DBMS >= 10G R2 with Spatial or Locator option
  
The 3D City Database Importer/Exporter can be run on any platform providing appropriate Java support.

Documentation and literature
----------------------------
A complete and comprehensive user manual on the 3D City Database and the Importer/Exporter is available
[online](https://3dcitydb-docs.readthedocs.io/en/release-v4.3.0/).

An Open Access paper on the 3DCityDB has been published in the International Journal on Open Geospatial Data,
Software and Standards 3 (5), 2018: [Z. Yao, C. Nagel, F. Kunde, G. Hudra, P. Willkomm, A. Donaubauer, T. Adolphi, T. H. Kolbe: 3DCityDB - a 3D geodatabase solution for the management, analysis, and visualization of semantic 3D city models based on CityGML](https://doi.org/10.1186/s40965-018-0046-7). Please use this reference when citing the 3DCityDB project.

Contributing
------------

* To file bugs found in the software create a GitHub issue.
* To contribute code for fixing filed issues create a pull request with the issue id.
* To propose a new feature create a GitHub issue and open a discussion.

Installing and running
----------------------
The easiest way to get the Importer/Exporter running on your computer is to download an installer from the
[releases section](https://github.com/3dcitydb/importer-exporter/releases). The installers are named
`3DCityDB-Importer-Exporter-<version>-Setup.jar` and are packaged as executable JAR file. So double-clicking the
JAR file should run the installer. The installer will guide you through the steps of the installation process.

After installation, start scripts are available in the installation directory to launch the application.
During setup, you can additionally choose to create shortcuts on your desktop and in the start menu of your
preferred OS.

Simply execute the start script suitable for your platform:

* `3DCityDB-Importer-Exporter.bat` (Microsoft Windows family)
* `3DCityDB-Importer-Exporter` (UNIX/Linux family, macOS)

On most platforms, double-clicking the start script or its shortcut launches the application.
Please refer to the [user manual](https://3dcitydb-docs.readthedocs.io/en/release-v4.3.0/) for more information
on how to install and run the tool.

Building
--------
The Importer/Exporter uses [Gradle](https://gradle.org/) as build system. To build the application from source,
clone the repository to your local machine and run the following command from the root of the repository. 

    > gradlew installDist

The script automatically downloads all required dependencies for building and running the Importer/Exporter.
So make sure you are connected to the internet. The build process runs on all major operating systems and only
requires a Java 8 JDK or higher to run.

If the build was successful, you will find the Importer/Exporter package under `impexp-client/build/install`.
To launch the application, simply use the starter scripts in the output folder.

You may also choose to build an installer for the Importer/Exporter with the following command.

    > gradlew buildInstaller

The installer package will be available under `impexp-client/build/distributions`.

Cooperation partners and supporters
-----------------------------------

The 3D City Database Importer/Exporter has been developed by and with the support from the
following cooperation partners:

* [Chair of Geoinformatics, Technical University of Munich](https://www.lrg.tum.de/gis/)
* [Virtual City Systems, Berlin](https://vc.systems/)
* [M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen](http://www.moss.de/)

More information
----------------
[OGC CityGML](http://www.opengeospatial.org/standards/citygml) is an open data model and XML-based format for the
storage and exchange of semantic 3D city models. It is an application schema for the
[Geography Markup Language version 3.1.1 (GML3)](http://www.opengeospatial.org/standards/gml), the extensible
international standard for spatial data exchange issued by the Open Geospatial Consortium (OGC) and the ISO TC211.
The aim of the development of CityGML is to reach a common definition of the basic entities, attributes,
and relations of a 3D city model.

CityGML is an international OGC standard and can be used free of charge.