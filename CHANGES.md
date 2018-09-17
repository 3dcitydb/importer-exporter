Change Log
==========

### 4.0 - tba

##### Additions
* Added new extension mechanism to support arbitrary CityGML ADEs.
* Added new ADE manager plugin to dynamically extend a 3DCityDB instance with support for a given CityGML ADE.
* Added support for connecting to different database schemas with the same user.
* Added support for XSL transformations on CityGML imports and exports.
* New database operation panel to change the spatial reference system used in the database (incl. optional coordinate transformation).
* New database operation panel to show supported ADEs.
* Added support for importing CityGML files with flat hierarchies.
* Added support for importing gml:MultiGeometry objects consisting only of surfaces. [#28](https://github.com/3dcitydb/importer-exporter/issues/28)
* Improved parsing of xAL addresses on CityGML imports.
* New LoD filter for CityGML exports.
* Added query language for CityGML exports. 
* Added support for exporting to glTF v2.0.
* Updated `collada2gltf` to latest version 2.1.3.
* Added styling of log messages on the console window.
* Introduced XML-based schema mapping file to denote the mapping of XML schema elements onto relational structures.
* Source code has been split into modules that are available as Maven artifacts from [Bintray](https://bintray.com/3dcitydb/maven).
* Switched from Ant to Gradle as build system.

##### Fixes
* Fixed bug when resolving geometry Xlinks and replacing gml:ids.
* Fixed bug in BBOX calculation on CityGML imports.
* Fixed `ConcurrentLockManager` to avoid concurrent modification issues.
* Fixed bug in PlantCover importer.
* Fixed bug in tiled CityGML exports.  
* Fixed KML/COLLADA/glTF export query for LandUse. [#52](https://github.com/3dcitydb/importer-exporter/pull/52)
* Fixed NPE in LandUse exporter. [#51](https://github.com/3dcitydb/importer-exporter/pull/51)
* Replaced DOS paths in KML network links. [#31](https://github.com/3dcitydb/importer-exporter/issues/31)
* Fixed bug in handling of surface_geometries with multiple surface_data. [#25](https://github.com/3dcitydb/importer-exporter/pull/25)
* Fixed KML/COLLADA/glTF export of appearances with more than one theme. [#24](https://github.com/3dcitydb/importer-exporter/issues/24)

##### Miscellaneous 
* [3DCityDB Docker images](https://github.com/tum-gis/3dcitydb-docker-postgis) are now available for a range of 3DCityDB versions to support continuous integration workflows.
