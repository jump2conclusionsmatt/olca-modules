package org.openlca.io.maps;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.MappingFileDao;
import org.openlca.core.model.MappingFile;
import org.openlca.util.BinUtils;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 * A helper class for using import / export maps. We store the mappings in CSV
 * files of a defined format (see the mapping specification for the details:
 * REF_DATA.md). A default file for each mapping should be always directly
 * attached as jar-resource in the package org.openlca.io.maps. Additionally, a
 * mapping file can be stored in the database (so that it can be updated without
 * updating the io-package).
 */
public class Maps {

	// TODO: we will add the other file names as constants as defined in the
	// REF_DATA specification when they are implemented in the respective
	// imports and exports

	/**
	 * Import map for flows in the SimaPro CSV format.
	 */
	public static final String SP_FLOW_IMPORT_MAP = "sp_flow_import_map.csv";

	private Maps() {
	}

	/**
	 * Reads all mappings from the given file using the given cell processors.
	 * It first tries to load the file from the database. If it does not exist
	 * it loads the mappings from the jar-internal resource file.
	 */
	public static List<List<Object>> readAll(String fileName,
			IDatabase database, CellProcessor... cellProcessors)
			throws Exception {
		try (CsvListReader reader = open(fileName, database)) {
			List<List<Object>> results = new ArrayList<>();
			List<Object> nextRow = null;
			while ((nextRow = reader.read(cellProcessors)) != null) {
				results.add(nextRow);
			}
			return results;
		}
	}

	/**
	 * Opens a CSV reader for the given. It first tries to load the file from
	 * the database. If it does not exist it loads the mapping from the
	 * jar-internal resource file.
	 */
	public static CsvListReader open(String fileName, IDatabase database)
			throws Exception {
		CsvListReader reader = fromDatabase(fileName, database);
		if (reader != null)
			return reader;
		else
			return createReader(Maps.class.getResourceAsStream(fileName));
	}

	private static CsvListReader fromDatabase(String fileName,
			IDatabase database) throws Exception {
		if (database == null)
			return null;
		MappingFileDao dao = new MappingFileDao(database);
		MappingFile file = dao.getForFileName(fileName);
		if (file == null || file.getContent() == null)
			return null;
		byte[] bytes = BinUtils.unzip(file.getContent());
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		return createReader(stream);
	}

	private static CsvListReader createReader(InputStream stream)
			throws Exception {
		CsvPreference pref = new CsvPreference.Builder('"', ';', "\n").build();
		// exclude the byte order mark, if there is any
		BOMInputStream bom = new BOMInputStream(stream, false,
				ByteOrderMark.UTF_8);
		InputStreamReader reader = new InputStreamReader(bom, "utf-8");
		BufferedReader buffer = new BufferedReader(reader);
		CsvListReader csvReader = new CsvListReader(buffer, pref);
		return csvReader;
	}

	/**
	 * Stores the given mapping file in the database. If there is already a
	 * mapping file with the given name in the database, this file will be
	 * updated by this method. The given must be the raw CSV stream. The content
	 * of this stream will be compressed before storing it in the database.
	 */
	public static void store(String fileName, InputStream stream,
			IDatabase database) throws Exception {
		MappingFileDao dao = new MappingFileDao(database);
		MappingFile oldFile = dao.getForFileName(fileName);
		if (oldFile != null)
			dao.delete(oldFile);
		byte[] bytes = IOUtils.toByteArray(stream);
		MappingFile file = new MappingFile();
		file.setContent(BinUtils.zip(bytes));
		file.setFileName(fileName);
		dao.insert(file);
	}

	public static String getString(List<Object> values, int i) {
		if (values == null || i >= values.size())
			return null;
		Object val = values.get(i);
		if (val == null)
			return null;
		else
			return val.toString();
	}

	public static Double getOptionalDouble(List<Object> values, int i) {
		if (values == null || i >= values.size())
			return null;
		Object val = values.get(i);
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		else
			return null;
	}

	public static double getDouble(List<Object> values, int i) {
		if (values == null || i >= values.size())
			return 0;
		Object val = values.get(i);
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		else
			return 0;
	}

	public static int getInt(List<Object> values, int i) {
		if (values == null || i >= values.size())
			return 0;
		Object val = values.get(i);
		if (val instanceof Number)
			return ((Number) val).intValue();
		else
			return 0;
	}
}
