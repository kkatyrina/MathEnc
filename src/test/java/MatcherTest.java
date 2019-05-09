import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;

/**
 * Created by Катерина on 12.04.2019.
 */

public class MatcherTest {
    private static String basePath = FilePath.basePath.toString();

    public static void main(String[] argv) {
        matchTest(basePath + FilePath.expertMatch.toString(),
                basePath + FilePath.articleMatch.toString());
    }

    private static void matchTest(String expertPath, String systemPath) {
        JsonObject expert = new JsonObject();
        JsonObject system = new JsonObject();

        try {
            JsonReader reader = new JsonReader(new FileReader(expertPath));
            expert = new Gson().fromJson(reader, JsonObject.class);
            reader = new JsonReader(new FileReader(systemPath));
            system = new Gson().fromJson(reader, JsonObject.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        int all = expert.keySet().size();
        int right = 0, found = 0;
        for (String key: expert.keySet()) {
            String matching = expert.get(key).getAsString();
            if (system.keySet().contains(key)) {
                ++found;
                String answer = system.get(key).getAsString();
                if (matching.equalsIgnoreCase(answer)) {
                    ++right;
                }
            }
        }
        System.out.println("=====RESULT=====");
        System.out.println("File: "+systemPath);
        System.out.println("Right: "+right);
        System.out.println("Found: "+found);
        System.out.println("All: "+all);
        float precision = right / (1f * found);
        float recall = found / (1f * all);
        System.out.println("Precision: "+precision);
        System.out.println("Recall: "+recall);
        float f = 2f * ((precision * recall) / (precision + recall));
        System.out.println("F1: "+f);
//        float alpha = 0.6f;
//        float f1 = 1f / (alpha / precision + (1f - alpha) / recall);
//        System.out.println("F0.6: "+f1);
    }
}
