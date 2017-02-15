package com.company;

import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.Option;
import scala.collection.JavaConversions;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

public class Main {

    public static void method() {
        String productName = "Бумага офисная"; //Наименование товара (строка) - обязательное поле
        productName = productName.replace(" ", "%20");
        String regionsString = Arrays.toString(new Integer[]{12, 57, 77});//Список регионов - необязательное поле - может быть несколько
        String ocpd2CodesString =  Arrays.toString(new String[]{"17.12.14.160", "17.12.14.162"});//Коды по классификатору ОКПД2 - необязательное поле - может быть несколько
        //measure = enc2utf8 //Мера измерения - необязательное поле
        try {
            String urlString = "http:127.0.0.1:5469/get_closest?name=[" + productName + "]&okdp=" + ocpd2CodesString + "&regions=" + regionsString;
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
            in.close();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private final static MyStem mystemAnalyzer =
            new Factory("-igd --eng-gr --format json --weight")
                    .newMyStem("3.0", Option.<File>empty()).get();

    public static void main(final String[] args) throws MyStemApplicationException {

        final Iterable<Info> result =
                JavaConversions.asJavaIterable(
                        mystemAnalyzer
                                .analyze(Request.apply("И вырвал грешный мой язык"))
                                .info()
                                .toIterable());

        for (final Info info : result) {
            System.out.println(info.initial() + " -> " + info.lex() + " | " + info.rawResponse());
        }
    }
}
