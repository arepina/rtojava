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
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    private final static MyStem mystemAnalyzer = new Factory("-igd --eng-gr --format json --weight").newMyStem("3.0", Option.<File>empty()).get();

    public static void main(final String[] args) throws MyStemApplicationException {
        String productName = Main.replacer("соленая белый полки"); //Product name

        String regionsString = Arrays.toString(new Integer[]{12, 57, 77});//Regions list
        String ocpd2CodesString = Arrays.toString(new String[]{"17.12.14.160", "17.12.14.162"});//OCPD2 classificator codes
        //measure = enc2utf8 //measure

        Iterable<Info> result =
                JavaConversions.asJavaIterable(
                                mystemAnalyzer
                                .analyze(Request.apply(productName))
                                .info()
                                .toIterable());

        Info firstNoun = null;
        ArrayList<String> lemmatizedArray = new ArrayList<String>();
        for (final Info info : result) {
            JSONObject jObject  = new JSONObject(info.rawResponse());
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

        double y = 0.5;
    }

    private static String replacer(String item) {
        item = item.replaceAll("\"", "");
        item = item.replaceAll("(\\d)(\\D)", "\\1\\ \\2");
        item = item.replaceAll("(\\D)(\\d)", "\\1\\ \\2");
        item = item.replaceAll("(\\D)(\\.|,)(\\D)", "\\1\\ \\3");

        item = item.replace("\r", " ");
        item = item.replace("\t", " ");
        item = item.replace("\n", " ");
        item = item.replace(",", "");
        item = item.replace(".", "");
        item = item.replace("•", "");
        item = item.replace(";", "");
        item = item.replace(":", "");
        item = item.replace("!", "");
        item = item.replace("?", "");
        item = item.replace(")", "");
        item = item.replace("(", "");
        item = item.replace("™", "");
        item = item.replace("®", "");
        item = item.replace("*", "");
        item = item.replace("/", "");
        item = item.replace("—", "");
        item = item.replace("-", "");
        item = item.replace("~", "");
        item = item.replace("'", "");
        return item.toLowerCase();
    }

}
