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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static ArrayList<FileType> dfm;
    private static ArrayList<String> headers;
    private static DB db;
    private static Integer[][] matrix;
    private final static MyStem mystemAnalyzer = new Factory("-igd --eng-gr --format json --weight").newMyStem("3.0", Option.<File>empty()).get();

    public static void main(final String[] args) throws MyStemApplicationException, IOException, SQLException, ClassNotFoundException {
        loadData();

        String request = "set prisma";
        Info firstNoun = null;
        ArrayList<String> lemmatizedArray = null;

        processRequest(request, firstNoun, lemmatizedArray);

        workWithDTM(request, firstNoun);

        //Product product = db.getProduct(118583781);
    }

    private static void loadData() {
        ArrayList<FileType> dfm = new ArrayList<FileType>();
        ArrayList<String> headers = ReadCSV.read("../nmzk/data/dfm.csv", dfm, "dfm");
        matrix = ReadCSV.formMatrix(dfm);
        db = new DB();
        db.connectDb();
    }

    private static void processRequest(String request, Info firstNoun, ArrayList<String> lemmatizedArray) throws MyStemApplicationException {
        request = Product.replacer(request);

        Iterable<Info> result =
                JavaConversions.asJavaIterable(
                        mystemAnalyzer
                                .analyze(Request.apply(request))
                                .info()
                                .toIterable());
        lemmatizedArray = formLemmatizedArray(result, firstNoun);
    }

    private static ArrayList<String> formLemmatizedArray(Iterable<Info> result, Info firstNoun) {
        ArrayList<String> lemmatizedArray = new ArrayList<String>();
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

    private static void workWithDTM(String request, Info firstNoun) {
        ArrayList<Integer> nonZeroRows = findNonZeroRows(request, headers, matrix);
        if (nonZeroRows.size() == 0) // we don't have a row or more with all the words
        {
            try {
                ArrayList<Integer> nonZeroFirstNounRows = findFirstNounRows(firstNoun.toString(), headers, matrix);
                calculateCos(nonZeroFirstNounRows, 0.7);
            } catch (NullPointerException e) {
                System.err.println("Didn't find the first noun in request");
                //todo find the first slovarniy term!!!
                //if not found return new ArrayList<Product>();
                //else calculateCos(nonZeroFirstTermRows, 0.7);
            }

        }
        else{
            calculateCos(nonZeroRows, 0.5);
        }
    }

    private static void calculateCos(ArrayList<Integer> nonZeroRows, double y) {

    }


    private static ArrayList<Integer> findNonZeroRows(String str, ArrayList<String> headers, Integer[][] matrix) {
        String[] words = str.split(" ");
        Map<String, Integer> columnWords = new HashMap<String, Integer>(); // column for each word
        for (String word : words) // get the column numbers for each word
        {
            columnWords.put(word, null);
            if (headers.contains(word)) {
                int columnNumber = headers.indexOf(word);
                columnWords.put(word, columnNumber);
            } else
                throw new IllegalArgumentException("Unknown word in non zero rows");
        }
        Map<String, ArrayList<Integer>> rowsWords = new HashMap<String, ArrayList<Integer>>(); // non zero rows for each word column
        for (Object o : columnWords.entrySet()) {// get the non zero rows for each word column
            Map.Entry pair = (Map.Entry) o;
            rowsWords.put(pair.getKey().toString(), null);
            int columnIndex = Integer.parseInt(pair.getValue().toString());
            ArrayList<Integer> rows = new ArrayList<Integer>();
            if (rowsWords.get(pair.getKey().toString()) != null)
                rows = rowsWords.get(pair.getKey().toString());
            for (int i = 0; i < matrix.length; i++) {
                if (matrix[i][columnIndex - 1] != 0)
                    rows.add(i);
            }
            rowsWords.put(pair.getKey().toString(), rows);
        }
        Map<Integer, Integer> rowsNumber = new HashMap<Integer, Integer>(); // number of each non zero row
        for (ArrayList<Integer> o : rowsWords.values()) {
            for (Integer anO : o) {
                if (rowsNumber.containsKey(anO)) {
                    rowsNumber.put(anO, rowsNumber.get(anO) + 1);
                } else {
                    rowsNumber.put(anO, 1);
                }
            }
        }
        ArrayList<Integer> resultRows = new ArrayList<Integer>();
        for (Integer rowNumber : rowsNumber.keySet()) {
            if (rowsNumber.get(rowNumber) == columnWords.keySet().size())
                resultRows.add(rowNumber);
        }
        return resultRows;
    }

    private static ArrayList<Integer> findFirstNounRows(String firstNoun, ArrayList<String> headers, Integer[][] matrix) {
        int columnIndex = 0;
        if (headers.contains(firstNoun))
            columnIndex = headers.indexOf(firstNoun);
        else
            throw new IllegalArgumentException("Unknown first noun word");
        ArrayList<Integer> rows = new ArrayList<Integer>(); // non zero rows for word column
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i][columnIndex - 1] != 0)
                rows.add(i);
        }
        return rows;
    }

}
