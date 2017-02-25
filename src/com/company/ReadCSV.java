package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FileType{
   public String toString()
   {return "";}
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
                    String [] data = line.replace("\"", "").split(cvsSplitBy);
                    FileType ob = null;
                    if (className.equals("products"))
                        ob = new Product(Integer.parseInt(data[5]), data[2], data[6], data[3], data[0], data[1], Double.parseDouble(data[4]), data[7]);
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

    public static Product findProductInfo(Integer id) {

        return new Product(0, "", "", "", "", "", 0.0, "");
    }
}
