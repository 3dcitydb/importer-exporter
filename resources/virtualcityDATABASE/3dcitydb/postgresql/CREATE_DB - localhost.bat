REM Shell script to create an instance of the 3D City Database
REM on PostgreSQL/PostGIS

REM Provide your database details here
set PGPORT=5432
set PGHOST=localhost
set PGUSER=cnagel
set CITYDB=wiesbaden
set PGBIN=C:\Program Files\PostgreSQL\9.5\bin

REM cd to path of the shell script
cd /d %~dp0

REM Run CREATE_DB.sql to create the 3D City Database instance
"%PGBIN%\psql" -d "%CITYDB%" -f "CREATE_DB.sql"

pause