SELECT first_name, last_name, discount_card, customer_sum FROM customer
                                                                   INNER JOIN (
    SELECT customer.id, SUM(products_sum_in_cart) as customer_sum FROM customer
                                                                           INNER JOIN cart ON cart.customer_id = customer.id
                                                                           INNER JOIN (
        SELECT cart.id, SUM(retail_price * p_in_c.quantity) as products_sum_in_cart FROM cart
                                                                                             INNER JOIN products_in_carts as p_in_c ON p_in_c.cart_id = cart.id
                                                                                             INNER JOIN product ON product.id = p_in_c.product_id
        GROUP BY cart.id
    ) carts_sum ON carts_sum.id=cart.id
    GROUP BY customer.id
) customers_sum ON customers_sum.id = customer.id
                                                                   INNER JOIN personal_data ON personal_data.id = customer.personal_data_id
ORDER BY customer_sum DESC;