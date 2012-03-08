
----------------------------------------------------------------------
--
-- S O L D N E R     P R O J E C T I O N
--
----------------------------------------------------------------------
-- Reference to the following Soldner projection and the parameter:

-- Source: http://spatialreference.org/ref/epsg/3068/postgis/

DELETE FROM public.spatial_ref_sys WHERE srid=81989002;

INSERT INTO spatial_ref_sys (srid, auth_name, auth_srid, srtext, proj4text) VALUES 
(3068,
'epsg',
3068,
'PROJCS["DHDN / Soldner Berlin",
	GEOGCS["DHDN",
		DATUM["Deutsches_Hauptdreiecksnetz",
			SPHEROID["Bessel 1841",6377397.155,299.1528128,AUTHORITY["EPSG","7004"]],
			AUTHORITY["EPSG","6314"]
		],
		PRIMEM["Greenwich",0,AUTHORITY["EPSG","8901"]],
		UNIT["degree",0.01745329251994328,AUTHORITY["EPSG","9122"]],
		AUTHORITY["EPSG","4314"]
	],
	UNIT["metre",1,AUTHORITY["EPSG","9001"]],
	PROJECTION["Cassini_Soldner"],
	PARAMETER["latitude_of_origin",52.41864827777778],
	PARAMETER["central_meridian",13.62720366666667],
	PARAMETER["false_easting",40000],
	PARAMETER["false_northing",10000],
	AUTHORITY["EPSG","3068"],
	AXIS["y",EAST], AXIS["x",NORTH]
]',
'+proj=cass 
+lat_0=52.41864827777778 
+lon_0=13.62720366666667 
+x_0=40000 
+y_0=10000 
+ellps=bessel 
+datum=potsdam 
+units=m +no_defs')
;