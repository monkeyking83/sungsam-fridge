package com.sungsam.smartfridge.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.sungsam.smartfridge.data.FridgeItemRepository;
import com.sungsam.smartfridge.data.ItemTypeRepository;
import com.sungsam.smartfridge.model.FridgeItem;
import com.sungsam.smartfridge.model.ItemType;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SmartFridgeManagerTest {

	
	private final static List<String> typeNames = Arrays.asList("1-Eggs", "2-Bacon", "3-Tofurkey", "4-Eggless Mayo", "5-Wonder Bread");

	private final static Map<String, ItemType> typesByName = typeNames.stream().collect(
			Collectors.toMap(s -> s.split("-")[1], s -> new ItemType(Long.valueOf(s.split("-")[0]), s.split("-")[1])));
	
	private Logger logger = LoggerFactory.getLogger(SmartFridgeManagerTest.class);


	
	private final FridgeItem eggs = new FridgeItem(UUID.fromString("77c82f1f-be67-43e2-88e8-d1b436335005"), typesByName.get("Eggs"), Double.valueOf(0.2d));
	private final FridgeItem bacon = new FridgeItem(UUID.fromString("ff5baa9a-6e33-4855-84ce-a5617304e8b6"), typesByName.get("Bacon"), Double.valueOf(0.2d));
	private final FridgeItem moreBacon = new FridgeItem(UUID.fromString("a65bab9a-6e83-4f55-94ce-a5617a04e8b6"), typesByName.get("Bacon"), Double.valueOf(0.6d));
	private final FridgeItem tofurkey = new FridgeItem(UUID.fromString("2ac4fe56-d1d7-4448-833c-837dfd52ef01"), typesByName.get("Tofurkey"), Double.valueOf(0.8d));
	private final FridgeItem egglessMayo = new FridgeItem(UUID.fromString("7946f450-7444-4991-bc98-190f2554587e"), typesByName.get("Eggless Mayo"), Double.valueOf(1.0d));
	private final FridgeItem wonderBread = new FridgeItem(UUID.fromString("934426a4-11fa-468f-be2c-806489bb1b18"), typesByName.get("Wonder Bread"), Double.valueOf(0.2d));

	

	@Autowired
	private SmartFridge smartFridgeManager;
	
	@Autowired
	private FridgeItemRepository fridgeItemRepo;
	
	@Autowired
	private ItemTypeRepository itemTypeRepo;
	
	
	@Test
	public void testAddItem() {


		handleItemsAdded(eggs, bacon, tofurkey);

		assertEquals(3, itemTypeRepo.count());
		assertEquals(3, fridgeItemRepo.count());
		assertEquals(eggs, fridgeItemRepo.findById(eggs.getItemId()).get());
		assertEquals(bacon, fridgeItemRepo.findById(bacon.getItemId()).get());
		assertEquals(tofurkey, fridgeItemRepo.findById(tofurkey.getItemId()).get());

		// ensure that adding more bacon results in extra bacon, but not in extra types of bacon
		handleItemAdded(moreBacon);
		assertEquals(moreBacon, fridgeItemRepo.findById(moreBacon.getItemId()).get());
		assertEquals(4, fridgeItemRepo.count());
		assertEquals(3, itemTypeRepo.count());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveItem() {
		
		handleItemAdded(egglessMayo);
		
		smartFridgeManager.handleItemRemoved(egglessMayo.getItemId().toString());
		
		assertFalse(fridgeItemRepo.findById(egglessMayo.getItemId()).isPresent());
		assertTrue(itemTypeRepo.findById(egglessMayo.getItemType().getItemTypeId()).isPresent());

		// ensure that there are no errors when removing something that's already been removed
		smartFridgeManager.handleItemRemoved(egglessMayo.getItemId().toString());
		
		smartFridgeManager.handleItemRemoved("perfectly valid UUID");
		
	}

	
	@Test
	public void testForgetItem() {
		
		handleItemsAdded(eggs, bacon, moreBacon, tofurkey, egglessMayo, wonderBread);
		
		smartFridgeManager.forgetItem(bacon.getItemType().getItemTypeId());
		
		assertEquals(4, itemTypeRepo.count());
		assertEquals(4, fridgeItemRepo.count());

		// try with an invalid number; should have no impact
		smartFridgeManager.forgetItem(-5l);
		assertEquals(4, itemTypeRepo.count());
		assertEquals(4, fridgeItemRepo.count());

		// make sure we can re-add the bacon
		handleItemAdded(bacon);
		assertEquals(5, itemTypeRepo.count());
		assertEquals(5, fridgeItemRepo.count());
		assertEquals(bacon, fridgeItemRepo.findById(bacon.getItemId()).get());
		
		
	}

	
	@Test
	public void testGetFillFactor() {
		
		handleItemsAdded(bacon, moreBacon, eggs);
		
		assertEquals(Double.valueOf(0.4), smartFridgeManager.getFillFactor(bacon.getItemType().getItemTypeId()));
		
		handleItemAdded(new FridgeItem(randomUUID(), bacon.getItemType(), Double.valueOf(0.1)));

		assertEquals(Double.valueOf(0.3), smartFridgeManager.getFillFactor(bacon.getItemType().getItemTypeId()));

		smartFridgeManager.handleItemRemoved(bacon.getItemId().toString());
		smartFridgeManager.handleItemRemoved(moreBacon.getItemId().toString());
		
		assertEquals(Double.valueOf(0.1), smartFridgeManager.getFillFactor(bacon.getItemType().getItemTypeId()));

		// we put an empty package of bacon back in the fridge
		handleItemAdded(new FridgeItem(randomUUID(), bacon.getItemType(), Double.valueOf(0.0)));

		assertEquals(Double.valueOf(0.1), smartFridgeManager.getFillFactor(bacon.getItemType().getItemTypeId()));

		
		assertEquals(Double.valueOf(0.0d),smartFridgeManager.getFillFactor(-1));

		
		smartFridgeManager.handleItemRemoved(eggs.getItemId().toString());
		assertEquals(Double.valueOf(0.0), smartFridgeManager.getFillFactor(eggs.getItemType().getItemTypeId()));
		
	}

	@Test
	public void testGetItems() {
		handleItemsAdded(eggs, bacon, moreBacon, tofurkey, egglessMayo, wonderBread);

		smartFridgeManager.getItems(1.0d);

		smartFridgeManager.getItems(0.3d);
		
		smartFridgeManager.handleItemRemoved(moreBacon.getItemId().toString());
		handleItemAdded(new FridgeItem(moreBacon.getItemId(), moreBacon.getItemType(), 0.0d));
		
		
		smartFridgeManager.getItems(0.3d);
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetItemsOverMax() {
		smartFridgeManager.getItems(1.0001d);
	}

	
	@Test(expected = IllegalArgumentException.class)
	public void testGetItemUnderMin() {
		smartFridgeManager.getItems(0.0);
	}

	
	private void handleItemsAdded(FridgeItem... items) {
		for (FridgeItem item : items) {
			handleItemAdded(item);
		}
	}
	
	private void handleItemAdded(FridgeItem item) {
		smartFridgeManager.handleItemAdded(item.getItemType().getItemTypeId(), item.getItemId().toString(),
				item.getItemType().getName(), item.getFillFactor());
	}
	
	
}
