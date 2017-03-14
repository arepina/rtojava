package com.company;

import javafx.util.Pair;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.qos.logback.core.util.OptionHelper.isEmpty;
import static java.util.stream.Collectors.toList;


class FilterData {
    static List<Product> filterByRegions(List<Product> products, String regionsString) {
        Map<String, Long> occurrences = products.stream().collect(Collectors.groupingBy(w -> w.regionsString, Collectors.counting()));
        if (occurrences.containsKey(regionsString) && occurrences.get(regionsString) > 20)
            return products.stream().filter(c -> c.regionsString.equals(regionsString)).collect(toList());
        return products;
    }

    static List<Product> filterByMeasure(List<Product> products, String measure) {
        if (measure != null && measure.length() != 0) {
            return products.stream().filter(c -> c.measure.equals(measure)).collect(toList());
        } else {
            Map<String, Long> occurrences = products.stream().collect(Collectors.groupingBy(w -> w.measure, Collectors.counting()));
            return products.stream().filter(c -> c.measure.equals(occurrences.entrySet().iterator().next().getKey())).collect(toList());
        }
    }

    static List<Product> filterByCos(ArrayList<Integer> nonZeroRows, Integer[][] matrix, double y, Integer[] requestVector) {
        List<Pair<Integer, Double>> cosineValues = new ArrayList<>();
        List<Integer> productID = new ArrayList<>();
        for (Integer row : nonZeroRows) {
            Integer[] rowVector = new Integer[matrix[row].length];
            System.arraycopy(matrix[row], 0, rowVector, 0, matrix[row].length);
            cosineValues.add(new Pair<>(row, cosineSimilarity(requestVector, rowVector)));
        }
        List<Pair<Integer, Double>> cosineValuesFiltered = cosineValues.stream().filter(c -> c.getValue() > y).collect(toList());
        productID.addAll(cosineValuesFiltered.stream().map(row -> matrix[0][row.getKey()]).collect(Collectors.toList()));
        List<Product> products = findProducts(productID);
        int count = 0;
        for (Product p : products) {
            p.setCos(cosineValuesFiltered.get(count).getValue());
            count += 1;
        }
        return products;
    }

    private static List<Product> findProducts(List<Integer> productID) {
        List<Product> products = new ArrayList<>();
        for (Integer id : productID) {
            try {
                Product p = Main.db.getProduct(id);
                products.add(p);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalStateException("Error in DB of products");
            }
        }
        return products;
    }


    private static Double cosineSimilarity(Integer[] vectorA, Integer[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    static List<Product> filterByOKPD(List<Product> products, String ocpd2CodesString) {
        List<Integer> numberOfSameDigits = new ArrayList<>();
        boolean isFirstTwoDigitsFound = false;
        String twoDigits = ocpd2CodesString.substring(0, 2);
        if(ocpd2CodesString.length() != 0){
            for(Product p : products) {
                int num = countMatches(ocpd2CodesString, p.ocpd2CodesString);
                numberOfSameDigits.add(num);
                if (twoDigits.equals(p.ocpd2CodesString.substring(0, 2)))
                        isFirstTwoDigitsFound = true;
            }
            if (!isFirstTwoDigitsFound)
                return new ArrayList<>();
            int max = Collections.max(numberOfSameDigits);
            products = IntStream.range(0, numberOfSameDigits.size())
                    .filter(i -> numberOfSameDigits.get(i) == max)
                    .mapToObj(products::get).collect(toList());
            for(Product p: products)
            {
                p.setCos(0.7 * p.getCos() + 0.3 * max / 9);
            }
            return products;
        }
        return products;
    }

    private static int countMatches(String str, String sub) {
        if (isEmpty(str) || isEmpty(sub)) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    static List<Product> filterByPercentile(List<Product> products) {
        double sum = 0;
        for (Product p : products)
            sum += p.price;
        double start_price = sum * 30 / 100;
        double finish_price = sum * 70 / 100;
        return products.stream().filter(x->x.price >= start_price && x.price <= finish_price).collect((Collectors.toList()));
    }
}
