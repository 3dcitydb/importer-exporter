REM Shell script to drop an instance of the 3D City Database
REM on PostgreSQL/PostGIS

REM Provide your database details here
set PGPORT=5432
set PGHOST=localhost
set PGUSER=cnagel
set CITYDB=devel
set PGBIN=C:\apps\PostgreSQL\9.2\bin

REM cd to path of the shell script
cd /d %~dp0

REM Run DROP_DB.sql to drop the 3D City Database instance
"%PGBIN%\psql" -d "%CITYDB%" -f "DROP_DB.sql"

pause