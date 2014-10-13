REM Shell script to drop an instance of the 3D City Database
REM on PostgreSQL/PostGIS

REM Provide your database details here
set PGPORT=5432
set PGHOST=localhost
set PGUSER=vcs_user
set CITYDB=citydb_test
set PGBIN=C:\Program Files (x86)\pgAdmin III\1.18

REM cd to path of the shell script
cd /d %~dp0

REM Run DROP_DB.sql to drop the 3D City Database instance
"%PGBIN%\psql" -d "%CITYDB%" -f "DROP_DB.sql"

pause