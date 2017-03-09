package com.company;

import java.util.ArrayList;

class CalcPercentiles {
    public static ArrayList<Product> calcPercentiles(ArrayList<Product> value_list) {

        int sum = 0;
        for (Product aValue_list1 : value_list) {
            sum += aValue_list1.price;
        }

        ArrayList<Double> percentiles = new ArrayList<>();
        for (Product aValue_list : value_list) {
            percentiles.add(100 * aValue_list.price / (float) sum);
        }

        return value_list;
    }
}
