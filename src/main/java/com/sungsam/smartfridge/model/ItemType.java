package com.sungsam.smartfridge.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "item_type")
public class ItemType {

    public static final Long MIN_ITEM_TYPE = 0l;
    public static final Long MAX_ITEM_TYPE = Long.valueOf(Long.MAX_VALUE);

    @Id
    @Column(name = "item_type_id", unique = true, nullable = false)
    private Long itemTypeId;

    @Column(name = "name")
    private String name;

    public ItemType(Long itemTypeId, String name) {
        this.itemTypeId = itemTypeId;
        this.name = name;
    }

    public ItemType(Long itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public ItemType() {

    }

    public Long getItemTypeId() {
        return itemTypeId;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

}
