package org.example.lab2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import com.github.javafaker.Faker;


public class DataGenerator {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/sport_shops";
    private static final String USER = "postgres";
    private static final String PASS = "postgres";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Statement statement = conn.createStatement();

            // Creating tables
            statement.executeUpdate("DROP TABLE vendor, product, personal_data, customer, cart, products_in_carts, store, products_in_stores, seller, seller_shifts");

            // Creating tables
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS vendor (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(255), " +
                    "contract_start_date DATE, " +
                    "contract_end_date DATE)");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS product (" +
                    "id SERIAL PRIMARY KEY, " +
                    "product_name VARCHAR(255), " +
                    "purchase_price DECIMAL, " +
                    "retail_price DECIMAL, " +
                    "vendor_id INTEGER REFERENCES vendor(id))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS personal_data (" +
                    "id SERIAL PRIMARY KEY, " +
                    "first_name VARCHAR(255), " +
                    "last_name VARCHAR(255), " +
                    "gender CHAR(1), " +
                    "date_of_birth DATE, " +
                    "phone_number VARCHAR(20))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS customer (" +
                    "id SERIAL PRIMARY KEY, " +
                    "discount_card BOOLEAN, " +
                    "personal_data_id INTEGER REFERENCES personal_data(id))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS cart (" +
                    "id SERIAL PRIMARY KEY, " +
                    "order_date DATE, " +
                    "rating INTEGER, " +
                    "feedback TEXT, " +
                    "customer_id INTEGER REFERENCES customer(id))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS products_in_carts (" +
                    "product_id INTEGER REFERENCES product(id), " +
                    "cart_id INTEGER REFERENCES cart(id), " +
                    "quantity INTEGER, " +
                    "PRIMARY KEY (product_id, cart_id))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS store (" +
                    "id SERIAL PRIMARY KEY, " +
                    "city VARCHAR(255), " +
                    "address VARCHAR(255), " +
                    "rental_price DECIMAL, " +
                    "rental_start_date DATE, " +
                    "rental_end_date DATE)");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS products_in_stores (" +
                    "product_id INTEGER REFERENCES product(id), " +
                    "store_id INTEGER REFERENCES store(id), " +
                    "quantity INTEGER, " +
                    "PRIMARY KEY (product_id, store_id))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS seller (" +
                    "id SERIAL PRIMARY KEY, " +
                    "salary DECIMAL, " +
                    "working_email_address VARCHAR(255), " +
                    "personal_data_id INTEGER REFERENCES personal_data(id), " +
                    "store_id INTEGER REFERENCES store(id))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS seller_shifts (" +
                    "seller_id INTEGER REFERENCES seller(id), " +
                    "weekday INTEGER, " +
                    "shift_number INTEGER, " +
                    "PRIMARY KEY (seller_id, weekday))");


            Faker faker = new Faker(new Locale("ru_RU"));
            Random random = new Random();

            // Vendor
            Set<String> vendorNames = new HashSet<>();
            while (vendorNames.size() < 5) {
                vendorNames.add(faker.company().name());
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO vendor (id, name, contract_start_date, contract_end_date) VALUES (?, ?, ?, ?)")) {
                int id = 4;
                for (String name : vendorNames) {
                    pstmt.setInt(1, id++);
                    pstmt.setString(2, name);
                    pstmt.setDate(3, new java.sql.Date(faker.date().past(365 * 3, java.util.concurrent.TimeUnit.DAYS).getTime()));
                    pstmt.setDate(4, new java.sql.Date(faker.date().future(365 * 3, java.util.concurrent.TimeUnit.DAYS).getTime()));
                    pstmt.executeUpdate();
                }
            }

            // Product
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO product (id, product_name, purchase_price, retail_price, vendor_id) VALUES (?, ?, ?, ?, ?)")) {
                for (int id = 8; id <= 100; id++) {
                    pstmt.setInt(1, id);
                    pstmt.setString(2, faker.lorem().characters(8, 30));
                    pstmt.setDouble(3, Math.round(random.nextDouble() * 99999.00 * 100) / 100.0);
                    pstmt.setDouble(4, Math.round((random.nextDouble() * 99999.99 + 0.01) * 100) / 100.0);
                    pstmt.setInt(5, random.nextInt(5) + 4);
                    pstmt.executeUpdate();
                }
            }

            // Personal data
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO personal_data (id, first_name, last_name, gender, date_of_birth, phone_number) VALUES (?, ?, ?, ?, ?, ?)")) {
                for (int id = 5; id <= 100; id++) {
                    String gender = random.nextBoolean() ? "М" : "Ж";
                    pstmt.setInt(1, id);
                    pstmt.setString(2, faker.name().firstName());
                    pstmt.setString(3, faker.name().firstName());
                    pstmt.setString(4, gender);
                    pstmt.setDate(5, new java.sql.Date(faker.date().birthday().getTime()));
                    pstmt.setString(6, faker.phoneNumber().phoneNumber());
                    pstmt.executeUpdate();
                }
            }

            // Customer
            Set<Integer> personalDataIds = new HashSet<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT personal_data_id FROM customer")) {
                while (rs.next()) {
                    personalDataIds.add(rs.getInt(1));
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO customer (id, discount_card, personal_data_id) VALUES (?, ?, ?)")) {
                for (int id = 4; id <= 20; id++) {
                    pstmt.setInt(1, id);
                    pstmt.setBoolean(2, random.nextBoolean());
                    int personalDataId;
                    do {
                        personalDataId = random.nextInt(95) +5;
                    } while (personalDataIds.contains(personalDataId));
                    personalDataIds.add(personalDataId);
                    pstmt.setInt(3, personalDataId);
                    pstmt.executeUpdate();
                }
            }

            // Cart
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO cart (id, order_date, rating, feedback, customer_id) VALUES (?, ?, ?, ?, ?)")) {
                for (int id = 5; id <= 100; id++) {
                    pstmt.setInt(1, id);
                    pstmt.setDate(2, new java.sql.Date(faker.date().past(365 * 3, java.util.concurrent.TimeUnit.DAYS).getTime()));
                    pstmt.setInt(3, random.nextInt(5) + 1);
                    pstmt.setString(4, faker.lorem().paragraph().replace("\n", " "));
                    pstmt.setInt(5, random.nextInt(16) + 4);
                    pstmt.executeUpdate();
                }
            }

            // Products in carts
            int[][] productsInCarts = new int[100][100];
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM products_in_carts")) {
                while (rs.next()) {
                    productsInCarts[rs.getInt(1) - 1][rs.getInt(2) - 1] = rs.getInt(3);
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO products_in_carts (product_id, cart_id, quantity) VALUES (?, ?, ?)")) {
                for (int id = 9; id <= 100; id++) {
                    int productId, cartId;
                    do {
                        productId = random.nextInt(92) + 8;
                        cartId = random.nextInt(95) + 5;
                    } while (productsInCarts[productId - 1][cartId - 1] != 0);
                    productsInCarts[productId - 1][cartId - 1] = random.nextInt(100) + 1;
                    pstmt.setInt(1, productId);
                    pstmt.setInt(2, cartId);
                    pstmt.setInt(3, random.nextInt(100) + 1);
                    pstmt.executeUpdate();
                }
            }

            // Store
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO store (id, city, address, rental_price, rental_start_date, rental_end_date) VALUES (?, ?, ?, ?, ?, ?)")) {
                for (int id = 3; id <= 15; id++) {
                    pstmt.setInt(1, id);
                    pstmt.setString(2, faker.options().option("Санкт-Петербург", "Москва", "Сочи", "Казань", "Барнаул"));
                    pstmt.setString(3, faker.address().streetAddress());
                    pstmt.setDouble(4, Math.round(random.nextDouble() * 999999.99 * 100) / 100.0);
                    pstmt.setDate(5, new java.sql.Date(faker.date().past(365 * 3, java.util.concurrent.TimeUnit.DAYS).getTime()));
                    pstmt.setDate(6, new java.sql.Date(faker.date().future(365 * 3, java.util.concurrent.TimeUnit.DAYS).getTime()));
                    pstmt.executeUpdate();
                }
            }

            // Products in stores
            int[][] productsInStores = new int[100][15];
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM products_in_stores")) {
                while (rs.next()) {
                    productsInStores[rs.getInt(1) - 1][rs.getInt(2) - 1] = rs.getInt(3);
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO products_in_stores (product_id, store_id, quantity) VALUES (?, ?, ?)")) {
                for (int id = 8; id <= 100; id++) {
                    int productId, storeId;
                    do {
                        productId = random.nextInt(92) + 8;
                        storeId = random.nextInt(12) + 3;
                    } while (productsInStores[productId - 1][storeId - 1] != 0);
                    productsInStores[productId - 1][storeId - 1] = random.nextInt(100) + 1;
                    pstmt.setInt(1, productId);
                    pstmt.setInt(2, storeId);
                    pstmt.setInt(3, random.nextInt(100) + 1);
                    pstmt.executeUpdate();
                }
            }

            // Seller
            personalDataIds.clear();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT personal_data_id FROM seller")) {
                while (rs.next()) {
                    personalDataIds.add(rs.getInt(1));
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO seller (id, salary, working_email_address, personal_data_id, store_id) VALUES (?, ?, ?, ?, ?)")) {
                for (int id = 3; id <= 30; id++) {
                    pstmt.setInt(1, id);
                    pstmt.setDouble(2, Math.round(random.nextDouble() * 999999.99 * 100) / 100.0);
                    pstmt.setString(3, faker.internet().emailAddress());
                    int personalDataId;
                    do {
                        personalDataId = random.nextInt(100) + 1;
                    } while (personalDataIds.contains(personalDataId));
                    personalDataIds.add(personalDataId);
                    pstmt.setInt(4, personalDataId);
                    pstmt.setInt(5, random.nextInt(12) + 3);
                    pstmt.executeUpdate();
                }
            }

            // Seller shifts
            int[][] sellerShifts = new int[30][7];
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM seller_shifts")) {
                while (rs.next()) {
                    sellerShifts[rs.getInt(1) - 1][rs.getInt(2) - 1] = rs.getInt(3);
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO seller_shifts (seller_id, weekday, shift_number) VALUES (?, ?, ?)")) {
                for (int id = 4; id <= 30; id++) {
                    int sellerId, weekday;
                    do {
                        sellerId = random.nextInt(27) + 3;
                        weekday = random.nextInt(7) + 1;
                    } while (sellerShifts[sellerId - 1][weekday - 1] != 0);
                    sellerShifts[sellerId - 1][weekday - 1] = random.nextInt(3) + 1;
                    pstmt.setInt(1, sellerId);
                    pstmt.setInt(2, weekday);
                    pstmt.setInt(3, random.nextInt(3) + 1);
                    pstmt.executeUpdate();
                }
            }

            System.out.println("Data generation completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

