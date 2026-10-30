package com.tokoku.order.infrastructure.adapters.out.persistence;

/**
 * L19.1 langkah 3: slice JPA dengan PostgreSQL asli lewat Testcontainers (slide 100), bukan H2.
 *
 * <p>TODO:
 * <ul>
 * <li>@DataJpaTest + @AutoConfigureTestDatabase(replace = NONE) + container postgres:17-alpine dengan
 * @ServiceConnection.</li>
 * <li>Data uji eksplisit: @Sql("/seed/orders.sql") (order milik cust-7 dan cust-8).</li>
 * <li>findByCustomerId("cust-7") hanya mengembalikan order cust-7; findByIdAndCustomerId menyembunyikan order
 * customer lain.</li>
 * </ul>
 */
class OrderRepositoryTest {

}
