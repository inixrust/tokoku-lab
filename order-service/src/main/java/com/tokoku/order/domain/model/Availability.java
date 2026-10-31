package com.tokoku.order.domain.model;

public enum Availability {

	AVAILABLE,

	INSUFFICIENT,

	/** Inventory tidak bisa ditanya (dipakai sebagai fallback di L10.1). */
	UNKNOWN

}
