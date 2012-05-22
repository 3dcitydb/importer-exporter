set PGPORT=5432
set PGHOST=your_host_address
set PGUSER=your_username
set THEDB=your_database_here
set PGBIN=path_to_psql.exe

REM creating 3DCityDB database
"%PGBIN%\psql" -d "%THEDB%" -f "CREATE_DB.sql"

pause