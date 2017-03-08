package com.company;

import javafx.util.Pair;
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

import static java.util.stream.Collectors.toList;

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
                calculateCos(nonZeroFirstNounRows, matrix, 0.7);
            } catch (NullPointerException e) {
                System.err.println("Didn't find the first noun in request");
                Collections.sort(headers);
                String firstTerm = "";
                for(String word : headers)
                {
                    if (word.startsWith("" + request.charAt(0)))
                    {
                        firstTerm = word;
                        break;
                    }
                }
                ArrayList<Integer> nonZeroFirstTermRows = findFirstNounRows(firstTerm, headers, matrix);
                if(nonZeroFirstTermRows.size() == 0)
                    throw new IllegalArgumentException("Didn't find anything, empty products lisy");
                else
                    calculateCos(nonZeroFirstTermRows, matrix,0.7);
            }

        } else {
            calculateCos(nonZeroRows, matrix, 0.5);
        }
    }

    private static void calculateCos(ArrayList<Integer> nonZeroRows, Integer[][] matrix, double y) {
        List<Pair<Integer, Double>> cosineValues = new ArrayList<>();
        List<Integer> productID = new ArrayList<>();
        for (Integer row : nonZeroRows) {
            //todo form the vector for a row and calc the cosineSimilarity(int[] vectorA, int[] vectorB)
            cosineValues.add(row, cosineSimilarity(vectA, vectB));
        }
        List<Pair<Integer, Double>> cosineValuesFiltered = cosineValues.stream().filter(c -> c.getValue() > y).collect(toList());
        for(Pair<Integer, Double> row : cosineValuesFiltered)
            productID.add(matrix[0][row.getKey()]);
        processProductIDs(productID, cosineValuesFiltered);
    }

    private static void processProductIDs(List<Integer> productID, List<Pair<Integer, Double>> cosineValuesFiltered) {
        List<Product> products = new ArrayList<>();
        for(Integer id : productID)
        {
            try {
                Product p = db.getProduct(id);
                products.add(p);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalStateException("Error in DB of products");
            }
        }
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
        ArrayList<Integer> rows = new ArrayList<>(); // non zero rows for word column
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i][columnIndex - 1] != 0)
                rows.add(i);
        }
        return rows;
    }

    private static double cosineSimilarity(int[] vectorA, int[] vectorB) {
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


}
