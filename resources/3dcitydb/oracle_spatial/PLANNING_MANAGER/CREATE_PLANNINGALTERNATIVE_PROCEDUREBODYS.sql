-- CREATE_PLANNINGALTERNATIVE_PROCEDUREBODYS.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Gerhard König <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
--     		Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Dr. Thomas H. Kolbe <kolbe@ikg.uni-bonn.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--
-- Copyright:   (c) 2007-2008  Institute for Geodesy and Geoinformation Science,
--                             Technische Universität Berlin, Germany
--                             http://www.igg.tu-berlin.de
--  		(c) 2004-2006, Institute for Cartography and Geoinformation,
--                             Universität Bonn, Germany
--                             http://www.ikg.uni-bonn.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
--
-- Prozeduren zum Umgang mit Planungsalternativen
--
--   + AddPlanningAlternativeBdy
--   + UpdatePlanningAlternativeBdy
--   + DiscardPlanningAlternativeBdy
--   + GetDiffBdy
--   + GetAllDiffBdy
--   + GetConflictsBdy
--   + GetAllConflictsBdy
--   + RefreshPlanningAlternativeBdy
--   + GetRefreshDateBdy
--   + DelAllPlanningAlternativesBdy
--   + DelTermPlanningAlternativesBdy
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 0.7.5     2006-04-03   release version                             LPlu
--                                                                    TKol
--                                                                    GGro
--                                                                    JSch
--                                                                    VStr
--


/* 1
 * Die Prozedur legt einen Datensatz in der Tabelle PLANNING_ALTERNATIVE an
 * und erzeugt einen Workspace. Der Workspacename setzt sich aus der Kennung
 * 'PA', dem Benutzernamen,der ID der Planung und der ID der Planungsalternative
 * zusammen (PID_PAID) und wird aus dem Workspace LIVE abgeleitet.
 * Workspacenamen dürfen max. 30 Zeichen lang sein. Ist der Benutzername länger als
 * 15 Zeichen, so werden die ersten zehn und die letzten fünf Zeichen des Namens
 * verwendet. So bleibt im Workspacenamen jeweils fünf Zeichen für PlanungsId und
 * ID der Planungsalternative, was 99.999 Planungen und ebenso vielen Alternativen
 * entspricht.
 * Die Prozedur wird nur ausgeführt, wenn die angegebene Planung noch aktiv ist.
 *
 * @param planningId Name der Planung, der eine Alternative hinzugefügt werden
 *        soll
 * @param title Kurzbezeichnung der anzulegenden Alternative
 * @param description Kurze Beschreibung der anzulegenden Alternative
 * @param generator Name desjenigen, der die Alternative anlegt hat
 * @param planner Name des Autors dieser Alternative (Planer)
 * @param fatherWorkspaceName Name des Workspace, aus dem der Workspace der
 *        Alternative abgeleitet werden soll
 *
 * @return Status der Ausführung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return Workspacename der Planungsalternative oder Fehlermeldung
 */



CREATE OR REPLACE PROCEDURE AddPlanningAlternativeBdy(
  planningId NUMBER,
  title VARCHAR2,
  description VARCHAR2,
  generator VARCHAR2,
  planner VARCHAR2,
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  sequenceGeneratedId NUMBER;
  planningCount NUMBER;
  planningStatus NUMBER;
  userName VARCHAR2(30);
  workspaceName VARCHAR2(30);
  planningCountException EXCEPTION;
  planningStatusException EXCEPTION;

BEGIN
  -- Abragen der Existenz und des Status der Planung
  checkPlanning(planningId, planningCount, planningStatus);

  IF planningCount = 1 THEN   -- Planung existiert
    IF planningStatus = 1 THEN  -- Planung noch aktiv

      -- den nächsten Zähler der Sequenz abholen
      SELECT planning_alternative_seq.nextval INTO sequenceGeneratedId
      FROM dual;

      -- den Benutzernamen dieser Session abfragen
      SELECT user INTO userName
      FROM dual;

      -- den Benutzernamen auf 15 Zeichen begrenzen
      IF LENGTH(userName) > 15 THEN
      	userName := SUBSTR(userName, 1, 9) || SUBSTR(userName, LENGTH(userName) - 5, LENGTH(userName));
      END IF;

      -- Workspacename festlegen
      workspaceName := 'PA_' || userName || '_' || TO_CHAR(planningId) || '_' || TO_CHAR(sequenceGeneratedId);

      -- Tupel einfügen
      INSERT INTO planning_alternative
      VALUES(
        sequenceGeneratedId,
        planningId,
        title,
        description,
        generator,
        planner,
        workspaceName,
        CURRENT_TIMESTAMP,
        null
      );

      -- Workspace anlegen und Savepoint 'refreshed' erzeugen
      DBMS_WM.GoToWorkspace('LIVE');
      DBMS_WM.CreateWorkspace(workspaceName);
      DBMS_WM.CreateSavepoint(workspaceName, 'refreshed');

      COMMIT;
      setOutParameter(1, workspaceName, outStatus, outMessage);

    ELSE   -- Planung bereits beendet
      RAISE planningStatusException;
    END IF;

  ELSE   -- Planung existiert nicht
    RAISE planningCountException;
  END IF;


EXCEPTION
  WHEN planningCountException THEN
    setOutParameter(0, '3D-Geo-DB: Planung existiert nicht', outStatus, outMessage);
  WHEN planningStatusException THEN
    setOutParameter(0, '3D-Geo-DB: Planung ist bereits beendet', outStatus, outMessage);
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/








/* 2
 * Die Prozedur ändert die Parameter Titel, Beschreibung,
 * Datenbankerzeuger und Planer einer aktiven Planungsalternative.
 * Alle existierenden Einträge werden überschrieben.
 *
 * @param id ID der Planungsalternative
 * @param title Kurzbezeichnung Planung
 * @param description Kurze Beschreibung der Planung
 * @param generator derjenige, der den Datensatz in die DB einfügt
 * @param planner Planer
 *
 * @return Status der Ausführung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return null oder Fehlermeldung
 */

CREATE OR REPLACE PROCEDURE UpdatePlanningAlternativeBdy(
  planningAlternativeId NUMBER,
  newTitle VARCHAR2,
  newDescription VARCHAR2,
  newGenerator VARCHAR2,
  newPlanner VARCHAR2,
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  paCount NUMBER;
  paStatus NUMBER;
  paCountException EXCEPTION;
  paStatusException EXCEPTION;

BEGIN
  -- Abragen der Existenz und des Status der Planung
  checkPlanningAlternative(planningAlternativeId, paCount, paStatus);

  IF paCount = 1 THEN   -- Planungsalternative existiert
    IF paStatus = 1 THEN  -- Planungsalternative noch aktiv
      -- Ändern eines bestehenden Tupels
      UPDATE planning_alternative
      SET
        title = newTitle,
        description = newDescription,
        generator = newGenerator,
        planner = newPlanner
      WHERE id = planningAlternativeId;

      COMMIT;
      setOutParameter(1, NULL, outStatus, outMessage);

    ELSE   -- Planungsalternative bereits beendet
      RAISE paStatusException;
    END IF;

  ELSE   -- Planungsalternative existiert nicht
    RAISE paCountException;
  END IF;


EXCEPTION
  WHEN paCountException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative existiert nicht', outStatus, outMessage);
  WHEN paStatusException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative ist bereits beendet', outStatus, outMessage);
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/






/* 3
 * Die Prozedur beendet einen Datensatz in der Tabelle PLANNING_ALTERNATIVE
 * indem ein Terminierungsdatum gesetzt wird.
 * Der zugehörige Workspace wird nicht gelöscht. Es wird lediglich ein Savepoint
 * mit dem Namen "terminated" gesetzt.
 * Die Prozedur wird nur ausgeführt, wenn die Planung noch nicht beendet wurde.
 *
 * @param planningAlternativeId ID der zu beendenden Planungsalternative
 *
 * @return Status der Ausführung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return null oder Fehlermeldung
 */

CREATE OR REPLACE PROCEDURE DiscardPlanningAlternativeBdy(
  planningAlternativeId NUMBER,
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  workspacename VARCHAR2(30);
  paCount NUMBER;
  paStatus NUMBER;
  paCountException EXCEPTION;
  paStatusException EXCEPTION;

BEGIN
  -- Abragen der Existenz und des Status der Planung
  checkPlanningAlternative(planningAlternativeId, paCount, paStatus);

  IF paCount = 1 THEN   -- Planungsalternative existiert
    IF paStatus = 1 THEN  -- Planungsalternative noch aktiv
	    -- Workspacenamen abfragen
	    SELECT workspace_name INTO workspacename
	    FROM planning_alternative
	    WHERE id = planningAlternativeId;

	    -- Terminierungsdatum setzen
	    UPDATE planning_alternative
	    SET termination_date = CURRENT_TIMESTAMP
	    WHERE id = PlanningAlternativeID;

	    -- Savepoint im Workspace setzen
	    DBMS_WM.CreateSavepoint(workspacename, 'terminated');

		  COMMIT;
		  setOutParameter(1, NULL, outStatus, outMessage);

    ELSE   -- Planungsalternative bereits beendet
      RAISE paStatusException;
    END IF;

  ELSE   -- Planungsalternative existiert nicht
    RAISE paCountException;
  END IF;


EXCEPTION
  WHEN paCountException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative existiert nicht', outStatus, outMessage);
  WHEN paStatusException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative ist bereits beendet', outStatus, outMessage);
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/





/* 4 warn
 * TODO: Übergabe der PlanungsalternativenID oder des Workspacenamen?
 *
 * Die Prozedur gibt die Gesamtzahl der Tupel zurück, die im Workspace LIVE
 * und dem angegebenen Workspace in unterschiedlichen Versionen vorliegen.
 * Zur Berechnung wird die Summe der Differenzen über alle versionierten
 * Tabellen (z.B. BUILDINGS, CITYOBJECT usw.) gebildet.
 * Diese Zahl ist einerseits ein Indikator für den Umfang der in der
 * Planungsalternativen durchgeführten Änderungen am 3D-Stadtmodell.
 * Andererseits gibt er Aufschluss über die Komplexität der Übernahme einer
 * Planungsalternative in den LIVE Workspace.
 *
 * @return Status der Ausführung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return Anzahl der Differenzen oder Fehlermeldung
 */

CREATE OR REPLACE PROCEDURE GetDiffBdy(
  planningAlternativeId VARCHAR2,
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  ws VARCHAR2(30);
  diff INTEGER;
  c INTEGER;
  paCount NUMBER;
  paStatus NUMBER;
  paCountException EXCEPTION;
  paStatusException EXCEPTION;

BEGIN
  -- Abfragen der Existenz und des Status der Planung
  checkPlanningAlternative(planningAlternativeId, paCount, paStatus);

  IF paCount = 1 THEN   -- Planungsalternative existiert
    IF paStatus = 1 THEN  -- Planungsalternative noch aktiv
    	-- Workspacenamen abfragen
    	SELECT workspace_name INTO ws
    	FROM planning_alternative
    	WHERE id = planningAlternativeId;

	    -- Differenzen zu LIVE auswerten lassen
	    dbms_wm.SetDiffVersions('LIVE', ws);

	    -- Differenzen zählen    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM SURFACE_GEOMETRY_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM CITYOBJECT_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM EXTERNAL_REFERENCE_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM CITYOBJECT_GENERICATTRIB_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM CITYOBJECTGROUP_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM GROUP_TO_CITYOBJECT_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM BREAKLINE_RELIEF_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM APPEARANCE_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM TIN_RELIEF_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM APPEAR_TO_SURFACE_DATA_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM MASSPOINT_RELIEF_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM GENERALIZATION_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM IMPLICIT_GEOMETRY_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM CITYMODEL_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM GENERIC_CITYOBJECT_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM BUILDING_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM CITY_FURNITURE_DIFF;
	    diff := diff + c;	    

	    SELECT (COUNT(WM_CODE) / 3) into c FROM ADDRESS_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM THEMATIC_SURFACE_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM BUILDING_FURNITURE_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM OPENING_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM BUILDING_INSTALLATION_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM ADDRESS_TO_BUILDING_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM CITYOBJECT_MEMBER_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM LAND_USE_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM OPENING_TO_THEM_SURFACE_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM ROOM_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM SURFACE_DATA_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM TEXTUREPARAM_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM PLANT_COVER_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM SOLITARY_VEGETAT_OBJECT_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM TRAFFIC_AREA_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM TRANSPORTATION_COMPLEX_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM WATERBODY_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM WATERBOUNDARY_SURFACE_DIFF;
	    diff := diff + c;
	    
	    SELECT (COUNT(WM_CODE) / 3) into c FROM WATERBOD_TO_WATERBND_SRF_DIFF;
	    diff := diff + c;
	    
	    setOutParameter(1, TO_CHAR(diff), outStatus, outMessage);

    ELSE   -- Planungsalternative bereits beendet
      RAISE paStatusException;
    END IF;

  ELSE   -- Planungsalternative existiert nicht
    RAISE paCountException;
  END IF;


EXCEPTION
  WHEN paCountException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative existiert nicht', outStatus, outMessage);
  WHEN paStatusException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative ist bereits beendet', outStatus, outMessage);
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/




/* 5 warn
 * Ähnlich der Prozedur GetDiff (s.o.) wird die Summe der Differenzen
 * zwischen LIVE und allen Workspaces der nicht beendeten Planungsalternativen
 * zurückgegeben.
 *
 * @return Summe der Differenzen oder "0 + Fehlercode" falls beim Ausführen der
 *         Prozedur Fehler auftreten
 */

CREATE OR REPLACE PROCEDURE GetAllDiffBdy(
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  planningAlternativeId VARCHAR2(30);
  status NUMBER;
  message VARCHAR(256);
  diff NUMBER;
  GetDiffException EXCEPTION;
  CURSOR planningAlternatives IS  -- enthält alle Workspacenamen
    SELECT id
    FROM planning_alternative
    WHERE termination_date IS NULL;

BEGIN
  diff := 0;
  -- Cursor durchlaufen und jeweiligen Differenzen abfragen
  OPEN planningAlternatives;
    LOOP
      FETCH planningAlternatives INTO planningAlternativeId;
      EXIT WHEN planningAlternatives%NOTFOUND;

      GetDiffBdy(planningAlternativeId, status, message);

      IF status = 1 THEN
        DBMS_OUTPUT.put_line(planningAlternativeId || ': ' || TO_NUMBER(message));
        diff := diff + TO_NUMBER(message);
      ELSE
        RAISE GetDiffException;
      END IF;

    END LOOP;
  CLOSE planningAlternatives;

  setOutParameter(1, TO_CHAR(diff), outStatus, outMessage);

EXCEPTION
  WHEN GetDiffException THEN
    setOutParameter(0, '3D-Geo-DB: Fehler beim Lesen der Differenzen', outStatus, outMessage);
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/




/* 6 warn
 * Die Prozedur gibt die Gesamtzahl der Tupel zurück, die sowohl im Workspace
 * LIVE, als auch im angegebenen Workspace geändert wurden. Zur Berechnung wird
 * die Summe der Konflikte über alle versionierten Tabellen (z.B. BUILDINGS,
 * CITYOBJECT usw.) gebildet.
 * Die Funktion zeigt also an, ob eine Übernahme der Planungsalternative in den
 * LIVE Workspace (Merge) oder ein „Erneuern“ des Originaldatenbestandes in
 * einer Planungsalterna-tive (Refresh) möglich ist. Merge und Refresh können
 * nur für Workspaces durchgeführt wer-den, zwischen denen keine Konflikte
 * bestehen.
 *
 * @param workspace Name des Workspaces, dessen Konflikte mit LIVE gezählt
 *        werden sollen
 *
 * @return Status der Ausführung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return Anzahl der Konflikte oder Fehlermeldung
 */

CREATE OR REPLACE PROCEDURE GetConflictsBdy(
  planningAlternativeId NUMBER,
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  conf INTEGER;
  ws VARCHAR2(30);
  c INTEGER;
  paCount NUMBER;
  paStatus NUMBER;
  paCountException EXCEPTION;
  paStatusException EXCEPTION;

BEGIN
  -- Abragen der Existenz und des Status der Planung
  checkPlanningAlternative(planningAlternativeId, paCount, paStatus);

  IF paCount = 1 THEN   -- Planungsalternative existiert
    IF paStatus = 1 THEN  -- Planungsalternative noch aktiv

    	-- Workspacenamen abfragen
    	SELECT workspace_name INTO ws
    	FROM planning_alternative
    	WHERE id = planningAlternativeId;

      -- Differenzen zu LIVE auswerten lassen
      dbms_wm.SetConflictWorkspace(ws);

      -- Differenzen zählen
      SELECT (COUNT(WM_DELETED) / 3) into c FROM SURFACE_GEOMETRY_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM CITYOBJECT_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM EXTERNAL_REFERENCE_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM CITYOBJECT_GENERICATTRIB_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM CITYOBJECTGROUP_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM GROUP_TO_CITYOBJECT_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM BREAKLINE_RELIEF_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM APPEARANCE_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM TIN_RELIEF_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM APPEAR_TO_SURFACE_DATA_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM MASSPOINT_RELIEF_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM GENERALIZATION_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM IMPLICIT_GEOMETRY_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM CITYMODEL_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM GENERIC_CITYOBJECT_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM BUILDING_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM CITY_FURNITURE_CONF;
      	    conf := conf + c;	    
      
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM ADDRESS_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM THEMATIC_SURFACE_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM BUILDING_FURNITURE_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM OPENING_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM BUILDING_INSTALLATION_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM ADDRESS_TO_BUILDING_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM CITYOBJECT_MEMBER_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM LAND_USE_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM OPENING_TO_THEM_SURFACE_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM ROOM_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM SURFACE_DATA_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM TEXTUREPARAM_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM PLANT_COVER_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM SOLITARY_VEGETAT_OBJECT_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM TRAFFIC_AREA_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM TRANSPORTATION_COMPLEX_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM WATERBODY_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM WATERBOUNDARY_SURFACE_CONF;
      	    conf := conf + c;
      	    
      	    SELECT (COUNT(WM_DELETED) / 3) into c FROM WATERBOD_TO_WATERBND_SRF_CONF;
	    conf := conf + c;
	    
	    setOutParameter(1, TO_CHAR(conf), outStatus, outMessage);

    ELSE   -- Planungsalternative bereits beendet
      RAISE paStatusException;
    END IF;

  ELSE   -- Planungsalternative existiert nicht
    RAISE paCountException;
  END IF;


EXCEPTION
  WHEN paCountException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative existiert nicht', outStatus, outMessage);
  WHEN paStatusException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative ist bereits beendet', outStatus, outMessage);
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/




/* 7 warn
 * TODO: Kann die Prozedur entfallen?
 *
 * Die Prozedur gibt die Anzahl der Konflikte zwischen LIVE und allen
 * existierenden Workspaces von Planungsalternativen zurück
 */


CREATE OR REPLACE PROCEDURE GetAllConflictsBdy(
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  planningAlternativeId VARCHAR2(30);
  status NUMBER;
  message VARCHAR(256);
  conf NUMBER;
  GetConfException EXCEPTION;
  CURSOR planningAlternatives IS  -- enthält alle Workspacenamen
    SELECT id
    FROM planning_alternative
    WHERE termination_date IS NULL;

BEGIN
  conf := 0;
  -- Cursor durchlaufen und jeweiligen Differenzen abfragen
  OPEN planningAlternatives;
    LOOP
      FETCH planningAlternatives INTO planningAlternativeId;
      EXIT WHEN planningAlternatives%NOTFOUND;

      GetConflictsBdy(planningAlternativeId, status, message);

      IF status = 1 THEN
        DBMS_OUTPUT.put_line(planningAlternativeId || ': ' || TO_NUMBER(message));
        conf := conf + TO_NUMBER(message);
      ELSE
        RAISE GetConfException;
      END IF;

    END LOOP;
  CLOSE planningAlternatives;

  setOutParameter(1, TO_CHAR(conf), outStatus, outMessage);

EXCEPTION
  WHEN GetConfException THEN
    setOutParameter(0, '3D-Geo-DB: Fehler beim Lesen der Konflikte', outStatus, outMessage);
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/








/* 8
 * Die Prozedur aktualisiert die Daten des Workspaces der angegebenen
 * Planungsalternative mit denen des LIVE Workspaces. Dies ist notwendig, wenn
 * sich der Datenbestand in LIVE seit em Anlegen der Planungsalternative
 * geändert hat. Diese ist beispielsweise dann der Fall, wenn eine anderen
 * Planungsalternative in den LIVE Workspace übernommen wurde. Ände-rungen im
 * LIVE Workspace werden nicht automatisch in alle Kind-Workspaces
 * (Planungsal-ternativen) übernommen!
 * Der Aufruf dieser Prozedur ist nur dann möglich, wenn keine Konflikte
 * zwischen dem LIVE Workspace und dem Workspace der angegebenen
 * Planungsalternative existieren. Es werden durch den Aufruf nur die Tupel der
 * versionierten Tabellen geändert, die in LIVE jünger sind, als im Workspace
 * der Planungsalternative. Die Anzahl dieser Tupel kann mit der Prozedur
 * GetDiff vorab analysiert werden.
 * Vor der Aktualisierung des Workspaces wird ein Savepoint mit dem Namen
 * "refreshed" gesetzt (ggf. überschrieben), der es ermöglicht, das Datum des
 * letzten Aufrufens der Prozedur zu speichern.
 *
 * @param planningAlternativeId ID der Planungsalternative
 *
 * @return Status der Ausführung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return null oder Fehlermeldung
 */


CREATE OR REPLACE PROCEDURE RefreshPlanningAlternativeBdy(
  planningAlternativeId NUMBER,
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  planningAlternativeStatus DATE;
  workspacename VARCHAR2(30);
  isRefreshed INTEGER;
  paCount NUMBER;
  paStatus NUMBER;
  paCountException EXCEPTION;
  paStatusException EXCEPTION;

BEGIN
  -- Abragen der Existenz und des Status der Planung
  checkPlanningAlternative(planningAlternativeId, paCount, paStatus);

  IF paCount = 1 THEN   -- Planungsalternative existiert
    IF paStatus = 1 THEN  -- Planungsalternative noch aktiv

      -- Suchen des Workspace Namen
      SELECT workspace_name INTO workspacename
      FROM planning_alternative
      WHERE id = planningAlternativeId;

      -- Prüfen ob Savepoint schon existiert
      SELECT count(savepoint) INTO isRefreshed
      FROM all_workspace_savepoints
      WHERE workspace = workspacename AND savepoint LIKE 'refreshed';

      -- Savepoint setzen
      IF isRefreshed > 0 THEN
        DBMS_WM.DeleteSavepoint(workspacename, 'refreshed');
      END IF;

      DBMS_WM.CreateSavepoint(workspacename, 'refreshed');

      -- Ausführen des Refresh
      DBMS_WM.RefreshWorkspace(workspacename);

	    setOutParameter(1, NULL, outStatus, outMessage);

    ELSE   -- Planungsalternative bereits beendet
      RAISE paStatusException;
    END IF;

  ELSE   -- Planungsalternative existiert nicht
    RAISE paCountException;
  END IF;


EXCEPTION
  WHEN paCountException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative existiert nicht', outStatus, outMessage);
  WHEN paStatusException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative ist bereits beendet', outStatus, outMessage);
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/





/* 9
 * Die Prozedur gibt das Datum der letzten Aktualisierung des Workspaces einer
 * Planungsalternative zurück. Wurde die Planungsalternative noch nicht
 * expliziet aktualisiert, so wird das Datum zurückgegeben, an dem der Workspace
 * angelegt wurde.
 *
 * @param planningAlternativeID ID der Planungsalternative
 *
 * @return Status der Ausführung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return Refresh-Datum oder Fehlermeldung
 */

CREATE OR REPLACE PROCEDURE GetRefreshDateBdy(
  planningAlternativeId NUMBER,
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  workspaceName VARCHAR2(30);
  refreshDate DATE;
  paCount NUMBER;
  paStatus NUMBER;
  paCountException EXCEPTION;
  paStatusException EXCEPTION;

BEGIN
  -- Abragen der Existenz und des Status der Planung
  checkPlanningAlternative(planningAlternativeId, paCount, paStatus);

  IF paCount = 1 THEN   -- Planungsalternative existiert
    IF paStatus = 1 THEN  -- Planungsalternative noch aktiv

      -- Name des Workspaces abfragen
      SELECT workspace_name INTO workspaceName
      FROM planning_alternative
      WHERE id = planningAlternativeId;

      -- Refresh-Datum abfragen
      SELECT createtime INTO refreshDate
    	FROM all_workspace_savepoints
    	WHERE savepoint LIKE 'refreshed' AND workspace LIKE workspaceName;

	    setOutParameter(1, TO_CHAR(refreshDate, 'DD.MM.YYYY HH24:MI:SS'), outStatus, outMessage);

    ELSE   -- Planungsalternative bereits beendet
      RAISE paStatusException;
    END IF;

  ELSE   -- Planungsalternative existiert nicht
    RAISE paCountException;
  END IF;

EXCEPTION
  WHEN paCountException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative existiert nicht', outStatus, outMessage);
  WHEN paStatusException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative ist bereits beendet', outStatus, outMessage);
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/





/* 10
 * Die Prozedur löscht alle in der Tabelle planning_alternative vermerkten
 * Workspaces (Spalte 'workspace_name') und die entsprechenden Tupel in der
 * Tabelle. Es werden somit alle Planungsalternativen und ihre Workspaces
 * gelöscht!
 * Achtung: Dies Prozedur löscht sämtliche Daten der Workspaces unwiderrufbar
 * und dient lediglich dem Optimieren der Systemperformance oder dem Löschen des
 * Datenbankschemas!
 *
 * @return Status der Ausführung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return null oder Fehlermeldung
 */


CREATE OR REPLACE PROCEDURE DelAllPlanningAlternativesBdy(
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  planningAlternativeId NUMBER;
  workspacename VARCHAR2(30);
  CURSOR workspaces IS  -- enthält alle Workspacenamen
    SELECT workspace_name
    FROM planning_alternative;

BEGIN
  -- Cursor durchlaufen und jeweiligen Workspace löschen
  OPEN workspaces;
    LOOP
      FETCH workspaces INTO workspacename;
      EXIT WHEN workspaces%NOTFOUND;

      SELECT id INTO planningAlternativeId
      FROM planning_alternative
      WHERE workspace_name LIKE workspacename;

      DBMS_WM.RemoveWorkspace(workspacename);

      DELETE planning_alternative
      WHERE id = planningAlternativeId;

      DBMS_OUTPUT.PUT_LINE(planningAlternativeId || ' gelöscht');

    END LOOP;
  CLOSE workspaces;

  COMMIT;
  setOutParameter(1, NULL, outStatus, outMessage);

EXCEPTION
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/







/* 11
 * Die Prozedur löscht alle terminierten und in der Tabelle planning_alternative
 * vermerkten Workspaces (Spalte 'workspace_name') und die entsprechenden Tupel
 * in der Tabelle. Es werden somit alle beendeten Planungsalternativen und ihre
 * Workspaces gelöscht!
 * Achtung: Dies Prozedur löscht sämtliche Daten der Workspaces unwiderrufbar
 * und dient lediglich dem Optimieren der Systemperformance oder dem Löschen des
 * Datenbankschemas!
 *
 * @return Status der Ausführung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return null oder Fehlermeldung
 */

CREATE OR REPLACE PROCEDURE DelTermPlanningAlternativesBdy(
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  planningAlternativeId NUMBER;
  workspacename VARCHAR2(30);
  CURSOR workspaces IS  -- enthält die Workspacenamen aller beendeten Planungsalternativen
    SELECT workspace_name
    FROM planning_alternative
    WHERE termination_date IS NOT NULL;

BEGIN
  -- Cursor durchlaufen und jeweiligen Workspace löschen
  OPEN workspaces;
    LOOP
      FETCH workspaces INTO workspacename;
      EXIT WHEN workspaces%NOTFOUND;

      SELECT id INTO planningAlternativeId
      FROM planning_alternative
      WHERE workspace_name LIKE workspacename;

      DBMS_WM.RemoveWorkspace(workspacename);
      DELETE planning_alternative WHERE id = planningAlternativeId;

      DBMS_OUTPUT.PUT_LINE(planningAlternativeId || ' gelöscht');
    END LOOP;
  CLOSE workspaces;

  COMMIT;
  setOutParameter(1, NULL, outStatus, outMessage);

EXCEPTION
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/