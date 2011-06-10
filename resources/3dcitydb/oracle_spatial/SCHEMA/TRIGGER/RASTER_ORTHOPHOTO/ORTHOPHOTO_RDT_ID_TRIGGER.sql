-- ORTHOPHOTO_RDT_ID_TRIGGER.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--              Dr. Andreas Poth <poth@lat-lon.de>
--
-- Copyright:   (c) 2007,      Institute for Geodesy and Geoinformation Science,
--                             Technische Universit�t Berlin, Germany
--                             http://www.igg.tu-berlin.de
--              (c) 2004-2006, Institute for Cartography and Geoinformation,
--                             Universit�t Bonn, Germany
--                             http://www.ikg.uni-bonn.de
--              (c) 2005-2006, lat/lon GmbH, Germany
--                             http://www.lat-lon.de
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
-- Version | Date       | Description                               | Author
-- 2.0       2007-12-10   release version                             TKol
--                                                                    LPlu
--                                                                    GGro
--                                                                    JSch
--                                                                    VStr
--                                                                    APot
--

drop trigger ORTHO_RDT_TRG;


create trigger ORTHO_RDT_TRG
before insert on ORTHOPHOTO_RDT
for each row
begin
	if :new.id is null then
		select ORTHOPHOTO_RDT_SEQ.nextval into :new.id from dual;
	end if;
end;
/

