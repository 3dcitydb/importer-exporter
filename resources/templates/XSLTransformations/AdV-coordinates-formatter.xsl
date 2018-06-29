<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs"
  xmlns:gml="http://www.opengis.net/gml" xmlns:bldg2="http://www.opengis.net/citygml/building/2.0"
  xmlns:bldg1="http://www.opengis.net/citygml/building/1.0" version="1.0">
  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//gml:posList/text()">
    <xsl:if test="not(@srsDimension)">
      <xsl:attribute name="srsDimension">3</xsl:attribute>
    </xsl:if>
    <xsl:call-template name="format-doublelist">
      <xsl:with-param name="input" select="."/>
      <xsl:with-param name="pattern" select="'0.000'"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="//gml:pos/text()">
    <xsl:if test="not(@srsDimension)">
      <xsl:attribute name="srsDimension">3</xsl:attribute>
    </xsl:if>
    <xsl:call-template name="format-doublelist">
      <xsl:with-param name="input" select="."/>
      <xsl:with-param name="pattern" select="'0.000'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="//gml:lowerCorner/text() | //gml:upperCorner/text()">
    <xsl:call-template name="format-doublelist">
      <xsl:with-param name="input" select="."/>
      <xsl:with-param name="pattern" select="'0.000'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="//bldg2:measuredHeight/text() | //bldg1:measuredHeight/text()">
    <xsl:call-template name="format-doublelist">
      <xsl:with-param name="input" select="."/>
      <xsl:with-param name="pattern" select="'0.000'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="format-doublelist">
    <xsl:param name="input"/>
    <xsl:param name="pattern"/>
    <xsl:param name="delim" select="' '"/>

    <xsl:variable name="normalized_input" select="normalize-space($input)"/>
    <xsl:variable name="first" select="substring-before($normalized_input, $delim)"/>
    <xsl:variable name="remaining" select="substring-after($normalized_input, $delim)"/>

    <xsl:choose>
      <xsl:when test="$first != ''">
        <xsl:value-of select="format-number(number($first), $pattern)"/>
        <xsl:text> </xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="format-number(number($normalized_input), $pattern)"/>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:if test="$remaining">
      <xsl:call-template name="format-doublelist">
        <xsl:with-param name="input" select="$remaining"/>
        <xsl:with-param name="pattern" select="$pattern"/>
        <xsl:with-param name="delim" select="$delim"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
