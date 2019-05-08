import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by must on 25.02.2018.
 */
public class Annotator {

    public static class Data {
        public int min;
        public int max;
        public int found;
        public float score;
        public String title;
        public int titleInd;

        public Data(int min, int max, int found) {
            this.min = min;
            this.max = max;
            this.found = found;
        }
    }

    /**
     * Метод выполняющий аннотирование текста статьями
     *
     * @param text - текст для аннотирования (в токенизированном виде, токены соединены пробелами)
     *
     * @param titles - названия статей, которыми аннотируется текст
     *
     * @param definitionsTokenized - статьи, которыми аннотируется текст (в токенизированном виде, токены соединены пробелами)
     *
     * @return список ссылок на статьи с указанием номера слова в исходном токенизированном тексте
     * */

    public static List<Data> annotate(String text, List<String> titles, List<String> originalTitles, List<String> definitionsTokenized) {
        return annotate(text, titles, originalTitles, definitionsTokenized, "", new HashMap<String, List<String>>(), -0.1f, 1f, 20,
                35, 0, 1f, 0.01f, 150, 0.99f, "");
    }

    public static List<Data> annotate(String text, List<String> titles, List<String> originalTitles, List<String> definitionsTokenized, String currentTitle,
                                      Map<String, List<String>> aliases, float minScore, float minRatio, int deltaLeft, int deltaRight,
                                      int windowSize, float minRatioNear, float singleShingleCoefficient, int defLength, float notFullCoef, String context) {
        String[] toks = text.split(" ");
        List<Data> annotations = new ArrayList<Data>();
        JsonObject pageRank = new JsonObject();

        try {
            JsonReader reader = new JsonReader(new FileReader(Annotator.class.getClassLoader().getResource("").getPath() +
                    "/pageRank.json"));
            pageRank = new Gson().fromJson(reader, JsonObject.class);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        float pageRankMedian = 0;
//        ArrayList<String> pageList = new ArrayList<>(pageRank.keySet());
//        int pageRankAmount = pageList.size();
//        if (pageRankAmount % 2 == 1) {
//            pageRankMedian = pageRank.get(pageList.get((pageRankAmount + 1)/2)).getAsInt();
//        }
//        else {
//            int a1 = pageRank.get(pageList.get(pageRankAmount/2)).getAsInt();
//            int a2 = pageRank.get(pageList.get((pageRankAmount + 2)/2)).getAsInt();
//            pageRankMedian = (a1 + a2) / 2f;
//        }

        int pageRankSum = 0;
        int pageRankAmount = pageRank.keySet().size();
        for (String key: pageRank.keySet()) {
            pageRankSum += pageRank.get(key).getAsInt();
        }
        pageRankMedian = pageRankSum / (pageRankAmount * 1f);
//        System.out.println(pageRankMedian);


//        for (String tok:toks)
//            System.out.println(tok+" ");


        Map<Integer, List<Data>> wordToTitle = new HashMap<>();
        boolean isRedirected = false;
        int redirectCount = 0;
        int currentAliasId = 0;
        for (int titleId = 0; titleId < titles.size(); titleId++) {
            String title = titles.get(titleId);
//            System.out.println(title);
            if (isRedirected) {
                if (currentAliasId < aliases.get(title).size()) {
                    title = aliases.get(title).get(currentAliasId);
                }
                redirectCount++;
                currentAliasId++;
                if (currentAliasId >= aliases.get(titles.get(titleId)).size()) {
                    isRedirected = false;
                    currentAliasId = 0;
                }
            } else if (aliases.get(title) != null) {
                isRedirected = true;
            }
            if (title == null || title.equalsIgnoreCase(currentTitle)) {
                isRedirected = false;
                continue;
            }
            List<List<Integer>> indexes = new ArrayList<List<Integer>>();
            for (String wordInTitle: title.split(" ")) {
//                System.out.println("wordInTitle = "+wordInTitle);
                List<Integer> ind = new ArrayList<Integer>();
                for (int i = 0; i < toks.length; i++) {
                    if (wordInTitle.equalsIgnoreCase(toks[i])) {
//                        System.out.println(wordInTitle+"=="+toks[i]);
                        ind.add(i);
                    }
                }
                indexes.add(ind);
            }
            int window = indexes.size() + windowSize;
//            System.out.println("window = "+window);
            List<Data> pretendents = new ArrayList<Data>();
            for (Integer ind: indexes.get(0)) {
                boolean found = false;
                if (indexes.size() > 1) {
                    for (Integer pretendent: indexes.get(1)) {
                        if (Math.abs(pretendent - ind) < window) {
                            pretendents.add(new Data(Math.min(ind, pretendent), Math.max(ind, pretendent), 2));
                            found = true;
                        }
                    }
                }
                if (!found) {
                    pretendents.add(new Data(ind, ind, 1));
                }
            }

            //INSERTED
//            for (Data data:pretendents) {
//                System.out.println("Title = "+data.title+" titleInd = "+data.titleInd+" min = "+data.min+" max = "+data.max);
//            }

            List<Integer> pretendentsAbsentCount = new ArrayList<Integer>();
            for (int i = 0; i < pretendents.size(); i++) {
                pretendentsAbsentCount.add(2 - pretendents.get(i).found);
            }

            //INSERTED
            for (Integer i:pretendentsAbsentCount) {
//                System.out.println("absentCount = "+i);
            }

            if (!pretendents.isEmpty()) {
                for (int i = 2; i < indexes.size(); i++) {
                    if (pretendents.isEmpty()) {
                        break;
                    }
                    List<Integer> currentIndexes = indexes.get(i);
                    for (int j = 0; j < pretendents.size(); j++) {
                        Data index = pretendents.get(j);
                        boolean isPresent = false;
                        for (Integer indexCurrent: currentIndexes) {
                            if (Math.abs(index.min - indexCurrent) <= window && Math.abs(index.max - indexCurrent) <= window) {
                                isPresent = true;
                                if (indexCurrent < index.min) {
                                    index.min = indexCurrent;
                                } else {
                                    if (indexCurrent > index.max) {
                                        index.max = indexCurrent;
                                    }
                                }
                                index.found++;
                                break;
                            }
                        }
                        if (!isPresent) {
                            pretendentsAbsentCount.set(j, pretendentsAbsentCount.get(j) + 1);
                        }
                    }
                }
            }
            List<Data> newPretendents = new ArrayList<Data>();
            for (int i = 0; i < pretendents.size(); i++) {
                if (pretendents.get(i).found >= title.split(" ").length) {
                    newPretendents.add(pretendents.get(i));
                }
            }

//            System.out.println("====NEW PRETENDENTS====");


            for (Data pretendent: newPretendents) {
                int min = Math.max(pretendent.min - deltaLeft, 0);
                int max = Math.min(pretendent.max + deltaRight, toks.length - 1);
                String[] selectedToks = new String[max - min];
                System.arraycopy(toks, min, selectedToks, 0, pretendent.min - min);
                if (pretendent.max < max) {
                    System.arraycopy(toks, pretendent.max + 1, selectedToks, pretendent.min - min, max - pretendent.max);
                }
                String[] defPartsTmp = definitionsTokenized.get(titleId).split(" ");
                String[] defParts = Arrays.copyOfRange(defPartsTmp, 0, Math.min(defLength, defPartsTmp.length));

                //INSERTED
                String selToks = "";
                for (int i = 0; i < selectedToks.length; ++i) {
                    if (selectedToks[i] != null) {
                        selectedToks[i] = selectedToks[i].replaceAll("[^а-я]+", "");
                        if (selectedToks[i].length() > 0) {
                            selToks = selToks + selectedToks[i] + " ";
                        }
                    }
                }
                String dP = "";
                for (int i = 0; i < defParts.length; ++i)
                    dP = dP + defParts[i] + " ";
//                System.out.println("selectedToks = "+selToks+"\ndefParts = "+dP);
                String[] textArray = context.split(" ");

//                float score = GeneralUtils.getScoresForTwoTexts(textArray, defParts);
//                score += singleShingleCoefficient * GeneralUtils.getScoresForTwoTexts(textArray, defParts, 1);
//
                float score = GeneralUtils.getScoresForTwoTexts(selectedToks, defParts);
                score += singleShingleCoefficient * GeneralUtils.getScoresForTwoTexts(selectedToks, defParts, 1);
//                float pageRankScore = 0.000000001f;
                if (pageRank.keySet().contains(originalTitles.get(titleId))) {
                    score += 1f / (pageRank.get(originalTitles.get(titleId)).getAsInt() * 1f);
                    score -= 1f / pageRankMedian;
                }

                pretendent.score = score * (1 - notFullCoef * (title.split(" ").length - pretendent.found));
                pretendent.title = originalTitles.get(titleId);
                pretendent.titleInd = titleId;
                for (int i = pretendent.min; i <= pretendent.max; i++) {
                    if (wordToTitle.get(i) != null) {
                        wordToTitle.get(i).add(pretendent);
//                        System.out.println("Added: to "+wordToTitle.get(i)+" this "+pretendent.title+" "+pretendent.score);
                    } else {
                        wordToTitle.put(i, new ArrayList<>(Arrays.asList(pretendent)));
//                        System.out.println("Put: to "+i+" this "+pretendent.title+" "+pretendent.score);
                    }
                }
//                System.out.println("Title = "+pretendent.title+" titleInd = "+pretendent.titleInd+" min = "+
//                        pretendent.min+" max = "+pretendent.max+" score = "+pretendent.score);
//                if (counter.keySet().contains(pretendent.title)) {
//                    int n = counter.get(pretendent.title).getAsInt();
//                    n++;
//                    counter.remove(pretendent.title);
//                    counter.add(pretendent.title, new JsonPrimitive(n));
//                }
//                else {
//                    counter.add(pretendent.title, new JsonPrimitive(1));
//                }
            }
            if (isRedirected) {
                titleId--;
            }
        }




        if (currentTitle.equalsIgnoreCase("пустой множество")) {
            System.out.println("RedirectCount = " + redirectCount);
        }

        //INSERTED
//        System.out.println("=========BEFORE SORT===========");
//        int count1 = 0;
//        for (List<Data> values: wordToTitle.values()) {
//            count1++;
//            System.out.println("count = "+count1);
//            int ccount = 0;
//            for (Data value : values) {
//                ++ccount;
//                System.out.println(ccount+" Title = "+value.title+" titleInd = "+value.titleInd+" min = "+
//                        value.min+" max = "+value.max+" score = "+value.score);
//            }
//        }

        //СТРАННО РАБОТАЕТ
        for (List<Data> values: wordToTitle.values()) {
            values.sort((o1, o2) -> {
                if (o1.score < o2.score) {
                    return 1;
                }
                if (o1.score > o2.score) {
                    return -1;
                }
                return 0;
            });
        }

//        //INSERTED
//        System.out.println("=========AFTER SORT===========");
        int count = 0;
        for (List<Data> values: wordToTitle.values()) {
            count++;
//            System.out.println("count = "+count);
            int ccount = 0;
            for (Data value : values) {
                ++ccount;
//                System.out.println(ccount+" Title = "+value.title+" titleInd = "+value.titleInd+" min = "+
//                        value.min+" max = "+value.max+" score = "+value.score);
            }
        }


        Set<String> foundTitles = new HashSet<>();
        for (Map.Entry<Integer, List<Data>> entry: wordToTitle.entrySet()) {
            Data data = entry.getValue().get(0);



//            System.out.println(" ENTRY[0] Title = "+data.title+" titleInd = "+data.titleInd+" min = "+
//                    data.min+" max = "+data.max+" score = "+data.score);

            Data bestData = data;

            if (data.score > minScore) {
                if (entry.getValue().size() > 1) {
//                    System.out.println(" ENTRY[1] Title = "+entry.getValue().get(1).title+" titleInd = "+
//                            entry.getValue().get(1).titleInd+" min = "+
//                            entry.getValue().get(1).min+" max = "+
//                            entry.getValue().get(1).max+" score = "+entry.getValue().get(1).score);
                    float currentRatio = data.score / entry.getValue().get(1).score;

//                    System.out.println("currentRatio = "+currentRatio);

                    for (int i = data.min; i <= data.max; i++) {
                        List<Data> near = wordToTitle.get(i);
                        if (near.size() > 1) {
//                            System.out.println(">1");
                            if (bestData.found <= near.get(0).found && currentRatio < (near.get(0).score / near.get(1).score) && (near.get(0).score / near.get(1).score) > minRatio && bestData.score < minRatioNear * near.get(0).score) {
                                bestData = near.get(0);
                                System.out.println(">1");
                            }
                        } else {
//                            System.out.println("<=1");
                            if (bestData.found <= near.get(0).found && bestData.score < minRatioNear * near.get(0).score) {
                                bestData = near.get(0);
                                System.out.println("<=1");
                            }
                        }
                    }
                    if ((bestData != data || currentRatio > minRatio) && !annotations.contains(bestData) &&
                            bestData.score >= minScore && !bestData.title.equalsIgnoreCase(currentTitle)) {
//                        annotations.add(bestData);
                        if (!foundTitles.contains(bestData.title)) {
//                            System.out.println("Add "+bestData.title);
                            annotations.add(bestData);
                            foundTitles.add(bestData.title);
                        }
                    }
                } else {

                    for (int i = data.min; i <= data.max; i++) {
                        List<Data> near = wordToTitle.get(i);

//                        System.out.println(" NEAR[0] Title = "+near.get(0).title+" titleInd = "+near.get(0).titleInd+" min = "+
//                                near.get(0).min+" max = "+near.get(0).max+" score = "+near.get(0).score);

                        if (near.size() > 1) {
//                            System.out.println(">1");
                            if (bestData.found <= near.get(0).found && (near.get(0).score / near.get(1).score) > minRatio && bestData.score < minRatioNear * near.get(0).score) {
                                bestData = near.get(0);
                            }
                        } else {
//                            System.out.println("<=1");
                            if (bestData.found <= near.get(0).found && bestData.score < minRatioNear * near.get(0).score) {
                                System.out.println("WTF i'm here");
                                bestData = near.get(0);
                            }
                        }
                    }
                    if (bestData.found >= bestData.title.split(" ").length && !annotations.contains(bestData) &&
                            bestData.score >= minScore && !bestData.title.equalsIgnoreCase(currentTitle)) {
//                        annotations.add(bestData);
                        if (!foundTitles.contains(bestData.title)) {
                            annotations.add(bestData);
                            foundTitles.add(bestData.title);
                        }
                    }
                }
            }
        }

        return annotations;
    }

//    public static Map<String, Integer> pageRank(String text, List<String> titles, List<String> definitionsTokenized, String currentTitle,
//                                Map<String, List<String>> aliases, float minScore, float minRatio, int deltaLeft, int deltaRight,
//                                int windowSize, float minRatioNear, float singleShingleCoefficient, int defLength, float notFullCoef, String context) {
//        Map<String, Integer> pageRank = new HashMap<>();
//        String[] toks = text.split(" ");
//
//        Map<Integer, List<Data>> wordToTitle = new HashMap<>();
//        boolean isRedirected = false;
//        int redirectCount = 0;
//        int currentAliasId = 0;
//        for (int titleId = 0; titleId < titles.size(); titleId++) {
//            String title = titles.get(titleId);
////            System.out.println(title);
//            if (isRedirected) {
//                if (currentAliasId < aliases.get(title).size()) {
//                    title = aliases.get(title).get(currentAliasId);
//                }
//                redirectCount++;
//                currentAliasId++;
//                if (currentAliasId >= aliases.get(titles.get(titleId)).size()) {
//                    isRedirected = false;
//                    currentAliasId = 0;
//                }
//            } else if (aliases.get(title) != null) {
//                isRedirected = true;
//            }
//            if (title == null || title.equalsIgnoreCase(currentTitle)) {
//                isRedirected = false;
//                continue;
//            }
//            List<List<Integer>> indexes = new ArrayList<>();
//            for (String wordInTitle: title.split(" ")) {
////                System.out.println("wordInTitle = "+wordInTitle);
//                List<Integer> ind = new ArrayList<>();
//                for (int i = 0; i < toks.length; i++) {
//                    if (wordInTitle.equalsIgnoreCase(toks[i])) {
////                        System.out.println(wordInTitle+"=="+toks[i]);
//                        ind.add(i);
//                    }
//                }
//                indexes.add(ind);
//            }
//            int window = indexes.size() + windowSize;
////            System.out.println("window = "+window);
//            List<Data> pretendents = new ArrayList<>();
//            for (Integer ind: indexes.get(0)) {
//                boolean found = false;
//                if (indexes.size() > 1) {
//                    for (Integer pretendent: indexes.get(1)) {
//                        if (Math.abs(pretendent - ind) < window) {
//                            pretendents.add(new Data(Math.min(ind, pretendent), Math.max(ind, pretendent), 2));
//                            found = true;
//                        }
//                    }
//                }
//                if (!found) {
//                    pretendents.add(new Data(ind, ind, 1));
//                }
//            }
//
//            //INSERTED
////            for (Data data:pretendents) {
////                System.out.println("Title = "+data.title+" titleInd = "+data.titleInd+" min = "+data.min+" max = "+data.max);
////            }
//
//            List<Integer> pretendentsAbsentCount = new ArrayList<>();
//            for (int i = 0; i < pretendents.size(); i++) {
//                pretendentsAbsentCount.add(2 - pretendents.get(i).found);
//            }
//
//            //INSERTED
//            for (Integer i:pretendentsAbsentCount) {
////                System.out.println("absentCount = "+i);
//            }
//
//            if (!pretendents.isEmpty()) {
//                for (int i = 2; i < indexes.size(); i++) {
//                    if (pretendents.isEmpty()) {
//                        break;
//                    }
//                    List<Integer> currentIndexes = indexes.get(i);
//                    for (int j = 0; j < pretendents.size(); j++) {
//                        Data index = pretendents.get(j);
//                        boolean isPresent = false;
//                        for (Integer indexCurrent: currentIndexes) {
//                            if (Math.abs(index.min - indexCurrent) <= window && Math.abs(index.max - indexCurrent) <= window) {
//                                isPresent = true;
//                                if (indexCurrent < index.min) {
//                                    index.min = indexCurrent;
//                                } else {
//                                    if (indexCurrent > index.max) {
//                                        index.max = indexCurrent;
//                                    }
//                                }
//                                index.found++;
//                                break;
//                            }
//                        }
//                        if (!isPresent) {
//                            pretendentsAbsentCount.set(j, pretendentsAbsentCount.get(j) + 1);
//                        }
//                    }
//                }
//            }
//            List<Data> newPretendents = new ArrayList<>();
//            for (int i = 0; i < pretendents.size(); i++) {
//                if (pretendents.get(i).found >= title.split(" ").length) {
//                    newPretendents.add(pretendents.get(i));
//                }
//            }
//
////            System.out.println("====NEW PRETENDENTS====");
//
//            for (Data pretendent: newPretendents) {
//                int min = Math.max(pretendent.min - deltaLeft, 0);
//                int max = Math.min(pretendent.max + deltaRight, toks.length - 1);
//                String[] selectedToks = new String[max - min];
//                System.arraycopy(toks, min, selectedToks, 0, pretendent.min - min);
//                if (pretendent.max < max) {
//                    System.arraycopy(toks, pretendent.max + 1, selectedToks, pretendent.min - min, max - pretendent.max);
//                }
//                String[] defPartsTmp = definitionsTokenized.get(titleId).split(" ");
//                String[] defParts = Arrays.copyOfRange(defPartsTmp, 0, Math.min(defLength, defPartsTmp.length));
//
//                //INSERTED
//                String selToks = "";
//                for (int i = 0; i < selectedToks.length; ++i) {
//                    if (selectedToks[i] != null) {
//                        selectedToks[i] = selectedToks[i].replaceAll("[^а-я]+", "");
//                        if (selectedToks[i].length() > 0) {
//                            selToks = selToks + selectedToks[i] + " ";
//                        }
//                    }
//                }
//                String dP = "";
//                for (int i = 0; i < defParts.length; ++i)
//                    dP = dP + defParts[i] + " ";
////                System.out.println("selectedToks = "+selToks+"\ndefParts = "+dP);
//                String[] textArray = context.split(" ");
//
////                float score = GeneralUtils.getScoresForTwoTexts(textArray, defParts);
////                score += singleShingleCoefficient * GeneralUtils.getScoresForTwoTexts(textArray, defParts, 1);
////
//                float score = GeneralUtils.getScoresForTwoTexts(selectedToks, defParts);
//                score += singleShingleCoefficient * GeneralUtils.getScoresForTwoTexts(selectedToks, defParts, 1);
//                pretendent.score = score * (1 - notFullCoef * (title.split(" ").length - pretendent.found));
//                pretendent.title = titles.get(titleId);
//                pretendent.titleInd = titleId;
//                for (int i = pretendent.min; i <= pretendent.max; i++) {
//                    if (wordToTitle.get(i) != null) {
//                        wordToTitle.get(i).add(pretendent);
////                        System.out.println("Added: to "+wordToTitle.get(i)+" this "+pretendent.title+" "+pretendent.score);
//                    } else {
//                        wordToTitle.put(i, new ArrayList<>(Arrays.asList(pretendent)));
////                        System.out.println("Put: to "+i+" this "+pretendent.title+" "+pretendent.score);
//                    }
//                }
//                System.out.println("Title = "+pretendent.title+" titleInd = "+pretendent.titleInd+" min = "+
//                        pretendent.min+" max = "+pretendent.max+" score = "+pretendent.score);
//            }
//            if (isRedirected) {
//                titleId--;
//            }
//        }
//
//
//        return pageRank;
//    }
}
