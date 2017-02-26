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

    static void read(String csvFile, List<FileType> items, String className) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        List<String> headers = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                if (headers.size() == 0)
                    headers.addAll(Arrays.asList(line.split(cvsSplitBy)));
                else {
                    String[] data = line.replace("\"", "").split(cvsSplitBy);
                    FileType ob = null;
                    if (className.equals("products")) {
                        List<String> values = new ArrayList<String>();
                        values.addAll(Arrays.asList(data));
                        while (values.size() != 9)
                            values.add("");
                        try
                        {
                            double k = Double.parseDouble(data[4]);
                        }
                        catch (Exception e)
                        {
                            values.set(4, "0.0");
                        }
                        try
                        {
                            int k = Integer.parseInt(data[5]);
                        }
                        catch (Exception e)
                        {
                            values.set(5, "0");
                        }
                        ob = new Product(values.get(0), values.get(1), values.get(2), values.get(3), Double.parseDouble(values.get(4)), Integer.parseInt(values.get(5)), values.get(6), values.get(7), values.get(8));
                    }
                    else
                        ob = new DFM();
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
    }
}
