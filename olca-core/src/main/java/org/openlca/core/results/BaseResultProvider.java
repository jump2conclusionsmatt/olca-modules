package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.model.descriptors.CostCategoryDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class BaseResultProvider<T extends BaseResult> implements
		IResultProvider {

	public final T result;
	public final EntityCache cache;

	public BaseResultProvider(T result, EntityCache cache) {
		this.result = result;
		this.cache = cache;
	}

	@Override
	public boolean hasImpactResults() {
		return result.hasImpactResults();
	}

	@Override
	public boolean hasCostResults() {
		return result.hasCostResults();
	}

	@Override
	public Set<ProcessDescriptor> getProcessDescriptors() {
		ProductIndex index = result.productIndex;
		if (index == null)
			return Collections.emptySet();
		Map<Long, ProcessDescriptor> values = cache.getAll(
				ProcessDescriptor.class, index.getProcessIds());
		HashSet<ProcessDescriptor> descriptors = new HashSet<>();
		descriptors.addAll(values.values());
		return descriptors;
	}

	@Override
	public Set<FlowDescriptor> getFlowDescriptors() {
		FlowIndex index = result.flowIndex;
		if (index == null)
			return Collections.emptySet();
		List<Long> ids = new ArrayList<>(index.size());
		for (long id : index.getFlowIds())
			ids.add(id);
		Map<Long, FlowDescriptor> values = cache.getAll(FlowDescriptor.class,
				ids);
		HashSet<FlowDescriptor> descriptors = new HashSet<>();
		descriptors.addAll(values.values());
		return descriptors;
	}

	@Override
	public Set<ImpactCategoryDescriptor> getImpactDescriptors() {
		LongIndex index = result.impactIndex;
		if (index == null)
			return Collections.emptySet();
		List<Long> ids = new ArrayList<>(index.size());
		for (long id : index.getKeys())
			ids.add(id);
		Map<Long, ImpactCategoryDescriptor> values = cache.getAll(
				ImpactCategoryDescriptor.class, ids);
		HashSet<ImpactCategoryDescriptor> descriptors = new HashSet<>();
		descriptors.addAll(values.values());
		return descriptors;
	}

	@Override
	public Set<CostCategoryDescriptor> getCostDescriptors() {
		LongIndex index = result.costIndex;
		if (index == null)
			return Collections.emptySet();
		List<Long> ids = new ArrayList<>();
		boolean hasOther = false;
		for (long id : index.getKeys()) {
			if (id == 0)
				hasOther = true;
			else
				ids.add(id);
		}
		Map<Long, CostCategoryDescriptor> values = cache.getAll(
				CostCategoryDescriptor.class, ids);
		HashSet<CostCategoryDescriptor> descriptors = new HashSet<>();
		descriptors.addAll(values.values());
		if (hasOther) {
			CostCategoryDescriptor other = new CostCategoryDescriptor();
			other.setId(0L);
			other.setName("Other");
			descriptors.add(other);
		}
		return descriptors;
	}

}
