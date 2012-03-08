set PGPORT=5432
set PGHOST=localhost
set PGUSER=postgres
set PGPASSWORD=pgflex
set THEDB=citydb
set FILES=C:\Users\FxK\.eclipse\3dcity_fxk1\resources\3dcitydb\postgis
set PGBIN=C:\Program Files (x86)\PostgreSQL\9.1\bin
set POSTGISVER=2.0

REM Create the Database
"%PGBIN%\psql"  -d "%THEDB%" -f "%FILES%\DROP_DB.sql"

pause