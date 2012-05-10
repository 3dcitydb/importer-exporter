-- MOSAIC.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <thomas.kolbe@igg.tu-berlin.de>
--				Dr. Andreas Poth <poth@lat-lon.de>
--
-- Conversion:	Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2012  Institute for Geodesy and Geoinformation Science,
--                             Technische Universität Berlin, Germany
--                             http://www.igg.tu-berlin.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
-- Stored FUNCTIONs written in PL/pgSQL for raster data management
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description      | Author | Conversion
-- 1.0.0     2012-05-10   PostGIS version    TKol	  FKun
--											 APot
--
-------------------------------------------------------------------------------
-- Conversion-Report:
-- Functions compiled but not yet tested.
--
-- ST_SetSRID(raster,integer) only changes the raster meta-data
-- but ST_Transform(ST_SetSRID(rast,3068), 3068, 0.0, 0.0, 'NearestNeighbor', 0.125)
-- led to an Error 
-- (ERROR: rt_raster_gdal_warp: Unable to create GDAL transformation object for output dataset creation
--
-- ST_Resample function is used as an equivalent to sdo_geor.generatePyramid,
-- but the scale-values are still 0. They have to be changed in order to generate
-- efficient raster-overlays.
-------------------------------------------------------------------------------


/*****************************************************************
* Set_Orthophoto_SRID
* 
* Stored FUNCTION for setting the SRID of the imported 
* orthophoto tiles to the database predefined CRS.
* This FUNCTION must be called after importing raster tiles
* using the import / export tool from lat/lon, because it leaves
* the SRID of the imported raster tiles empty.
******************************************************************/

CREATE OR REPLACE FUNCTION geodb_pkg.mos_Set_Orthophoto_SRID() RETURNS SETOF void AS
$$
DECLARE
	rast          raster;
	orthophoto_id ORTHOPHOTO_IMP.ID%TYPE;
	srs_id        DATABASE_SRS.SRID%TYPE;

	c_orthophoto_imp CURSOR FOR  /* select all imported orthophoto tiles */
		SELECT id, orthophotoproperty FROM ORTHOPHOTO_IMP FOR UPDATE;

BEGIN

-- // Fetch SRID for this database 
SELECT srid INTO srs_id FROM DATABASE_SRS;

OPEN c_orthophoto_imp;
   FOR recordvar IN c_orthophoto_imp LOOP
      FETCH c_orthophoto_imp INTO orthophoto_id, rast;
      EXIT WHEN NOT FOUND;

    	PERFORM ST_SetSRID(rast, srs_id);
    	UPDATE ORTHOPHOTO_IMP SET orthophotoproperty=rast
    	WHERE CURRENT OF c_orthophoto_imp;
   END LOOP;
CLOSE c_orthophoto_imp;

END;
$$
LANGUAGE plpgsql;


/*****************************************************************
* Set_Raster_Relief_SRID
* 
* Stored FUNCTION for setting the SRID of the imported
* RasterRelief tiles to the database predefined CRS.
* This FUNCTION must be called after importing raster tiles
* using the import / export tool from lat/lon, because it leaves
* the SRID of the imported raster tiles empty.
******************************************************************/

CREATE OR REPLACE FUNCTION geodb_pkg.mos_Set_Raster_Relief_SRID() RETURNS SETOF void AS
$$
DECLARE
	rast          raster;
	raster_relief_id RASTER_RELIEF_IMP.ID%TYPE;
	srs_id        DATABASE_SRS.SRID%TYPE;

	c_raster_relief_imp CURSOR FOR  /* select all imported raster_relief tiles */
		SELECT id, rasterproperty FROM RASTER_RELIEF_IMP FOR UPDATE;

BEGIN

-- // Fetch SRID for this database 
SELECT srid INTO srs_id FROM DATABASE_SRS;

OPEN c_raster_relief_imp;
   FOR recordvar IN c_raster_relief_imp LOOP
      FETCH c_raster_relief_imp INTO raster_relief_id, rast;
      EXIT WHEN NOT FOUND;

      PERFORM ST_SetSRID(rast, srs_id);
      UPDATE RASTER_RELIEF_IMP SET rasterproperty=rast
      WHERE CURRENT OF c_raster_relief_imp;
   END LOOP;
CLOSE c_raster_relief_imp;

END;
$$
LANGUAGE plpgsql;


/*****************************************************************
* mosaicOrthophotosInitial
* 
* Stored FUNCTION which unions orthophoto tiles to one large 
* raster data object for a given LOD. The new big raster is stored
* in the table ORTHOPHOTO with a new ID by CITYOBJECT_ID_SEQ.
* This ID is printed to stdout. In order to see the ID value, 
* database output has to be activated in SQL*Plus before calling the 
* FUNCTION by the following command:
* 	SET SERVEROUTPUT on
*	PARAMETERS:
*	  nameVal = name of the orthophoto
*     typeVal = type description of the orhtophoto
*     lineageVal = lineage (sensor, source) of the orthophoto
*     LoDVal = LoD of the Orthophoto
*   Example:
*     Execute geodb_pkg.mos_mosaicOrthophotosInitial
*				('Orthophoto1','True Orthophoto 0.2m','HRSC camera flight',2); 
*
******************************************************************/

CREATE OR REPLACE FUNCTION geodb_pkg.mos_mosaicOrthophotosInitial ( 
	nameVal VARCHAR, 
	typeVal VARCHAR, 
	IN lineageVal VARCHAR, 
	IN LoDVal NUMERIC ) 
RETURNS SETOF void AS
$$
DECLARE
	ras           raster;
	rast          raster;
	fprnt         geometry(PolygonZ);
	orthophoto_id CITYOBJECT.ID%TYPE;
	srs_id        DATABASE_SRS.SRID%TYPE;
    tabname       VARCHAR(50);

BEGIN
	-- // Fetch SRID for this database 
	SELECT srid INTO srs_id FROM DATABASE_SRS;
	
	-- // Generate new ID value for the new Orthophoto-CITYOBJECT 
	SELECT nextval('CITYOBJECT_ID_SEQ') INTO orthophoto_id;
  
	-- // Use dummy footprint because real envelope is not known before 
	-- // calling mosaic. 
	fprnt := ST_GeomFromEWKT('SRID=' || srs_id || ';POLYGON((0 0 0,0 1 0,1 1 1,1 0 1,0 0 0))');

	-- // ***** To do: GMLID and GMLID_CODESPACE should be set to sensible values
	INSERT INTO CITYOBJECT ( ID, CLASS_ID, ENVELOPE, CREATION_DATE, LINEAGE ) 
	       VALUES ( orthophoto_id, 20, fprnt, now(), lineageVal );

	-- // set the modelSRID of all raster tiles
	PERFORM geodb_pkg.mos_Set_Orthophoto_SRID();
	-- // create big raster object by mosaicking the image tiles
	RAISE NOTICE 'Mosaicking image tiles... ';

	-- // get owner of table scheme
        SELECT user INTO tabname;
        tabname := tabname||'.ORTHOPHOTO_RDT';

	ras := ST_Union((SELECT ORTHOPHOTOPROPERTY FROM ORTHOPHOTO_IMP));
	-- // set the CRS of the big raster
	PERFORM ST_SetSRID(ras, srs_id);
	-- // insert big raster into table ORTHOPHOTO
	INSERT INTO ORTHOPHOTO ( id, ORTHOPHOTOPROPERTY, NAME, TYPE, DATUM, LOD ) 
	       VALUES ( orthophoto_id, ras, nameVal, typeVal, now(), LoDVal );

	-- // update footprint
	SELECT ST_Evelope(ORTHOPHOTOPROPERTY) INTO fprnt 
	       FROM ORTHOPHOTO 
	       WHERE id = orthophoto_id FOR UPDATE;
		   
	-- maybe ST_Polygon (concerns holes) or Box3D(ORTHOPHOTOPROPERTY)::geometry is wanted here
    -- // ***** Problem: footprint is 2D, but must be 3D for CITYOBJECT
	-- UPDATE CITYOBJECT set ENVELOPE = fprnt where ID = orthophoto_id;

	-- // create pyramid - Raster Overview, as they're called in the PostGIS world,
	--	are usually created during the raster2pgsql-process
	-- maybe a different approac has to be used here
	/*
	-- this is the used FUNCTION:
	-- raster ST_Resample(raster rast, integer srid=NULL, double precision scalex=0, double precision scaley=0, 
	--   double precision gridx=NULL, double precision gridy=NULL, double precision skewx=0, double precision skewy=0,
	--   text algorithm=NearestNeighbor (or Bilinear, Cubic, CubicSpline, Lanczos), double precision maxerr=0.125);
	
	RAISE NOTICE 'Generating image pyramid... ';

	SELECT ST_Resample(ORTHOPHOTOPROPERTY, srs_id, 0.0, 0.0, NULL, NULL, 0.0, 0.0, 'Cubic', 0.125) INTO rast FROM ORTHOPHOTO 
	       WHERE id = orthophoto_id FOR UPDATE;
		   
	UPDATE ORTHOPHOTO SET ORTHOPHOTOPROPERTY = rast WHERE id = orthophoto_id;
	*/
	COMMIT;
	RAISE NOTICE 'New Orthophoto-Cityobject generated with ID %', orthophoto_id;
END;
$$
LANGUAGE plpgsql;

/*****************************************************************
* mosaicOrthophotosUpdate
* 
* Stored FUNCTION for updating an existing Orthophoto.
* This is useful if some image tiles in ORTHOPHOTO_IMP have been 
* replaced by updated versions. The FUNCTION calls the ST_Union 
* FUNCTION for gathering orthophoto tiles within one large raster
* data object which then replaces the former Raster of the given 
* Orthophoto.
*
* Example:
*   Execute geodb_pkg.mos_mosaicOrthophotosUpdate
*				(8197,'Update of some tiles','Mr Smith');  
*
******************************************************************/

CREATE OR REPLACE FUNCTION geodb_pkg.mos_mosaicOrthophotosUpdate( 
	IN idVal NUMERIC, 
	IN reason VARCHAR, 
	IN updatingPerson VARCHAR ) 
RETURNS SETOF void AS
$$
DECLARE
	ras 	raster;
	rast 	raster;
	fprnt 	geometry(PolygonZ);
	srs_id	DATABASE_SRS.SRID%TYPE;
    tabname VARCHAR(50);
	
BEGIN	
	-- // Fetch SRID for this database 
	SELECT srid INTO srs_id FROM DATABASE_SRS;
	
	DELETE FROM ORTHOPHOTO_RDT WHERE RASTERID = idVal;

	-- // set the modelSRID of all raster tiles
	PERFORM geodb_pkg.mos_Set_Orthophoto_SRID();
	-- // create big raster object by mosaicking the image tiles
	RAISE NOTICE 'Mosaicking image tiles... ';

	-- // get owner of table scheme
        SELECT user INTO tabname;
        tabname := tabname||'.ORTHOPHOTO_RDT';

	ras := ST_Union((SELECT ORTHOPHOTOPROPERTY FROM ORTHOPHOTO_IMP));
	-- // set the CRS of the big raster 
	PERFORM ST_SetSRID(ras, srs_id);
	-- // update big raster in table ORTHOPHOTO
	UPDATE ORTHOPHOTO SET ORTHOPHOTOPROPERTY = ras 
	       WHERE id = idVAL;
	
	-- // update footprint
	SELECT ST_Envelope(ORTHOPHOTOPROPERTY) INTO fprnt 
	       FROM ORTHOPHOTO WHERE id = idVal FOR UPDATE;
	
	UPDATE CITYOBJECT SET ENVELOPE = fprnt, LAST_MODIFICATION_DATE = now(), 
	       UPDATING_PERSON = updatingPerson, REASON_FOR_UPDATE = reason;
	
	-- // create pyramid - Raster Overview, as they're called in the PostGIS world,
	--	are usually created during the raster2pgsql-process
	-- maybe a different approac has to be used here
	/*
	RAISE NOTICE 'Generating image pyramid... ';

	SELECT ST_Resample(ORTHOPHOTOPROPERTY, srs_id, 0.0, 0.0, NULL, NULL, 0.0, 0.0, 'Cubic', 0.125) INTO rast FROM ORTHOPHOTO 
	       WHERE id = idVal FOR UPDATE;
	
	UPDATE ORTHOPHOTO SET ORTHOPHOTOPROPERTY = rast WHERE id = idVal;
	*/
	COMMIT;
END;
$$
LANGUAGE plpgsql;


/*****************************************************************
* mosaicRasterReliefInitial
* 
* Stored FUNCTION which unions RasterRelief tiles to one large 
* raster data object for a given LOD. The new big raster is stored
* in the table RASTER_RELIEF with a new ID by CITYOBJECT_ID_SEQ.
* Furthermore, a new RELIEF tuple with a new ID from CITYOBJECT_ID_SEQ
* is generated of which the new RasterRelief becomes the only member.
* Both IDs are printed to stdout. In order to see the ID value, 
* database output has to be activated in SQL*Plus before calling the 
* FUNCTION by the following command:
* 	SET SERVEROUTPUT on
*	PARAMETERS:
*     gmlIdRelief = gml:id of the ReliefFeature feature
*	  gmlIdRaster = gml:id of the Raster feature
*	  gmlIdCodespace = Codespace for the gml:id values
*	  nameVal = name of the orthophoto
*	  descVal = description of the orthophoto
*	  lineageVal = lineage (sensor, source) of the orthophoto
*	  LoDVal = LoD of the Orthophoto
*	Example:
*	  Execute geodb_pkg.mos_mosaicRasterReliefInitial
*				('UUID_2000abcd','UUID_2000abce','UUID','DTM of Berlin',
*					'0.5m Raster','Photogrammetric Processing',2); 
*
******************************************************************/

CREATE OR REPLACE FUNCTION geodb_pkg.mos_mosaicRasterReliefInitial ( 
	gmlIdRelief VARCHAR, 
	gmlIdRaster VARCHAR, 
	gmlIdCodespace VARCHAR,
	nameVal VARCHAR, 
	descVal VARCHAR, 
	IN lineageVal VARCHAR, 
	IN LoDVal NUMERIC ) 
RETURNS SETOF void AS
$$
DECLARE
	ras              raster;
	rast             raster;
	fprnt            geometry(PolygonZ);
	relief_id        CITYOBJECT.ID%TYPE;
	relief_component_id CITYOBJECT.ID%TYPE;
	srs_id		 	 DATABASE_SRS.SRID%TYPE;
    tabname          VARCHAR(50);
	
BEGIN
	-- // Fetch SRID for this database 
	SELECT srid INTO srs_id FROM DATABASE_SRS;
	
	-- // generate new ID values for the two new CITYOBJECTs (RASTER_RELIEF 
	-- // and RELIEF)
	SELECT nextval('CITYOBJECT_ID_SEQ') INTO relief_component_id;
	SELECT nextval('CITYOBJECT_ID_SEQ') INTO relief_id;
  
	-- // use dummy because real envelope is not known before calling mosaic
	fprnt := ST_GeomFromEWKT('SRID=' || srs_id || ';POLYGON((0 0 0,0 1 0,1 1 1,1 0 1,0 0 0))');

	-- // create ReliefFeature object
	INSERT INTO CITYOBJECT ( ID, GMLID, GMLID_CODESPACE, CLASS_ID, ENVELOPE, 
				 CREATION_DATE, LINEAGE ) 
	       VALUES ( relief_id, gmlIdRelief, gmlIdCodespace, 14, fprnt, now(), lineageVal );
	INSERT INTO RELIEF_FEATURE ( ID, NAME, DESCRIPTION, LOD ) 
	       VALUES ( relief_id, nameVal, descVal, LoDVal);

	-- // create RASTER object
	INSERT INTO CITYOBJECT ( ID, GMLID, GMLID_CODESPACE, CLASS_ID, ENVELOPE, 
				 CREATION_DATE, LINEAGE ) 
	       VALUES ( relief_component_id, gmlIdRaster, gmlIdCodespace, 19, fprnt, 
	       		now(), lineageVal );
	INSERT INTO RELIEF_COMPONENT ( ID, NAME, DESCRIPTION, LOD ) 
	       VALUES ( relief_component_id, nameVal, descVal, LoDVal );

	-- // set the modelSRID of all raster tiles
	PERFORM geodb_pkg.mos_Set_Raster_Relief_SRID();
	-- // create big raster object by mosaicking the image tiles
	RAISE NOTICE 'Mosaicking DTM tiles...';

	-- // get owner of table scheme
        SELECT user INTO tabname;
        tabname := tabname||'.RASTER_RELIEF_RDT';

	ras := ST_Union((SELECT RASTERPROPERTY FROM RASTER_RELIEF_IMP));
	-- // set the CRS of the big raster 
	PERFORM ST_SetSRID(ras,srs_id);
	-- // insert big raster into table RASTER_RELIEF
	INSERT INTO RASTER_RELIEF ( ID, RASTERPROPERTY ) 
	       VALUES ( relief_component_id, ras );

	-- // update footprint
	SELECT ST_Envelope(RASTERPROPERTY) INTO fprnt 
	       FROM RASTER_RELIEF 
	       WHERE id = relief_component_id FOR UPDATE;

	-- // set correct envelope in all respective tables
	UPDATE RELIEF_COMPONENT SET EXTENT = fprnt WHERE ID = relief_component_id;
	-- // ***** Problem: footprint is 2D, but must be 3D for CITYOBJECT
	-- UPDATE CITYOBJECT set ENVELOPE = fprnt where ID = relief_id;
	-- UPDATE CITYOBJECT set ENVELOPE = fprnt where ID = relief_component_id;

	-- // insert association between ReliefFeature and Raster
	INSERT INTO RELIEF_FEAT_TO_REL_COMP ( RELIEF_FEATURE_ID, RELIEF_COMPONENT_ID ) 
	       VALUES ( relief_id, relief_component_id);

	-- // create pyramid - Raster Overview, as they're called in the PostGIS world,
	--	are usually created during the raster2pgsql-process
	-- maybe a different approac has to be used here
	/*
	RAISE NOTICE 'Generating image pyramid... ';

	SELECT ST_Resample(RASTERPROPERTY, srs_id, 0.0, 0.0, NULL, NULL, 0.0, 0.0, 'Cubic', 0.125) INTO rast FROM RASTER_RELIEF 
	       WHERE id = relief_component_id FOR UPDATE;

	UPDATE RASTER_RELIEF SET RASTERPROPERTY = rast WHERE id = relief_component_id;
	*/
	-- // update tuples in RASTER_RELIEF_IMP to point to the generated 
	-- // RELIEF and RASTER_RELIEF tuples
	UPDATE RASTER_RELIEF_IMP SET RELIEF_ID=relief_id, 
	                             RASTER_RELIEF_ID=relief_component_id;
  
	COMMIT;
	RAISE NOTICE 'New Raster-Cityobject generated with ID %', relief_component_id;
	RAISE NOTICE 'New ReliefFeature-Cityobject generated with ID %', relief_id;
END;
$$
LANGUAGE plpgsql;


/*****************************************************************
* mosaicRasterReliefUpdate
* 
* Stored FUNCTION for updating an existing RasterRelief.
* This is useful if some image tiles in RASTER_RELIEF_IMP have been 
* replaced by updated versions. The FUNCTION calls the ST_Union 
* FUNCTION for gathering raster_relief tiles within one large raster
* data object which then replaces the former Raster of the given 
* ReliefObject.
*
* Example:
*   Execute geodb_pkg.mos_mosaicRasterReliefUpdate
*				(15233,'Update of some tiles','Mr Smith');   
*
******************************************************************/

CREATE OR REPLACE FUNCTION geodb_pkg.mos_mosaicRasterReliefUpdate( 
	idVal IN NUMERIC, 
	reason IN VARCHAR, 
	updatingPerson IN VARCHAR ) 
RETURNS SETOF void AS
$$
DECLARE 
	ras		raster;
	rast	raster;
	fprnt	geometry(PolygonZ);
	srs_id	DATABASE_SRS.SRID%TYPE;
    tabname VARCHAR(50);

BEGIN
	-- // Fetch SRID for this database 
	SELECT srid INTO srs_id FROM DATABASE_SRS;
	
	-- // discard the old raster
	-- delete from RASTER_RELIEF_RDT where RASTER_RELIEF_ID = idVal;

	-- // set the modelSRID of all imported raster tiles
	PERFORM geodb_pkg.mos_Set_Raster_Relief_SRID();

	-- // get owner of table scheme
        SELECT USER INTO tabname FROM dual;
        tabname := tabname||'.RASTER_RELIEF_RDT';

	RAISE NOTICE 'Mosaicking DTM tiles...';
	ras := ST_Union((SELECT RASTERPROPERTY FROM RASTER_RELIEF_IMP));
	-- // set the CRS of the big raster 
	PERFORM ST_SetSRID(ras,srs_id);
	-- // update big raster in table RASTER_RELIEF
	UPDATE RASTER_RELIEF set RASTERPROPERTY = ras WHERE id = idVAL;
	
	-- // update footprint
	SELECT ST_Envelope(RASTERPROPERTY) into fprnt 
	       FROM RASTER_RELIEF WHERE id = idVal FOR UPDATE;
	
	UPDATE CITYOBJECT set ENVELOPE = fprnt, LAST_MODIFICATION_DATE = now(), 
	       UPDATING_PERSON = updatingPerson, REASON_FOR_UPDATE = reason
	       WHERE id=idVal;
	UPDATE CITYOBJECT set ENVELOPE = fprnt, LAST_MODIFICATION_DATE = now(), 
	       UPDATING_PERSON = updatingPerson, REASON_FOR_UPDATE = reason
	       WHERE id=(select RELIEF_FEATURE_ID FROM RELIEF_FEAT_TO_REL_COMP
	       		 WHERE RELIEF_COMPONENT_ID=idVal);
	UPDATE RELIEF_COMPONENT set EXTENT = fprnt WHERE id=idVal;
	
	-- // create pyramid - Raster Overview, as they're called in the PostGIS world,
	--	are usually created during the raster2pgsql-process
	-- maybe a different approac has to be used here
	/*
	RAISE NOTICE 'Generating image pyramid... ';

	SELECT ST_Resample(RASTERPROPERTY, srs_id, 0.0, 0.0, NULL, NULL, 0.0, 0.0, 'Cubic', 0.125) INTO rast FROM RASTER_RELIEF 
	       WHERE id = idVal FOR UPDATE;
	
	UPDATE RASTER_RELIEF set RASTERPROPERTY = rast WHERE id = idVal;
	*/
	COMMIT;
END;
$$
LANGUAGE plpgsql;
