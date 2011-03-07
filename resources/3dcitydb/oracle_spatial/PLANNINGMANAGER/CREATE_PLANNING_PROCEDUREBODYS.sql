-- CREATE_PLANNING_PROCEDUREBODYS.sql
--
-- Authors:     Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Dr. Thomas H. Kolbe <kolbe@ikg.uni-bonn.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--
-- Copyright:   (c) 2004-2006, Institute for Cartography and Geoinformation,
--                             Universit�t Bonn, Germany
--                             http://www.ikg.uni-bonn.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
--
-- Prozeduren zum Umgang mit Planungen
--
--   + AddPlanningBdy
--   + UpdatePlanningBdy
--   + DiscardPlanningBdy
--   + AcceptPlanningBdy
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 0.5.3     2006-04-03   release version                             LPlu
--                                                                    TKol
--                                                                    GGro
--                                                                    JSch
--                                                                    VStr
--



/* Die Prozedur beginnt eine Planung indem ein Datensatz
 * in der Tabelle PLANNING angelegt wird.
 * Soll zu der Planung keine r�umliche Begrenzung gespeichert werden,
 * so ist der Parameter mit NULL anzugeben.
 *
 * @param title Kurzbezeichnung Planung
 * @param description Kurze Beschreibung der Planung
 * @param executive Verantwortliche Person/Stelle f�r das Anlegen der Planung
 * @param spatialExtent r�umliche Begrenzung des Planungsgebiets
 *
 * @return Status der Ausf�hrung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return ID der Planung oder Fehlermeldung
 */

CREATE OR REPLACE PROCEDURE AddPlanningBdy(
  title VARCHAR2,
  description VARCHAR2,
  executive VARCHAR2,
  spatialExtent MDSYS.SDO_GEOMETRY,
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  sequenceGeneratedId NUMBER;

BEGIN
  -- den n�chsten Z�hler der Sequenz abholen
  SELECT planning_seq.nextval INTO sequenceGeneratedId
  FROM dual;

  -- Anlegen eines neuen Tupels
  INSERT INTO planning
  VALUES(
    sequenceGeneratedId,
    title,
    description,
    executive,
    spatialExtent,
    CURRENT_TIMESTAMP,
    null
  );

  COMMIT;

  setOutParameter(1, TO_CHAR(sequenceGeneratedId), outStatus, outMessage);

EXCEPTION
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/






/* TODO: k�nnen Metadaten beendeter Planungen aktualisiert werden?
 *
 * Die Prozedur �ndert die Parameter Titel, Beschreibung,
 * Verantwortliche Stelle und r�umliche Begrenzung einer
 * bestehenden Planung. Alle existierenden Eintr�ge werden �berschrieben.
 *
 * @param id Id der Planung, deren Metadaten ge�ndert werden sollen
 * @param title neue Kurzbezeichnung der Planung
 * @param description neue Beschreibung der Planung
 * @param executive neue verantwortliche Person/Stelle f�r das Anlegen der Planung
 * @param spatialExtent neue r�umliche Begrenzung des Planungsgebiets
 *
 * @return Status der Ausf�hrung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return null oder Fehlermeldung
 */

CREATE OR REPLACE PROCEDURE UpdatePlanningBdy(
  planningId NUMBER,
  newTitle VARCHAR2,
  newDescription VARCHAR2,
  newExecutive VARCHAR2,
  newSpatialExtent MDSYS.SDO_GEOMETRY,
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  planningCount NUMBER;
  planningStatus NUMBER;
  planningCountException EXCEPTION;
  planningStatusException EXCEPTION;

BEGIN
  -- Abragen der Existenz und des Status der Planung
  checkPlanning(planningId, planningCount, planningStatus);

  IF planningCount = 1 THEN   -- Planung existiert
    IF planningStatus = 1 THEN  -- kein Terminierungsdatum
      UPDATE planning
      SET
        title = newTitle,
        description = newDescription,
        executive = newExecutive,
        spatial_extent = newSpatialExtent
      WHERE id = planningId;

      COMMIT;
      setOutParameter(1, NULL, outStatus, outMessage);

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









/* Die Prozedur beendet eine Planung indem f�r alle Alternativen und f�r
 * die Planung selber ein Terminierungsdatum gesetzt wird.
 * F�r die Workspaces der Alternativen werden Savepoints mit dem Namen
 * "termination" gestzt. Die Workspaces werden nicht gel�scht.
 * Die Prozedur wird nur ausgef�hrt, wenn die Planung noch aktiv ist.
 * Es werden nur aktive Planungsalternativen beendet.
 *
 * @param planningId ID der zu beendenden Planung
 *
 * @return Status der Ausf�hrung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return null oder Fehlermeldung
 */


CREATE OR REPLACE PROCEDURE DiscardPlanningBdy(
  planningId NUMBER,
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  planningCount NUMBER;
  planningStatus NUMBER;
  planningCountException EXCEPTION;
  planningStatusException EXCEPTION;
  planningAlternativeId NUMBER;
  CURSOR planningAlternatives IS
    SELECT id
    FROM planning_alternative
    WHERE planning_id = planningId AND termination_date IS NULL;

BEGIN
  -- Abragen der Existenz und des Status der Planung
  checkPlanning(planningId, planningCount, planningStatus);

  IF planningCount = 1 THEN   -- Planung existiert
    IF planningStatus = 1 THEN  -- kein Terminierungsdatum
      -- Terminierungsdatum f�r die Planung setzen
      UPDATE planning
      SET termination_date = CURRENT_TIMESTAMP
      WHERE id = planningId;

      -- Terminierungsdatum f�r die zugenordneten Alternativen setzen
      OPEN planningAlternatives;
        LOOP
          FETCH planningAlternatives INTO planningAlternativeId;
          exit when planningAlternatives%NOTFOUND;
          UPDATE planning_alternative
          SET termination_date = CURRENT_TIMESTAMP
          WHERE id = planningAlternativeId;
        END LOOP;
      CLOSE planningAlternatives;

      COMMIT;
      setOutParameter(1, NULL, outStatus, outMessage);

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






/* Die Prozedur beendet eine Planung.
 * Die ausgew�hlte Planungsalternative wird in den Elternworkspace �berf�hrt.
 * Anschlie�end werden f�r alle Alternativen und die Planung selber
 * ein Terminierungsdatum gesetzt.
 * F�r die Workspaces der Alternativen werden Savepoints mit dem Namen
 * "termination" gestzt.
 * Die Prozedur wird nur dann ausgef�hrt, wenn die Planung und die
 * Planungsalternative noch aktive sind.
 *
 * @param planningId ID der zu beendenden Planung
 * @param acceptedAlternativeId ID der zu �bernehmenden Planungsalternative
 *
 * @return Status der Ausf�hrung: 1 = fehlerfrei, 0 = fehlerhaft
 * @return null oder Fehlermeldung
 */


CREATE OR REPLACE PROCEDURE AcceptPlanningBdy(
  planningId NUMBER,
  acceptedAlternativeId NUMBER,
  outStatus OUT NUMBER,
  outMessage OUT VARCHAR2
)

IS
  -- lokale Variablen
  workspaceName VARCHAR2(30);
  planningCount NUMBER;
  planningStatus NUMBER;
  isValidPlanningAlternative NUMBER;
  planningDiscardException EXCEPTION;
  planningCountException EXCEPTION;
  planningStatusException EXCEPTION;
  invalidAlternativeException EXCEPTION;

BEGIN
  -- Abragen der Existenz und des Status der Planung
  checkPlanning(planningId, planningCount, planningStatus);

  -- Pr�fen, ob die angegeben Planungsalternative zu dieser Planung geh�rt und
  -- noch nicht beendet ist.
  SELECT COUNT(id) INTO isValidPlanningalternative
  FROM planning_alternative
  WHERE termination_date IS NULL AND planning_id = planningId AND id = acceptedAlternativeId;

  IF planningCount = 1 THEN   -- Planung existiert
    IF planningStatus = 1 THEN  -- Planung noch aktiv
      IF isValidPlanningalternative = 1 THEN   -- Planungsalternative g�ltig
        -- Abfragen des Workspacenamen der akzeptierten Alternative
        SELECT workspace_name INTO workspaceName
        FROM planning_alternative
        WHERE id = acceptedAlternativeId;

        -- Konflikte m�ssen in einem Anwendungsprogramm gel�st werden!
        DBMS_WM.MergeWorkspace(workspaceName);

        -- die Planung und ihre Alternativen beenden
        DiscardPlanningBdy(planningId, outStatus, outMessage);

        IF outStatus = 1 THEN   -- Planung fehlerfrei beendet
          COMMIT;
          setOutParameter(1, NULL, outStatus, outMessage);
        ELSE
          RAISE planningDiscardException;
        END IF;

      ELSE   -- Alternative ung�ltig
        RAISE invalidAlternativeException;
      END IF;

    ELSE   -- Planung bereits beendet
      RAISE planningStatusException;
    END IF;

  ELSE   -- Planung existiert nicht
    RAISE planningCountException;
  END IF;

EXCEPTION
  WHEN planningDiscardException THEN
    setOutParameter(0, '3D-Geo-DB: Planung fehlerhaft beendet', outStatus, outMessage);
  WHEN invalidAlternativeException THEN
    setOutParameter(0, '3D-Geo-DB: Planungsalternative ung�ltig', outStatus, outMessage);
  WHEN planningCountException THEN
    setOutParameter(0, '3D-Geo-DB: Planung existiert nicht', outStatus, outMessage);
  WHEN planningStatusException THEN
    setOutParameter(0, '3D-Geo-DB: Planung ist bereits beendet', outStatus, outMessage);
  WHEN OTHERS THEN
    setOutParameter(0, sqlerrm, outStatus, outMessage);

END;
/