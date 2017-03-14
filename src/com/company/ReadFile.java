package com.company;

import com.sun.tools.javac.util.ArrayUtils;
import org.ibex.nestedvm.util.Seekable;

import java.io.*;
import java.lang.reflect.Array;
import java.util.BitSet;
import java.util.*;


class ReadFile {

    static void csv() {
        BufferedReader br;
        BufferedWriter bw;
        List<String> list = new ArrayList<>();
        String line;
        try {
            br = new BufferedReader(new FileReader("../nmzk/data/dfm.csv"));
            while ((line = br.readLine()) != null) {
                line = line.replace(",", ";");
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        list.remove(0);
        try {
            bw = new BufferedWriter(new FileWriter("../nmzk/data/dfm_new.csv"));
            int c = 0;
            for (String item : list) {
                System.out.println(c);
                c++;
                bw.write(item + "\n");
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static Map<String, BitSet> formMatrix(String filePath, ArrayList<String> features, ArrayList<String> indexes) {
        Map<String, ArrayList<Byte>> matrix_prepare = new LinkedHashMap<>();
        BufferedReader br;
        String line;
        int count = 0;
        for (String feature : features) {
            if (matrix_prepare.size() == 100)
                break;
            matrix_prepare.put(feature, null);
            count += 1;
        }
        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {
                ArrayList<String> bits = new ArrayList<>(Arrays.asList(line.split(";")));
                bits.remove(0);
                count = 0;
                for (String key : matrix_prepare.keySet()) {
                    byte b = Byte.parseByte(bits.get(count));
                    if (matrix_prepare.get(key) != null) {
                        ArrayList<Byte> l = matrix_prepare.get(key);
                        l.add(b);
                        matrix_prepare.put(key, l);
                    } else {
                        ArrayList<Byte> l = new ArrayList<>();
                        l.add(b);
                        matrix_prepare.put(key, l);
                    }
                    count += 1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, BitSet> matrix = new LinkedHashMap<>();
        for (Map.Entry<String, ArrayList<Byte>> entry : matrix_prepare.entrySet()) {
            Byte[] byte_arr = entry.getValue().toArray(new Byte[entry.getValue().size()]);
            String str = Arrays.toString(byte_arr);
            BitSet bitSet = createFromString(str);
            matrix.put(entry.getKey(), bitSet);
        }
        return matrix;
    }

    private static BitSet createFromString(String s) {
        BitSet t = new BitSet(s.length());
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '1')
                t.set(s.length() - 1 - i);
        }
        return t;
    }

    static ArrayList<String> readHeaders(String csvFile) {
        BufferedReader br;
        String line;
        ArrayList<String> head = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                head.add(line.substring(line.indexOf(",") + 1, line.length()).replace("\"", ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return head;
    }
}
