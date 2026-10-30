package com.tokoku.inventory.domain.model;

/** Hasil reservasi stok untuk satu order. Sealed: switch tanpa default wajib menangani semua kasus. */
public sealed interface ReservationResult {

	record Reserved() implements ReservationResult {
	}

	record Rejected(long productId, int requested, int available) implements ReservationResult {
	}

}
