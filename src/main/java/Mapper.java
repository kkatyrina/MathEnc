import java.util.*;

public class Mapper {

//    private static final String STOP_SYMBOLS[] = {".", ",", "!", "?", ":", ";", "-", "\\", "/", "*", "(", ")", "+", "@",
//            "#", "$", "%", "^", "&", "=", "'", "\"", "[", "]", "{", "}", "|"};
//    private static final String STOP_WORDS_RU[] = {"это", "как", "так", "и", "в", "над", "к", "до", "не", "на", "но", "за",
//            "то", "с", "ли", "а", "во", "от", "со", "для", "о", "же", "ну", "вы",
//            "бы", "что", "кто", "он", "она"};
    private static int count = 0;


    /**
     * Метод выполняющий сопоставление статей из списков queryString и not_tokenized
     *
     * @param part числовой параметр от 0 до 1, позволяющий варьировать точность и полноту сопоставления
     *
     * @return для каждой статьи из queryString ставит в соответствие id статьи из not_tokenized,
     *      если такое соответствие есть, -1 - иначе
     * */
    public static List<Integer> map(List<String> queryString, List<String> not_tokenized, float part) {

//        System.out.println(queryString);
        tokenize(not_tokenized);

        List<Integer> scores;
        List<Float> points = new ArrayList<>();
        System.out.println(part);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < queryString.size(); i++) {
            System.out.println(i);
            String[] toks = queryString.get(i).split(" ");
//            for (String s:toks) System.out.println(s);
            points.clear();
//            for (String token : toks) System.out.println("map toks: "+token);
            scores = GeneralUtils.getScoresShingles(toks, 10000, 6.0, false, points);

//            for (Integer is : scores) System.out.println("score: " + is);
//            for (Float id : points) System.out.println("point: " + id);

            if (scores.size() > 0 && points.get(1) / points.get(0) < part) {
                result.add(scores.get(0));
            } else {
                result.add(-1);
            }
        }
        return result;
    }

    public static List<Integer> mapRussian(String queryString, int id, List<String> not_tokenized, float part) {
        List<Integer> result = new ArrayList<>();
        List<String> newTokenized = new ArrayList<>();
        for (int j = 0; j < not_tokenized.size(); ++j) {
            if (j != id) {
                newTokenized.add(not_tokenized.get(j));
            }
            else {
                newTokenized.add("");
            }
        }
        tokenize(newTokenized);
        List<Integer> scores;
        List<Float> points = new ArrayList<>();
        System.out.println(id);

        String[] toks = queryString.split(" ");
        scores = GeneralUtils.getScoresShingles(toks, 10000, 5.0, false, points);
        int count = 0;
        while (scores.size() > count && count < 5) {
            result.add(scores.get(count));
            ++count;
        }
        if (count == 0) {
            result.add(-1);
        }

//        System.out.println(queryString);
        return result;
    }

    private static void tokenize(List<String> not_tokenized){
//        List<String> tokenized = new ArrayList<>();
        GeneralUtils.shingles.clear();
        for (String s: not_tokenized){
//            String tokenizedStr = getTokenized(s);
//            tokenized.add(tokenizedStr);
            GeneralUtils.shingles.add(GeneralUtils.genShingle(s.split(" ")));
        }
//        return  tokenized;
    }

}
