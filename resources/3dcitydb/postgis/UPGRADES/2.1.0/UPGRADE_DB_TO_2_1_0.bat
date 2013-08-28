REM Shell script to upgrade an instance of the 3D City Database
REM on PostgreSQL/PostGIS to version 2.1.0

REM Provide your database details here
set PGPORT=5432
set PGHOST=your_host_address
set PGUSER=your_username
set CITYDB=your_database
set PGBIN=path_to_psql.exe

REM cd to path of the shell script
cd /d %~dp0

REM Run UPGRADE_DB_TO_2_1_0.sql to upgrade the 3D City Database instance
"%PGBIN%\psql" -d "%CITYDB%" -f "UPGRADE_DB_TO_2_1_0.sql"

pause