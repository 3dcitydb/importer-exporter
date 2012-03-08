-- SPATIAL_INDEX.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Gerhard König <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
-- Conversion:  Laure Fraysse <Laure.fraysse@etumel.univmed.fr>
--
-- Copyright:   (c) 2007-2011  Institute for Geodesy and Geoinformation Science,
--                             Technische Universität Berlin, Germany
--                             http://www.igg.tu-berlin.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
--
--
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description     | Author | Conversion
-- 2.0.0     2011-12-09   PostGIS version    TKol	  LFra	
--                                           GKoe     
--                                           CNag
--                                           ASta
--

-- CITYOBJECT SPATIAL INDEX************************************************************************************************************
CREATE INDEX CITYOBJECT_SPX 				ON CITYOBJECT 					USING GIST ( ENVELOPE );


-- SURFACE_GEOMETRY SPATIAL INDEX********************************************************************************************************
CREATE INDEX SURFACE_GEOM_SPX 				ON SURFACE_GEOMETRY 			USING GIST ( GEOMETRY );


-- BREAKLINE_RELIEF SPATIAL INDEX********************************************************************************************************
CREATE INDEX BREAKLINE_RID_SPX 				ON BREAKLINE_RELIEF 			USING GIST ( RIDGE_OR_VALLEY_LINES );
CREATE INDEX BREAKLINE_BREAK_SPX 			ON BREAKLINE_RELIEF 			USING GIST ( BREAK_LINES );


-- MASSPOINT_RELIEF SPATIAL INDEX********************************************************************************************************
CREATE INDEX MASSPOINT_REL_SPX 				ON MASSPOINT_RELIEF 			USING GIST ( RELIEF_POINTS );


-- ORTHOPHOTO_IMP SPATIAL INDEX********************************************************************************************************
CREATE INDEX ORTHOPHOTO_IMP_SPX				ON ORTHOPHOTO_IMP 				USING GIST ( FOOTPRINT );


-- TIN_RELIEF SPATIAL INDEX********************************************************************************************************
CREATE INDEX TIN_RELF_STOP_SPX 				ON TIN_RELIEF 					USING GIST ( STOP_LINES );
CREATE INDEX TIN_RELF_BREAK_SPX 			ON TIN_RELIEF 					USING GIST ( BREAK_LINES ); 
CREATE INDEX TIN_RELF_CRTLPTS_SPX			ON TIN_RELIEF 					USING GIST ( CONTROL_POINTS );


-- GENERIC_CITYOBJECT SPATIAL INDEX********************************************************************************************************
CREATE INDEX GENERICCITY_LOD0TERR_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD0_TERRAIN_INTERSECTION );
CREATE INDEX GENERICCITY_LOD1TERR_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD1_TERRAIN_INTERSECTION );
CREATE INDEX GENERICCITY_LOD2TERR_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD2_TERRAIN_INTERSECTION );
CREATE INDEX GENERICCITY_LOD3TERR_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD3_TERRAIN_INTERSECTION );
CREATE INDEX GENERICCITY_LOD4TERR_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD4_TERRAIN_INTERSECTION );
CREATE INDEX GENERICCITY_LOD1REFPNT_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD1_IMPLICIT_REF_POINT );
CREATE INDEX GENERICCITY_LOD2REFPNT_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD2_IMPLICIT_REF_POINT );
CREATE INDEX GENERICCITY_LOD3REFPNT_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD3_IMPLICIT_REF_POINT );
CREATE INDEX GENERICCITY_LOD4REFPNT_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD4_IMPLICIT_REF_POINT );


-- BUILDING SPATIAL INDEX********************************************************************************************************
CREATE INDEX BUILDING_LOD1TERR_SPX 			ON BUILDING			 			USING GIST ( LOD1_TERRAIN_INTERSECTION );
CREATE INDEX BUILDING_LOD2TERR_SPX 			ON BUILDING			 			USING GIST ( LOD2_TERRAIN_INTERSECTION );
CREATE INDEX BUILDING_LOD3TERR_SPX			ON BUILDING			 			USING GIST ( LOD3_TERRAIN_INTERSECTION );
CREATE INDEX BUILDING_LOD4TERR_SPX			ON BUILDING			 			USING GIST ( LOD4_TERRAIN_INTERSECTION );
CREATE INDEX BUILDING_LOD2MULTI_SPX			ON BUILDING			 			USING GIST ( LOD2_MULTI_CURVE );
CREATE INDEX BUILDING_LOD3MULTI_SPX			ON BUILDING			 			USING GIST ( LOD3_MULTI_CURVE );
CREATE INDEX BUILDING_LOD4MULTI_SPX			ON BUILDING			 			USING GIST ( LOD4_MULTI_CURVE );


-- BUILDING_FURNITURE SPATIAL INDEX********************************************************************************************************
CREATE INDEX BUILDING_FURN_LOD4REFPNT_SPX 	ON BUILDING_FURNITURE 			USING GIST ( LOD4_IMPLICIT_REF_POINT );


-- CITY_FURNITURE SPATIAL INDEX********************************************************************************************************
CREATE INDEX CITY_FURN_LOD1TERR_SPX			ON CITY_FURNITURE 				USING GIST ( LOD1_TERRAIN_INTERSECTION );
CREATE INDEX CITY_FURN_LOD2TERR_SPX			ON CITY_FURNITURE 				USING GIST ( LOD2_TERRAIN_INTERSECTION );
CREATE INDEX CITY_FURN_LOD3TERR_SPX			ON CITY_FURNITURE 				USING GIST ( LOD3_TERRAIN_INTERSECTION );
CREATE INDEX CITY_FURN_LOD4TERR_SPX			ON CITY_FURNITURE 				USING GIST ( LOD4_TERRAIN_INTERSECTION );
CREATE INDEX CITY_FURN_LOD1REFPNT_SPX		ON CITY_FURNITURE 				USING GIST ( LOD1_IMPLICIT_REF_POINT );
CREATE INDEX CITY_FURN_LOD2REFPNT_SPX		ON CITY_FURNITURE 				USING GIST ( LOD2_IMPLICIT_REF_POINT );
CREATE INDEX CITY_FURN_LOD3REFPNT_SPX		ON CITY_FURNITURE 				USING GIST ( LOD3_IMPLICIT_REF_POINT );
CREATE INDEX CITY_FURN_LOD4REFPNT_SPX 		ON CITY_FURNITURE 				USING GIST ( LOD4_IMPLICIT_REF_POINT );


-- CITYMODEL SPATIAL INDEX********************************************************************************************************
CREATE INDEX CITYMODEL_SPX	 				ON CITYMODEL 					USING GIST ( ENVELOPE );


-- CITYOBJECTGROUP SPATIAL INDEX********************************************************************************************************
CREATE INDEX CITYOBJECTGROUP_SPX			ON CITYOBJECTGROUP 				USING GIST ( GEOMETRY );


-- RELIEF_COMPONENT SPATIAL INDEX********************************************************************************************************
CREATE INDEX RELIEF_COMPONENT_SPX 			ON RELIEF_COMPONENT 			USING GIST ( EXTENT );


-- SOLITARY_VEGETAT_OBJECT SPATIAL INDEX********************************************************************************************************
CREATE INDEX SOL_VEGETAT_OBJ_LOD1REFPNT_SPX	ON SOLITARY_VEGETAT_OBJECT 		USING GIST ( LOD1_IMPLICIT_REF_POINT );
CREATE INDEX SOL_VEGETAT_OBJ_LOD2REFPNT_SPX	ON SOLITARY_VEGETAT_OBJECT 		USING GIST ( LOD2_IMPLICIT_REF_POINT );
CREATE INDEX SOL_VEGETAT_OBJ_LOD3REFPNT_SPX	ON SOLITARY_VEGETAT_OBJECT 		USING GIST ( LOD3_IMPLICIT_REF_POINT );
CREATE INDEX SOL_VEGETAT_OBJ_LOD4REFPNT_SPX	ON SOLITARY_VEGETAT_OBJECT 		USING GIST ( LOD4_IMPLICIT_REF_POINT );


-- SURFACE_DATA SPATIAL INDEX********************************************************************************************************
CREATE INDEX SURFACE_DATA_SPX 				ON SURFACE_DATA 				USING GIST ( GT_REFERENCE_POINT );


-- TRANSPORTATION_COMPLEX SPATIAL INDEX********************************************************************************************************
CREATE INDEX TRANSPORTATION_COMPLEX_SPX		ON TRANSPORTATION_COMPLEX 		USING GIST ( LOD0_NETWORK );


-- WATERBODY SPATIAL INDEX********************************************************************************************************
CREATE INDEX WATERBODY_LOD0MULTI_SPX		ON WATERBODY 					USING GIST ( LOD0_MULTI_CURVE );
CREATE INDEX WATERBODY_LOD1MULTI_SPX		ON WATERBODY 					USING GIST ( LOD1_MULTI_CURVE );