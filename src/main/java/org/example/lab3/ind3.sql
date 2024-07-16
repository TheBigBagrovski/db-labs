CREATE OR REPLACE FUNCTION price_increase() RETURNS void AS $$
BEGIN
    UPDATE product
    SET retail_price = retail_price * 1.1
    WHERE product.id IN (
        SELECT product_id FROM cart
                                   INNER JOIN products_in_carts ON products_in_carts.cart_id = cart.id
        GROUP BY product_id
        HAVING AVG(rating) > 4.5
    );
END;
$$ LANGUAGE plpgsql;

SELECT * FROM price_increase();

SELECT product.id, retail_price FROM (
                                         SELECT product_id, AVG(rating) as avg_rating FROM cart
                                                                                               INNER JOIN products_in_carts ON products_in_carts.cart_id = cart.id
                                         GROUP BY product_id
                                         HAVING AVG(rating) > 4.5
                                     ) as products_with_high_rating
                                         INNER JOIN product ON product.id = products_with_high_rating.product_id;