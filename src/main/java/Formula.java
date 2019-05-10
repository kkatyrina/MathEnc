import com.google.gson.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Created by Катерина on 05.03.2019.
 */

public class Formula {

    private static String basePath = FilePath.basePath.toString();

    public static void main(String[] argv) {

        getFormulas(basePath + FilePath.englishArticles, basePath + FilePath.articleFormulas);
    }

    private static void getFormulas(String filePath, String resultPath) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            Gson gson = new Gson();
            JsonArray array = gson.fromJson(in, JsonArray.class);
            JsonObject result = new JsonObject();

            for (int i = 0; i < array.size(); ++i) {
                JsonObject object = array.get(i).getAsJsonObject();
                String title = GeneralUtils.parseTitle(object.get("name").getAsString());
                title = title.replaceAll(" ", "_");
                title += ("_" + i);
                String rawText = object.get("text").getAsString();
                ArrayList<String> formulas = new ArrayList<>();
                rawText = GeneralUtils.removeBetween("\\begin{equation}", "\\end{equation}", rawText, formulas, formulas.size(), true);
                rawText = GeneralUtils.removeBetween("\\begin{equation*}", "\\end{equation*}", rawText, formulas, formulas.size(), true);
                rawText = GeneralUtils.removeBetween("\\begin{subarray}", "\\end{subarray}", rawText, formulas, formulas.size(), true);
                rawText = GeneralUtils.removeBetween("\\begin{smallmatrix}", "\\end{smallmatrix}", rawText, formulas, formulas.size(), true);
                rawText = GeneralUtils.removeBetween("$$", "$$", rawText, formulas, formulas.size(), true);
                if (formulas.size() > 0) {
                    JsonArray formulasArray = new JsonArray();
                    for (String formula : formulas) {
                        formulasArray.add(new JsonPrimitive(formula));
                    }
                    result.add(title, formulasArray);
                }
            }
//            System.out.println(result.keySet().size());
            FileWriter file = new FileWriter(resultPath);
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
