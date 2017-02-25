package com.company;


class Product extends FileType{

    String productName;
    String regionsString;
    String measure;
    String date;
    String ocpd2CodesString;
    Double price;
    String link;
    Integer id;

    Product(Integer id, String productName, String regionsString, String measure, String date, String ocpd2CodesString, Double price, String link) {
        this.id = id;
        this.productName = replacer(productName);
        this.regionsString = regionsString;
        this.measure = measure;
        this.date = date;
        this.ocpd2CodesString = ocpd2CodesString;
        this.price = price;
        this.link = link;
    }


    private static String replacer(String item) {
        item = item.replaceAll("\"", "");
        item = item.replaceAll("(\\d)(\\D)", "\\1\\ \\2");
        item = item.replaceAll("(\\D)(\\d)", "\\1\\ \\2");
        item = item.replaceAll("(\\D)(\\.|,)(\\D)", "\\1\\ \\3");

        item = item.replace("\r", " ");
        item = item.replace("\t", " ");
        item = item.replace("\n", " ");
        item = item.replace(",", "");
        item = item.replace(".", "");
        item = item.replace("•", "");
        item = item.replace(";", "");
        item = item.replace(":", "");
        item = item.replace("!", "");
        item = item.replace("?", "");
        item = item.replace(")", "");
        item = item.replace("(", "");
        item = item.replace("™", "");
        item = item.replace("®", "");
        item = item.replace("*", "");
        item = item.replace("/", "");
        item = item.replace("—", "");
        item = item.replace("-", "");
        item = item.replace("~", "");
        item = item.replace("'", "");
        return item.toLowerCase();
    }
}
