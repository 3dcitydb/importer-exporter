Change Log
==========

### 5.2.0 - tba

##### Additions
* Added an option to replace all identifiers of features and geometries with UUIDs when exporting to CityGML/CityJSON. [#249](https://github.com/3dcitydb/importer-exporter/pull/249)
* Extended options for defining the subdirectories and filenames for storing tiles when tiling is enabled for CityGML exports. [#250](https://github.com/3dcitydb/importer-exporter/pull/250)

#### Changes
* Consistently use `switchLocale` instead of `doTranslation`. [#247](https://github.com/3dcitydb/importer-exporter/pull/247)
* Added default preferences component for internal use. [#247](https://github.com/3dcitydb/importer-exporter/pull/247)
* Moved default preferences classes to GUI plugin package. [#247](https://github.com/3dcitydb/importer-exporter/pull/247)
* Added DefaultView and DefaultViewComponent to the GUI extension plugin. [#247](https://github.com/3dcitydb/importer-exporter/pull/247)

#### Fixes
* Avoid duplicate entries for the `implicit_geometry` table, if the same implicit geometry is used in different input files during the CityGML import.
This fix requires a **3DCityDB version 4.3.0 or higher** to work. [#243](https://github.com/3dcitydb/importer-exporter/pull/243)

##### Miscellaneous
* Updated PostgreSQL driver to 42.3.4
* Updated Oracle driver to 21.3.0.0
* Updated GeoTools to 26.4.

### 5.1.1 - 2022-04-12

##### Fixes
* Fixed initialization of GUI extensions. [#244](https://github.com/3dcitydb/importer-exporter/pull/244)
* Fixed missing srsName on envelopes when using an LoD filer. [#248](https://github.com/3dcitydb/importer-exporter/issues/248)

### 5.1.0 - 2022-02-10

##### Additions
* Added the possibility to generate a `SELECT` statement from an XML query expression used in the Importer/Exporter. [#231](https://github.com/3dcitydb/importer-exporter/pull/231)
* Added an option to control whether to compute the number of top-level features matching the filter criteria for
CityGML exports. This can take a long time on large databases. When disabled, the export process
starts immediately instead. [#235](https://github.com/3dcitydb/importer-exporter/pull/235)

##### Changes
* Changed the handling of date-time values and timezones in the Importer/Exporter. Before this change, `xsd:date` values
in CityGML exports could be different from the values stored in the database in case different timezone settings were
used for the database server and the import and export operations. [#226](https://github.com/3dcitydb/importer-exporter/pull/226)
* Reworked the use of import log files in both the GUI and the CLI. [#228](https://github.com/3dcitydb/importer-exporter/pull/228)
  * Users must now provide the full path to the import log file. Providing just a directory is not supported anymore.
  * Added the three modes `append`, `truncate` and `unique` for creating the log file.
* Simplified database queries used by the CityGML export operation for top-level features involving many nested
features/tables to avoid extremely large result sets causing performance issues. [#234](https://github.com/3dcitydb/importer-exporter/pull/234)
* Improved validation of XML queries to report more errors besides XML schema violations. [#224](https://github.com/3dcitydb/importer-exporter/issues/224), [#239](https://github.com/3dcitydb/importer-exporter/pull/239)
* When using an LoD filter for CityGML/CityJSON exports, the bounding box of the city objects is now calculated
based on the exported geometries instead of using the `ENVELOPE` value stored in the database. [#104](https://github.com/3dcitydb/importer-exporter/issues/104), [#239](https://github.com/3dcitydb/importer-exporter/pull/239)

##### Fixes

* Fixed bug in parsing of XPath expressions of XML queries that caused a slash `/` being part of a literal value
to be interpreted as step operator. [#220](https://github.com/3dcitydb/importer-exporter/issues/220), [#221](https://github.com/3dcitydb/importer-exporter/pull/221)
* Fixed bug in mapping the spatial `disjoint` filter operator in XML queries to SQL. [#227](https://github.com/3dcitydb/importer-exporter/pull/227)
* The `dimension` attribute of query geometries in XML queries is now parsed correctly. [#227](https://github.com/3dcitydb/importer-exporter/pull/227)
* Empty strings for gml:id attributes are no longer imported but the `GMLID` column is set to `NULL` instead.
* Fixed incorrect mapping of boolean values in XML queries to SQL. [#229](https://github.com/3dcitydb/importer-exporter/issues/229), [#230](https://github.com/3dcitydb/importer-exporter/pull/230)
* Fixed SQL exception when terminating city objects in the database using a delete list filter. [#237](https://github.com/3dcitydb/importer-exporter/pull/237)
* Fixed SQL filter to accept ";" characters in the SQL string. [#240](https://github.com/3dcitydb/importer-exporter/pull/240)
* The SRS information was not correctly updated in the GUI after changing the reference system on the `Database` tab.
* Avoid importing empty CityGML boundary surfaces for unreferenced semantic surfaces in CityJSON input files.

##### Miscellaneous
* Updated H2 database used for local caching to 2.1.210. [#236](https://github.com/3dcitydb/importer-exporter/pull/236)
* Updated PostgreSQL driver to 42.3.1 and PostGIS driver to 2021.1.0.
* Updated citygml4j to 2.12.0.

### 5.0.0 - 2021-10-08

##### Additions
* Added Docker files to build your own images for the Importer/Exporter command-line interface. Pre-built Docker images
  are available from Docker Hub at https://hub.docker.com/r/3dcitydb/impexp. [#209](https://github.com/3dcitydb/importer-exporter/pull/209)
* Added an import list filter to the import operation for both the GUI and the CLI that allows a user to provide a CSV
  file containing identifiers of features that shall be imported or skipped. [#202](https://github.com/3dcitydb/importer-exporter/pull/202)
* Enhanced feature version filter to support searching for terminated features. [#192](https://github.com/3dcitydb/importer-exporter/pull/192),
  [#185](https://github.com/3dcitydb/importer-exporter/issues/185)
* Added a *Plugins* section to the preferences tab of the Importer/Exporter GUI that lists all installed and available
  plugins and lets a user dynamically enable and disable plugins. [#194](https://github.com/3dcitydb/importer-exporter/pull/194)
* Added the `FeatureImportExtension` plugin extension that lets you hook your code into the import operation. [#195](https://github.com/3dcitydb/importer-exporter/pull/195)
* `MetadataProvider` is now a plugin extension in its own right and can be used without `FeatureExportExtension`.
* Affine transformations are now also available for the CityGML/CityJSON export operation. [#196](https://github.com/3dcitydb/importer-exporter/pull/196)
* Added support for importing `gml:MultiGeometry` objects in case they only contain points or curves.
* Added support for providing database connection details via environment variables when running the Importer/Exporter
  on the command line. [#212](https://github.com/3dcitydb/importer-exporter/pull/212)
* CityJSON output now uses the latest CityJSON version 1.0.3.

##### Changes
* CityGML/CityJSON import and export operations now fail fast on errors by default. [#215](https://github.com/3dcitydb/importer-exporter/pull/215)
* Avoid pollution of database schema with temporary tables in case an import or export operation terminates
  abnormally. [#190](https://github.com/3dcitydb/importer-exporter/pull/190)
* *Breaking:* Refactoring of the Plugin API. [#200](https://github.com/3dcitydb/importer-exporter/pull/200)
* *Breaking:* Replaced `impexp-client` module with two separate modules `impexp-client-cli` and `impexp-client-gui` to enable
  Docker images for the command-line interface. [#187](https://github.com/3dcitydb/importer-exporter/pull/187)
* Moved installer build script to [3dcitydb-suite](https://github.com/3dcitydb/3dcitydb-suite) repository. [#186](https://github.com/3dcitydb/importer-exporter/pull/186)
* *Breaking:* Restructured source code to avoid packages being spread over different modules.

##### Fixes
* Fixed inconsistent behaviour of the feature version filter for different CLI commands. [#188](https://github.com/3dcitydb/importer-exporter/pull/188)
* Fixed NPE during imports when at least one ADE extension is loaded and the dataset contains 3DCityDB ADE properties.
* Fixed `StatementFinalizer` of the Tomcat JDBC pool to be thread-safe to avoid that connections cannot be released
  from the pool. [#189](https://github.com/3dcitydb/importer-exporter/pull/189)

##### Miscellaneous
* Updated PostgreSQL driver to 42.2.23 and Oracle driver to 21.1.0.
* Updated citygml4j to 2.11.4.

### 4.3.0 - 2021-04-28

##### Additions
* Completely redesigned command-line interface (CLI) to be able to invoke default import, export and delete operations
  without the need for passing an XML config file. [#147](https://github.com/3dcitydb/importer-exporter/pull/147)
* CityJSON import and export. [#88](https://github.com/3dcitydb/importer-exporter/issues/88),
  [#92](https://github.com/3dcitydb/importer-exporter/pull/92), [#89](https://github.com/3dcitydb/importer-exporter/issues/89),
  [#166](https://github.com/3dcitydb/importer-exporter/pull/166)
* Improved CityGML export performance (up tp 10-15 times faster in case the Importer/Exporter is not running on
  the same machine or in the same local network as the database server). [#126](https://github.com/3dcitydb/importer-exporter/pull/126),
  [#136](https://github.com/3dcitydb/importer-exporter/pull/136), [#137](https://github.com/3dcitydb/importer-exporter/pull/137)
* Implemented a more modern GUI look & feel that scales better on HiDPI displays. [#149](https://github.com/3dcitydb/importer-exporter/pull/149)
* Extended ADE export API to support the export of ADE data in KML/COLLADA/glTF. [#124](https://github.com/3dcitydb/importer-exporter/pull/124)
* Enhanced LoD export filter with the option to only export the minimum or maximum LoD of a city object. [#133](https://github.com/3dcitydb/importer-exporter/pull/133)
* Added a feature version filter to be able to query city objects that have been terminated in the database.
* Added more filter options to the KML/COLLADA/glTF export and substantially reworked the preference settings. A
  bounding box filter is not mandatory for KML/COLLADA/glTF exports anymore.
* Added support for exporting binary glTF and Draco compression in KML/COLLADA/glTF exports. [#142](https://github.com/3dcitydb/importer-exporter/pull/142)
* Added support for importing and exporting arbitrary surfaces as exterior of solids. [#130](https://github.com/3dcitydb/importer-exporter/pull/130)
* GML geometry types not supported by the `SURFACE_GEOMETRY` table are internally mapped to a `gml:MultiSurface` to
  be able to import them. [#130](https://github.com/3dcitydb/importer-exporter/pull/130)
* Added a database ID filter to the XML query language.
* Added explicit CLI script in addition to the start script for simplifying the use of Importer/Exporter CLI mode. [#123](https://github.com/3dcitydb/importer-exporter/pull/123)
* Users can now choose to write log messages to a specific log file and not just a log directory. 
* Completely updated user manual at https://3dcitydb-docs.readthedocs.io/en/release-v4.3.0/
* General tweaks, bugfixes, and code refactoring

##### Changes
* Instead of Oracle, PostgreSQL/PostGIS is now the default database connection. [#162](https://github.com/3dcitydb/importer-exporter/pull/162)
* Reworked GUI and config file to manage and store one workspace per connection for Oracle databases. [#164](https://github.com/3dcitydb/importer-exporter/pull/164)
* When importing local geometry XLinks into a 3DCityDB instance of version 4.1, the `IS_XLINK` column of `SURFACE_GEOMETRY` is now filled with value `2`, which allows to substantially improve the export performance. [#159](https://github.com/3dcitydb/importer-exporter/pull/159)

##### Fixes
* Fixed bug in SQL query builder. [#122](https://github.com/3dcitydb/importer-exporter/pull/122)
* Fixed possible database deadlocks when updating involved tables during XLink resolution.
* Fixed NPE in CityGML exports in case the output file path has no parent folder.
* Fixed export of XLinks to surface data members.
* XLinks in WaterBody objects are now exported differently depending on the target CityGML version.
* Fixed bug in CityGML export which caused a city object to be exported on multiple tiles in rare cases.
* Fixed missing tile counter in export status dialog when using XML queries.
* Fixed bug when reading ZIP input files which could cause the application to freeze.
* Fixed error due to incorrect database ID values when importing a `gml:OrientableSurface`.  
* Fixed bug in resolving geometry XLinks across top-level features.
* Fixed missing X3D materials in KML/COLLADA/glTF exports when theme is set to `none`.
* Color settings are used as fallback in COLLADA/glTF exports when the selected theme is not available for a 
  feature.
* Fixed hierarchical geometry query to work with Oracle 11g and previous versions.

##### Miscellaneous
* Updated to latest versions of the ADE manager plugin and the Spreadsheet Generator plugin.
* Upgrade PostgreSQL driver to 42.2.14 and PostGIS to 2.5.0.
* Updated citygml4j to 2.11.3.
* Starting from this version, all 3DCityDB Maven artifacts are maintained in the JFrog Artifactory Cloud using the new
  repository link https://citydb.jfrog.io/artifactory/maven. [#183](https://github.com/3dcitydb/importer-exporter/pull/183)

### 4.2.3 - 2020-04-06

##### Additions
* Added support for sorting the top-level features in a CityGML export based on one or more 
simple thematic attributes. The sorting criteria can be defined using the new `<sortBy>` element of
the XML query language. [#86](https://github.com/3dcitydb/importer-exporter/pull/86)
* Added support for implicit geometries of nested sub-features in KML/COLLADA/glTF exports. [#93](https://github.com/3dcitydb/importer-exporter/issues/93),
[#94](https://github.com/3dcitydb/importer-exporter/pull/94)
* Added a delete operation to the CLI through the new `-delete` option. A corresponding `<delete>` element in the
config file lets you define the behavior of the delete operation (e.g. by providing a filter expression). [#102](https://github.com/3dcitydb/importer-exporter/pull/102)
* Added support for importing `gml:PolygonPatch` geometry objects.
* Added installation via CLI to the documentation. [#99](https://github.com/3dcitydb/importer-exporter/issues/99) 
* Added import and export counter for global appearances. [#95](https://github.com/3dcitydb/importer-exporter/issues/95),
[#115](https://github.com/3dcitydb/importer-exporter/pull/115)
* Added date picker in GUI.
* Added a link to the [online documentation](https://3dcitydb-docs.readthedocs.io/en/release-v4.2.3/) in the `Help` menu of the GUI.

##### Changes
* Reworked the feature counter filter for CityGML imports and exports. Instead of providing a lower and upper
boundary, you can now provide the total `count` of features and the `start index` of the first feature. For 
exports, this is mapped to SQL `LIMIT` and `OFFSET` clauses. Note that the definition in the XML query
language now uses a `<limit>` element instead of the previous `<count>` element.
* Improved SQL query builder to create more concise SQL statements.

##### Fixes
* Fixed memory leak for large exports under PostgreSQL.
* Fixed OpenStreetMap data not showing in the map window. [#97](https://github.com/3dcitydb/importer-exporter/issues/97)
* Fixed NPE when importing invalid xAL address elements. [#103](https://github.com/3dcitydb/importer-exporter/issues/103)
* Fixed NPE when exporting PlantCover features as KML/COLLADA/glTF. [#91](https://github.com/3dcitydb/importer-exporter/issues/91)
* Fixed memory issue due to re-opening of ZIP files in CityGML imports.
* Fixed import of curve geometries. If a curve geometry was given by several curve segments, then the
interior start and end points of the segments were imported into the database. This has been corrected.
* Fixed the `IzPack` library to support automated install scripts.

##### Miscellaneous 
* Updated to latest versions of the ADE manager plugin and the Spreadsheet Generator plugin.
* Updated to latest PostgreSQL driver v42.2.10.
* Updated to latest Oracle driver 19.3.
* Updated citygml4j to 2.10.5.
* Updated sqlbuilder to 2.2.1.

### 4.2.2 - 2019-08-06

##### Additions
* Added config option to suppress the product information header comment. 

##### Changes
* Moved input and output file information from internal config to external API.

### 4.2.1 - 2019-04-17

##### Additions
* Added the `-pid-file` command-line argument to create a file storing the process ID of the Importer/Exporter at the 
provided path. This can be useful to check whether the Importer/Exporter is running or to issue a `kill` command to end it.
* Added GUI warning dialogs if a Importer/Exporter module does not support ADEs registered in the database.
* Updated Spreadsheet Plugin to version 3.1.2.
* Updated ADE Manager Plugin to version 1.1.2.
* Updated 3DCityDB-Web-Map to version 1.7.1.

##### Fixes
* Added `distinct` to complex SQL queries to avoid exporting duplicates of a top-level feature.
* Fixed memory leak when importing ZIP files.
* Fixed database schema mapping to correctly support qualified XML attributes in CityGML and ADE XML schemas.
* Fixed NPE in XPath parser.

### 4.2 - 2019-02-13

##### Additions
* Reworked Plugin API to support non-GUI plugins. [#78](https://github.com/3dcitydb/importer-exporter/pull/78)
  * Added `CityGMLExportExtension` as a first non-GUI extension point for plugins. Using this extension, a plugin receives
  and can process all CityGML features before they are written to the output file. Besides using XSLT stylesheets
  for CityGML exports, this adds another way of having full control over the output.
  * Due to the changes to the Plugin API, make sure to only use plugins built for this version.
* Property projections can now also be defined for abstract feature types.
* Added possibility to define a gml:id prefix for the UUIDs that are created during CityGML imports.
* Added config options to control the writing of `gml:Envelope` elements on features and the root `CityModel`.
* Added config options to define XML prefixes and schema locations for the CityGML output file.

##### Fixes
* Fixed broken feature type filter for CityGML imports. [#75](https://github.com/3dcitydb/importer-exporter/issues/75)
* Fixed NPE in `GeometryConverter` when using affine transformations during CityGML imports. [#77](https://github.com/3dcitydb/importer-exporter/issues/77)
* The CLI version of the Importer/Exporter now returns an exit code of 1 on failures.

##### Miscellaneous 
* Updated citygml4j to 2.9.1.

### 4.1 - 2019-01-09

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
* Fixed error in spreadsheet exports when column titles have leading and trailing whitespaces. [#65](https://github.com/3dcitydb/importer-exporter/issues/65)
* Fixed bug when using the Importer/Exporter installer in non-GUI installations. [#47](https://github.com/3dcitydb/importer-exporter/issues/47), 
[#64](https://github.com/3dcitydb/importer-exporter/issues/64)
* Fixed NPE when using a local cache for CityGML exports.
* Fixed NPE when exporting appearances without textures.
* Fixed CityGML writer to use default namespaces in tiled exports.

##### Miscellaneous 
* Upgrade to latest PostgreSQL driver v42.2.5 and PostGIS driver v2.3.0.
* Upgrade to latest Oracle driver 18.3.

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
