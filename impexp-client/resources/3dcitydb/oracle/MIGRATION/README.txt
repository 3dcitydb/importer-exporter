-- 3D City Database - The Open Source CityGML Database
-- http://www.3dcitydb.org/
-- 
-- Copyright 2013 - 2017
-- Chair of Geoinformatics
-- Technical University of Munich, Germany
-- https://www.gis.bgu.tum.de/
-- 
-- The 3D City Database is jointly developed with the following
-- cooperation partners:
-- 
-- virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
-- M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--     http://www.apache.org/licenses/LICENSE-2.0
--     
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

== Migration steps from version 2.1 to version 3.3 ==

1. Create the database v3.3 with @CREATE_DB.sql 

    - Enter the SRID for the database 
    - Enter the SRSName to be used 
    - Specify if the versioning shall be enabled 
    - Specify the used oracle license (spatial / locator)
	
    - Note: The database user must own the CREATE SYNONYM privilege
      in addition to the default privileges/roles required by the
      3DCityDB. Otherwise, step 3 of the migration will fail.

2. Run the script @GRANT_ACCESS.sql as v2.1 schema user 

    - Enter the schema name (v3.3) on which the accesses to be granted 

3. Execute @MIGRATE_DB.sql as your current (v3.3) user 

    - Enter the schema name from which the data will be migrated 
    - Specify the used oracle license (spatial / locator)

Done! 

Example: 

Let's assume that version 2.1 DB has UTM32 Coordinate System which corresponds 
to SRID = 83032 and assume that the schema name which we will copy the data
from is named as "3DCITYDB_TEST2" and your schema name is named as "3DCITYDB_TEST3". 

    - First, you create a new user and run @CREATE_DB.sql script for version
      3.3 on this schema. You give the SRID as "83032" and the corresponding 
      SRSName (urn:ogc:def:crs,crs:EPSG:6.12:25832,crs:EPSG:6.12:5783) and
      your choice about versioning and used oracle license (spatial / locator).

    - After the database is created, you log on to v2.1 schema user to be able to 
      grant select accesses to this user for the schema which we will migrate
      from and run @GRANT_ACCESS.sql script.
      You give the value "3DCITYDB_TEST3" as parameter. 

    - When the script is finished, you log on with your username "3DCITYDB_TEST3" 
      again and run the script @MIGRATE_DB.sql and give the schema name which the
      data will be copied as the parameter: "3DCITYDB_TEST2". 
      Second parameter is the used oracle license (spatial / locator).

    - When the migration script is completed, you see a message
      "DB migration is completed successfully." on the console.
