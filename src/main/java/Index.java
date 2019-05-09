import com.google.gson.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;

/**
 * Created by Катерина on 05.03.2019.
 */

public class Index {

    private static JsonObject MSC = new JsonObject();

    public static void main(String[] argv) {
        String basePath = FilePath.basePath.toString();
        getMSCfromArticles(basePath + FilePath.englishArticles.toString(),
                basePath + FilePath.articleIndexes.toString());

        getMSCFromFile(basePath + FilePath.categoriesMSC.toString(),
                basePath + FilePath.articleIndexes.toString());

//        bindArticlesAndMSC();
    }
    private static void getMSCfromArticles(String filePath, String resultPath) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(in, JsonArray.class);
            for (int i = 0; i < array.size(); ++i) {
                JsonObject object = array.get(i).getAsJsonObject();
                String title = GeneralUtils.parseTitle(object.get("name").getAsString());

                String rawText = object.get("text").getAsString();
                String newText = rawText.replaceAll("<[^>]*>", " ");
                GeneralUtils.extractMSC(title, newText, MSC);
            }
            FileWriter fileMSC = new FileWriter(resultPath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String resultString = gson.toJson(MSC);
            fileMSC.write(resultString);
            fileMSC.flush();
            fileMSC.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getMSCFromFile(String filePath, String resultPath) {
//        System.out.println("1: " + MSC.keys().size());
        try {
            BufferedReader in = new BufferedReader(new FileReader(resultPath));
            MSC = new Gson().fromJson(in, JsonObject.class);
//            System.out.println(MSC.keySet().size());
            Document indexesXML = null;
            indexesXML = Jsoup.parse(new File(filePath), "UTF-8");
            if (indexesXML == null) return;

            Elements categories = indexesXML.getElementsByTag("category");
            for (Element category: categories) {
                Elements newIndexes = category.getElementsByTag("msc");
                if (newIndexes.size() < 1) continue;
                Elements articles = category.getElementsByTag("item");

                for (Element article:articles) {
                    String title = article.text();
                    title = title.replaceAll("\\u2013", "-");

                    JsonArray indexes = new JsonArray();
                    if (MSC.keySet().contains(title)) {
//                        System.out.println(title);
//                        if (newIndexes.size() > 1) continue;
                        indexes = MSC.get(title).getAsJsonArray();
                        MSC.remove(title);
                    }

                    for (Element indexElement : newIndexes) {
                        String code = indexElement.ownText().replaceAll("--", "-").toUpperCase();
                        if (code.length() == 3) code = code + "XX";
                        if (code.length() == 4)
                            code = code.substring(0, 2) + "-" + code.substring(2, 4);
                        if (!indexes.contains(new JsonPrimitive(code)) && !GeneralUtils.isParent(indexes, code)) {
                            indexes.add(code);
                            String parent = GeneralUtils.findParent(indexes, code);
                            if (parent.length() > 0) indexes.remove(new JsonPrimitive(parent));
                        }
                    }

                    MSC.add(title, indexes);
                }
            }
            System.out.println(MSC.keySet().size());
            FileWriter fileMSC = new FileWriter(resultPath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String resultString = gson.toJson(MSC);
            fileMSC.write(resultString);
            fileMSC.flush();
            fileMSC.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
