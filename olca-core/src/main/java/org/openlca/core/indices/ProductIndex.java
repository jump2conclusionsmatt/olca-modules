package org.openlca.core.indices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A product index is used to map process products to rows and columns in
 * matrices. A process product is represented by a {@link LongPair}, where the
 * first entry is the ID of the process and the second ID the ID of the
 * respective product flow.
 */
public class ProductIndex {

	/** Maps the process-products to an ordinal index. */
	private final HashMap<LongPair, Integer> productIndex = new HashMap<>();

	/**
	 * The products in this index. The position in this list must match the
	 * respective index value of a product.
	 */
	private final ArrayList<LongPair> products = new ArrayList<>();

	/** Maps an input product (=key) to an output product (=value, =provider). */
	private final HashMap<LongPair, LongPair> productLinks = new HashMap<>();

	/** Maps a process ID to the output products of this process. */
	private final HashMap<Long, List<LongPair>> processProducts = new HashMap<>();

	private LongPair refProduct;

	public ProductIndex(LongPair refProduct) {
		this.refProduct = refProduct;
		put(refProduct);
	}

	public LongPair getRefProduct() {
		return refProduct;
	}

	/**
	 * Returns the number of products in the index. This is equal to the number
	 * of rows and columns in a technology matrix with this index.
	 */
	public int size() {
		return productIndex.size();
	}

	/**
	 * Returns the ordinal index of the given process product.
	 */
	public int getIndex(LongPair product) {
		Integer idx = productIndex.get(product);
		if (idx == null)
			return -1;
		return idx;
	}

	/**
	 * Returns true if the given product is contained in this index.
	 */
	public boolean contains(LongPair product) {
		return productIndex.containsKey(product);
	}

	/**
	 * Adds the given product to this index. Does nothing if the given product
	 * is already contained in this index.
	 */
	public void put(LongPair product) {
		if (contains(product))
			return;
		int idx = productIndex.size();
		productIndex.put(product, idx);
		List<LongPair> list = processProducts.get(product.getFirst());
		if (list == null) {
			list = new ArrayList<>();
			processProducts.put(product.getFirst(), list);
		}
		list.add(product);
		products.add(product);
	}

	/**
	 * Returns the process product at the given index.
	 */
	public LongPair getProductAt(int index) {
		return products.get(index);
	}

	/**
	 * Returns the products for the process with the given ID.
	 */
	public List<LongPair> getProducts(long processId) {
		List<LongPair> products = processProducts.get(processId);
		if (products == null)
			return Collections.emptyList();
		return new ArrayList<>(products);
	}

	/**
	 * Adds a link between the given products to this index. The output product
	 * is added to the index if it is not yet contained. NOTE: that the input is
	 * not part of the index!
	 */
	public void putLink(LongPair input, LongPair output) {
		put(output);
		productLinks.put(input, output);
	}

	/**
	 * Returns true if the given product is an input that is linked with another
	 * product (which is an output) in this index.
	 */
	public boolean isLinkedInput(LongPair product) {
		return productLinks.containsKey(product);
	}

	/**
	 * Returns the output product (provider) to which the given product input is
	 * linked, or null if there is no such product.
	 */
	public LongPair getLinkedOutput(LongPair product) {
		return productLinks.get(product);
	}

}
