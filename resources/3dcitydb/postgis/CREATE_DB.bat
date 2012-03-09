set PGPORT=5432
set PGHOST=localhost
set PGUSER=postgres
set PGPASSWORD=pgflex
set THEDB=citydb
set FILES=C:\Users\FxK\.eclipse\postgis_dev\resources\3dcitydb\postgis
set PGBIN=C:\Program Files (x86)\PostgreSQL\9.1\bin

REM Create the Database
"%PGBIN%\psql"  -d "%THEDB%" -f "%FILES%\CREATE_DB.sql"

pause