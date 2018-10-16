package com.sungsam.smartfridge.data;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.sungsam.smartfridge.model.FridgeItem;
import com.sungsam.smartfridge.model.ItemType;

public interface FridgeItemRepository extends CrudRepository<FridgeItem, UUID> {

    /**
     * 
     * @param itemId
     * @return The number of rows deleted that match an item's ID... so 1 or 0
     */
    public int deleteByItemId(UUID itemId);

    /**
     * 
     * @param itemType
     * @return All {@link FridgeItem} records of type {@link ItemType}
     */
    public List<FridgeItem> findByItemType(ItemType itemType);

    /**
     * 
     * @param itemType
     * @return The number of {@link FridgeItem} of type {@link ItemType} that were
     *         deleted
     */
    public int deleteByItemType(ItemType itemType);
}
