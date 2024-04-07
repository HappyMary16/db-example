package org.example;

import org.example.model.Cat;
import org.example.model.CatBehaviour;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.example.model.CatBehaviour.CALM;
import static org.example.model.CatBehaviour.CRAZY;

public class SqliteExample {

    public static void main(String[] args) {
        //Створюємо підключення до БД
        String url = "jdbc:sqlite:db_cats";
        Connection conn;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to DB", e);
        }

        //Створюємо таблицю, в якій зберігатимемо інформацію про котів
        String createTableSql = "CREATE TABLE IF NOT EXISTS cats (cat_name TEXT PRIMARY KEY, behaviour TEXT)";
        try (PreparedStatement createTableStmt = conn.prepareStatement(createTableSql)) {
            createTableStmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to DB", e);
        }

        Cat cat1 = new Cat("Tom", CALM);
        Cat cat2 = new Cat("Murzyk", CRAZY);

        //Створюємо 2х котів в одній транзакції
        String insertSql = "INSERT INTO cats (cat_name, behaviour) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            //Починаємо транзакцію
            conn.setAutoCommit(false);

            insertStmt.setString(1, cat1.getName());
            insertStmt.setString(2, cat1.getBehaviour().toString());
            insertStmt.executeUpdate();

            insertStmt.setString(1, cat2.getName());
            insertStmt.setString(2, cat2.getBehaviour().toString());
            insertStmt.executeUpdate();
            //Зберігаємо всі зміни, які зробили в транзакції в БД
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to DB", e);
        }

        //Дістаємо котів з БД
        String selectSql = "SELECT * FROM cats";
        try (PreparedStatement insertStmt = conn.prepareStatement(selectSql);
             ResultSet resultSet = insertStmt.executeQuery()) {

            List<Cat> cats = new ArrayList<>();

            while (resultSet.next()) {
                String catName = resultSet.getString("cat_name");
                CatBehaviour behaviour = CatBehaviour.valueOf(resultSet.getString("behaviour"));
                cats.add(new Cat(catName, behaviour));
            }

            System.out.println("Коти збережені в базі даних:");
            System.out.println(cats);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to DB", e);
        }

        //Видаляємо котів з бази даних
        String deleteSql = "DELETE FROM cats WHERE cat_name = ?";
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setString(1, cat1.getName());
            deleteStmt.executeUpdate();

            deleteStmt.setString(1, cat2.getName());
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to DB", e);
        }

        //Дістаємо котів з БД
        try (PreparedStatement insertStmt = conn.prepareStatement(selectSql);
             ResultSet resultSet = insertStmt.executeQuery()) {

            List<Cat> cats = new ArrayList<>();

            while (resultSet.next()) {
                String catName = resultSet.getString("cat_name");
                CatBehaviour behaviour = CatBehaviour.valueOf(resultSet.getString("behaviour"));
                cats.add(new Cat(catName, behaviour));
            }

            System.out.println("Коти збережені в базі даних після видалення доданих котів:");
            System.out.println(cats);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to DB", e);
        }

        // Закриваємо підключення до БД
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to DB", e);
        }
    }
}