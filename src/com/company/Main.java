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

import static java.util.stream.Collectors.toList;

public class Main {

    private static ArrayList<String> headers;
    static DB db;
    private static Integer[][] matrix;
    private static Info firstNoun;
    private final static MyStem mystemAnalyzer = new Factory("-igd --eng-gr --format json --weight").newMyStem("3.0", Option.<File>empty()).get();

    public static void main(final String[] args) throws MyStemApplicationException, IOException, SQLException, ClassNotFoundException {

//        List<FileType> list = new ArrayList<>();
//        ReadCSV.read("../nmzk/data/products.csv", list, "products");

        loadData();

        Product p = db.getProduct(120848643);

        String name = "set prisma";
        String measure = "1";
        Product product = new Product("", "", name, measure, 0.0, 0, "", "", "");

        ArrayList<String> lemmatizedArray = processRequest(product);

        //TODO form a vector, step 4
        Integer[] requestVector = new Integer[0];

        List<Product> result = workWithDTM(product, requestVector);
    }

    private static void loadData() {
        ArrayList<FileType> dfm = new ArrayList<>();
        headers = ReadCSV.read("../nmzk/data/dfm.csv", dfm, "dfm");
        matrix = ReadCSV.formMatrix(dfm);
        db = new DB();
        db.connectDb();
    }

    private static ArrayList<String> processRequest(Product product) throws MyStemApplicationException {
        String request = Product.replacer(product.productName);
        Iterable<Info> result =
                JavaConversions.asJavaIterable(
                        mystemAnalyzer
                                .analyze(Request.apply(request))
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
        ArrayList<Integer> nonZeroRows = RowsFinder.findNonZeroRows(product.productName, headers, matrix);
        List<Product> products = new ArrayList<>();
        if (nonZeroRows.size() == 0) // we don't have a row or more with all the words
        {
            try {
                ArrayList<Integer> nonZeroFirstNounRows = RowsFinder.findFirstNounRows(firstNoun.toString(), headers, matrix);
                products = FilterData.filterByCos(nonZeroFirstNounRows, matrix, 0.7, requestVector);
            } catch (NullPointerException e) {
                System.err.println("Didn't find the first noun in request");
                Collections.sort(headers);
                String firstTerm = "";
                for (String word : headers) {
                    if (word.startsWith("" + product.productName.charAt(0))) {
                        firstTerm = word;
                        break;
                    }
                }
                ArrayList<Integer> nonZeroFirstTermRows = RowsFinder.findFirstNounRows(firstTerm, headers, matrix);
                if (nonZeroFirstTermRows.size() == 0)
                    return products;
                else
                    products = FilterData.filterByCos(nonZeroFirstTermRows, matrix, 0.7, requestVector);
            }

        } else {
            products = FilterData.filterByCos(nonZeroRows, matrix, 0.5, requestVector);
        }
        products = FilterData.filterByMeasure(products, product.measure);
        products = FilterData.filterByRegions(products, product.regionsString);
        products = FilterData.filterByOKPD(products, product.ocpd2CodesString);
        products = FilterData.filterByPercentile(products);
        products = products.stream().sorted(Comparator.comparing(Product::getCos)).collect(toList());
        return products;
    }
}
