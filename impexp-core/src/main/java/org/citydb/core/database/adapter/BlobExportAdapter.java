/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.database.adapter;

import org.citydb.config.project.database.ExportBatching;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BlobExportAdapter {
	protected final Connection connection;
	private final BlobType blobType;
	private final String schema;
	private final Map<Long, BatchEntry> batches;

	private PreparedStatement psExport;
	private PreparedStatement psBulk;
	private int batchSize;

	public BlobExportAdapter(Connection connection, BlobType blobType, String schema) {
		this.connection = connection;
		this.blobType = blobType;
		this.schema = schema;

		batches = new HashMap<>();
		batchSize = ExportBatching.DEFAULT_BATCH_SIZE;
	}

	public BlobExportAdapter withBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public int addBatch(long id, BatchEntry entry) throws IOException, SQLException {
		batches.put(id, entry);
		return batches.size() == batchSize ? executeBatch() : 0;
	}

	public int executeBatch() throws SQLException, IOException {
		if (batches.isEmpty())
			return 0;

		try {
			if (psBulk == null) {
				psBulk = connection.prepareStatement((blobType == BlobType.TEXTURE_IMAGE ?
						"select id, tex_image_data from " + schema + ".tex_image " :
						"select id, library_object from " + schema + ".implicit_geometry ") +
						"where id in (" + String.join(",", Collections.nCopies(batchSize, "?")) + ")");
			}

			Long[] ids = batches.keySet().toArray(new Long[0]);
			for (int i = 0; i < batchSize; i++)
				psBulk.setLong(i + 1, i < ids.length ? ids[i] : 0);

			int exported = 0;
			try (ResultSet rs = psBulk.executeQuery()) {
				while (rs.next()) {
					BatchEntry entry = batches.get(rs.getLong(1));
					if (entry != null
							&& entry.canWrite.get()
							&& writeToStream(rs.getBytes(2), entry.streamSupplier.get()))
						exported++;
				}
			}

			return exported;
		} finally {
			batches.clear();
		}
	}

	public byte[] getInByteArray(long id) throws SQLException {
		if (psExport == null) {
			psExport = connection.prepareStatement((blobType == BlobType.TEXTURE_IMAGE ?
					"select tex_image_data from " + schema + ".tex_image " :
					"select library_object from " + schema + ".implicit_geometry ") +
					"where id = ?");
		}

		psExport.setLong(1, id);
		try (ResultSet rs = psExport.executeQuery()) {
			return rs.next() ? rs.getBytes(1) : null;
		}
	}

	public boolean writeToFile(long id, String fileName) throws SQLException, IOException {
        return writeToStream(getInByteArray(id), Files.newOutputStream(Paths.get(fileName)));
	}

	public boolean writeToStream(long id, OutputStream stream) throws SQLException, IOException {
	    return writeToStream(getInByteArray(id), stream);
	}

	private boolean writeToStream(byte[] buffer, OutputStream stream) throws IOException {
	    if (buffer != null && buffer.length != 0) {
	        try (OutputStream out = stream) {
                out.write(buffer);
                return true;
            }
        } else
            return false;
    }

	public void close() throws SQLException {
		if (psExport != null)
			psExport.close();

		if (psBulk != null)
			psBulk.close();
	}

	@FunctionalInterface
	public interface OutputStreamSupplier {
		OutputStream get() throws IOException;
	}

	public static class BatchEntry {
		private final OutputStreamSupplier streamSupplier;
		private final Supplier<Boolean> canWrite;

		public BatchEntry(OutputStreamSupplier streamSupplier, Supplier<Boolean> canWrite) {
			this.streamSupplier = streamSupplier;
			this.canWrite = canWrite;
		}
	}
}
