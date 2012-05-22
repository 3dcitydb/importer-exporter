set PGPORT=5432
set PGHOST=your_host_address
set PGUSER=your_username
set THEDB=your_database_here
set PGBIN=path_to_psql.exe

REM dropping the 3DCityDB Database
"%PGBIN%\psql" -d "%THEDB%" -f "DROP_DB.sql"

pause