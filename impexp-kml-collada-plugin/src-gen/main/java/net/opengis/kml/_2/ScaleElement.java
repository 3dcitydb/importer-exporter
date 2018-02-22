//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2018.02.22 um 11:14:03 PM CET 
//


package net.opengis.kml._2;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

public class ScaleElement
    extends JAXBElement<ScaleType>
{

    protected final static QName NAME = new QName("http://www.opengis.net/kml/2.2", "Scale");

    public ScaleElement(ScaleType value) {
        super(NAME, ((Class) ScaleType.class), null, value);
    }

    public ScaleElement() {
        super(NAME, ((Class) ScaleType.class), null, null);
    }

}
