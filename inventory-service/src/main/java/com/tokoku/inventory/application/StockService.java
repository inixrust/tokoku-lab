package com.tokoku.inventory.application;

import java.util.List;
import java.util.Map;

import com.tokoku.inventory.domain.exception.StockNotFoundException;
import com.tokoku.inventory.domain.model.ReservationLine;
import com.tokoku.inventory.domain.model.ReservationResult;
import com.tokoku.inventory.domain.model.Stock;
import com.tokoku.inventory.domain.port.out.StockStore;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StockService {

	private final StockStore stocks;

	public StockService(StockStore stocks) {
		this.stocks = stocks;
	}

	public Stock get(long productId) {
		return stocks.findByProductId(productId).orElseThrow(() -> new StockNotFoundException(productId));
	}

	/**
	 * Reservasi semua item atau tidak sama sekali. Ikut transaction pemanggil (listener), sehingga
	 * dedup event, perubahan stok, dan event hasil ke outbox commit bersama.
	 */
	@Transactional
	public ReservationResult reserve(List<ReservationLine> lines) {
		Map<Long, Integer> available = stocks.lockAvailable(lines.stream().map(ReservationLine::productId).toList());
		for (ReservationLine line : lines) {
			int onHand = available.getOrDefault(line.productId(), 0);
			if (onHand < line.quantity()) {
				return new ReservationResult.Rejected(line.productId(), line.quantity(), onHand);
			}
		}
		lines.forEach(line -> stocks.decrement(line.productId(), line.quantity()));
		return new ReservationResult.Reserved();
	}

}
