SELECT * FROM vendor;
SELECT * FROM product;
SELECT * FROM personal_data;
SELECT * FROM customer;
SELECT * FROM cart;
SELECT * FROM products_in_carts;
SELECT * FROM store;
SELECT * FROM products_in_stores;
SELECT * FROM seller;
SELECT * FROM warehouse;
SELECT * FROM products_in_warehouses;
SELECT * FROM worker;

SELECT * FROM warehouse
WHERE city LIKE '%Санкт-Петербург%';

SELECT * FROM warehouse
WHERE rental_end_date BETWEEN '2023-01-01' AND '2023-12-31';

SELECT * FROM warehouse
WHERE id IN (1, 8, 13);

SELECT id, city, address, rental_price / warehouse_area as price_per_unit_area_of_warehouse
FROM warehouse;

SELECT * FROM seller
ORDER BY store_id ASC, salary DESC;

SELECT * FROM product
WHERE vendor_id = 4;

SELECT AVG(retail_price) as average_retail_price, MAX(retail_price) as max_retail_price,
       MIN(retail_price) as min_retail_price, COUNT(*) as number_of_products FROM product
WHERE vendor_id = 4;

SELECT product.product_name, vendor.name, product.purchase_price FROM product
                                                                          INNER JOIN vendor ON product.vendor_id = vendor.id;

SELECT personal_data.first_name, personal_data.last_name,
       worker.salary, warehouse.city FROM worker
                                              INNER JOIN personal_data ON worker.personal_data_id = personal_data.id
                                              INNER JOIN warehouse ON worker.warehouse_id = warehouse.id;

SELECT warehouse_id, COUNT(*) as number_of_workers FROM worker
GROUP BY warehouse_id
ORDER BY number_of_workers DESC;

SELECT warehouse_id, COUNT(*) as number_of_workers FROM worker
GROUP BY warehouse_id
HAVING COUNT(*) >= 3
ORDER BY number_of_workers DESC;

SELECT * FROM cart
WHERE cart.customer_id IN
      (SELECT customer.id FROM customer
       WHERE customer.discount_card = true);

INSERT INTO vendor
(id, name, contract_start_date, contract_end_date)
VALUES
    (9, 'Adidas', '2022-09-10', '2024-09-10');

INSERT INTO product
(id, product_name, purchase_price, retail_price, vendor_id)
VALUES
    (101, 'Баскетбольный мяч', 1300.00, 1699.99, 2);

INSERT INTO personal_data
(id, first_name, last_name, gender, date_of_birth, phone_number)
VALUES
    (101, 'Пётр', 'Петров', 'М', '2000-04-11', '8 900 123 00 00');

INSERT INTO customer
(id, discount_card, personal_data_id)
VALUES
    (21, FALSE, 100);

INSERT INTO cart
(id, order_date, rating, feedback, customer_id)
VALUES
    (101, '2022-09-10', 5, 'Супер', 20);

INSERT INTO products_in_carts
(product_id, cart_id, quantity)
VALUES
    (100, 100, 2);

INSERT INTO store
(id, city, address, rental_price, rental_start_date, rental_end_date)
VALUES
    (16, 'Казань', 'ул. Третья, д.3', 80000.00, '2022-09-10', '2023-09-10');

INSERT INTO products_in_stores
(product_id, store_id, quantity)
VALUES
    (100, 15, 2);

INSERT INTO seller
(id, salary, working_email_address, personal_data_id, store_id)
VALUES
    (31, 40000.00, 'AbCdeF@mail.ru', 100, 15);

INSERT INTO warehouse
(id, city, address, warehouse_area, rental_price, rental_start_date, rental_end_date)
VALUES
    (16, 'Казань', 'пл. Cкладов, стр.1', 1500, 170000.00, '2022-09-10', '2024-09-10');

INSERT INTO products_in_warehouses
(product_id, warehouse_id, quantity)
VALUES
    (100, 15, 20);

INSERT INTO worker
(id, salary, working_email_address, personal_data_id, warehouse_id)
VALUES
    (31, 25000.00, 'FeDcBa@mail.ru', 100, 15);

SELECT * FROM worker WHERE warehouse_id = 15;

UPDATE worker SET salary = salary + 10000.00
WHERE warehouse_id = 15;

SELECT * FROM worker WHERE warehouse_id = 15;

INSERT INTO warehouse
(id, city, address, warehouse_area, rental_price, rental_start_date, rental_end_date)
VALUES
    (17, 'Санкт-Петербург', 'ул. Первая, стр.10', 300, 900000.00, '2021-08-10', '2023-08-10');

SELECT id, address, warehouse_area, rental_price,
       rental_price / warehouse_area as price_per_unit_area_of_warehouse
FROM warehouse WHERE city LIKE '%Санкт-Петербург%';

DELETE FROM warehouse
WHERE city LIKE '%Санкт-Петербург%' AND rental_price / warehouse_area = (
    SELECT MAX(rental_price / warehouse_area) as price_per_unit_area_of_warehouse
    FROM warehouse WHERE city LIKE '%Санкт-Петербург%');

SELECT id, address, warehouse_area, rental_price,
       rental_price / warehouse_area as price_per_unit_area_of_warehouse
FROM warehouse WHERE city LIKE '%Санкт-Петербург%';

SELECT * FROM vendor;

DELETE FROM vendor WHERE vendor.id NOT IN (SELECT vendor_id FROM product);

SELECT * FROM vendor;