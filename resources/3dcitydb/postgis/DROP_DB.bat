set PGPORT=5432
set PGHOST=localhost
set PGUSER=postgres
set PGPASSWORD=your_password_here
set THEDB=your_database_here
set PGBIN=C:\Program Files (x86)\PostgreSQL\9.1\bin

REM Create the Database
"%PGBIN%\psql" -d "%THEDB%" -f "DROP_DB.sql"

pause