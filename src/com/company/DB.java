package com.company;

import java.sql.*;


class DB {
    private Connection conn;

    void connectDb() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:./src/com/company/data/Products.db");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    Product getProduct(String id) throws SQLException {
        //TODO UNCOMMENT AND REMOVE NOT COMMENTED
//        Statement statement = conn.createStatement();
//        ResultSet resultSet = statement.executeQuery("SELECT * FROM Products WHERE product_id = " + Integer.parseInt(id));
//        return new Product(resultSet.getString("ct_publish_date"), resultSet.getString("product_okpd_2"),
//                resultSet.getString("product_name"), resultSet.getString("product_measure"),
//                resultSet.getDouble("product_price"), resultSet.getInt("product_id"),
//                resultSet.getString("region_code"), resultSet.getString("ct_href"),
//                resultSet.getString("rev_okpd"));
        if (Integer.parseInt(id) == 120848642)
            return new Product("2016-02-02", "32.50.13.190",
                    "устройство гемофильтрации prisma set модель st 100 pre set", "",
                    (double) 30800, 120848642,
                    "77", "http://zakupki.gov.ru/epz/contract/contractCard/common-info.html?reestrNumber=1773409783116000002",
                    "91310523");
        if(Integer.parseInt(id) == 120815115)
            return new Product("2016-02-01", "35.11.10.116",
                    "поставка электрической энергии", "",
                    (double) 6.4, 120815115,
                    "71", "http://zakupki.gov.ru/epz/contract/contractCard/common-info.html?reestrNumber=1710602130016000004",
                    "611011153");
        if(Integer.parseInt(id) == 120815114)
            return new Product("2016-02-01", "35.11.10.116",
                    "поставка электрической энергии", "",
                    7.96, 120815114,
                    "71", "http://zakupki.gov.ru/epz/contract/contractCard/common-info.html?reestrNumber=1710602130016000004",
                    "611011153");
        return new Product("", "",
                "", "",
                (double) 0, 0,
                "", "",
                "");
    }


}
