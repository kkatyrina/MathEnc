import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Катерина on 05.03.2019.
 */
public class Matcher {
    static List<String> russian = new ArrayList<>();
    static List<String> english = new ArrayList<>();
    static List<String> enTitles = new ArrayList<>();
    static List<String> ruTitles = new ArrayList<>();

    public static void main(String[] argv) {
        String basePath = FilePath.basePath.toString();

        GeneralUtils.getArticlesJson(FilePath.basePath.toString() + FilePath.russianArticlesParsed.toString(),
                ruTitles, new ArrayList<>(), true);
        GeneralUtils.getArticlesTxt(FilePath.basePath.toString() + FilePath.russianArticlesNormShort.toString(),
                new ArrayList<>(), russian,true);
        GeneralUtils.getArticlesTxt(FilePath.basePath.toString() + FilePath.englishArticlesParsed.toString(),
                enTitles, new ArrayList<>(), true);
        GeneralUtils.getArticlesTxt(FilePath.basePath.toString() + FilePath.englishArticlesNormalized.toString(),
                new ArrayList<>(), english, true);

        russian = GeneralUtils.cleanNonCyrillic(russian);

//        matchArticles(basePath + FilePath.articleMatch.toString());

        matchArticlesRussian(basePath + FilePath.russianArticlesRelated1);
    }

    public static void matchArticles(String filePath) {

        int foundArticle = 0;
        JsonObject match = new JsonObject();
//        String line = "";
//        List<String> russianToMatch = new ArrayList<>();
//        List<Integer> articleIndexes = new ArrayList<>();
//        int step = matchTitlesIndexes.size() / 100;
//        for (int i = 0; i < matchTitlesIndexes.size(); i++) {
//            String article = russian.get(matchTitlesIndexes.get(i));
//            String title = ruOriginal.get(matchTitlesIndexes.get(i));
////            System.out.println("title: "+title);
////            int bound = article.length() > 100? 100 : article.length();
////            System.out.println("    article: "+article.substring(0, bound));
//            russianToMatch.add(article);
//            articleIndexes.add(matchTitlesIndexes.get(i));
//        }

//        GeneralUtils.docs = russianToMatch.size();
        GeneralUtils.docs = russian.size();
//            System.out.println(titlesRu);
//        List<Integer> res = Mapper.map(russianToMatch, english, 0.8f);
        List<Integer> res = Mapper.map(russian, english, 1.0f);
//            System.out.println(res);
        long start = System.nanoTime();
        // поиск смысла жизни ...

//        for (int i = 0; i < russianToMatch.size(); ++i) {
        for (int i = 0; i < russian.size(); ++i) {
//            String article = russianToMatch.get(i);
//            int bound1 = article.length() > 100? 100 : article.length();
//            System.out.println("match this: "+article.substring(0, bound1));
//            System.out.println("result: "+res.get(i));
            String matcher = "";
            if (res.get(i) != -1) {
                foundArticle++;
                matcher = enTitles.get(res.get(i));
                match.addProperty(ruTitles.get(i), matcher);
            }
//            match.addProperty(ruOriginal.get(articleIndexes.get(i)), matcher);
//            match.addProperty(ruOriginal.get(i), matcher);
        }
        long finish = System.nanoTime();
        long timeConsumedMillis = finish - start;
        System.out.println("found: "+ foundArticle);
        System.out.println("time: "+ timeConsumedMillis);
        try {
            FileWriter file = new FileWriter(filePath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            file.write(gson.toJson(match));
            file.flush();
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void matchArticlesRussian(String filePath) {
        int foundArticle = 0;
        JsonObject match = new JsonObject();
        GeneralUtils.docs = russian.size();
        long start = System.nanoTime();
        // поиск смысла жизни ...

//        for (int i = 0; i < russianToMatch.size(); ++i) {
        for (int i = 200; i < russian.size(); ++i) {
            List<Integer> res = Mapper.mapRussian(russian.get(i), i, russian, 1.0f);
            int count = 0;
            JsonArray binds = new JsonArray();
            while (res.size() > count) {
                if (res.get(count) != -1) {
//                    foundArticle++;
                    String matcher = ruTitles.get(res.get(count));
                    binds.add(matcher);
                }
                ++count;
            }
            match.add(ruTitles.get(i), binds);

//            match.addProperty(ruOriginal.get(articleIndexes.get(i)), matcher);
//            match.addProperty(ruOriginal.get(i), matcher);
        }
        long finish = System.nanoTime();
        long timeConsumedMillis = finish - start;
//        System.out.println("found: "+ foundArticle);
        System.out.println("time: "+ timeConsumedMillis + "ns");
        try {
            FileWriter file = new FileWriter(filePath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            file.write(gson.toJson(match));
            file.flush();
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
