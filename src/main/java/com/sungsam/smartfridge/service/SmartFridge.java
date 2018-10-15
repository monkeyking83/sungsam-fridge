package com.sungsam.smartfridge.service;

import static com.sungsam.smartfridge.model.FridgeItem.MAX_FILL_FACTOR;
import static com.sungsam.smartfridge.model.FridgeItem.MIN_FILL_FACTOR;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sungsam.smartfridge.data.FridgeItemRepository;
import com.sungsam.smartfridge.data.ItemTypeRepository;
import com.sungsam.smartfridge.model.FridgeItem;
import com.sungsam.smartfridge.model.ItemType;

@Component
public class SmartFridge implements SmartFridgeManager {

	private Logger logger = LoggerFactory.getLogger(SmartFridge.class);

	@Autowired
	private ItemTypeRepository itemTypeRepo;

	@Autowired
	private FridgeItemRepository fridgeItemRepo;

	@Override
	@Transactional
	public void handleItemRemoved(String itemUUID) {

		int deleted = fridgeItemRepo.deleteByItemId(UUID.fromString(itemUUID));

		if (deleted == 0) {
			logger.warn(String.format("Attempted to delete item %s, but it does not exist.", itemUUID));
		}

	}

	@Override
	@Transactional
	public void handleItemAdded(long itemType, String itemUUID, String name, Double fillFactor) {

		ItemType type = new ItemType(itemType, name);

		FridgeItem item = new FridgeItem(UUID.fromString(itemUUID), type, fillFactor);

		// Ensure item type exists
		itemTypeRepo.save(type);

		// TODO: error if item is already in the fridge?

		// add/update item
		fridgeItemRepo.save(item);

	}

	/**
	 * My best guess based on the javadocs was to list all found times in each
	 * array, along with an object containing the item type and average fill factor.
	 * If I had my way, this would have been a list of ItemResults objects, each
	 * containing a list of the found items, and the itemType, and the average fill
	 * factor.
	 */
	@Override
	public Object[] getItems(Double fillFactor) {
		if (fillFactor.compareTo(MIN_FILL_FACTOR) <= 0 || fillFactor.compareTo(MAX_FILL_FACTOR) > 0) {
			throw new IllegalArgumentException(String.format(
					"Fill factor must be a value between 0 exclusive, and 1.0 inclusive; the provided value was %s.",
					fillFactor));
		}

		Iterable<FridgeItem> allItems = fridgeItemRepo.findAll();

		Map<ItemType, List<FridgeItem>> byType = StreamSupport.stream(allItems.spliterator(), false)
				.filter(f -> f.lessThanFillFactor(fillFactor))
				.collect(Collectors.groupingBy(FridgeItem::getItemType));

		Object[] results = new Object[byType.size()];
		int index = 0;
		
		for (Map.Entry<ItemType, List<FridgeItem>> itemEntry : byType.entrySet()) {
			List<FridgeItem> items = itemEntry.getValue();
			ItemType itemType = itemEntry.getKey();
			Object[] itemArray = new Object[items.size() + 1];
			items.toArray(itemArray);
			itemArray[items.size()] = new FillFactorResult(itemType.getItemTypeId(), calculateAverageFillFactor(items));
			results[index] = itemArray;
			index++;
		}
		
		return results;
	}

	@Override
	public Double getFillFactor(long itemType) {
		List<FridgeItem> fridgeItems = fridgeItemRepo.findByItemType(new ItemType(itemType));

		if (fridgeItems.size() == 0) {
			logger.warn(String.format("No items were found with item type %s", itemType));
		}
	
		return calculateAverageFillFactor(fridgeItems);

	}

	
	
	@Override
	@Transactional
	public void forgetItem(long itemType) {

		// delete all items of type "itemType"
		ItemType toForget = new ItemType(itemType);
		int deleted = fridgeItemRepo.deleteByItemType(toForget);

		if (deleted > 0) {
			itemTypeRepo.delete(toForget);
		} else {
			logger.warn(String.format("Attempted to forget item %s, but it does not exist", itemType));
		}

	}

	
	private Double calculateAverageFillFactor(List<FridgeItem> fridgeItems) {
		Double fillFactor = Double.valueOf(0);

		int nonEmptyCount = 0;

		for (FridgeItem item : fridgeItems) {
			if (!item.isEmpty()) {
				fillFactor += item.getFillFactor();
				nonEmptyCount++;
			}
		}
		return nonEmptyCount > 0 ? fillFactor / nonEmptyCount : fillFactor;
	}

}
