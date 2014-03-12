package org.openlca.io.refdata;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefDataExport implements Runnable {

	private IDatabase database;
	private File dir;
	private Logger log = LoggerFactory.getLogger(getClass());

	public RefDataExport(File dir, IDatabase database) {
		this.dir = dir;
		this.database = database;
	}

	@Override
	public void run() {
		try {
			if (!dir.exists())
				dir.mkdirs();
			export("locations.csv", new LocationExport());
			export("categories.csv", new CategoryExport());
			export("units.csv", new UnitExport());
			export("unit_groups.csv", new UnitGroupExport());
			export("flow_properties.csv", new FlowPropertyExport());
			export("flows.csv", new FlowExport());
			export("flow_property_factors.csv", new FlowPropertyFactorExport());
			export("lcia_methods.csv", new ImpactMethodExport());
			export("lcia_categories.csv", new ImpactCategoryExport());
			export("lcia_factors.csv", new ImpactFactorExport());
			export("nw_sets.csv", new NwSetExport());
		} catch (Exception e) {
			log.error("Reference data export failed", e);
		}
	}

	private void export(String fileName, AbstractExport export) {
		File file = new File(dir, fileName);
		if (file.exists()) {
			log.warn("the file already exists; did not changed it");
		} else {
			export.run(file, database);
		}
	}
}
