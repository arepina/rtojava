package com.company;


import java.util.*;

class RowsFinder {

    static ArrayList<Integer> findFirstRows(String word, Map<String, BitSet> matrix) {
        if (matrix.get(word) != null) {
            BitSet bitSet = matrix.get(word);
            ArrayList<Integer> nonzero = new ArrayList<>();
            for (int i = bitSet.nextSetBit(0); i != -1; i = bitSet.nextSetBit(i + 1)) {
                nonzero.add((Main.MAX - i) / 3);
            }
            return nonzero;
        } else
            throw new IllegalArgumentException("Unknown word");
    }

    static ArrayList<Integer> findNonZeroRows(String str, Map<String, BitSet> matrix) {
        String[] words = str.split(" ");
        Map<String, ArrayList<Integer>> nonZero = new HashMap<>();
        for (String word : words) // get the nonzero rows for each word
        {
            ArrayList<Integer> nonZeroRows = findFirstRows(word, matrix);
            nonZero.put(word, nonZeroRows);
        }
        Map<Integer, Integer> rowsNumber = new HashMap<>(); // number of each non zero row
        for (ArrayList<Integer> nonZeroRowsBitSet : nonZero.values()) {
            for (Integer row : nonZeroRowsBitSet) {
                if (rowsNumber.containsKey(row))
                    rowsNumber.put(row, rowsNumber.get(row) + 1);
                else
                    rowsNumber.put(row, 1);
            }
        }
        ArrayList<Integer> resultRows = new ArrayList<>();
        for (Integer rowNumber : rowsNumber.keySet()) {
            if (rowsNumber.get(rowNumber) == words.length)  //try to find the full similarity
                resultRows.add(rowNumber);
        }
        return resultRows;
    }

    private static ArrayList<Integer> countBits(int x) {
        ArrayList<Integer> rows = new ArrayList<>();
        String binaryStr = Integer.toBinaryString(x);
        int length = binaryStr.length();
        for (int i = 0; i < length; i++) {
            if (binaryStr.charAt(i) == '1')
                rows.add(i);
        }
        return rows;
    }
}
