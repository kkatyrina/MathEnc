import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import org.apache.jena.query.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Катерина on 21.04.2019.
 */

public class AnnotatorTest {
    private static String basePath = FilePath.basePath.toString();

    private static List<String> articlesToAnnotate = new ArrayList<>();
    private static List<String> titlesForAnnotation = new ArrayList<>();
    private static List<String> articlesForAnnotation = new ArrayList<>();
    private static List<String> originalTitles = new ArrayList<>();
    private static List<String> originalArticles = new ArrayList<>();

    private static float minScore = 0.1f;

    public static void main(String[] argv) {
        GeneralUtils.getArticlesJson(basePath + FilePath.russianArticlesNormFull.toString(),
                titlesForAnnotation, articlesToAnnotate, false);
        GeneralUtils.getArticlesTxt(basePath + "russianArticlesTokenizedShort.txt",
                new ArrayList<>(), articlesForAnnotation, false);
        GeneralUtils.getArticlesJson(basePath + FilePath.russianArticlesParsed.toString(),
                originalTitles, originalArticles, false);
        articlesToAnnotate = GeneralUtils.cleanReplacements(articlesToAnnotate);

        findAnnotations(basePath + FilePath.annotationTest.toString());
        checkAnnotator(basePath + FilePath.annotationExpert.toString(),
                basePath + FilePath.annotationTest.toString());
    }

    private static void findAnnotations(String filePath) {
        JsonObject resultObject = new JsonObject();
//        JsonObject counter = new JsonObject();

        for (int articleIdx = 0; articleIdx < articlesToAnnotate.size(); articleIdx+=128) {
//        for (int articleIdx = 0; articleIdx < articlesToAnnotate.size(); articleIdx++) {
            JsonObject articleObject = new JsonObject();
            String rawArticle = articlesToAnnotate.get(articleIdx);
            String[] articleWords = rawArticle.split(" ");
            String numberedArticle = "";
            for (int wordIdx = 0; wordIdx < articleWords.length; ++wordIdx) {
                numberedArticle += (articleWords[wordIdx] + "(№" +wordIdx + ") ");
            }
            String articleTitle = originalTitles.get(articleIdx);
            List<Annotator.Data> result = Annotator.annotate(rawArticle, titlesForAnnotation, originalTitles, articlesForAnnotation,
                    originalTitles.get(articleIdx), new HashMap<>(), minScore, 1f, 20, 20, 0,
                    1f, 0.01f, 150, 0.99f, articlesForAnnotation.get(articleIdx));
            JsonArray annsArray = new JsonArray();
            for (Annotator.Data data: result) {
                JsonObject annObject = new JsonObject();
                boolean isWiki = false;
                String titleUpperCase = data.title.substring(0, 1).toUpperCase()+data.title.substring(1).toLowerCase();

                ParameterizedSparqlString qs = new ParameterizedSparqlString("" +
                        "select ?resource ?type where {\n" +
                        "{?resource ?type \"" + titleUpperCase + "\"@ru}\n" +
                        "union\n" +
                        "{?resource ?type \"" + data.title.toLowerCase() + "\"@ru}\n" +
                        "}");

//                System.out.println(qs);

                QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", qs.asQuery());
                try {
                    ResultSet results = ResultSetFactory.copyResults(exec.execSelect());

                    if (results.hasNext()) {
                        isWiki = true;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                annObject.addProperty("title", data.title);
                annObject.addProperty("index", data.titleInd);
                annObject.addProperty("original", originalTitles.get(data.titleInd));
                annObject.addProperty("start", data.min);
                annObject.addProperty("end", data.max);
                annObject.addProperty("score", data.score);
                annObject.addProperty("isWiki", isWiki);
                annsArray.add(annObject);
            }
            articleObject.addProperty("text", numberedArticle);
            articleObject.add("annotations", annsArray);
            resultObject.add(articleTitle, articleObject);
            System.out.println(articleIdx);
        }

//        Set<String> keys = counter.keySet();
//        ArrayList<String> list = new ArrayList<>(keys);
//        list.sort((o1, o2) -> {
//            if (counter.get(o1).getAsInt() < counter.get(o2).getAsInt()) {
//                return 1;
//            }
//            if (counter.get(o1).getAsInt() > counter.get(o2).getAsInt()) {
//                return -1;
//            }
//            return 0;
//        });
//
//        JsonObject newCounter = new JsonObject();
//        for (String key: list) {
//            newCounter.addProperty(key, counter.get(key).getAsInt());
//        }
//
//        try {
//            FileWriter pageRank = new FileWriter(basePath +
//                    "pageRank.json");
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            String resultString = gson.toJson(newCounter);
////            System.out.println(resultString);
//            pageRank.write(resultString);
//            pageRank.flush();
//            pageRank.close();
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            FileWriter file = new FileWriter(filePath, false);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String resultString = gson.toJson(resultObject);
            file.write(resultString);
            file.flush();
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkAnnotator(String expertPath, String annotatorPath) {
        JsonObject expert = new JsonObject();
        JsonObject annotator = new JsonObject();

        try {
            JsonReader reader = new JsonReader(new FileReader(expertPath));
            expert = new Gson().fromJson(reader, JsonObject.class);
            reader = new JsonReader(new FileReader(annotatorPath));
            annotator = new Gson().fromJson(reader, JsonObject.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        int expertAmountTotal = 0;
        int annotatorAmountTotal = 0;
        int correctTotal = 0, correctLocal = 0, expertOnly = 0, annotatorOnly = 0;
        int differentPlace = 0;
        for (String key: expert.keySet()) {
            correctLocal = 0;
            JsonArray expertAnnotations = expert.get(key).getAsJsonObject().get("annotations").getAsJsonArray();
            JsonArray annotatorAnnotations = annotator.get(key).getAsJsonObject().get("annotations").getAsJsonArray();
            int expertAmount = expertAnnotations.size();
            int annotatorAmount = annotatorAnnotations.size();
            expertAmountTotal += expertAmount;
            annotatorAmountTotal += annotatorAmount;

            for (int i = 0; i < expertAmount; ++i) {
                JsonObject eAnnotation = expertAnnotations.get(i).getAsJsonObject();
                for (int j = 0; j < annotatorAmount; ++j) {
                    JsonObject aAnnotation = annotatorAnnotations.get(j).getAsJsonObject();
                    if (eAnnotation.get("index").getAsInt() == aAnnotation.get("index").getAsInt()) {
                        if (eAnnotation.get("start").getAsInt() == aAnnotation.get("start").getAsInt() &&
                                eAnnotation.get("end").getAsInt() == aAnnotation.get("end").getAsInt()) {
                            ++correctLocal;
                        }
                        else {
                            ++differentPlace;
                            ++correctLocal;
                        }
                    }
                }
            }
            if (expertAmount > correctLocal) {
                expertOnly += expertAmount - correctLocal;
            }
            if (annotatorAmount > correctLocal) {
                annotatorOnly += annotatorAmount - correctLocal;
            }
            correctTotal += correctLocal;
        }
        System.out.println("=====RESULT=====");
        System.out.println("CorrectTotal: "+correctTotal);
        System.out.println("DifferentPlace: "+differentPlace);
        System.out.println("ExpertOnly: "+expertOnly);
        System.out.println("AnnotatorOnly: "+annotatorOnly);
        System.out.println("ExpertTotal: "+expertAmountTotal);
        System.out.println("AnnotatorTotal: "+annotatorAmountTotal);
        float precision = correctTotal / ((correctTotal+annotatorOnly)*1f);
        float recall = correctTotal / ((correctTotal+expertOnly)*1f);
        System.out.println("Precision: "+precision);
        System.out.println("Recall: "+recall);
        System.out.println("F1: "+2f*(precision*recall/(precision+recall)));
    }
}
