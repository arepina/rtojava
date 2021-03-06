package com.company;

import org.json.JSONObject;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.Option;
import scala.collection.JavaConversions;

import java.io.*;
import java.sql.SQLException;
import java.util.*;


public class Main {

    final static int MAX = 298;
    private static ArrayList<String> indexes;
    private static ArrayList<String> features;
    static DB db;
    private static Map<String, BitSet> matrix;
    private static Info firstNoun;
    private final static MyStem mystemAnalyzer = new Factory("-igd --eng-gr --format json --weight").newMyStem("3.0", Option.empty()).get();

    public static void main(final String[] args) throws MyStemApplicationException, IOException, SQLException, ClassNotFoundException {
        loadData();
        serialize();
        matrix = null;
        deserialize();
        long start = System.currentTimeMillis();
        Product requestProduct = new Product("", "35.35.17.123", "set prisma", "", (double) 0, 0, "", "", "");
        //TODO UNCOMMENT
        //ArrayList<String> lemmatizedArray = processRequest(requestProduct);
        //TODO form a vector using lemmatizedArray, step 4
        Integer[] requestVector = new Integer[matrix.size()];
        Arrays.fill(requestVector, 0);
        requestVector[3] = 1;
        requestVector[4] = 1;
        List<Product> result = workWithDTM(requestProduct, requestVector);
        for (Product p : result)
            System.out.println(p.toString());
        long finish = System.currentTimeMillis();
        long timeConsumedMillis = finish - start;
        System.out.println((double)(timeConsumedMillis) + " ms");
    }

    private static void deserialize() {
        Serialization ser;
        try {
            FileInputStream fileIn = new FileInputStream("/tmp/serialized_matrix.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            ser = (Serialization) in.readObject();
            matrix = ser.matrix;
            in.close();
            fileIn.close();
        }catch(IOException i) {
            i.printStackTrace();
        }catch(ClassNotFoundException c) {
            System.out.println("Employee class not found");
            c.printStackTrace();
        }
    }

    private static void serialize() {
        Serialization ser = new Serialization();
        ser.matrix = matrix;
        try {
            FileOutputStream fileOut = new FileOutputStream("/tmp/serialized_matrix.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(ser);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in /tmp/serialized_matrix.ser");
        }catch(IOException i) {
            i.printStackTrace();
        }
    }

    private static void loadData() {
        features = ReadFile.readHeaders("./src/com/company/data/features.csv");
        indexes = ReadFile.readHeaders("./src/com/company/data/docs.csv");
        matrix = ReadFile.formMatrix("./src/com/company/data/dfm_new.csv", features, indexes);
        db = new DB();
        db.connectDb();
    }

    private static ArrayList<String> processRequest(Product product) throws MyStemApplicationException {
        Iterable<Info> result =
                JavaConversions.asJavaIterable(
                        mystemAnalyzer
                                .analyze(Request.apply(product.productName))
                                .info()
                                .toIterable());
        return formLemmatizedArray(result);
    }

    private static ArrayList<String> formLemmatizedArray(Iterable<Info> result) {
        ArrayList<String> lemmatizedArray = new ArrayList<>();
        for (final Info info : result) {
            JSONObject jObject = new JSONObject(info.rawResponse());
            String analysis = jObject.get("analysis").toString();
            analysis = analysis.replace("[", "");
            analysis = analysis.replace("]", "");
            String gr = new JSONObject(analysis).get("gr").toString();
            if (firstNoun == null && gr.charAt(0) == 'S')
                firstNoun = info;
            System.out.println(info.initial() + " -> " + info.lex() + " | " + info.rawResponse());
            lemmatizedArray.add(info.lex().toString().substring(5, info.lex().toString().length() - 1));
        }
        System.out.println(firstNoun);
        return lemmatizedArray;
    }

    private static List<Product> workWithDTM(Product product, Integer[] requestVector) {
        ArrayList<Integer> nonZeroRows = RowsFinder.findNonZeroRows(product.productName, matrix);
        List<Product> products = new ArrayList<>();
        if (nonZeroRows.size() == 0) // we don't have a row or more with all the words
        {
            try {
                ArrayList<Integer> nonZeroFirstNounRows = RowsFinder.findFirstRows(firstNoun.toString(), matrix);
                products = FilterData.filterByCos(nonZeroFirstNounRows, matrix, 0.7, requestVector, indexes);
            } catch (NullPointerException e) {
                System.err.println("Didn't find the first noun in request");
                Collections.sort(features);
                String firstTerm = "";
                for (String word : features) {
                    if (word.startsWith("" + product.productName.charAt(0))) {
                        firstTerm = word;
                        break;
                    }
                }
                ArrayList<Integer> nonZeroFirstTermRows = RowsFinder.findFirstRows(firstTerm, matrix);
                if (nonZeroFirstTermRows.size() == 0)
                    return products;
                else
                    products = FilterData.filterByCos(nonZeroFirstTermRows, matrix, 0.7, requestVector, indexes);
            }

        } else
            products = FilterData.filterByCos(nonZeroRows, matrix, 0.5, requestVector, indexes);
        return FilterData.processFilters(products, product);
    }

}
