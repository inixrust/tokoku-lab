package com.tokoku.order.payment.domain;

import java.util.Optional;

public interface PaymentStore {

	Payment save(Payment payment);

	Optional<Payment> findById(long id);

}
