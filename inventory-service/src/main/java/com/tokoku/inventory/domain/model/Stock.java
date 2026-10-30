package com.tokoku.inventory.domain.model;

public record Stock(long productId, int available) {

	public boolean canReserve(int quantity) {
		return available >= quantity;
	}

}
