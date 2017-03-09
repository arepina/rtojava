package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RowsFinder {
    public static ArrayList<Integer> findFirstNounRows(String firstNoun, ArrayList<String> headers, Integer[][] matrix) {
        int columnIndex = 0;
        if (headers.contains(firstNoun))
            columnIndex = headers.indexOf(firstNoun);
        else
            throw new IllegalArgumentException("Unknown first noun word");
        ArrayList<Integer> rows = new ArrayList<>(); // non zero rows for word column
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i][columnIndex - 1] != 0)
                rows.add(i);
        }
        return rows;
    }

    public static ArrayList<Integer> findNonZeroRows(String str, ArrayList<String> headers, Integer[][] matrix) {
        String[] words = str.split(" ");
        Map<String, Integer> columnWords = new HashMap<>(); // column for each word
        for (String word : words) // get the column numbers for each word
        {
            columnWords.put(word, null);
            if (headers.contains(word)) {
                int columnNumber = headers.indexOf(word);
                columnWords.put(word, columnNumber);
            } else
                throw new IllegalArgumentException("Unknown word in non zero rows");
        }
        Map<String, ArrayList<Integer>> rowsWords = new HashMap<>(); // non zero rows for each word column
        for (Object o : columnWords.entrySet()) {// get the non zero rows for each word column
            Map.Entry pair = (Map.Entry) o;
            rowsWords.put(pair.getKey().toString(), null);
            int columnIndex = Integer.parseInt(pair.getValue().toString());
            ArrayList<Integer> rows = new ArrayList<>();
            if (rowsWords.get(pair.getKey().toString()) != null)
                rows = rowsWords.get(pair.getKey().toString());
            for (int i = 0; i < matrix.length; i++) {
                if (matrix[i][columnIndex - 1] != 0)
                    rows.add(i);
            }
            rowsWords.put(pair.getKey().toString(), rows);
        }
        Map<Integer, Integer> rowsNumber = new HashMap<>(); // number of each non zero row
        for (ArrayList<Integer> o : rowsWords.values()) {
            for (Integer anO : o) {
                if (rowsNumber.containsKey(anO)) {
                    rowsNumber.put(anO, rowsNumber.get(anO) + 1);
                } else {
                    rowsNumber.put(anO, 1);
                }
            }
        }
        ArrayList<Integer> resultRows = new ArrayList<>();
        for (Integer rowNumber : rowsNumber.keySet()) {
            if (rowsNumber.get(rowNumber) == columnWords.keySet().size())
                resultRows.add(rowNumber);
        }
        return resultRows;
    }
}
