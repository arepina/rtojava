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
import java.util.Iterator;
import java.util.Map;

public class Main {

    private final static MyStem mystemAnalyzer = new Factory("-igd --eng-gr --format json --weight").newMyStem("3.0", Option.<File>empty()).get();

    public static void main(final String[] args) throws MyStemApplicationException, IOException, SQLException, ClassNotFoundException {
        ArrayList<FileType> dfm = new ArrayList<FileType>();
        ArrayList<String> headers = ReadCSV.read("../nmzk/data/dfm.csv", dfm, "dfm");
        Integer[][] matrix = ReadCSV.formMatrix(dfm);
        DB db = new DB();
        db.connectDb();

        String str = "Бумага офисная";
        str = Product.replacer(str);
        //Product product = db.getProduct(118583781);

        Iterable<Info> result =
                JavaConversions.asJavaIterable(
                        mystemAnalyzer
                                .analyze(Request.apply(str))
                                .info()
                                .toIterable());

        ArrayList<String> lemmatizedArray = formLemmatizedArray(result);

        double y = 0.5;
    }

    private static ArrayList<String> formLemmatizedArray(Iterable<Info> result) {
        Info firstNoun = null;
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
                throw new IllegalArgumentException("Unknown word");
        }
        Map<String, ArrayList<Integer>> rowsWords = new HashMap<String, ArrayList<Integer>>(); // non zero rows for each word column
        for (Object o : columnWords.entrySet()) {// get the non zero rows for each word column
            Map.Entry pair = (Map.Entry) o;
            rowsWords.put(pair.getKey().toString(), null);
            int columnIndex = Integer.parseInt(pair.getValue().toString());
            ArrayList<Integer> rows = new ArrayList<Integer>();
            if (rowsWords.get(pair.getKey().toString()) != null)
                rows = rowsWords.get(pair.getKey().toString());
            for(int i = 0; i < matrix.length; i++)
            {
                if (matrix[i][columnIndex] != 0)
                    rows.add(i);
            }
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
        for(Integer rowNumber: rowsNumber.keySet())
        {
            if (rowsNumber.get(rowNumber) == columnWords.keySet().size())
                resultRows.add(rowNumber);
        }
        return resultRows;
    }

}
