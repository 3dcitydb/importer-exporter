/*
 * This file is part of citygml4j.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universität Berlin, Germany
 * http://www.igg.tu-berlin.de/
 *
 * The citygml4j library is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see 
 * <http://www.gnu.org/licenses/>.
 */

package org.citydb.query.builder;

public class QueryBuildException extends Exception {
	private static final long serialVersionUID = -3716015045363231263L;
	
	public QueryBuildException() {
		super();
	}
	
	public QueryBuildException(String message) {
		super(message);
	}
	
	public QueryBuildException(Throwable cause) {
		super(cause);
	}
	
	public QueryBuildException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
