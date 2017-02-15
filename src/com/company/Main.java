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
import java.util.Arrays;

public class Main {

    private final static MyStem mystemAnalyzer = new Factory("-igd --eng-gr --format json --weight").newMyStem("3.0", Option.<File>empty()).get();

    public static void main(final String[] args) throws MyStemApplicationException {
        String productName = "соленая домик полки"; //Product name
        productName = productName.replace(" ", "%20");

        String regionsString = Arrays.toString(new Integer[]{12, 57, 77});//Regions list
        String ocpd2CodesString = Arrays.toString(new String[]{"17.12.14.160", "17.12.14.162"});//OCPD2 classificator codes
        //measure = enc2utf8 //measure

        final Iterable<Info> result =
                JavaConversions.asJavaIterable(
                        mystemAnalyzer
                                .analyze(Request.apply(productName))
                                .info()
                                .toIterable());
        Info firstNoun = null;
        for (final Info info : result) {
            JSONObject jObject  = new JSONObject(info.rawResponse());
            String analysis = jObject.get("analysis").toString();
            analysis = analysis.replace("[", "");
            analysis = analysis.replace("]", "");
            String gr = new JSONObject(analysis).get("gr").toString();
            if (firstNoun == null && gr.charAt(0) == 'S')
                firstNoun = info;
            System.out.println(info.initial() + " -> " + info.lex() + " | " + info.rawResponse());
        }

        double y = 0.5;

    }
}
