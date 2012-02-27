/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 *
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.kml.concurrent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;

import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import de.tub.citydb.api.concurrent.BalloonTemplateHandler;
import de.tub.citydb.log.Logger;

public class BalloonTemplateHandlerImpl implements BalloonTemplateHandler {

	public static final String balloonDirectoryName = "balloons";
	public static final String parentFrameStart =
		"<html>\n" +
		"  <body onload=\"resizeFrame(document.getElementById('childframe'))\">\n" +
		"    <script type=\"text/javascript\">\n" +
		"      function resizeFrame(f) {\n" +
		"        f.style.height = (f.contentWindow.document.body.scrollHeight + 20) + \"px\";\n" +
		"        f.style.width = (f.contentWindow.document.body.scrollWidth + 20) + \"px\";\n" +
      	"      }\n" +
      	"    </script>\n" +
      	"    <iframe frameborder=0 border=0 src=\"./" + balloonDirectoryName + "/";

	public static final String parentFrameEnd = ".html\" id=\"childframe\"></iframe>\n" +
		"  </body>\n" +
		"</html>";

	private Connection connection;

	List<BalloonStatement> statementList = null;
	List<String> htmlChunkList = null;

	public BalloonTemplateHandlerImpl(File templateFile, Connection connection) {
		setConnection(connection);
		setTemplate(templateFile);
	}

	public BalloonTemplateHandlerImpl(String templateString, Connection connection) {
		setConnection(connection);
		setTemplate(templateString);
	}

	private void setConnection(Connection connection) {
		this.connection = connection;
	}

	private void setTemplate(File templateFile) {
		statementList = new ArrayList<BalloonStatement>();
		htmlChunkList = new ArrayList<String>();

		if (templateFile == null) return; // it was a dummy call
		
		// read file as String
	    byte[] buffer = new byte[(int)templateFile.length()];
	    FileInputStream f = null;
	    try {
		    f = new FileInputStream(templateFile);
		    f.read(buffer);
	    }
	    catch (FileNotFoundException fnfe) {
			Logger.getInstance().warn("Exception when trying to read file: " + templateFile.getAbsolutePath() + "\nFile not found.");
	    } 
	    catch (Exception e) {
			Logger.getInstance().warn("Exception when trying to read file: " + templateFile.getAbsolutePath() + "\n");
			e.printStackTrace();
	    } 
	    finally {
	        if (f != null) try { f.close(); } catch (Exception ignored) { }
	    }
	    String template = new String(buffer);
		try {
			fillStatementAndHtmlChunkList(template);
		}
		catch (Exception e) {
			Logger.getInstance().warn("Following message applies to file: " + templateFile.getAbsolutePath());
			Logger.getInstance().warn(e.getMessage());
		}
	}
	
	private void setTemplate(String templateString) {
		statementList = new ArrayList<BalloonStatement>();
		htmlChunkList = new ArrayList<String>();

		if (templateString == null) return; // it was a dummy call
		
		try {
			fillStatementAndHtmlChunkList(templateString);
		}
		catch (Exception e) {
			Logger.getInstance().warn(e.getMessage());
		}
	}
	
	public String getBalloonContent(String template, String gmlId, int lod) throws Exception {
		if (connection == null) throw new SQLException("Null or invalid connection");

		String balloonContent = "";
		List<BalloonStatement> statementListBackup = statementList;
		List<String> htmlChunkListBackup = htmlChunkList;
		statementList = new ArrayList<BalloonStatement>();
		htmlChunkList = new ArrayList<String>();
		try {
			fillStatementAndHtmlChunkList(template);
			balloonContent = getBalloonContent(gmlId, lod);
		}
		catch (Exception e) {
			Logger.getInstance().warn("Following message applies to generic attribute 'Balloon_Content' for cityobject with gmlid = " + gmlId);
			Logger.getInstance().warn(e.getMessage());
		}
		statementList = statementListBackup;
		htmlChunkList = htmlChunkListBackup;
		return balloonContent;
	}

	public String getBalloonContent(String gmlId, int lod) throws Exception {
		if (connection == null) throw new SQLException("Null or invalid connection");
		if (statementList == null && htmlChunkList == null) throw new Exception("Invalid template file"); 

		StringBuffer balloonContent = new StringBuffer();
		
		if (statementList != null) {
			List<String> resultList = new ArrayList<String>();
			for (BalloonStatement statement: statementList) {
				resultList.add(executeStatement(statement, gmlId, lod));
			}

			Iterator<String> htmlChunkIterator = htmlChunkList.iterator();
			Iterator<String> resultIterator = resultList.iterator();

			while (htmlChunkIterator.hasNext()) {
				balloonContent.append(htmlChunkIterator.next());
				if (resultIterator.hasNext()) {
					balloonContent.append(resultIterator.next());
				}
			}
		}
		return balloonContent.toString();
	}
	
	private String executeStatement(BalloonStatement statement, String gmlId, int lod) {
		String result = "";
		if (statement != null) {
			PreparedStatement preparedStatement = null;
			OracleResultSet rs = null;
			try {
				if (statement.isForeach()) {
					return executeForeachStatement(statement, gmlId, lod);
				}

				if (statement.isNested()) {
					String rawStatement = statement.getRawStatement();
					List<String> textBetweenNestedStatements = new ArrayList<String>();
					List<BalloonStatement> nestedStatementList = new ArrayList<BalloonStatement>();
					int nestingLevel = 0;
					int lastIndex = 0;
					int index = 0;
					int beginOfSubexpression = 0;

					while (nestingLevel > 0 || rawStatement.indexOf(END_TAG, index) > -1) {
						int indexOfNextStart = rawStatement.indexOf(START_TAG, index);
						int indexOfNextEnd = rawStatement.indexOf(END_TAG, index);
						if (indexOfNextStart != -1 && indexOfNextStart < indexOfNextEnd) {
							nestingLevel++;
							if (nestingLevel == 1) {
								textBetweenNestedStatements.add(rawStatement.substring(lastIndex, indexOfNextStart));
								beginOfSubexpression = indexOfNextStart + START_TAG.length();
							}
							index = indexOfNextStart + START_TAG.length();
						}
						else {
							nestingLevel--;
							index = indexOfNextEnd;
							if (nestingLevel == 0) {
								String originalNestedStatement = rawStatement.substring(beginOfSubexpression, index);
								BalloonStatement nestedStatement = new BalloonStatement(originalNestedStatement);
								nestedStatement.setNested(originalNestedStatement.contains(START_TAG));
								nestedStatementList.add(nestedStatement);
								lastIndex = index + END_TAG.length();
							}
							index = index + END_TAG.length();
						}
					}
					textBetweenNestedStatements.add(rawStatement.substring(index));
					
					StringBuffer notNestedAnymore = new StringBuffer();
					if (nestedStatementList != null) {
						List<String> resultList = new ArrayList<String>();
						for (BalloonStatement nestedStatement: nestedStatementList) {
							resultList.add(executeStatement(nestedStatement, gmlId, lod));
						}

						Iterator<String> textIterator = textBetweenNestedStatements.iterator();
						Iterator<String> resultIterator = resultList.iterator();

						while (textIterator.hasNext()) {
							notNestedAnymore.append(textIterator.next());
							if (resultIterator.hasNext()) {
								notNestedAnymore.append(resultIterator.next());
							}
						}
					}

					BalloonStatement dummy = new BalloonStatement(notNestedAnymore.toString());
					preparedStatement = connection.prepareStatement(dummy.getProperSQLStatement(lod));
				}
				else { // not nested
					if (statement.getProperSQLStatement(lod) == null) {
						// malformed expression between proper START_TAG and END_TAG
						return result; // skip db call, rs and preparedStatement are currently null
					}
					preparedStatement = connection.prepareStatement(statement.getProperSQLStatement(lod));
				}

				for (int i = 1; i <= preparedStatement.getParameterMetaData().getParameterCount(); i++) {
					preparedStatement.setString(i, gmlId);
				}
				rs = (OracleResultSet)preparedStatement.executeQuery();
				while (rs.next()) {
					if (rs.getRow() > 1) {
						result = result + ", ";
					}
					Object object = rs.getObject(1);
					if (object != null) {
						if (object instanceof STRUCT){
							STRUCT buildingGeometryObj = (STRUCT)rs.getObject(1); 
							JGeometry surface = JGeometry.load(buildingGeometryObj);
							int dimensions = surface.getDimensions();
							double[] ordinatesArray = surface.getOrdinatesArray();
							result = result + "(";
							for (int i = 0; i < ordinatesArray.length; i = i + dimensions) {
								for (int j = 0; j < dimensions; j++) {
									result = result + ordinatesArray [i+j];
									if (j < dimensions-1) {
										result = result + ",";
									}
								}
								if (i+dimensions < ordinatesArray.length) {
									result = result + " ";
								}
							}
							result = result + ")";
						}
						else {
							result = result + rs.getObject(1).toString().replaceAll("\"", "&quot;"); // workaround, the JAXB KML marshaler does not escape " properly;
						}
					}
				}
			}
			catch (Exception e) {
				Logger.getInstance().warn(e.getMessage());
//				Logger.getInstance().warn("Exception when executing balloon statement: " + statement + "\n");
//				e.printStackTrace();
			}
			finally {
				try {
					if (rs != null) rs.close();
					if (preparedStatement != null) preparedStatement.close();
				}
				catch (Exception e2) {}
			}
		}
		return result;
	}

	private String executeForeachStatement(BalloonStatement statement, String gmlId, int lod) {
		String resultBody = "";
		PreparedStatement preparedStatement = null;
		OracleResultSet rs = null;
		try {
			if (statement != null && statement.getProperSQLStatement(lod) != null) {
				preparedStatement = connection.prepareStatement(statement.getProperSQLStatement(lod));
				for (int i = 1; i <= preparedStatement.getParameterMetaData().getParameterCount(); i++) {
					preparedStatement.setString(i, gmlId);
				}

				rs = (OracleResultSet)preparedStatement.executeQuery();
				while (rs.next()) {
					String iterationBody = statement.getForeachBody();
					for (int n = 0; n <= statement.getColumnAmount(); n++) {
						String columnValue = "";
						if (n == 0) {
							columnValue = String.valueOf(rs.getRow());
						}
						else {
							Object object = rs.getObject(n);
							if (object != null) {
								if (object instanceof STRUCT){
									STRUCT buildingGeometryObj = (STRUCT)rs.getObject(1); 
									JGeometry surface = JGeometry.load(buildingGeometryObj);
									int dimensions = surface.getDimensions();
									double[] ordinatesArray = surface.getOrdinatesArray();
									columnValue = columnValue + "(";
									for (int i = 0; i < ordinatesArray.length; i = i + dimensions) {
										for (int j = 0; j < dimensions; j++) {
											columnValue = columnValue + ordinatesArray [i+j];
											if (j < dimensions-1) {
												columnValue = columnValue + ",";
											}
										}
										if (i + dimensions < ordinatesArray.length) {
											columnValue = columnValue + " ";
										}
									}
									columnValue = columnValue + ")";
								}
								else {
									columnValue = rs.getObject(n).toString().replaceAll("\"", "&quot;"); // workaround, the JAXB KML marshaler does not escape " properly
								}
							}
						}
						iterationBody = iterationBody.replaceAll("%" + n, columnValue);
					}
					resultBody = resultBody + iterationBody;
				}
			}
		}
		catch (Exception e) {
			Logger.getInstance().warn(e.getMessage());
		}
		finally {
			try {
				if (rs != null) rs.close();
				if (preparedStatement != null) preparedStatement.close();
			}
			catch (Exception e2) {}
		}
		return resultBody;
	}

	private void fillStatementAndHtmlChunkList(String template) throws Exception {
		// parse like it's 1999
	    int lastIndex = 0;
	    int index = 0;
	    while (template.indexOf(START_TAG, lastIndex) != -1) {

			index = template.indexOf(START_TAG, lastIndex);
			int nestingLevel = 1;
			htmlChunkList.add(template.substring(lastIndex, index));
			index = index + START_TAG.length();
			int beginOfExpression = index;
			while (nestingLevel > 0) {
				int indexOfNextStart = template.indexOf(START_TAG, index);
				int indexOfNextEnd = template.indexOf(END_TAG, index);
				if (indexOfNextEnd == -1) {
					throw new Exception("Malformed balloon template. Please review nested " + START_TAG + " expressions.");
				}
				if (indexOfNextStart != -1 && indexOfNextStart < indexOfNextEnd) {
					nestingLevel++;
					index = indexOfNextStart + START_TAG.length();
				}
				else {
					nestingLevel--;
					index = indexOfNextEnd;
					if (nestingLevel == 0) {
						String originalStatement = template.substring(beginOfExpression, index).trim();
						BalloonStatement statement = new BalloonStatement(originalStatement);
						statement.setNested(originalStatement.contains(START_TAG));
						statement.setForeach(originalStatement.toUpperCase().startsWith(FOREACH_TAG));
						if (statement.isForeach()) {
							// look for END FOREACH statement
							index = index + END_TAG.length();
							indexOfNextStart = template.indexOf(START_TAG, index);
							indexOfNextEnd = template.indexOf(END_TAG, index);
							if (indexOfNextStart == -1 || indexOfNextEnd == -1 || indexOfNextEnd < indexOfNextStart) {
								throw new Exception("Malformed balloon template. Please review " + START_TAG + FOREACH_TAG + " expressions.");
							}
							String closingStatement = template.substring(indexOfNextStart + START_TAG.length(), indexOfNextEnd).trim();
							if (!END_FOREACH_TAG.equalsIgnoreCase(closingStatement)) {
								throw new Exception("Malformed balloon template. Please review " + START_TAG + FOREACH_TAG + " expressions.");
							}
							statement.setForeachBody(template.substring(index, indexOfNextStart));
							index = indexOfNextEnd;
						}
						statementList.add(statement);
						lastIndex = index + END_TAG.length();
					}
					index = index + END_TAG.length();
				}
			}
	    }
	    htmlChunkList.add(template.substring(index)); // last chunk
	}



	private class BalloonStatement {
		private String rawStatement;
		private boolean nested = false;
		private String properSQLStatement = null;
		private boolean conversionTried = false;
		
		private int columnAmount;
		private boolean foreach = false;
		private String foreachBody;

		BalloonStatement (String rawStatement) {
			this.setRawStatement(rawStatement);
		}


		public void setRawStatement(String rawStatement) {
			this.rawStatement = rawStatement;
		}


		public String getRawStatement() {
			return rawStatement;
		}


		public void setNested(boolean nested) {
			this.nested = nested;
		}

		public boolean isNested() {
			return nested;
		}

		private void setProperSQLStatement(String properSQLStatement) {
			this.properSQLStatement = properSQLStatement;
		}

		public String getProperSQLStatement(int lod) throws Exception {
			if (!conversionTried && properSQLStatement == null) {
				this.convertStatementToProperSQL(lod);
				conversionTried = true;
			}
			return properSQLStatement;
		}

		public boolean isForeach() {
			return foreach;
		}

		public void setForeach(boolean foreach) {
			this.foreach = foreach;
		}

		public String getForeachBody() {
			return foreachBody;
		}

		public void setForeachBody(String foreachBody) {
			this.foreachBody = foreachBody;
		}

		public int getColumnAmount() {
			return columnAmount;
		}

		public void setColumnAmount(int columnAmount) {
			this.columnAmount = columnAmount;
		}
		
		private void convertStatementToProperSQL(int lod) throws Exception {
			String sqlStatement = null;
			String table = null;
			String aggregateFunction = null;
			List<String> columns = null;
			String condition = null;
			
			int index = rawStatement.indexOf('/');
			if (index == -1) {
				throw new Exception("Invalid statement \"" + rawStatement + "\". Column name not set.");
			}
			
			if (isForeach()) {
				table = rawStatement.substring(FOREACH_TAG.length(), index).trim();
			}
			else {
				table = rawStatement.substring(0, index).trim();
			}
			
			index++;
			if (index >= rawStatement.length()) {
				throw new Exception("Invalid statement \"" + rawStatement + "\". Column name not set.");
			}
			if (rawStatement.charAt(index) == '[') { // beginning of aggregate function
				if (isForeach()) {
					throw new Exception("Invalid statement \"" + rawStatement + "\". No aggregation functions allowed here.");
				}
				index++;
				if (index >= rawStatement.length()) {
					throw new Exception("Invalid statement \"" + rawStatement + "\"");
				}
				if (rawStatement.indexOf(']', index) == -1) {
					throw new Exception("Invalid statement \"" + rawStatement + "\". Missing ']' character.");
				}
				aggregateFunction = rawStatement.substring(index, rawStatement.indexOf(']', index)).trim();
				index = rawStatement.indexOf(']', index) + 1;
				if (index >= rawStatement.length()) {
					throw new Exception("Invalid statement \"" + rawStatement + "\". Column name not set.");
				}
			}

			String columnsClauseString = null;
			if (rawStatement.indexOf('[', index) == -1) { // no condition
				columnsClauseString = rawStatement.substring(index).trim();
			}
			else {
				columnsClauseString = rawStatement.substring(index, rawStatement.indexOf('[', index)).trim();
				index = rawStatement.indexOf('[', index) + 1;
				if (index >= rawStatement.length()) {
					throw new Exception("Invalid statement \"" + rawStatement + "\"");
				}
				if (rawStatement.indexOf(']', index) == -1) {
					throw new Exception("Invalid statement \"" + rawStatement + "\". Missing ']' character.");
				}
				condition = rawStatement.substring(index, rawStatement.indexOf(']', index)).trim();
			}

			if (columnsClauseString == null) {
				throw new Exception("Invalid statement \"" + rawStatement + "\". Column name not set.");
			}
			else {
				columns = new ArrayList<String>();
				StringTokenizer columnTokenizer = new StringTokenizer(columnsClauseString, ",");
				while (columnTokenizer.hasMoreTokens()) {
					columns.add(columnTokenizer.nextToken().toUpperCase().trim());
				}
				setColumnAmount(columns.size());
			}

			String aggregateString = "";
			String aggregateClosingString = "";
			if (aggregateFunction != null) {
				if (MAX.equalsIgnoreCase(aggregateFunction) ||
					MIN.equalsIgnoreCase(aggregateFunction) ||
					AVG.equalsIgnoreCase(aggregateFunction) ||
					COUNT.equalsIgnoreCase(aggregateFunction) ||
					SUM.equalsIgnoreCase(aggregateFunction)) {
					aggregateString = aggregateFunction + "(";
					aggregateClosingString = ")";
				}
				else if (!FIRST.equalsIgnoreCase(aggregateFunction) &&
						 !LAST.equalsIgnoreCase(aggregateFunction)) { 
					throw new Exception("Unsupported aggregate function \"" + aggregateFunction + "\" in statement \"" + rawStatement + "\"");
				}
			}


			String tableShortId;
			boolean orderByColumnAllowed = true;

			if (ADDRESS_TABLE .equalsIgnoreCase(table)) {
				tableShortId = "a";
				orderByColumnAllowed = (!columns.get(0).equals("MULTI_POINT") && !columns.get(0).equals("XAL_SOURCE"));
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, ADDRESS_COLUMNS) + aggregateClosingString +
								   " FROM CITYOBJECT co, ADDRESS_TO_BUILDING a2b, ADDRESS " + tableShortId +
								   " WHERE co.gmlid = ?" +
								   " AND a2b.building_id = co.id" +
								   " AND a.id = a2b.address_id";
			}
			else if (ADDRESS_TO_BUILDING_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "a2b";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, ADDRESS_TO_BUILDING_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, ADDRESS_TO_BUILDING " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND a2b.building_id = co.id";
			}
			else if (APPEAR_TO_SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "a2sd";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, APPEAR_TO_SURFACE_DATA_COLUMNS) + aggregateClosingString +
								   " FROM CITYOBJECT co, APPEARANCE a, APPEAR_TO_SURFACE_DATA " + tableShortId +
								   " WHERE co.gmlid = ?" +
								   " AND a.cityobject_id = co.id" +
								   " AND a2sd.appearance_id = a.id";
			}
			else if (APPEARANCE_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "a";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, APPEARANCE_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, APPEARANCE " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND a.cityobject_id = co.id";
			}
			else if (BUILDING_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "b";
				orderByColumnAllowed = (!columns.get(0).equals("LOD1_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD2_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD3_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD4_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD2_MULTI_CURVE") &&
										!columns.get(0).equals("LOD3_MULTI_CURVE") &&
										!columns.get(0).equals("LOD4_MULTI_CURVE"));
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, BUILDING_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, BUILDING " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND b.id = co.id";
			}
			else if (BUILDING_INSTALLATION_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "bi";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, BUILDING_INSTALLATION_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, BUILDING b, BUILDING_INSTALLATION " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND b.building_root_id = co.id" +
							   " AND bi.building_id = b.id";
			}
	 		else if (CITYMODEL_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "cm";
				orderByColumnAllowed = (!columns.get(0).equals("ENVELOPE"));
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, CITYMODEL_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, CITYOBJECT_MEMBER com, CITYMODEL " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND com.cityobject_id = co.id" +
							   " AND cm.id = com.citymodel_id";
			}
	 		else if (CITYOBJECT_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "co";
				orderByColumnAllowed = (!columns.get(0).equals("ENVELOPE") && !columns.get(0).equals("XML_SOURCE"));
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, CITYOBJECT_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT " + tableShortId +
							   " WHERE co.gmlid = ?";
			}
			else if (CITYOBJECT_GENERICATTRIB_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "coga";
				orderByColumnAllowed = (!columns.get(0).equals("GEOMVAL") && !columns.get(0).equals("BLOBVAL"));
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, CITYOBJECT_GENERICATTRIB_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, CITYOBJECT_GENERICATTRIB " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND coga.cityobject_id = co.id";
			}
			else if (CITYOBJECTGROUP_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "cog";
				orderByColumnAllowed = (!columns.get(0).equals("GEOMETRY"));
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, CITYOBJECTGROUP_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, GROUP_TO_CITYOBJECT g2co, CITYOBJECTGROUP " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND g2co.cityobject_id = co.id" +
							   " AND cog.id = g2co.cityobjectgroup_id";
			}
	 		else if (CITYOBJECT_MEMBER_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "com";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, CITYOBJECT_MEMBER_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, CITYOBJECT_MEMBER " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND com.cityobject_id = co.id";
			}
	 		else if (COLLECT_GEOM_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "cg";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, COLLECT_GEOM_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, COLLECT_GEOM " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND cg.cityobject_id = co.id";
			}
			else if (EXTERNAL_REFERENCE_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "er";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, EXTERNAL_REFERENCE_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, EXTERNAL_REFERENCE " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND er.cityobject_id = co.id";
			}
			else if (GENERALIZATION_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "g";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, GENERALIZATION_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, GENERALIZATION " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND g.cityobject_id = co.id";
			}
			else if (GROUP_TO_CITYOBJECT_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "g2co";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, GROUP_TO_CITYOBJECT_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, GROUP_TO_CITYOBJECT " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND g2co.cityobject_id = co.id";
			}
			else if (OBJECTCLASS_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "oc";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, OBJECTCLASS_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, OBJECTCLASS " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND oc.id = co.class_id";
			}
			else if (OPENING_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "o";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, OPENING_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, BUILDING b, THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE o2ts, OPENING " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND b.building_root_id = co.id" +
							   " AND ts.building_id = b.id" +
							   " AND o2ts.thematic_surface_id = ts.id" +
							   " AND o.id = o2ts.opening_id";
			}
			else if (OPENING_TO_THEM_SURFACE_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "o2ts";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, OPENING_TO_THEM_SURFACE_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, BUILDING b, THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND b.building_root_id = co.id" +
							   " AND ts.building_id = b.id" +
							   " AND o2ts.thematic_surface_id = ts.id";
			}
			else if (ROOM_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "r";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, ROOM_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, BUILDING b, ROOM " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND b.building_root_id = co.id" +
							   " AND r.building_id = b.id";
			}
			else if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "sd";
				orderByColumnAllowed = (!columns.get(0).equals("TEX_IMAGE") && !columns.get(0).equals("GT_REFERENCE_POINT"));
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, SURFACE_DATA_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND a.cityobject_id = co.id" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "sg";
				orderByColumnAllowed = (!columns.get(0).equals("GEOMETRY"));
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, SURFACE_GEOMETRY_COLUMNS) + aggregateClosingString +
							   " FROM SURFACE_GEOMETRY " + tableShortId +
							   " WHERE sg.id IN" +
							   "(SELECT sg.id" +
							   " FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND b.building_root_id = co.id" +
							   " AND sg.root_id = b.lod" + lod + "_geometry_id";
				if (lod > 1) {
					sqlStatement = sqlStatement +
							   " UNION " +
							   "SELECT sg.id" +
							   " FROM CITYOBJECT co, BUILDING b, THEMATIC_SURFACE ts, SURFACE_GEOMETRY " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND b.building_root_id = co.id" +
							   " AND ts.building_id = b.id" + 
							   " AND sg.root_id = ts.lod" + lod + "_multi_surface_id";
				}
				sqlStatement = sqlStatement + ")";
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "tp";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, TEXTUREPARAM_COLUMNS) + aggregateClosingString +
						   	   " FROM TEXTUREPARAM " + tableShortId +
						   	   " WHERE tp.surface_geometry_id IN" +
						   	   " (SELECT sg.id" + 
						   	   " FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY sg" + 
						   	   " WHERE co.gmlid = ?" +
							   " AND b.building_root_id = co.id" +
						   	   " AND sg.root_id = b.lod" + lod + "_geometry_id";
				if (lod > 1) {
					sqlStatement = sqlStatement +
							   " UNION " +
						   	   "SELECT sg.id" + 
						   	   " FROM CITYOBJECT co, BUILDING b, THEMATIC_SURFACE ts, SURFACE_GEOMETRY sg" +
						   	   " WHERE co.gmlid = ?" +
							   " AND b.building_root_id = co.id" +
							   " AND ts.building_id = b.id" + 
						   	   " AND sg.root_id = ts.lod" + lod + "_multi_surface_id";
				}
				sqlStatement = sqlStatement + ")";
			}
			else if (THEMATIC_SURFACE_TABLE.equalsIgnoreCase(table)) {
				tableShortId = "ts";
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(tableShortId, columns, THEMATIC_SURFACE_COLUMNS) + aggregateClosingString +
							   " FROM CITYOBJECT co, BUILDING b, THEMATIC_SURFACE " + tableShortId +
							   " WHERE co.gmlid = ?" +
							   " AND b.building_root_id = co.id" +
							   " AND ts.building_id = b.id";
			}
			else {
				throw new Exception("Unsupported table \"" + table + "\" in statement \"" + rawStatement + "\"");
			}

			if (sqlStatement != null) {
				int rownum = 0;
				if (condition != null) {
					try {
						rownum = Integer.parseInt(condition);
					}
					catch (Exception e) { // not a number, but a logical condition 
						sqlStatement = sqlStatement + " AND " + tableShortId + "." + condition;
					}
				}
				if (orderByColumnAllowed) {
					sqlStatement = sqlStatement + " ORDER by " + tableShortId + "." + columns.get(0);
				}
			
				if (rownum > 0) {
					sqlStatement = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (" + sqlStatement 
								   + " ASC) a WHERE ROWNUM <= " + rownum + ") WHERE rnum >= " + rownum;
				}
				else if (FIRST.equalsIgnoreCase(aggregateFunction)) {
					sqlStatement = "SELECT * FROM (" + sqlStatement + " ASC) WHERE ROWNUM = 1";
				}
				else if (LAST.equalsIgnoreCase(aggregateFunction)) {
					sqlStatement = "SELECT * FROM (" + sqlStatement + " DESC) WHERE ROWNUM = 1";
				} 
			}

			setProperSQLStatement(sqlStatement);
		}
		
		private String getColumnsClause(String tableShortId,
										List<String> statementColumns,
										Set<String> tableColumns) throws Exception {
			String columnsClause = "";
			if (!tableColumns.containsAll(statementColumns)) {
				for (String column: statementColumns) {
					if (!tableColumns.contains(column)) {
						throw new Exception("Unsupported column \"" + column + "\" in statement \"" + rawStatement + "\"");
					}
				}
			}
			else {
//				columnsClause = "(";
				ListIterator<String> statementColumnIterator = statementColumns.listIterator();
				while (statementColumnIterator.hasNext()) {
					columnsClause = columnsClause + tableShortId + "." + statementColumnIterator.next();
					if (statementColumnIterator.hasNext()) {
						columnsClause = columnsClause + ", ";
					}
				}
//				columnsClause = columnsClause + ")";
			}
			return columnsClause;
		}

	}
	
}
