package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FileType {
    public String toString() {
        return "";
    }
}

class ReadCSV {

    static ArrayList<String> read(String csvFile, List<FileType> items, String className) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<String> headers = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                if (headers.size() == 0)
                    headers.addAll(Arrays.asList(line.replace("\"", "").split(cvsSplitBy)));
                else {
                    String[] data = line.replace("\"", "").split(cvsSplitBy);
                    FileType ob = null;
                    if (className.equals("products")) {
                        List<String> values = new ArrayList<String>();
                        values.addAll(Arrays.asList(data));
                        while (values.size() != 9)
                            values.add("");
                        try {
                            double k = Double.parseDouble(data[4]);
                        } catch (Exception e) {
                            values.set(4, "0.0");
                        }
                        try {
                            int k = Integer.parseInt(data[5]);
                        } catch (Exception e) {
                            values.set(5, "0");
                        }
                        ob = new Product(values.get(0), values.get(1), values.get(2), values.get(3), Double.parseDouble(values.get(4)), Integer.parseInt(values.get(5)), values.get(6), values.get(7), values.get(8));
                    } else
                        ob = new DFM(data);
                    items.add(ob);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return headers;
    }

    static Integer[][] formMatrix(List<FileType> items)
    {
        Integer matrix[][]= new Integer[items.size()][((DFM)items.get(0)).data.length];
        for(int i = 0; i < items.size(); i++) {
            for(int j = 0; j < ((DFM)items.get(0)).data.length; j++) {
                matrix[i][j] = ((DFM)items.get(i)).data[j];
            }
            System.out.println(" ]");
        }
        return matrix;
    }
}
