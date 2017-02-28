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

public class Main {

    private final static MyStem mystemAnalyzer = new Factory("-igd --eng-gr --format json --weight").newMyStem("3.0", Option.<File>empty()).get();

    public static void main(final String[] args) throws MyStemApplicationException, IOException, SQLException, ClassNotFoundException {
        DB db = new DB();
        db.connectDb();
        Product product = db.getProduct(118583781);

        Iterable<Info> result =
                JavaConversions.asJavaIterable(
                        mystemAnalyzer
                                .analyze(Request.apply(product.productName))
                                .info()
                                .toIterable());

        ArrayList<String> lemmatizedArray = formLemmatizedArray(result);

        double y = 0.5;
    }

    private static ArrayList<String> formLemmatizedArray(Iterable<Info> result)
    {
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

}
