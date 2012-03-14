set PGPORT=5432
set PGHOST=localhost
set PGUSER=postgres
set PGPASSWORD=your_password_here
set THEDB=your_database_here
set PGBIN=C:\Program Files (x86)\PostgreSQL\9.1\bin

REM Create the 3DCityDB database
"%PGBIN%\psql" -d "%THEDB%" -f "CREATE_DB.sql"

pause