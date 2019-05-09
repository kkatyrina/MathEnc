/**
 * Created by Катерина on 05.03.2019.
 */
public enum FilePath {
    basePath("D:\\MathEncyclopedia\\src\\main\\resources\\"),
    englishArticles("soma.json"),
    englishArticlesParsed("englishArticles.txt"),
    englishArticlesTranslated("englishArticlesTranslate.txt"),
    englishArticlesNormalized("englishArticlesNormalized.txt"),
    russianArticles("oldick.json"),
    russianArticlesParsed("russianArticles.json"),
    russianArticlesNormShort("russianArticlesNormShort.txt"),
    russianArticlesNormFull("russianArticlesNormFull.json"),
    expertMatch("matchExpert.json"),
    articleMatch("articleMatch.json"),
    mystemCommand("mystem -cl "),
    articleIndexes("englishArticlesMSC.json"),
    categoriesMSC("MathCategoriesEN.xml"),
    articleFormulas("englishArticlesFormulas.json"),
    pageRank("pageRank.json"),
    russianArticlesAnnotated("russianArticlesAnnotated.json"),
    annotationExpert("annotationExpert.json"),
    annotationTest("annotationTest.json"),
    texterraAnnotations("texterra.json"),
    MSCConcepts("11_MSC_Concepts.xml"),
    MSCRelations("13_MSC_Relations.xml"),
    WWW("http://www.mathEnc.ru");

    private String value;

    FilePath(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
