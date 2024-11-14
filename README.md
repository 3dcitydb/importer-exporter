![Gradle build](https://img.shields.io/github/actions/workflow/status/3dcitydb/importer-exporter/impexp-build.yml?logo=Gradle&logoColor=white&style=flat-square)
[
![Docker build 3dcitydb/impexp:edge](https://img.shields.io/github/actions/workflow/status/3dcitydb/importer-exporter/docker-build-edge.yml?label=debian&style=flat-square&logo=Docker&logoColor=white)
![debian size](https://img.shields.io/docker/image-size/3dcitydb/impexp/edge?label=debian&logo=Docker&logoColor=white&style=flat-square)
![https://hub.docker.com/repository/docker/3dcitydb/impexp](https://img.shields.io/github/actions/workflow/status/3dcitydb/importer-exporter/docker-build-edge-alpine.yml?label=alpine&style=flat-square&logo=Docker&logoColor=white)
![alpine size](https://img.shields.io/docker/image-size/3dcitydb/impexp/edge-alpine?label=alpine&logo=Docker&logoColor=white&style=flat-square)
](https://hub.docker.com/repository/docker/3dcitydb/impexp)

3D City Database Importer/Exporter
==================================

# Project Overview
The 3D City Database Importer/Exporter is a Java-based client for the 3D City Database, designed for high-performance loading and extracting of 3D city model data.
### Key Features

    - Support for CityGML 2.0 and 1.0, and CityJSON 1.0
    - PostgreSQL/PostGIS and Oracle Spatial support
    - Export of KML/COLLADA/glTF models
    - Multithreaded data processing
    
## Prerequisites

    - Java JRE or JDK >= 11
    - PostgreSQL DBMS >= 12 with PostGIS extension >= 3.0
    - Oracle DBMS >= 19c or PolarDB for PostgreSQL >= 1.1 with Ganos >= 4.6
    
## Installation Steps
### Option 1: Manual Installation
Download the [latest release](https://github.com/3dcitydb/importer-exporter/releases) and follow the provided instructions.
### Option 2: Using the Installer
The 3D City Database Suite installer bundles the Importer/Exporter and other tools. A setup wizard guides the installation.
## Building from Source

    1. Clone the repository: `git clone https://github.com/3dcitydb/importer-exporter.git`
    2. Navigate to the project root: `cd importer-exporter`
    3. Run: `./gradlew installDist` (requires Java 8 JDK or higher)
    
## Using Docker

    - Pull the pre-built Docker image from Docker Hub: `docker pull 3dcitydb/impexp`
    - Run the Docker container: `docker run --rm --name impexp 3dcitydb/impexp`

## Running the Application

After a successful installation, you can run the application using the provided start scripts:

### GUI Version:
- On Windows: `3DCityDB-Importer-Exporter.bat`
- On macOS/Linux: `3DCityDB-Importer-Exporter`

### Command-Line Version:
The executables for the CLI version are located in the `lib` subfolder of the installation directory.

To run the CLI tool:
```
java -jar impexp-client-gui/lib/3DCityDB-Importer-Exporter-<version>.jar <options>
```

Refer to the [user manual](http://3dcitydb-docs.rtfd.io/) for detailed command-line options and usage examples.
   
## Documentation

    Comprehensive user manual available [here](http://3dcitydb-docs.rtfd.io/).
    
## Contributing

    - File bugs via GitHub Issues.
    - Submit pull requests for fixes or enhancements.
    - Propose new features through GitHub Issues.
    
## License
This project is licensed under the Apache License 2.0. See the LICENSE file for details.
