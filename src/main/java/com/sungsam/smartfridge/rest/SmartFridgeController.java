package com.sungsam.smartfridge.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sungsam.smartfridge.model.FridgeItem;
import com.sungsam.smartfridge.service.SmartFridgeManager;

@RestController
@RequestMapping("smart-fridge")
public class SmartFridgeController {

	@Autowired
	private SmartFridgeManager fridgeManager;

	@PostMapping("/items")
	public void handleItemAdded(@RequestBody FridgeItem fridgeItem) {
		fridgeManager.handleItemAdded(fridgeItem.getItemType().getItemTypeId(), fridgeItem.getItemId().toString(),
				fridgeItem.getItemType().getName(), fridgeItem.getFillFactor());
	}

	@DeleteMapping("/items/{itemUUID}")
	public void handleItemRemoved(@PathVariable("itemUUID") String itemUuid) {
		fridgeManager.handleItemRemoved(itemUuid);
	}

	@GetMapping("/items")
	public Object[] getItems(@RequestParam(name = "fillFactor", required = true) Double fillFactor) {

		return fridgeManager.getItems(fillFactor);
	}

	@GetMapping("/item-types/{itemType}")
	public Double getFillFactor(@PathVariable("itemType") Long itemType) {
		return fridgeManager.getFillFactor(itemType);
	}

	@DeleteMapping("/item-types/{itemType}")
	public void forgetItem(@PathVariable("itemType") Long itemType) {
		fridgeManager.forgetItem(itemType);
	}

}
