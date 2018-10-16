package com.sungsam.smartfridge.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "fridge_item")
public class FridgeItem {

    public static final Double MAX_FILL_FACTOR = 1.0;
    public static final Double MIN_FILL_FACTOR = 0.0;

    @Id
    @Column(name = "item_id", unique = true, nullable = false)
    private UUID itemId;

    @JoinColumn(name = "type")
    @ManyToOne
    private ItemType itemType;

    @Column(name = "fill_factor")
    private Double fillFactor;

    public FridgeItem(UUID itemId, ItemType type, Double fillFactor) {
        super();
        this.itemId = itemId;
        this.itemType = type;
        this.fillFactor = fillFactor;
    }

    public FridgeItem() {

    }

    public UUID getItemId() {
        return itemId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public Double getFillFactor() {
        return fillFactor;
    }

    public boolean isEmpty() {
        return fillFactor.doubleValue() == 0;
    }

    public boolean lessThanFillFactor(Double testFillFactor) {
        return fillFactor.compareTo(testFillFactor) <= 0;
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
