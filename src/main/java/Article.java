/**
 * Created by Катерина on 07.03.2019.
 */
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Article {
    private static List<String> keys = new ArrayList<>(Arrays.asList(
            "trnsl.1.1.20190313T095920Z.f86b19b4ab9d5866.a1e440162ec0d18e3a15f06ba4be9b6d541fcb71",
            "trnsl.1.1.20190321T090956Z.76d75fa3ae6b5baf.4aec2a9e6f1c3e029447d5dd519f5d2670e83ebd",
            "trnsl.1.1.20190321T140647Z.c1d8b3c10b0b3586.0dfc2fad5c98ac15b2e09a1cc44d786a9be9db32"
    ));
    public static void main(String[] args) {
        String basePath = FilePath.basePath.toString();
//        Предобработка английских статей с сохранением в файл
//        parseEnglishArticles(FilePath.basePath.toString() + FilePath.englishArticles.toString(),
//                FilePath.basePath.toString() + FilePath.englishArticlesParsed.toString());
//
//        //Перевод английских статей с сохранением в файл
//        translateEnglishArticles(FilePath.basePath.toString() + FilePath.englishArticlesParsed.toString(),
//                FilePath.basePath.toString() + FilePath.englishArticlesTranslated.toString());

        //Нормализация английских статей с сохранением в файл
//        normalizeEnglishArticles(FilePath.basePath.toString() + FilePath.englishArticlesTranslated.toString(),
//                FilePath.basePath.toString() + FilePath.englishArticlesNormalized.toString());

        //Предобработка русских статей с сохранением в файл
        parseRussianArticles(basePath + FilePath.russianArticles.toString(),
                basePath + FilePath.russianArticlesParsed.toString());

        //Нормализация русских статей с сохранением полных и кратких версий
        normalizeRussianArticles(basePath + FilePath.russianArticlesParsed.toString(),
                basePath + FilePath.russianArticlesNormShort.toString(),
                basePath + FilePath.russianArticlesNormFull.toString());
    }
    private static void parseEnglishArticles(String filePath, String resultPath) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            Gson gson = new Gson();
            JsonArray array = gson.fromJson(in, JsonArray.class);
            String articles = "";

            for (int i = 0; i < array.size(); ++i) {
                JsonObject object = array.get(i).getAsJsonObject();
                String title = GeneralUtils.parseTitle(object.get("name").getAsString());
                String rawText = object.get("text").getAsString();
                Document html = Jsoup.parse(rawText);
                Element toc = html.selectFirst("div.toc");
                if (toc != null) {
                    html.select("div.toc").first().remove();
                }
                String newText = rawText;
                newText = html.toString();
                newText = newText.replaceAll("\n", "");
                newText = newText.replaceAll("<[^>]*>", " ");
                if (newText.charAt(0) == '"') newText = newText.substring(1);
                if (newText.charAt(newText.length() - 1) == '"') newText = newText.substring(0, newText.length() - 1);

                int refIndex = newText.indexOf("References");
                int citeIndex = newText.indexOf("How to Cite This Entry");
                int comIndex = newText.indexOf("Comments");
                int min = GeneralUtils.minIndex(refIndex, GeneralUtils.minIndex(citeIndex, comIndex));
                if (min > -1) newText = newText.substring(0, min);

                newText = GeneralUtils.removeBetween("begin{equation}", "end{equation}", newText, new ArrayList<String>(), 0, false);
                newText = GeneralUtils.removeBetween("2010 Mathematics Subject Classification", "ZBL", newText, new ArrayList<String>(), 0, false);

                newText = newText.replaceAll("\\[[^\\]]+\\]", " "); //remove []
                newText = newText.replaceAll("[\\$]+[^\\$]*[\\$]+", " "); //remove formulas
                newText = newText.replaceAll("[ ]+", " "); //remove multiple spaces
                while (newText.charAt(0) == ' ') newText = newText.substring(1);

                newText = newText.substring(0, GeneralUtils.minIndex(newText.length(), 2000));
                articles = articles + title + " --:-- " + newText + "\n";
                System.out.println(i);
            }
            FileWriter file = new FileWriter(resultPath);
            file.write(articles);
            file.flush();
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void translateEnglishArticles(String filePath, String resultPath) {
        List<String> articles = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String article;
            while ((article = in.readLine()) != null) {
                articles.add(article);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String translatedArticles = "";
        int keyIndex = 0;
        int currentIndex = 0;
        int endIndex = articles.size();
        boolean outputFlag = false;
        try {
            File preFile = new File(resultPath);
            if (preFile.length() > 0) {
                outputFlag = true;
            }
            FileWriter file = new FileWriter(resultPath, true);
            int tries = 1;
            while (currentIndex < endIndex && tries <= keys.size()) {
                String key = keys.get(keyIndex);
                String article = articles.get(currentIndex);
//                System.out.println(article);
                String[] parts = article.split("[ ]+--:--[ ]+");
                String title = GeneralUtils.yandexTranslate(parts[0], key);
                String text = GeneralUtils.yandexTranslate(parts[1], key);
                if (title.equals("") || text.equals("")) {
                    System.out.println("not translated: " + currentIndex + " with " + keyIndex);
//                        if (tries == keys.size()) break;
                    ++tries;
                    keyIndex = (keyIndex + 1) % keys.size();
                } else {
                    System.out.println(currentIndex + " with key " + keyIndex);
                    translatedArticles += (title + " --:-- " + text + "\n");
                    currentIndex++;
                    tries = 1;
                }
            }
            file.write(translatedArticles);
            file.flush();
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (outputFlag) {
            System.out.println("Переведённые статьи были сохранены в конец файла");
        }
    }

    private static void normalizeEnglishArticles(String filePath, String resultPath) {
        try {
            File file = new File(resultPath);
            if (file.length() > 0) {
                boolean success = file.delete();
                if (!success) {
                    throw new FileAlreadyExistsException("Can not delete existing file");
                }
            }
            int count = 0;

            String inputPath = FilePath.basePath.toString() + "input.txt";
            String outputPath = FilePath.basePath.toString() + "output.txt";
            File output = new File(outputPath);
            File input = new File(inputPath);
            if (!output.exists()) {
                boolean success = output.createNewFile();
                if (!success) throw new FileNotFoundException("Can not create new file");
            }
            String command = FilePath.basePath.toString() + FilePath.mystemCommand.toString() +
                    inputPath.replaceAll("[/]+", "/") + " " +
                    outputPath.replaceAll("[/]+", "/");
            System.out.println(command);

            BufferedReader in = new BufferedReader(new FileReader(filePath));
            FileWriter result = new FileWriter(resultPath);
            String line;
            String articles = "";
            while ((line = in.readLine()) != null) {
                count++;
                String[] parts = line.split("[ ]+--:--[ ]+");
                String title = parts[0], text = parts[1];
                String titleLemmas = GeneralUtils.runMystem(inputPath, outputPath, command, title);
                String textLemmas = GeneralUtils.runMystem(inputPath, outputPath, command, text);
                articles = titleLemmas + " --:-- " + textLemmas + "\n";
                result.write(articles);
                result.flush();
                System.out.println(count);
            }
            result.close();
            boolean inputFlag = input.delete();
            boolean outputFlag = output.delete();
            if (!inputFlag || !outputFlag) {
                throw new FileAlreadyExistsException("Can not delete existing file");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseRussianArticles(String filePath, String resultPath) {
        try {
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);
            String articles = "";
            JsonArray result = new JsonArray();

            for (int i = 0; i < array.size(); ++i) {
                JsonObject object = array.get(i).getAsJsonObject();
                String title = GeneralUtils.parseTitle(object.get("name").getAsString());
                title = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();

                String newText = object.get("text").getAsString();
                List<String> cut = new ArrayList<>();
                newText = GeneralUtils.removeBetween("<!-- *** native", "push({});", newText, cut, cut.size(), false);
                newText = GeneralUtils.removeBetween("<!-- *** buf", "<!-- *** text *** -->", newText, cut, cut.size(), false);

                String literature = "";
                int literatureIndex = newText.lastIndexOf("Лит.");
                if (literatureIndex > 0) {
                    literature = newText.substring(literatureIndex);
                    newText = newText.substring(0, literatureIndex);
                }

                newText = GeneralUtils.removeBetween("<", ">", newText, cut, cut.size(), true);
                newText = newText.replaceAll("\n", " newline ");
                newText = newText.replaceAll("\t", " tabulation ");
                newText = newText.replaceAll("\r", " return ");
                if (newText.charAt(0) == '"') newText = newText.substring(1);
                if (newText.endsWith("\"")) newText = newText.substring(0, newText.length()-1);
                newText = newText.replaceAll("[ ]+", " "); //remove multiple spaces
                while (newText.charAt(0) == ' ' || newText.charAt(0) == '-')
                    newText = newText.substring(1);
                JsonArray jsonCut = new JsonArray();
                for (String item: cut) {
                    jsonCut.add(new JsonPrimitive(item));
                }

                JsonObject article = new JsonObject();
                article.addProperty("title", title);
                article.addProperty("id", i);
                article.addProperty("text", newText);
                article.addProperty("literature", literature);
                article.add("cut", jsonCut);
                result.add(article);

                articles = articles + title + " --:-- " + newText + "\n";
                System.out.println(i);
            }

            FileWriter file = new FileWriter(resultPath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String resultString = gson.toJson(result);
            file.write(resultString);
            file.flush();
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void normalizeRussianArticles(String sourcePath, String shortResultPath, String fullResultPath) {
        JsonArray result = new JsonArray();
        try {
            File shortResultFile = new File(shortResultPath);
            if (shortResultFile.length() > 0) {
                boolean success = shortResultFile.delete();
                if (!success) throw new FileAlreadyExistsException("Can not delete existing file");
            }
            BufferedReader in = new BufferedReader(new FileReader(sourcePath));
            Gson gson = new Gson();
            JsonArray array = gson.fromJson(in, JsonArray.class);

//            String line;
            String inputPath = FilePath.basePath.toString() + "input.txt";
            String outputPath = FilePath.basePath.toString() + "output.txt";

            File output = new File(outputPath);
            File input = new File(inputPath);

            if (!output.exists()) {
                boolean success = output.createNewFile();
                if (!success) throw new FileNotFoundException("Can not create new file");
            }

            String command = FilePath.basePath.toString() + FilePath.mystemCommand.toString() +
                    inputPath.replaceAll("[/]+", "/") + " " +
                    outputPath.replaceAll("[/]+", "/");

            for (int i = 0; i < array.size(); ++i) {
                JsonObject object = array.get(i).getAsJsonObject();
                JsonArray cut = object.get("cut").getAsJsonArray();
                String title = object.get("title").getAsString();
                String text = object.get("text").getAsString();

                String titleLemmas = GeneralUtils.runMystem(inputPath, outputPath, command, title);
                String textLemmas = GeneralUtils.runMystem(inputPath, outputPath, command, text);

                String shortTextLemmas = textLemmas.replaceAll("@", "");
                shortTextLemmas = shortTextLemmas.replaceAll("[ ]+", " ");
                if (shortTextLemmas.length() > 2000) {
                    shortTextLemmas = shortTextLemmas.substring(0, 2000);
                }
                FileWriter global_output = new FileWriter(shortResultPath, true);
                String lemmas = titleLemmas + " --:-- " + shortTextLemmas + "\n";
                global_output.write(lemmas);
                global_output.close();

                JsonObject article = new JsonObject();
                article.addProperty("title", titleLemmas);
                article.addProperty("id", i + 1);
                article.addProperty("text", textLemmas);
                article.addProperty("literature", object.get("literature").getAsString());
                article.add("cut", cut);
                result.add(article);

                System.out.println(i+1);
            }

            FileWriter file = new FileWriter(fullResultPath, false);
            gson = new GsonBuilder().setPrettyPrinting().create();
            String resultString = gson.toJson(result);
            file.write(resultString);
            file.flush();
            file.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}
