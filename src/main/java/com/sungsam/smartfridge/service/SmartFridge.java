package com.sungsam.smartfridge.service;

import static com.sungsam.smartfridge.model.FridgeItem.MAX_FILL_FACTOR;
import static com.sungsam.smartfridge.model.FridgeItem.MIN_FILL_FACTOR;
import static com.sungsam.smartfridge.model.ItemType.MAX_ITEM_TYPE;
import static com.sungsam.smartfridge.model.ItemType.MIN_ITEM_TYPE;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.persistence.EntityExistsException;
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

        int deleted = fridgeItemRepo.deleteByItemId(toUuid(itemUUID));

        if (deleted == 0) {
            logger.warn(String.format("Attempted to delete item %s, but it does not exist.", itemUUID));
        }
    }

    @Override
    @Transactional
    public void handleItemAdded(long itemType, String itemUUID, String name, Double fillFactor) {

        validateItem(itemUUID, itemType, name, fillFactor);

        ItemType type = new ItemType(itemType, name);
        UUID itemId = toUuid(itemUUID);
        FridgeItem item = new FridgeItem(itemId, type, fillFactor);

        if (fridgeItemRepo.findById(itemId).isPresent()) {
            // We follow the laws of newtonian physics in this refrigerator... two of the
            // same exact object cannot occupy the same spot.

            throw new EntityExistsException(String.format(
                    "Fridge item with uuid %s already exists. It must first be removed before being re-added", itemId));
        }

        itemTypeRepo.save(type);

        fridgeItemRepo.save(item);

    }

    @Override
    public Object[] getItems(Double fillFactor) {

        if (fillFactor == null || fillFactor.compareTo(MIN_FILL_FACTOR) <= 0
                || fillFactor.compareTo(MAX_FILL_FACTOR) > 0) {
            logger.warn(String.format(
                    "An invalid fillFactor was provided: %s; valid values are between 0 exclusive and 1 inclusive.",
                    fillFactor));
            return new Object[0];
        }

        Iterable<FridgeItem> allItems = fridgeItemRepo.findAll();

        Map<Long, List<FillFactorResult>> byType = StreamSupport.stream(allItems.spliterator(), false)
                .filter(f -> f.lessThanFillFactor(fillFactor)).map(f -> new FillFactorResult(f))
                .collect(Collectors.groupingBy(FillFactorResult::getItemType));

        Object[] results = new Object[byType.size()];

        int i = 0;
        for (List<FillFactorResult> fillFactors : byType.values()) {
            results[i] = fillFactors.toArray();
            i++;
        }

        return results;
    }

    @Override
    public Double getFillFactor(long itemType) {
        List<FridgeItem> fridgeItems = fridgeItemRepo.findByItemType(new ItemType(itemType));

        if (fridgeItems.size() == 0) {
            logger.warn(String.format("No items were found with item type %s", itemType));
            return Double.valueOf(0);
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

    private UUID toUuid(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidItemIdException(e, uuid);
        }
    }

    /**
     * 
     * Validation methods. A potential improvement here is to use javax.validation
     * annotations. This ultimately throws an Exception if anything was found to
     * have failed.
     */

    private void validateItem(String uuid, long itemType, String itemName, Double fillFactor) {
        StringBuilder validationMessages = new StringBuilder();

        checkNull("Item Name", itemName, validationMessages);
        checkNull("Fill Factor", fillFactor, validationMessages);
        checkNull("Item UUID", uuid, validationMessages);

        if (Long.valueOf(itemType).compareTo(MIN_ITEM_TYPE) <= 0
                || Long.valueOf(itemType).compareTo(MAX_ITEM_TYPE) > 0) {
            validationMessages.append(String.format("Item Type must be a value between %s and %s, and not %s.",
                    MIN_ITEM_TYPE, MAX_ITEM_TYPE, itemType));
        }

        if (fillFactor != null) {
            if (fillFactor.compareTo(MIN_FILL_FACTOR) < 0 || fillFactor.compareTo(MAX_FILL_FACTOR) > 0) {
                validationMessages.append(String.format("Fill Factor must be a value between %s and %s, and not %s.",
                        MIN_FILL_FACTOR, MAX_FILL_FACTOR, fillFactor));
            }
        }
        
        if (validationMessages.length() > 0) {
            throw new ItemValidationException(validationMessages.toString());
        }
    }

    private void checkNull(String fieldName, Object value, StringBuilder validationMessages) {
        if (value == null || value.toString().trim().length() == 0) {
            validationMessages.append(String.format("%s cannot be empty.\n", fieldName));
        }

    }

}
