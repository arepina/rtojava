package com.company;

import javafx.util.Pair;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.*;
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

    static List<Product> filterByCos(ArrayList<Integer> nonZeroRows, Map<String, BitSet> matrix, double y, Integer[] requestVector, ArrayList<String> indexes) {
        List<Pair<Integer, Double>> cosineValues = new ArrayList<>();
        List<String> productID = new ArrayList<>();
        for (Integer row : nonZeroRows) { // get the rows vectors
            Integer[] rowVector = new Integer[matrix.entrySet().size()];
            int count = 0;
            for (Map.Entry<String, BitSet> entry : matrix.entrySet()) {
                String str = entry.getValue().toString().replace("{", "").replace("}", "").replace(" ", "");
                List<String> nonZeroIndexes = Arrays.asList(str.split(","));
                if(nonZeroIndexes.contains(String.valueOf(Main.MAX - 3 * row)))
                    rowVector[count] = 1;
                else
                    rowVector[count] = 0;
                count++;
            }
            cosineValues.add(new Pair<>(row, cosineSimilarity(requestVector, rowVector))); // calc cosine similarity
        }
        List<Pair<Integer, Double>> cosineValuesFiltered = cosineValues.stream().filter(c -> c.getValue() > y).collect(toList());
        productID.addAll(cosineValuesFiltered.stream().map(row -> indexes.get(Integer.parseInt(String.valueOf(row.getKey())) + 1)).collect(Collectors.toList()));
        List<Product> products = findProducts(productID);
        int count = 0;
        for (Product p : products) { // add the cos values to products info
            p.setCos(cosineValuesFiltered.get(count).getValue());
            count += 1;
        }
        return products;
    }

    private static List<Product> findProducts(List<String> productID) {
        List<Product> products = new ArrayList<>();
        for (String id : productID) {
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
        if (ocpd2CodesString.length() != 0) {
            String twoDigits = ocpd2CodesString.substring(0, 2);
            for (Product p : products) {
                int num = countMatches(ocpd2CodesString, p.ocpd2CodesString);
                numberOfSameDigits.add(num);
                if (p.ocpd2CodesString != null && p.ocpd2CodesString.length() != 0 && twoDigits.equals(p.ocpd2CodesString.substring(0, 2)))
                    isFirstTwoDigitsFound = true;
            }
            if (!isFirstTwoDigitsFound)
                return new ArrayList<>();
            int max = Collections.max(numberOfSameDigits);
            products = IntStream.range(0, numberOfSameDigits.size())
                    .filter(i -> numberOfSameDigits.get(i) == max)
                    .mapToObj(products::get).collect(toList());
            for (Product p : products) {
                p.setCos(0.7 * p.getCos() + 0.3 * max / 9);
            }
            return products;
        }
        return products;
    }

    private static int countMatches(String search, String product) {
        if (isEmpty(search) || isEmpty(product) || search.length() != product.length()) {
            return 0;
        }
        search = search.replace(".", "");
        product = product.replace(".", "");
        int count = 0;
        for(int i = 0; i < product.length(); i++)
        {
            if (product.charAt(i) == search.charAt(i))
                count++;
        }
        return count;
    }

    static List<Product> filterByPercentile(List<Product> products) {
        double sum = 0;
        for (Product p : products)
            sum += p.price;
        double start_price = sum * 30 / 100;
        double finish_price = sum * 70 / 100;
        return products.stream().filter(x -> x.price >= start_price && x.price <= finish_price).collect((Collectors.toList()));
    }
}
