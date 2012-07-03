REM batchfile that calls DROP_DB.sql

set PGPORT=5432
set PGHOST=your_host_address
set PGUSER=your_username
set CITYDB=your_database_here
set PGBIN=path_to_psql.exe

REM dropping the 3D City Database
"%PGBIN%\psql" -d "%CITYDB%" -f "DROP_DB.sql"

pause