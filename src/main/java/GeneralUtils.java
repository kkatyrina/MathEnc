//import com.google.gson.*;
//import com.google.gson.stream.JsonReader;
//import org.apache.jena.base.Sys;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.InputStreamReader;
//import java.lang.reflect.Array;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.net.URLEncoder;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.StringTokenizer;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Created by must on 16.11.2017.
// */
//public class GeneralUtils {
//
//    private static final int SHINGLE_LEN = 2;
//
//    static int docs = 0;
//
//    static List<ArrayList<Integer>> shingles = new ArrayList<>();
//
//    private static final String STOP_SYMBOLS[] = {".", ",", "!", "?", ":", ";", "-", "—", "\\", "/", "*", "(", ")", "+", "@",
//            "#", "$", "%", "^", "&", "=", "'", "\"", "[", "]", "{", "}", "|"};
//    private static final String STOP_WORDS_RU[] = {"это", "как", "так", "и", "в", "над", "к", "до", "не", "на", "но", "за",
//            "то", "с", "ли", "а", "во", "от", "со", "для", "о", "же", "ну", "вы",
//            "бы", "что", "кто", "он", "она"};
//
//    public static float getScoresForTwoTexts(String[] toksFirst, String[] toksSecond, int shingleLength){
//        ArrayList<Integer> shinglesFirst = new ArrayList<>();
//        return compare(shinglesFirst, genShingle(toksSecond, shingleLength));
//    }
//
//    public static float getScoresForTwoTexts(String[] toksFirst, String[] toksSecond){
//        return getScoresForTwoTexts(toksFirst, toksSecond, SHINGLE_LEN);
//    }
//
//    public static List<Integer> getScoresShingles(String[] toks, int count, double min_score, boolean expanded, List<Float> result, int shingleLength){
//        List<Integer> scores = new ArrayList<>();
//        int[] valSaved;
//        String savesShingle = "";
//        try {
//
////        for (String token: toks) System.out.println("toks: " + token);
//
//            ArrayList<Integer> shinglesThis = genShingle(toks, shingleLength);
//            List<Float> lst = new ArrayList<>();
//            for (int i = 0; i < docs; i++) {
//                lst.add(compare(shinglesThis, shingles.get(i)));
////            System.out.println("compare "+shinglesThis);
//            }
//
//            int id[] = new int[docs];
//            float val[] = new float[docs];
////            for (int i = 0; i<val.length; ++i) valSaved[i] = val[i];
//            for (int i = 0; i < docs; i++) {
//                id[i] = i;
//                val[i] = lst.get(i);
//            }
////            System.out.println("val before: ");
////            for (float value:val) System.out.print(value+" ");
////            System.out.println("");
//            for (int i = val.length - 1; i > 0; i--) {
//                for (int j = 0; j < i; j++) {
//                    if (val[j] < val[j + 1]) {
//                        float tmp = val[j];
//                        val[j] = val[j + 1];
//                        val[j + 1] = tmp;
//                        int tmp_id = id[j];
//                        id[j] = id[j + 1];
//                        id[j + 1] = tmp_id;
//                    }
//                }
//            }
////            System.out.println("val after: ");
////            for (float value:val) System.out.print(value+" ");
////            System.out.println("");
//            int i = 0;
//            while (i < docs && i < count && val[i] > min_score) {
//                scores.add(id[i]);
//                i++;
//            }
//            result.add(val[0]);
//            result.add(val[1]);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
////            System.out.println("val: ");
//        }
//        return scores;
//    }
//
//    public static List<Integer> getScoresShingles(String[] toks, int count, double min_score, boolean expanded, List<Float> result){
//        return getScoresShingles(toks, count, min_score, expanded, result, SHINGLE_LEN);
//    }
//
//    private static ArrayList<Integer> genShingle(String [] words, int shingleLength) {
//        ArrayList<Integer> shingles = new ArrayList<>();
//
////        for (String word: words) System.out.println("genShingle words: "+word);
//
//        int shinglesNumber = words.length - shingleLength;
//
//        //Create all shingles
//        for (int i = 0; i <= shinglesNumber; i++) {
//            String shingle = "";
//
//            //Create one shingle
//            for (int j = 0; j < shingleLength; j++) {
//                shingle = shingle + words[i+j] + " ";
//            }
//
//            shingles.add(shingle.hashCode());
//        }
//
//        return shingles;
//    }
//
//    public static ArrayList<Integer> genShingle(String [] words) {
//        return genShingle(words, SHINGLE_LEN);
//    }
//
//    /**
//     * Метод сравнивает две последовательности шинглов
//     *
//     * @param textShingles1New первая последовательность шинглов
//     * @param textShingles2New вторая последовательность шинглов
//     * @return процент сходства шинглов
//     */
//    public static float compare(ArrayList<Integer> textShingles1New, ArrayList<Integer> textShingles2New) {
//        if (textShingles1New == null || textShingles2New == null) return 0.0f;
//
//        int textShingles1Number = textShingles1New.size();
//        int textShingles2Number = textShingles2New.size();
//
//        float similarShinglesNumber = 0;
//
//        for (int i = 0; i < textShingles1Number; i++) {
//            if (textShingles2New.contains(textShingles1New.get(i)))
//                similarShinglesNumber += 1;
//        }
//
//        return ((similarShinglesNumber / ((textShingles1Number + textShingles2Number) / 2.0f)) * 100);
//    }
//
//    public static boolean containsTwice(List<String> list, String value) {
//        int count = 0;
//        for (String str: list) {
//            if (str.equalsIgnoreCase(value)) {
//                count++;
//                if (count > 1)
//                    return true;
//            }
//        }
//        return false;
//    }
//
//    public static int minIndex(int a, int b) {
//        if (a == -1) return b;
//        if (b == -1) return a;
//        if (a > b) return b;
//        return a;
//    }
//
//    public static String removeBetween(String sub1, String sub2, String source,
//                                        List<String> cut, int count, boolean toSave) {
////        System.out.println(source);
//        int sub1Index = source.indexOf(sub1);
//        String result = source;
//        String current = source;
//        while (sub1Index > -1) {
//            int sub2Index = current.indexOf(sub2, sub1Index + sub1.length());
////            System.out.println(sub1Index+" "+sub2Index);
////            System.out.println(current.charAt(sub1Index)+ " "+current.charAt(sub2Index));
////            System.out.println(current.substring(sub1Index, sub2Index));
//            if (sub2Index > -1) {
//                count++;
//                if (toSave) {
//                    result = current.substring(0, sub1Index) + "@" + current.substring(sub2Index + sub2.length());
////                    System.out.println(current.substring(sub1Index, sub2Index)+sub2);
//                    cut.add(current.substring(sub1Index, sub2Index)+sub2);
//                }
//                else {
//                    result = current.substring(0, sub1Index) + " " + current.substring(sub2Index + sub2.length());
//                }
//            }
//            else break;
////            System.out.println(result);
//
//            sub1Index = result.indexOf(sub1);
//            current = result;
//        }
//        return result;
//    }
//
//    public static String yandexTranslate(String requestText, String key) {
//        boolean flag = false;
//        try {
//            String requestUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate?key="
//                    + key + "&text=" + URLEncoder.encode(requestText, "UTF-8") + "&lang=en-ru";
//
//            URL url = new URL(requestUrl);
//            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
//            httpConnection.connect();
//            int rc = httpConnection.getResponseCode();
//
//            if (rc == 200) {
//                String line = null;
//                BufferedReader buffReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
//                StringBuilder strBuilder = new StringBuilder();
//                while ((line = buffReader.readLine()) != null) {
//                    strBuilder.append(line);
//                }
//                String translation = strBuilder.toString();
//                JsonObject object = new Gson().fromJson(translation, JsonObject.class);
//
//                StringBuilder sb = new StringBuilder();
//                JsonArray array = object.get("text").getAsJsonArray();
//                for (Object s : array) {
//                    sb.append(s.toString());
//                }
//                translation = sb.toString();
//                translation = translation.replaceAll("\"", "");
//                return translation;
//
//            } else {
//                return "";
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//
//    public static String runMystem(String inputPath, String outputPath, String command, String text) {
//        try {
//            FileWriter input = new FileWriter(inputPath);
//            input.write(text);
//            input.close();
//            Process process = Runtime.getRuntime().exec(command);
//            process.waitFor();
//            process.destroy();
//
//            BufferedReader output = new BufferedReader(new FileReader(outputPath));
//            String outputLine = output.readLine();
//            output.close();
//            return extractLemmas(outputLine);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//
//    private static String extractLemmas(String source) {
////        Pattern lemmasPattern = Pattern.compile("\\{[^\\}]+\\}");
////        Matcher lemmasMatcher = lemmasPattern.matcher(source);
//        String result = "";
//        int openIndex = source.indexOf('{');
//
//        while (openIndex > -1) {
//            int closeIndex = source.indexOf('}', openIndex);
//            int repeatIndex = source.indexOf('{', openIndex+1);
//            if (repeatIndex > -1 && repeatIndex < closeIndex) {
//                System.out.println("Got u!");
//                result = result + source.substring(0, repeatIndex);
//                source = source.substring(repeatIndex);
//                openIndex = source.indexOf('{');
////                continue;
//            }
//            else {
//                if (closeIndex > -1) {
//                    String lemmas = source.substring(openIndex + 1, closeIndex);
////                    System.out.println(lemmas);
//                    lemmas = lemmas.replaceAll("[?]+", "");
////                System.out.println(source);
//                    String[] parts = lemmas.split("\\|");
//                    if (parts.length > 0) {
//                        result = result + source.substring(0, openIndex) + parts[0];
//                    } else {
//                        result = result + source.substring(0, closeIndex + 1);
//                    }
//                    source = source.substring(closeIndex + 1);
//                    openIndex = source.indexOf('{');
//                } else {
//                    break;
//                }
//            }
//        }
//        result += source;
//        return result;
//    }
//
//    public static void extractMSC(String title, String text, JsonObject MSC) {
//        Pattern pattern = Pattern.compile("Mathematics Subject Classification[^\\[]+\\[");
//        Matcher mathcer = pattern.matcher(text);
//
//        JsonArray indexes = new JsonArray();
//        if (MSC.keySet().contains(title)) {
//            indexes = MSC.get(title).getAsJsonArray();
//            MSC.remove(title);
//        }
//
//        while (mathcer.find()) {
//            String group = mathcer.group();
//            group = group.replaceAll("\\:", "");
//            group = group.replaceAll("\\[", "");
//            group = group.replaceAll("Mathematics Subject Classification", " ");
//            group = group.replaceAll("Primary", " ");
//            group = group.replaceAll("Secondary", " ");
//            String[] parts = group.split("[ ]+");
//            for (String index: parts) {
////                System.out.println(index);
//                String[] splitted = index.split(",");
//                for (String code: splitted) {
//                    if (code.length() > 0) {
//                        code = code.replaceAll("--", "-").toUpperCase();
//                        if (code.length() == 3) code = code + "XX";
//                        if (code.length() == 4)
//                            code = code.substring(0, 2) + "-" + code.substring(2, 4);
//                        if (! indexes.contains(new JsonPrimitive(code)) && !isParent(indexes, code)) {
//                            indexes.add(code);
//                            String parent = findParent(indexes, code);
//                            if (parent.length() > 0) indexes.remove(new JsonPrimitive(parent));
//                        }
//                    }
//                }
//            }
//        }
//        if (indexes.size() > 0) MSC.add(title, indexes);
//    }
//
//    public static boolean isParent(JsonArray array, String newItem) {
////        System.out.println("CHECK PARENT");
//        for (JsonElement value: array) {
//            String index = value.getAsString().replaceAll("\"", "");
////            System.out.println("existing: "+index+" new: "+newItem);
//            if (newItem.equals(index.substring(0, 3) + "XX")) return true;
//            if (newItem.equals(index.substring(0, 2) + "-XX")) return true;
//            if (newItem.equals(index.substring(0, 2) + "-" + index.substring(3, 5))) return true;
//        }
//        return false;
//    }
//
//    public static String findParent(JsonArray array, String newItem) {
////        System.out.println("FIND PARENT");
//        for (JsonElement value: array) {
//            String index = value.getAsString().replaceAll("\"", "");
////            System.out.println("existing: "+index+" new: "+newItem);
//            if (!index.equals(newItem) && index.equals(newItem.substring(0, 3) + "XX")) return index;
//            if (!index.equals(newItem) && index.equals(newItem.substring(0, 2) + "-XX")) return index;
//            if (!index.equals(newItem) && index.equals(newItem.substring(0, 2) + "-" + index.substring(3, 5))) return index;
//        }
//        return "";
//    }
//
//    public static String parseTitle(String preTitle) {
//        String title = preTitle;
//        if (title.charAt(0) == '"') title = title.substring(1);
//        if (title.charAt(title.length() - 1) == '"') title = title.substring(0, title.length() - 1);
//        title = title.replaceAll("_", " ");
//        title = title.replaceAll("\\\\u2013", "-");
//        return title;
//    }
//
//    public static void getArticlesJson(String filePath, List<String> titles, List<String> texts, boolean flag) {
//        try {
//            JsonReader reader = new JsonReader(new FileReader(filePath));
//            JsonArray array = new Gson().fromJson(reader, JsonArray.class);
//            for (int i = 0; i < array.size(); ++i) {
//                String title = array.get(i).getAsJsonObject().get("title").getAsString();
//                int id = array.get(i).getAsJsonObject().get("id").getAsInt();
//                if (flag) {
//                    titles.add(title + id);
//                }
//                else {
//                    titles.add(title);
//                }
//                String rawText = array.get(i).getAsJsonObject().get("text").getAsString();
//                texts.add(rawText);
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void getArticlesTxt(String filePath, List<String> titles, List<String> texts, boolean flag) {
//        try {
//            BufferedReader in = new BufferedReader(new FileReader(filePath));
//            String line;
//            int id = 0;
//            String text = "";
//            while ((line = in.readLine()) != null) {
//                String[] parts = line.split("[ ]+--:--[ ]+");
//                String preTerm = parts[0];
//                if (parts.length > 1) {
//                    text = parts[1];
//                }
////                else {
////                    System.out.println(parts[0]);
////                }
//                if (flag) {
//                    titles.add(preTerm + id);
//                }
//                else {
//                    titles.add(preTerm);
//                }
//                texts.add(text);
//                ++id;
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static List<String> cleanReplacements(List<String> texts) {
//        ArrayList<String> result = new ArrayList<>();
//        for (String text: texts) {
////            String newText = text.replaceAll("[^А-Яа-я ]+", "");
//            String newText = text;
//            newText = newText.replaceAll("@", "");
//            newText = newText.replaceAll("newline", "");
//            newText = newText.replaceAll("return", "");
//            newText = newText.replaceAll("tabulation", "");
//            result.add(newText);
//        }
//        return result;
//    }
//
//    public static List<String> cleanNonCyrillic(List<String> texts) {
//        ArrayList<String> result = new ArrayList<>();
//        for (String text: texts) {
//            String newText = text.replaceAll("[^А-Яа-я ]+", "");
//            newText = newText.replaceAll("[ ]+", " ");
//            result.add(newText);
//        }
//        return result;
//    }
//}


import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.apache.jena.base.Sys;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.StringTokenizer;

/**
 * Created by must on 16.11.2017.
 */
public class GeneralUtils {

    private static final int SHINGLE_LEN = 2;

    static int docs = 0;

    static List<HashSet<Integer>> shingles = new ArrayList<>();

    private static final String STOP_SYMBOLS[] = {".", ",", "!", "?", ":", ";", "-", "—", "\\", "/", "*", "(", ")", "+", "@",
            "#", "$", "%", "^", "&", "=", "'", "\"", "[", "]", "{", "}", "|"};
    private static final String STOP_WORDS_RU[] = {"это", "как", "так", "и", "в", "над", "к", "до", "не", "на", "но", "за",
            "то", "с", "ли", "а", "во", "от", "со", "для", "о", "же", "ну", "вы",
            "бы", "что", "кто", "он", "она"};

    public static float getScoresForTwoTexts(String[] toksFirst, String[] toksSecond, int shingleLength){


        ArrayList<Integer> shinglesFirst = new ArrayList<>();
        int shinglesNumber = toksFirst.length - shingleLength;

        //Create all shingles
        for (int i = 0; i <= shinglesNumber; i++) {
            String shingle = "";

            //Create one shingle
            for (int j = 0; j < shingleLength; j++) {
                shingle = shingle + toksFirst[i+j] + " ";
            }

            shinglesFirst.add(shingle.hashCode());
        }

        return compare(shinglesFirst, genShingle(toksSecond, shingleLength));
    }

    public static float getScoresForTwoTexts(String[] toksFirst, String[] toksSecond){
        return getScoresForTwoTexts(toksFirst, toksSecond, SHINGLE_LEN);
    }

    public static List<Integer> getScoresShingles(String[] toks, int count, double min_score, boolean expanded, List<Float> result, int shingleLength){
        List<Integer> scores = new ArrayList<>();
        int[] valSaved;
        String savesShingle = "";
        try {

//        for (String token: toks) System.out.println("toks: " + token);

            ArrayList<Integer> shinglesThis = new ArrayList<>();
            int shinglesNumber = toks.length - shingleLength;

            //Create all shingles
            for (int i = 0; i <= shinglesNumber; i++) {
                String shingle = "";

                //Create one shingle
                for (int j = 0; j < shingleLength; j++) {
                    shingle = shingle + toks[i + j] + " ";
                }
//            System.out.println("shingle: " + shingle);
                shinglesThis.add(shingle.hashCode());
            }
//            System.out.println("docs = "+docs);
            List<Float> lst = new ArrayList<>();
            for (int i = 0; i < docs; i++) {
                lst.add(compare(shinglesThis, shingles.get(i)));
//            System.out.println("compare "+shinglesThis);
            }

            int id[] = new int[docs];
            float val[] = new float[docs];
//            for (int i = 0; i<val.length; ++i) valSaved[i] = val[i];
            for (int i = 0; i < docs; i++) {
                id[i] = i;
                val[i] = lst.get(i);
            }
//            System.out.println("val before: ");
//            for (float value:val) System.out.print(value+" ");
//            System.out.println("");
            for (int i = val.length - 1; i > 0; i--) {
                for (int j = 0; j < i; j++) {
                    if (val[j] < val[j + 1]) {
                        float tmp = val[j];
                        val[j] = val[j + 1];
                        val[j + 1] = tmp;
                        int tmp_id = id[j];
                        id[j] = id[j + 1];
                        id[j + 1] = tmp_id;
                    }
                }
            }
//            System.out.println("val after: ");
//            for (float value:val) System.out.print(value+" ");
//            System.out.println("");
            int i = 0;
            while (i < docs && i < count && val[i] > min_score) {
                scores.add(id[i]);
                i++;
            }
            result.add(val[0]);
            result.add(val[1]);
        }
        catch (Exception e) {
            e.printStackTrace();
//            System.out.println("val: ");
        }
        return scores;
    }

    public static List<Integer> getScoresShingles(String[] toks, int count, double min_score, boolean expanded, List<Float> result){
        return getScoresShingles(toks, count, min_score, expanded, result, SHINGLE_LEN);
    }

    public static HashSet<Integer> genShingle(String [] words, int shingleLength) {
        HashSet<Integer> shingles = new HashSet<>();

//        for (String word: words) System.out.println("genShingle words: "+word);

        int shinglesNumber = words.length - shingleLength;

        //Create all shingles
        for (int i = 0; i <= shinglesNumber; i++) {
            String shingle = "";

            //Create one shingle
            for (int j = 0; j < shingleLength; j++) {
                shingle = shingle + words[i+j] + " ";
            }

            shingles.add(shingle.hashCode());
        }

        return shingles;
    }

    public static HashSet<Integer> genShingle(String [] words) {
        return genShingle(words, SHINGLE_LEN);
    }

    /**
     * Метод сравнивает две последовательности шинглов
     *
     * @param textShingles1New первая последовательность шинглов
     * @param textShingles2New вторая последовательность шинглов
     * @return процент сходства шинглов
     */
    public static float compare(ArrayList<Integer> textShingles1New, HashSet<Integer> textShingles2New) {
        if (textShingles1New == null || textShingles2New == null) return 0.0f;

        int textShingles1Number = textShingles1New.size();
        int textShingles2Number = textShingles2New.size();

        float similarShinglesNumber = 0;

        for (int i = 0; i < textShingles1Number; i++) {
            if (textShingles2New.contains(textShingles1New.get(i)))
                similarShinglesNumber += 1;
        }

        return ((similarShinglesNumber / ((textShingles1Number + textShingles2Number) / 2.0f)) * 100);
    }

    public static boolean containsTwise(List<String> list, String value) {
        int count = 0;
        for (String str: list) {
            if (str.equalsIgnoreCase(value)) {
                count++;
                if (count > 1)
                    return true;
            }
        }
        return false;
    }

        public static int minIndex(int a, int b) {
        if (a == -1) return b;
        if (b == -1) return a;
        if (a > b) return b;
        return a;
    }

    public static String removeBetween(String sub1, String sub2, String source,
                                        List<String> cut, int count, boolean toSave) {
//        System.out.println(source);
        int sub1Index = source.indexOf(sub1);
        String result = source;
        String current = source;
        while (sub1Index > -1) {
            int sub2Index = current.indexOf(sub2, sub1Index + sub1.length());
//            System.out.println(sub1Index+" "+sub2Index);
//            System.out.println(current.charAt(sub1Index)+ " "+current.charAt(sub2Index));
//            System.out.println(current.substring(sub1Index, sub2Index));
            if (sub2Index > -1) {
                count++;
                if (toSave) {
                    result = current.substring(0, sub1Index) + "@" + current.substring(sub2Index + sub2.length());
//                    System.out.println(current.substring(sub1Index, sub2Index)+sub2);
                    cut.add(current.substring(sub1Index, sub2Index)+sub2);
                }
                else {
                    result = current.substring(0, sub1Index) + " " + current.substring(sub2Index + sub2.length());
                }
            }
            else break;
//            System.out.println(result);

            sub1Index = result.indexOf(sub1);
            current = result;
        }
        return result;
    }

    public static String yandexTranslate(String requestText, String key) {
        boolean flag = false;
        try {
            String requestUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate?key="
                    + key + "&text=" + URLEncoder.encode(requestText, "UTF-8") + "&lang=en-ru";

            URL url = new URL(requestUrl);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.connect();
            int rc = httpConnection.getResponseCode();

            if (rc == 200) {
                String line = null;
                BufferedReader buffReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                StringBuilder strBuilder = new StringBuilder();
                while ((line = buffReader.readLine()) != null) {
                    strBuilder.append(line);
                }
                String translation = strBuilder.toString();
                JsonObject object = new Gson().fromJson(translation, JsonObject.class);

                StringBuilder sb = new StringBuilder();
                JsonArray array = object.get("text").getAsJsonArray();
                for (Object s : array) {
                    sb.append(s.toString());
                }
                translation = sb.toString();
                translation = translation.replaceAll("\"", "");
                return translation;

            } else {
                return "";
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String runMystem(String inputPath, String outputPath, String command, String text) {
        try {
            FileWriter input = new FileWriter(inputPath);
            input.write(text);
            input.close();
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            process.destroy();

            BufferedReader output = new BufferedReader(new FileReader(outputPath));
            String outputLine = output.readLine();
            output.close();
            return extractLemmas(outputLine);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String extractLemmas(String source) {
//        Pattern lemmasPattern = Pattern.compile("\\{[^\\}]+\\}");
//        Matcher lemmasMatcher = lemmasPattern.matcher(source);
        String result = "";
        int openIndex = source.indexOf('{');

        while (openIndex > -1) {
            int closeIndex = source.indexOf('}', openIndex);
            int repeatIndex = source.indexOf('{', openIndex+1);
            if (repeatIndex > -1 && repeatIndex < closeIndex) {
                System.out.println("Got u!");
                result = result + source.substring(0, repeatIndex);
                source = source.substring(repeatIndex);
                openIndex = source.indexOf('{');
//                continue;
            }
            else {
                if (closeIndex > -1) {
                    String lemmas = source.substring(openIndex + 1, closeIndex);
//                    System.out.println(lemmas);
                    lemmas = lemmas.replaceAll("[?]+", "");
//                System.out.println(source);
                    String[] parts = lemmas.split("\\|");
                    if (parts.length > 0) {
                        result = result + source.substring(0, openIndex) + parts[0];
                    } else {
                        result = result + source.substring(0, closeIndex + 1);
                    }
                    source = source.substring(closeIndex + 1);
                    openIndex = source.indexOf('{');
                } else {
                    break;
                }
            }
        }
        result += source;
        return result;
    }

    public static void extractMSC(String title, String text, JsonObject MSC) {
        Pattern pattern = Pattern.compile("Mathematics Subject Classification[^\\[]+\\[");
        Matcher mathcer = pattern.matcher(text);

        JsonArray indexes = new JsonArray();
        if (MSC.keySet().contains(title)) {
            indexes = MSC.get(title).getAsJsonArray();
            MSC.remove(title);
        }

        while (mathcer.find()) {
            String group = mathcer.group();
            group = group.replaceAll("\\:", "");
            group = group.replaceAll("\\[", "");
            group = group.replaceAll("Mathematics Subject Classification", " ");
            group = group.replaceAll("Primary", " ");
            group = group.replaceAll("Secondary", " ");
            String[] parts = group.split("[ ]+");
            for (String index: parts) {
//                System.out.println(index);
                String[] splitted = index.split(",");
                for (String code: splitted) {
                    if (code.length() > 0) {
                        code = code.replaceAll("--", "-").toUpperCase();
                        if (code.length() == 3) code = code + "XX";
                        if (code.length() == 4)
                            code = code.substring(0, 2) + "-" + code.substring(2, 4);
                        if (! indexes.contains(new JsonPrimitive(code)) && !isParent(indexes, code)) {
                            indexes.add(code);
                            String parent = findParent(indexes, code);
                            if (parent.length() > 0) indexes.remove(new JsonPrimitive(parent));
                        }
                    }
                }
            }
        }
        if (indexes.size() > 0) MSC.add(title, indexes);
    }

    public static boolean isParent(JsonArray array, String newItem) {
//        System.out.println("CHECK PARENT");
        for (JsonElement value: array) {
            String index = value.getAsString().replaceAll("\"", "");
//            System.out.println("existing: "+index+" new: "+newItem);
            if (newItem.equals(index.substring(0, 3) + "XX")) return true;
            if (newItem.equals(index.substring(0, 2) + "-XX")) return true;
            if (newItem.equals(index.substring(0, 2) + "-" + index.substring(3, 5))) return true;
        }
        return false;
    }

    public static String findParent(JsonArray array, String newItem) {
//        System.out.println("FIND PARENT");
        for (JsonElement value: array) {
            String index = value.getAsString().replaceAll("\"", "");
//            System.out.println("existing: "+index+" new: "+newItem);
            if (!index.equals(newItem) && index.equals(newItem.substring(0, 3) + "XX")) return index;
            if (!index.equals(newItem) && index.equals(newItem.substring(0, 2) + "-XX")) return index;
            if (!index.equals(newItem) && index.equals(newItem.substring(0, 2) + "-" + index.substring(3, 5))) return index;
        }
        return "";
    }

    public static String parseTitle(String preTitle) {
        String title = preTitle;
        if (title.charAt(0) == '"') title = title.substring(1);
        if (title.charAt(title.length() - 1) == '"') title = title.substring(0, title.length() - 1);
        title = title.replaceAll("_", " ");
        title = title.replaceAll("\\u2013", "-");
        title = title.replaceAll("\\\\u2013", "-");
        return title;
    }

    public static void getArticlesJson(String filePath, List<String> titles, List<String> texts, boolean flag) {
        try {
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);
            for (int i = 0; i < array.size(); ++i) {
                String title = array.get(i).getAsJsonObject().get("title").getAsString();
                title = parseTitle(title);
                int id = array.get(i).getAsJsonObject().get("id").getAsInt();
                if (flag) {
                    title = title.replaceAll(" ", "_");
                    titles.add(title + "_" + id);
                }
                else {
                    titles.add(title);
                }
                String rawText = array.get(i).getAsJsonObject().get("text").getAsString();
                texts.add(rawText);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getArticlesTxt(String filePath, List<String> titles, List<String> texts, boolean flag) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String line;
            int id = 0;
            String text = "";
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("[ ]+--:--[ ]+");
                String preTerm = parts[0];
                preTerm = parseTitle(preTerm);
                if (parts.length > 1) {
                    text = parts[1];
                }
//                else {
//                    System.out.println(parts[0]);
//                }
                if (flag) {
                    preTerm = preTerm.replaceAll(" ", "_");
                    titles.add(preTerm + "_" + id);
                }
                else {
                    titles.add(preTerm);
                }
                texts.add(text);
                ++id;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> cleanReplacements(List<String> texts) {
        ArrayList<String> result = new ArrayList<>();
        for (String text: texts) {
//            String newText = text.replaceAll("[^А-Яа-я ]+", "");
            String newText = text;
            newText = newText.replaceAll("@", "");
            newText = newText.replaceAll("newline", "");
            newText = newText.replaceAll("return", "");
            newText = newText.replaceAll("tabulation", "");
            result.add(newText);
        }
        return result;
    }

    public static List<String> cleanNonCyrillic(List<String> texts) {
        ArrayList<String> result = new ArrayList<>();
        for (String text: texts) {
            String newText = text.replaceAll("[^А-Яа-я ]+", "");
            newText = newText.replaceAll("[ ]+", " ");
            result.add(newText);
        }
        return result;
    }

}
