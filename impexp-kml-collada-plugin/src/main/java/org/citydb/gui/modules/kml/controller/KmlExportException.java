/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citydb.gui.modules.kml.controller;

import org.citydb.config.exception.ApplicationException;
import org.citydb.config.exception.ErrorCode;

public class KmlExportException extends ApplicationException {

	public KmlExportException(ErrorCode errorCode) {
		super(errorCode);
	}

	public KmlExportException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public KmlExportException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}

	public KmlExportException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}

	public KmlExportException() {
		super();
	}

	public KmlExportException(String message) {
		super(message);
	}

	public KmlExportException(String message, Throwable cause) {
		super(message, cause);
	}

	public KmlExportException(Throwable cause) {
		super(cause);
	}
}