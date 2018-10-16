package com.sungsam.smartfridge.service;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sungsam.smartfridge.model.FridgeItem;

public class FillFactorResult {
	private Long itemType;
	private Double fillFactor;

	public FillFactorResult(Long itemType, Double fillFactor) {
		this.itemType = itemType;
		this.fillFactor = fillFactor;
	}

	public FillFactorResult(FridgeItem fridgeItem) {
		this(fridgeItem.getItemType().getItemTypeId(), fridgeItem.getFillFactor());
	}

	public Long getItemType() {
		return itemType;
	}

	public Double getFillFactor() {
		return fillFactor;
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
