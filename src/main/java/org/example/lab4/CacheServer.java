package org.example.lab4;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.github.javafaker.Faker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CacheServer {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/sport_shops";
    private static final String USER = "postgres";
    private static final String PASS = "postgres";

    private static Connection conn;
    private static Faker faker;
    private static Random random = new Random();

    // Кэш запросов с использованием Guava
    private static Cache<String, String> queryCache = CacheBuilder.newBuilder()
            .maximumSize(128)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);
            faker = new Faker(new Locale("ru_RU"));

            System.out.println("SELECT для personal_data");
            System.out.println("\nisCashed = True");
            executeSelectQueries(true, generateSelectQueries("personal_data", 1000, 100), 1000);
            System.out.println("\nisCashed = False");
            executeSelectQueries(false, generateSelectQueries("personal_data", 1000, 100), 1000);

            System.out.println("SELECT для cart");
            System.out.println("\nisCashed = True");
            executeSelectQueries(true, generateSelectQueries("cart", 3000, 100), 3000);
            System.out.println("\nisCashed = False");
            executeSelectQueries(false, generateSelectQueries("cart", 3000, 100), 3000);

            System.out.println("SELECT для store");
            System.out.println("\nisCashed = True");
            executeSelectQueries(true, generateSelectQueries("store", 1000, 29), 1000);
            System.out.println("\nisCashed = False");
            executeSelectQueries(false, generateSelectQueries("store", 1000, 29), 1000);

            List<int[]> ratios = Arrays.asList(new int[][]{
                    {8, 1}, {4, 1}, {2, 1}, {1, 1}, {1, 2}, {1, 4}, {1, 8}
            });

            for (int[] ratio : ratios) {
                System.out.println("INSERT + SELECT для personal_data");
                executeInsertUpdateDeleteQueries("INSERT", ratio, 1024, 1000);

                System.out.println("UPDATE + SELECT для personal_data");
                executeInsertUpdateDeleteQueries("UPDATE", ratio, 1024, 1000);

                System.out.println("DELETE + SELECT для personal_data");
                executeInsertUpdateDeleteQueries("DELETE", ratio, 1024, 1000);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void executeSelectQueries(boolean isCached, Map<Integer, String> queries, int n) throws SQLException {
        long maxTime = 0;
        long minTime = Long.MAX_VALUE;
        long sumTime = 0;

        for (int i = 0; i < n; i++) {
            long startTime = System.currentTimeMillis();
            String query = queries.get(i);

            if (isCached) {
                cacheQuery(query);
            } else {
                executeQuery(query);
            }

            conn.commit();

            long endTime = System.currentTimeMillis();
            long resultTime = endTime - startTime;

            maxTime = Math.max(maxTime, resultTime);
            minTime = Math.min(minTime, resultTime);
            sumTime += resultTime;

            try { Thread.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
        }

        System.out.printf("\tКоличество выполненных запросов: %d%n", n);
        System.out.printf("\tСреднее время выполнения запроса: %.5f%n", (double) sumTime / n);
        System.out.printf("\tСуммарное время выполнения запроса: %.5f%n", (double) sumTime);
        System.out.printf("\tМинимальное время выполнения запроса: %.5f%n", (double) minTime);
        System.out.printf("\tМаксимальное время выполнения запроса: %.5f%n", (double) maxTime);
    }

    private static void executeInsertUpdateDeleteQueries(String type, int[] ratio, int numberOfInsertQueries, int idStart) throws SQLException {
        Map<Integer, String> insertSelectQueries = generateInsertUpdateDeleteQueries(type, numberOfInsertQueries, idStart);
        int numberOfSelectQueries = ratio[0] * numberOfInsertQueries / ratio[1];
        insertSelectQueries.putAll(generateSelectQueries("personal_data", numberOfSelectQueries, 1));

        Map<Integer, String> randomQueries = new LinkedHashMap<>();
        List<Integer> keys = new ArrayList<>(insertSelectQueries.keySet());
        Collections.shuffle(keys);
        for (int key : keys) {
            randomQueries.put(randomQueries.size(), insertSelectQueries.get(key));
        }

        System.out.printf("%s + SELECT для personal_data%n", type);
        System.out.printf("ratio = %d SELECT к %d %s%n", ratio[0], ratio[1], type);

        System.out.println("\nisCashed = True");
        executeSelectQueries(true, randomQueries, randomQueries.size());
        queryCache.invalidateAll();

        System.out.printf("%s + SELECT для personal_data%n", type);
        System.out.printf("ratio = %d SELECT к %d %s%n", ratio[0], ratio[1], type);

        System.out.println("\nisCashed = False");
        executeSelectQueries(false, randomQueries, randomQueries.size());
        queryCache.invalidateAll();
    }

    private static Map<Integer, String> generateSelectQueries(String tableName, int numberOfQueries, int idLimit) {
        Map<Integer, String> selectQueries = new HashMap<>();
        for (int i = 0; i < numberOfQueries; i++) {
            Set<Integer> ids = new HashSet<>();
            while (ids.size() < 100) {
                ids.add(random.nextInt(idLimit) + 1);
            }
            selectQueries.put(i, String.format("SELECT * FROM %s WHERE id IN (%s);", tableName, ids.toString().replaceAll("[\\[\\]]", "")));
        }
        return selectQueries;
    }

    private static Map<Integer, String> generateInsertUpdateDeleteQueries(String type, int numberOfQueries, int idStart) {
        Map<Integer, String> queries = new HashMap<>();
        for (int i = 0; i < numberOfQueries; i++) {
            if (type.equals("INSERT")) {
                String gender = faker.random().nextBoolean() ? "М" : "Ж";
                String firstName = faker.name().firstName();
                String lastName = faker.name().lastName();
                LocalDate dateOfBirth = faker.date().birthday().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                String phoneNumber = faker.phoneNumber().phoneNumber();
                queries.put(i, String.format("INSERT INTO personal_data (id, first_name, last_name, gender, date_of_birth, phone_number) " +
                        "VALUES (%d, '%s', '%s', '%s', '%s', '%s');", idStart + i, firstName, lastName, gender, dateOfBirth, phoneNumber));
            } else if (type.equals("UPDATE")) {
                String gender = faker.random().nextBoolean() ? "М" : "Ж";
                Set<Integer> ids = new HashSet<>();
                while (ids.size() < random.nextInt(21) + 1) {
                    ids.add(random.nextInt(numberOfQueries) + idStart);
                }
                queries.put(i, String.format("UPDATE personal_data SET gender = '%s' WHERE id IN (%s);", gender, ids.toString().replaceAll("[\\[\\]]", "")));
            } else if (type.equals("DELETE")) {
                queries.put(i, String.format("DELETE FROM personal_data WHERE id = %d;", idStart + i));
            }
        }
        return queries;
    }

    private static void cacheQuery(String query) throws SQLException {
        String key = query.split(" ")[0];
        queryCache.put(key, query);
        executeQuery(query);
    }

    private static void executeQuery(String query) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        }
    }
}
