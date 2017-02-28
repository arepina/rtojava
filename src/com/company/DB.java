package com.company;

import java.sql.*;


class DB {
    private Connection conn;

    Connection connectDb() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:../nmzk/data/Products.db");
//            Statement statement = conn.createStatement();
//            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM Products");
            return conn;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    Product getProduct(Integer id) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM Products WHERE product_id = " + id);
        return new Product(resultSet.getString("ct_publish_date"), resultSet.getString("product_okpd_2"),
                resultSet.getString("product_name"), resultSet.getString("product_measure"),
                resultSet.getDouble("product_price"), resultSet.getInt("product_id"),
                resultSet.getString("region_code"), resultSet.getString("ct_href"),
                resultSet.getString("rev_okpd"));

    }


}
