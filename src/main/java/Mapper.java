import java.util.*;

public class Mapper {

    private static final String STOP_SYMBOLS[] = {".", ",", "!", "?", ":", ";", "-", "\\", "/", "*", "(", ")", "+", "@",
            "#", "$", "%", "^", "&", "=", "'", "\"", "[", "]", "{", "}", "|"};
    private static final String STOP_WORDS_RU[] = {"это", "как", "так", "и", "в", "над", "к", "до", "не", "на", "но", "за",
            "то", "с", "ли", "а", "во", "от", "со", "для", "о", "же", "ну", "вы",
            "бы", "что", "кто", "он", "она"};
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
            scores = GeneralUtils.getScoresShingles(toks, 10000, 2.0, false, points);

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


//    protected static String getTokenized(String s){
//        s = s.toLowerCase();
////        System.out.println("to tokenize: "+s);
//        for (String stopSymbol : STOP_SYMBOLS) {
//            s = s.replace(stopSymbol, " ");
//        }
//
//        s = s.replaceAll("[ ]+", " ");
//        for (String stopWord : STOP_WORDS_RU) {
//            s = s.replace(" " + stopWord + " ", " ");
//        }
//
////        System.out.println("to tokenize2: "+s);
//        String result = "";
////        MorphAnalyzer morphAnalyzer = new MorphAnalyzer();
//        StringTokenizer st = new StringTokenizer(s);
////        System.out.println("auto tokens: "+st.toString());
//        List<String> words = new ArrayList<>();
//        while (st.hasMoreTokens()) {
//            String token = st.nextToken();
//            ++count;
////            System.out.println("token (" + token + ")");
//            if (token.length() > 1) {
//                try {
//                    Morph morph = MorphAnalyzer.get(token);
//                    if (morph != null) {
//                        result += (" " + morph.getNorm());
//                        //                    System.out.println("token: "+token+" norm: "+morph.getNorm());
//                    } else {
//                        System.out.println(count);
//                        morph = MorphPredictor.get(token);
//                        if (morph != null) {
//                            result += (" " + morph.getNorm());
//                            //System.out.println("token: "+token+" norm: "+morph.getNorm());
//                        } else {
//                            result += (" " + token);
////                        System.out.println("token: "+token);
//                        }
//                    }
//                }
//                catch(Exception e) {
//                    System.out.println(token);
//                    e.printStackTrace();
//                }
//            }
////            }
//            else {
//                result += (" " + token);
////                System.out.println("token: "+token);
//            }
////            words.add(st.nextToken());
//        }
////        return String.join(" ", MorphoDictionary.getNormForm(words));
//        if (result.charAt(0) == ' ') result = result.substring(1);
//        return result;
//    }
//
//    protected static String getTokenized2(String s){
//        s = s.toLowerCase();
//        for (String stopSymbol : STOP_SYMBOLS) {
//            s = s.replace(stopSymbol, " ");
//        }
//
//        s = s.replaceAll("[ ]+", " ");
//        for (String stopWord : STOP_WORDS_RU) {
//            s = s.replace(" " + stopWord + " ", " ");
//        }
//
//        String result = "";
//        StringTokenizer st = new StringTokenizer(s);
//        List<String> words = new ArrayList<>();
//        while (st.hasMoreTokens()) {
////            words.add(st.nextToken());
//            String token = st.nextToken();
//            ++count;
////            System.out.println("token2 " + token);
//            if (token.length() > 1) {
//                try {
//                    Morph morph = MorphAnalyzer.get(token);
//                    if (morph != null)
//                        result += (" " + morph.getNorm());
//                    else {
//                        System.out.println(count);
//                        morph = MorphPredictor.get(token);
//                        if (morph != null) {
//                            result += (" " + morph.getNorm());
//                            //System.out.println("token: "+token+" norm: "+morph.getNorm());
//                        } else {
//                            result += (" " + token);
//                            //                        System.out.println("token: "+token);
//                        }
//                    }
//                }
//                catch(Exception e) {
//                    System.out.println(token);
//                    e.printStackTrace();
//                }
//            }
////            }
//            else result += (" " + token);
//        }
////        return String.join(" ", MorphoAnalyst.normalize(words));
//        if (result.charAt(0) == ' ') result = result.substring(1);
//        return result;
//    }

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
