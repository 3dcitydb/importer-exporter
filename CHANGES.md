Change Log
==========

### 4.1 - tbd

##### Additions
* Added support for using SQL queries and XML-based filter expressions in CityGML exports. Please refer to the documentation
to learn more about the new filter capabilities.
* Added support for importing CityGML data from ZIP/GZIP files and for exporting CityGML content to ZIP/GZIP files. [#62](https://github.com/3dcitydb/importer-exporter/issues/62), 
[#63](https://github.com/3dcitydb/importer-exporter/issues/63)
* Added a counter and a progress bar to spreadsheet exports. [#50](https://github.com/3dcitydb/importer-exporter/issues/50)

##### Changes
* Due to changes in the Google Maps API license and usage terms, the Google services now require an API key
([read more here](https://developers.google.com/maps/documentation/geocoding/get-api-key)). This affects the map window
and the KML/COLLADA/glTF export of the Importer/Exporter, where Google services are used for address searches and for
retrieving height values from the Google Earth terrain model. [#61](https://github.com/3dcitydb/importer-exporter/issues/61)
  * If you want to continue using the Goolge services, then enter your Google API key in the corresponding global preferences
    dialog that has been added in this release.
  * The map window now uses [OSM Nominatim](https://wiki.openstreetmap.org/wiki/Nominatim) as the default
    geocoding service, so no need for a Google API key.
  * Querying height values from the Google Earth terrain model in KML/COLLADA/glTF exports requires the Google Elevation
    service though. Simply deactivate this option if you do not have an API key.

##### Fixes
* When running on Java 9 or higher, the following warning was printed to the console: `WARNING: Illegal reflective access by com.sun.xml.bind.v2.runtime.reflect.opt.Injector`.
This [JAXB](https://github.com/eclipse-ee4j/jaxb-ri) issue has been resolved in this release by updating to 
[citygml4j 2.8.1](https://github.com/citygml4j/citygml4j).
* Fixed SQL error when querying the highest LOD of `PlantCover` objects in KML/COLLADA/glTF exports. [#72](https://github.com/3dcitydb/importer-exporter/issues/72)
* Fixed error in spreadsheet exports when column titles have leading and trailing whitespace. [#65](https://github.com/3dcitydb/importer-exporter/issues/65)
* Fixed bug when using the Importer/Exporter installer in non-GUI installations. [#47](https://github.com/3dcitydb/importer-exporter/issues/47), 
[#64](https://github.com/3dcitydb/importer-exporter/issues/64)
* Fixed NPE when using a local cache for CityGML exports.
* Fixed NPE when exporting appearances without textures.
* Fixed CityGML writer to use default namespaces in tiled exports.

### 4.0 - 2018-09-18

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
