package com.company;


class Product{

    String productName;
    String regionsString;
    String measure;
    private String date;
    String ocpd2CodesString;
    Double price;
    private String link;
    private Integer id;
    private String revOCPD;
    private Double cos;

    Product(String date, String ocpd2CodesString, String productName, String measure, Double price,  Integer id,  String regionsString, String link, String revOCPD) {
        this.id = id;
        this.productName = replacer(productName);
        this.regionsString = regionsString;
        this.measure = measure;
        this.date = date;
        this.ocpd2CodesString = ocpd2CodesString;
        this.price = price;
        this.link = link;
        this.revOCPD = revOCPD;
    }

    void setCos(Double cos) {
        this.cos = cos;
    }

    Double getCos() {
        return cos;
    }

    @Override
    public String toString() {
        return date + " " + ocpd2CodesString + " " + productName + " " + measure + " " + price + " " + id + " " + regionsString + " " + link + " " + revOCPD;
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
