package com.sungsam.smartfridge.data;

import org.springframework.data.repository.CrudRepository;

import com.sungsam.smartfridge.model.ItemType;

public interface ItemTypeRepository extends CrudRepository<ItemType, Long> {

}
