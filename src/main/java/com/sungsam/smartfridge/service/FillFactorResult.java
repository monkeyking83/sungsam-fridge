package com.sungsam.smartfridge.service;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class FillFactorResult {
	private Long itemType;
	private Double fillFactor;

	public FillFactorResult(Long itemType, Double fillFactor) {
		super();
		this.itemType = itemType;
		this.fillFactor = fillFactor;
	}

	public Long getItemType() {
		return itemType;
	}

	public void setItemType(Long itemType) {
		this.itemType = itemType;
	}

	public Double getFillFactor() {
		return fillFactor;
	}

	public void setFillFactor(Double fillFactor) {
		this.fillFactor = fillFactor;
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
