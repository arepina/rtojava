package com.company;

import javafx.util.Pair;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


public class FilterData {
    public static List<Product> filterByRegions(List<Product> products, String regionsString) {
        Map<String, Long> occurrences = products.stream().collect(Collectors.groupingBy(w -> w.regionsString, Collectors.counting()));
        if (occurrences.containsKey(regionsString) && occurrences.get(regionsString) > 20)
            return products.stream().filter(c -> c.regionsString.equals(regionsString)).collect(toList());
        return products;
    }

    public static List<Product> filterByMeasure(List<Product> products, String measure) {
        if (measure != null && measure.length() != 0) {
            return products.stream().filter(c -> c.measure.equals(measure)).collect(toList());
        } else {
            Map<String, Long> occurrences = products.stream().collect(Collectors.groupingBy(w -> w.measure, Collectors.counting()));
            return products.stream().filter(c -> c.measure.equals(occurrences.entrySet().iterator().next().getKey())).collect(toList());
        }
    }

    public static List<Product> filterByCos(ArrayList<Integer> nonZeroRows, Integer[][] matrix, double y, Integer[] requestVector) {
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

    public static List<Product> filterByOKPD(List<Product> products, String ocpd2CodesString) {

        return null;
    }

    public static List<Product> filterByPercentile(List<Product> products, int start, int end) {

        return null;
    }
}
