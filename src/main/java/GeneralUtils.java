import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.jena.base.Sys;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by must on 16.11.2017.
 */
public class GeneralUtils {

    private static final int SHINGLE_LEN = 2;

    static int docs = 0;

    static List<ArrayList<Integer>> shingles = new ArrayList<>();

    private static final String STOP_SYMBOLS[] = {".", ",", "!", "?", ":", ";", "-", "—", "\\", "/", "*", "(", ")", "+", "@",
            "#", "$", "%", "^", "&", "=", "'", "\"", "[", "]", "{", "}", "|"};
    private static final String STOP_WORDS_RU[] = {"это", "как", "так", "и", "в", "над", "к", "до", "не", "на", "но", "за",
            "то", "с", "ли", "а", "во", "от", "со", "для", "о", "же", "ну", "вы",
            "бы", "что", "кто", "он", "она"};

    public static float getScoresForTwoTexts(String[] toksFirst, String[] toksSecond, int shingleLength){
        ArrayList<Integer> shinglesFirst = new ArrayList<>();
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

            ArrayList<Integer> shinglesThis = genShingle(toks, shingleLength);
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

    private static ArrayList<Integer> genShingle(String [] words, int shingleLength) {
        ArrayList<Integer> shingles = new ArrayList<>();

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

    public static ArrayList<Integer> genShingle(String [] words) {
        return genShingle(words, SHINGLE_LEN);
    }

    /**
     * Метод сравнивает две последовательности шинглов
     *
     * @param textShingles1New первая последовательность шинглов
     * @param textShingles2New вторая последовательность шинглов
     * @return процент сходства шинглов
     */
    public static float compare(ArrayList<Integer> textShingles1New, ArrayList<Integer> textShingles2New) {
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

    public static boolean containsTwice(List<String> list, String value) {
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
            int sub2Index = current.indexOf(sub2, sub1Index);
//            System.out.println(sub1Index+" "+sub2Index);
//            System.out.println(current.charAt(sub1Index)+ " "+current.charAt(sub2Index));
//            System.out.println(current.substring(sub1Index, sub2Index));
            if (sub2Index > -1) {
                count++;
                if (toSave) {
                    result = current.substring(0, sub1Index) + "$" + current.substring(sub2Index + sub2.length());
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
}
