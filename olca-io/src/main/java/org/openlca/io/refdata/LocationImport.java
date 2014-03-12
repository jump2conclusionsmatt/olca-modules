package org.openlca.io.refdata;

import org.openlca.core.model.ModelType;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.sql.PreparedStatement;
import java.util.List;

class LocationImport extends AbstractImport {

	@Override
	protected String getStatement() {
		return "insert into tbl_locations (id, ref_id, description, name, " +
				"longitude, latitude, code) values (?, ?, ?, ?, ?, ?, ?)";
	}

	@Override
	protected CellProcessor[] getCellProcessors() {
		CellProcessor notEmpty = new StrNotNullOrEmpty();
		CellProcessor optional = new Optional();
		CellProcessor number = new ParseDouble();
		return new CellProcessor[]{
				notEmpty, // id
				notEmpty, // name
				optional, // description
				notEmpty, // code
				number, // latitude
				number // longitude
		};
	}

	@Override
	protected boolean isValid(List<Object> values) {
		String refId = getString(values, 0);
		if (!seq.isInDatabase(ModelType.LOCATION, refId))
			return true;
		log.info("location {} {} is already in the database", values.get(1),
				values.get(0));
		return false;
	}

	@Override
	protected void setValues(PreparedStatement statement, List<Object> vals)
			throws Exception {
		String refId = getString(vals, 0);
		long id = seq.get(ModelType.LOCATION, refId);
		statement.setLong(1, id);
		statement.setString(2, refId);
		statement.setString(3, getString(vals, 2));
		statement.setString(4, getString(vals, 1));
		statement.setDouble(5, getDouble(vals, 5));
		statement.setDouble(6, getDouble(vals, 4));
		statement.setString(7, getString(vals, 3));
	}
}
