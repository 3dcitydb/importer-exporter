This folder contains template files to be used with the KML/COLLADA
exporter. The templates are HTML files defining different types of
information balloons that can be displayed when the user clicks on an 
exported KML object in a digital virtual globe like Google Earth.

The files contain placeholders referring to database tables and
attributes. They are marked by the elements <3DCityDB></3DCityDB>.
During KML/COLLADA export these placeholders are replaced by the 
values retrieved from the referenced tables and attributes for the 
respective city object.

Please refer to the 3DCityDB documentation for a detailed explanation
of how to use the placeholders. 

List of included files:
-----------------------

BalloonSource_CityObject_template1.html

      A template file that can be used with any CityGML feature type.
      The information balloon displays the gml:id, gml:name, the 
      feature type and some metadata attributes like creation_date
      and lineage.

BalloonSource_CityObject_template2.html

      A template file that can be used with any CityGML feature type.
      The information balloon displays the gml:id and all generic
      attributes of the respective city object.

BalloonSource_Buildings_template1.html

      A template file that can be used with building features only.
      The information balloon displays the gml:id, gml:name, the 
      (first) building address, most of the CityGML building attributes,
      all generic attributes, and all external references of the 
      respective building object. It is also indicated in which LODs
      the building geometry is available.

BalloonSource_Buildings_template2.html

      A template file that can be used with building features only.
      The information balloon displays the gml:id, the (first)
      building address, and the 2D coordinates of the center point
      of the bounding box of the respective building object using WGS84
      latitude/longitude coordinates. Furthermore, two hyperlinks 
      are generated that - when clicked - open Google Maps or
      Open Streetmap, resepctively, with a marker at the building's 
      position.
