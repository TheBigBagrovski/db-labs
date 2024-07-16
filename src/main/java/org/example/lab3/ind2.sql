DROP TABLE analytics;

CREATE TABLE analytics (
                           month_number INTEGER NOT NULL PRIMARY KEY,
                           income NUMERIC(14, 2),
                           products_purchase NUMERIC(14, 2),
                           stores_rent NUMERIC(14, 2),
                           warehouses_rent NUMERIC(14, 2),
                           profit NUMERIC(14, 2)
);

INSERT INTO analytics (month_number, income, products_purchase, stores_rent, warehouses_rent, profit)
SELECT i, 0.0, 0.0, 0.0, 0.0, 0.0 FROM generate_series(1, 12) AS i;

UPDATE analytics
SET income = month_sums_of_orders.month_sum
FROM (
         SELECT order_month, SUM(products_sum_in_cart) as month_sum FROM (
                                                                             SELECT products_sum_in_cart, EXTRACT(MONTH FROM order_date) as order_month FROM cart
                                                                                                                                                                 INNER JOIN (
                                                                                 SELECT cart.id, SUM(retail_price * p_in_c.quantity) as products_sum_in_cart FROM cart
                                                                                                                                                                      INNER JOIN products_in_carts as p_in_c ON p_in_c.cart_id = cart.id
                                                                                                                                                                      INNER JOIN product ON product.id = p_in_c.product_id
                                                                                 GROUP BY cart.id
                                                                             ) carts_sum ON carts_sum.id=cart.id
                                                                             WHERE order_date BETWEEN '2021-01-01' AND '2021-12-31'
                                                                         ) q GROUP BY order_month
     ) as month_sums_of_orders
WHERE month_number = order_month;

UPDATE analytics
SET products_purchase = month_purchases_for_stores.stores_sums
FROM (
         SELECT EXTRACT(MONTH FROM p_in_s.purchase_date) as purchase_month,
                SUM(purchase_price * p_in_s.quantity) as stores_sums FROM store
                                                                              INNER JOIN products_in_stores as p_in_s ON p_in_s.store_id = store.id
                                                                              INNER JOIN product ON product.id = p_in_s.product_id
         WHERE p_in_s.purchase_date BETWEEN '2021-01-01' AND '2021-12-31'
         GROUP BY purchase_month
     ) as month_purchases_for_stores
WHERE month_number = purchase_month;

UPDATE analytics
SET products_purchase = products_purchase + month_purchases_for_warehouses.warehouses_sums
FROM (
         SELECT EXTRACT(MONTH FROM p_in_w.purchase_date) as purchase_month,
                SUM(purchase_price * p_in_w.quantity) as warehouses_sums FROM warehouse
                                                                                  INNER JOIN products_in_warehouses as p_in_w ON p_in_w.warehouse_id = warehouse.id
                                                                                  INNER JOIN product ON product.id = p_in_w.product_id
         WHERE p_in_w.purchase_date BETWEEN '2021-01-01' AND '2021-12-31'
         GROUP BY purchase_month
     ) as month_purchases_for_warehouses
WHERE month_number = purchase_month;

UPDATE analytics
SET stores_rent = (SELECT SUM(rental_price) FROM store
                   WHERE rental_start_date <= '2021-01-31' AND rental_end_date >= '2021-01-01')
WHERE month_number = 1;

UPDATE analytics
SET stores_rent = (SELECT SUM(rental_price) FROM store
                   WHERE rental_start_date <= '2021-02-28' AND rental_end_date >= '2021-02-01')
WHERE month_number = 2;

UPDATE analytics
SET stores_rent = (SELECT SUM(rental_price) FROM store
                   WHERE rental_start_date <= '2021-03-31' AND rental_end_date >= '2021-03-01')
WHERE month_number = 3;

UPDATE analytics
SET stores_rent = (SELECT SUM(rental_price) FROM store
                   WHERE rental_start_date <= '2021-04-30' AND rental_end_date >= '2021-04-01')
WHERE month_number = 4;

UPDATE analytics
SET stores_rent = (SELECT SUM(rental_price) FROM store
                   WHERE rental_start_date <= '2021-05-31' AND rental_end_date >= '2021-05-01')
WHERE month_number = 5;

UPDATE analytics
SET stores_rent = (SELECT SUM(rental_price) FROM store
                   WHERE rental_start_date <= '2021-06-30' AND rental_end_date >= '2021-06-01')
WHERE month_number = 6;

UPDATE analytics
SET stores_rent = (SELECT SUM(rental_price) FROM store
                   WHERE rental_start_date <= '2021-07-31' AND rental_end_date >= '2021-07-01')
WHERE month_number = 7;

UPDATE analytics
SET stores_rent = (SELECT SUM(rental_price) FROM store
                   WHERE rental_start_date <= '2021-08-31' AND rental_end_date >= '2021-08-01')
WHERE month_number = 8;

UPDATE analytics
SET stores_rent = (SELECT SUM(rental_price) FROM store
                   WHERE rental_start_date <= '2021-09-30' AND rental_end_date >= '2021-09-01')
WHERE month_number = 9;

UPDATE analytics
SET stores_rent = (SELECT SUM(rental_price) FROM store
                   WHERE rental_start_date <= '2021-10-31' AND rental_end_date >= '2021-10-01')
WHERE month_number = 10;

UPDATE analytics
SET stores_rent = (SELECT SUM(rental_price) FROM store
                   WHERE rental_start_date <= '2021-11-30' AND rental_end_date >= '2021-11-01')
WHERE month_number = 11;

UPDATE analytics
SET stores_rent = (SELECT SUM(rental_price) FROM store
                   WHERE rental_start_date <= '2021-12-31' AND rental_end_date >= '2021-12-01')
WHERE month_number = 12;

UPDATE analytics
SET warehouses_rent = (SELECT SUM(rental_price) FROM warehouse
                       WHERE rental_start_date <= '2021-01-31' AND rental_end_date >= '2021-01-01')
WHERE month_number = 1;

UPDATE analytics
SET warehouses_rent = (SELECT SUM(rental_price) FROM warehouse
                       WHERE rental_start_date <= '2021-02-28' AND rental_end_date >= '2021-02-01')
WHERE month_number = 2;

UPDATE analytics
SET warehouses_rent = (SELECT SUM(rental_price) FROM warehouse
                       WHERE rental_start_date <= '2021-03-31' AND rental_end_date >= '2021-03-01')
WHERE month_number = 3;

UPDATE analytics
SET warehouses_rent = (SELECT SUM(rental_price) FROM warehouse
                       WHERE rental_start_date <= '2021-04-30' AND rental_end_date >= '2021-04-01')
WHERE month_number = 4;

UPDATE analytics
SET warehouses_rent = (SELECT SUM(rental_price) FROM warehouse
                       WHERE rental_start_date <= '2021-05-31' AND rental_end_date >= '2021-05-01')
WHERE month_number = 5;

UPDATE analytics
SET warehouses_rent = (SELECT SUM(rental_price) FROM warehouse
                       WHERE rental_start_date <= '2021-06-30' AND rental_end_date >= '2021-06-01')
WHERE month_number = 6;

UPDATE analytics
SET warehouses_rent = (SELECT SUM(rental_price) FROM warehouse
                       WHERE rental_start_date <= '2021-07-31' AND rental_end_date >= '2021-07-01')
WHERE month_number = 7;

UPDATE analytics
SET warehouses_rent = (SELECT SUM(rental_price) FROM warehouse
                       WHERE rental_start_date <= '2021-08-31' AND rental_end_date >= '2021-08-01')
WHERE month_number = 8;

UPDATE analytics
SET warehouses_rent = (SELECT SUM(rental_price) FROM warehouse
                       WHERE rental_start_date <= '2021-09-30' AND rental_end_date >= '2021-09-01')
WHERE month_number = 9;

UPDATE analytics
SET warehouses_rent = (SELECT SUM(rental_price) FROM warehouse
                       WHERE rental_start_date <= '2021-10-31' AND rental_end_date >= '2021-10-01')
WHERE month_number = 10;

UPDATE analytics
SET warehouses_rent = (SELECT SUM(rental_price) FROM warehouse
                       WHERE rental_start_date <= '2021-11-30' AND rental_end_date >= '2021-11-01')
WHERE month_number = 11;

UPDATE analytics
SET warehouses_rent = (SELECT SUM(rental_price) FROM warehouse
                       WHERE rental_start_date <= '2021-12-31' AND rental_end_date >= '2021-12-01')
WHERE month_number = 12;

UPDATE analytics
SET profit = income - products_purchase - stores_rent - warehouses_rent;

SELECT * FROM analytics
ORDER BY month_number;