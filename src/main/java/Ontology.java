/**
 * Created by Катерина on 06.03.2019.
 */

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Ontology {
    private static String filename = FilePath.basePath.toString() + OntProperties.name.toString();
    private static Model model;
    private static OntModel ontModel;
    private static InfModel inf;
    private static String basePath = FilePath.basePath.toString();

    private static String createNewURI(String name) {
        return OntProperties.WWW.toString() + name;
    }

    public static void main(String[] argv) {
        setUpOntology();

        loadRussian(basePath + FilePath.russianArticlesAnnotated.toString());

        for (int iteration = 0; iteration < 8; ++iteration) {
            setUpOntology();
            loadRussianRelated(basePath + FilePath.russianArticlesRelated, iteration);
        }

        loadEnglish(basePath + FilePath.englishArticles.toString());
//
        loadIndexes(basePath + FilePath.MSCConcepts, basePath + FilePath.MSCRelations);
//
        loadArticleMatch(basePath + FilePath.articleMatch);
//
        loadIndexArticle(basePath + FilePath.englishArticlesParsed, basePath + FilePath.articleIndexes);
//
        loadAnnotations(basePath + FilePath.russianArticlesAnnotated);
//
        loadFormulas(basePath + FilePath.articleFormulas);




    }

    private static void setUpOntology() {
        try {
            model = ModelFactory.createDefaultModel();
            InputStream in = new FileInputStream(filename);
            model = model.read(in, null);
            inf = ModelFactory.createRDFSModel(model);
            ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadEnglish(String filePath) {
        try {
            String articleClassURI = createNewURI(OntProperties.articleEnClass.toString());
            String termClassURI = createNewURI(OntProperties.termClass.toString());
            System.out.println(articleClassURI);
            System.out.println(termClassURI);
            Resource articleClassResource = model.getResource(articleClassURI);
            Resource termClassResource = model.getResource(termClassURI);
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);

            for (int i = 0; i < array.size(); ++i) {
                JsonObject object = array.get(i).getAsJsonObject();
                String preTitle = GeneralUtils.parseTitle(object.get("name").getAsString());
                String text = object.get("text").getAsString();
                text = text.replaceAll("href=\"/index.php", "href=\"/en");
                String title = preTitle.replaceAll(" ", "_");

                String articleURI = createNewURI("Article_" + title+ "_" + i);
                String termURI = createNewURI("Term_" + title+ "_" + i);
                String link = "/en/" + title;

                OntologyUtils.load(model, articleURI, termURI, preTitle, text, link, false);
            }

            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadRussian(String filePath) {
        try {
            String articleClassURI = createNewURI(OntProperties.articleRuClass.toString());
            String termClassURI = createNewURI(OntProperties.termClass.toString());
            System.out.println(articleClassURI);
            System.out.println(termClassURI);
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);

            for (int i = 0; i < array.size(); ++i) {
                JsonObject object = array.get(i).getAsJsonObject();
                String preTitle = object.get("title").getAsString();
                String text = object.get("text").getAsString();
                String title = preTitle.replaceAll(" ", "_");

                String articleURI = createNewURI("Статья_" + title + "_" + i);
                String termURI = createNewURI("Термин_" + title + "_" + i);
                String link = "/ru/" + i;

                OntologyUtils.load(model, articleURI, termURI, preTitle, text, link, true);
            }

            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private static void loadIndexes(String conceptsPath, String relationsPath) {
        List<String> MSCIndexes = new ArrayList<>();
        List<String> MSCCategories = new ArrayList<>();
        Document xmlFile = null;
        OntologyUtils.readMSCIndexes(conceptsPath, MSCIndexes, MSCCategories);
        String classURI = createNewURI(OntProperties.MSCClass.toString());
        Resource className = model.getResource(classURI);

        for (int i = 0; i < MSCIndexes.size(); ++i) {
            String index = MSCIndexes.get(i);
            String category = MSCCategories.get(i);

//            System.out.println("index: " + index);
//            System.out.println("category: " + category);
            Resource indexResource = model.createResource(createNewURI(index));

            Property commentProperty = model.getProperty(OntProperties.comment.toString());
            Statement commentStatement = model.createStatement(indexResource, commentProperty, category);
            model.add(commentStatement);

            Property typeProperty = model.getProperty(OntProperties.instance.toString());
            Statement typeStatement = model.createStatement(indexResource, typeProperty, className);
            model.add(typeStatement);
        }

        try {
            xmlFile = Jsoup.parse(new File(relationsPath), "UTF-8");
            Elements items = xmlFile.getElementsByTag("taxon");

            for (Element item : items) {
                String index = item.attr("uri");
                index = index.replaceAll("http://libmeta.ru/taxon/msc#", "");
                index = index.replaceAll("--", "-").toUpperCase();
//                System.out.println(index);
                String indexURI = createNewURI(index);

                if (OntologyUtils.ifInstance(inf, classURI, indexURI)) {

                    Elements parents = item.getElementsByTag("parent");
                    if (!parents.isEmpty()) {
                        String parent = parents.first().ownText();
//                        index = index.replaceAll("http://libmeta.ru/taxon/msc#", "");
                        parent = parent.replaceAll("http://libmeta.ru/taxon/msc#", "");
                        parent = parent.replaceAll("--", "-").toUpperCase();

//                        System.out.println("index: " + index);
//                        System.out.println("parent: " + parent);
                        String parentURI = createNewURI(parent);

                        if (OntologyUtils.ifInstance(inf, classURI, indexURI) &&
                                OntologyUtils.ifInstance(inf, classURI, parentURI)) {
                            Resource indexResource = model.getResource(indexURI);
                            Resource parentResource = model.getResource(parentURI);
                            Property indexProperty = model.getProperty(OntProperties.WWW.toString() + OntProperties.subIndex.toString());
                            Statement indexStatement = model.createStatement(indexResource, indexProperty, parentResource);
                            model.add(indexStatement);
                        }
                    }

                    Elements related = item.getElementsByTag("related");
                    if (!related.isEmpty()) {
                        String relatedOne = related.first().ownText();
                        relatedOne = relatedOne.replaceAll("http://libmeta.ru/taxon/msc#", "");
                        relatedOne = relatedOne.replaceAll("--", "-").toUpperCase();
//                        System.out.println("index: " + index);
//                        System.out.println("related: " + relatedOne);
                        String relatedURI = createNewURI(relatedOne);

//                        System.out.println(ifRelated(indexURI, relatedURI, "MSC_MSC"));
//                        System.out.println(ifSaved(index, relatedOne));

//                        if (OntologyUtils.ifInstance(inf, classURI, relatedURI) &&
//                                !OntologyUtils.ifRelated(inf, indexURI, relatedURI, "_MSC_MSC") &&
//                                !OntologyUtils.ifSaved(index, relatedOne)) {
                        if (OntologyUtils.ifInstance(inf, classURI, relatedURI)) {
                            Resource indexResource = model.getResource(indexURI);
                            Resource relatedResource = model.getResource(relatedURI);
                            Property relateProperty = model.getProperty(OntProperties.WWW.toString() + OntProperties.relatedIndex + "_MSC_MSC");
                            Statement relateStatement = model.createStatement(indexResource, relateProperty, relatedResource);
                            model.add(relateStatement);
                            relateStatement = model.createStatement(relatedResource, relateProperty, indexResource);
                            model.add(relateStatement);
                        }
                    }
                }
            }
            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadArticleMatch(String filePath) {
        try {
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonObject articles = new Gson().fromJson(reader, JsonObject.class);
            for (String key: articles.keySet()) {
//                String idRus = key.substring(key.indexOf('_' + 1));
                String value = articles.get(key).getAsString();
//                key = key.replaceAll(" ", "_");
//                String idEn = value.substring(value.indexOf('_') + 1);
//                value = value.replaceAll(" ", "_");
//                value = value.replaceAll("\\u2013", "-");

                String ruURI = createNewURI("Статья_" + key);
                String enURI = createNewURI("Article_" + value);
//                Resource ruClassResource = model.getResource(OntProperties.articleRuClass.toString());
//                Resource enClassResource = model.getResource(OntProperties.articleEnClass.toString());
                Resource ruResource = model.getResource(ruURI);
                Resource enResource = model.getResource(enURI);
                Property match = model.getProperty(OntProperties.WWW.toString() + OntProperties.articleMatch.toString());
                Statement statement = model.createStatement(ruResource, match, enResource);
                model.add(statement);

            }
            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadIndexArticle(String originalPath, String matchPath) {
        try {
            JsonReader reader = new JsonReader(new FileReader(matchPath));
            JsonObject articles = new Gson().fromJson(reader, JsonObject.class);
            ArrayList<String> original = new ArrayList<>();
            GeneralUtils.getArticlesTxt(originalPath, original, new ArrayList<>(), false);
//            for (String s: original) {
//                System.out.println(s);
//            }
            for (String key: articles.keySet()) {
                key = key.replaceAll("–", "-");
                int id = original.indexOf(key);
                String articleURI = createNewURI("Article_" + key.replaceAll(" ", "_") + "_" + id);
//                System.out.println(articleURI);
                Resource articleResource = model.getResource(articleURI);
                JsonArray indexes = articles.get(key).getAsJsonArray();
                for (int i = 0; i < indexes.size(); ++i) {
                    String index = indexes.get(i).getAsString().toUpperCase();
                    index = index.replaceAll("\\.", "-");
//                    System.out.println(index);
                    String nearest = OntologyUtils.findNearest(inf, index);
//                    String nearest = index;
                    String articleClassURI = createNewURI(OntProperties.articleEnClass.toString());
                    String indexClassURI = createNewURI(OntProperties.MSCClass.toString());
                    String indexURI = createNewURI(nearest.toUpperCase());

                    if (OntologyUtils.ifInstance(inf, articleClassURI, articleURI)) {
                        Resource indexResource = model.getResource(indexURI);
                        if (!OntologyUtils.ifInstance(inf, indexClassURI, indexURI)) {
                            System.out.println(index);
                            Resource classResource = model.getResource(indexClassURI);
                            indexResource = model.getResource(createNewURI(index));
                            Property instance = model.getProperty(OntProperties.instance.toString());
                            Statement statement = model.createStatement(indexResource, instance, classResource);
                            model.add(statement);
                        }

                        Property bindProperty = model.getProperty(OntProperties.WWW.toString() + OntProperties.MSCArticle);
                        Statement bindStatement = model.createStatement(articleResource, bindProperty, indexResource);
                        model.add(bindStatement);
                    }
                }
            }
            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadAnnotations(String filePath) {
        try {
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray articles = new Gson().fromJson(reader, JsonArray.class);
            for (int i = 0; i < articles.size(); ++i) {
                JsonObject article = articles.get(i).getAsJsonObject();
                int articleId = article.get("id").getAsInt();
                String articleTitle = article.get("title").getAsString().replaceAll(" ", "_");
                JsonArray annotations = article.get("annotations").getAsJsonArray();
                for (int k = 0; k < annotations.size(); ++k) {
                    JsonObject annotation = annotations.get(k).getAsJsonObject();
                    int termId = annotation.get("titleId").getAsInt();
                    String termTitle = annotation.get("title").getAsString().replaceAll(" ", "_");
                    int start = annotation.get("start").getAsInt();
                    int end = annotation.get("end").getAsInt();
                    String termURI = createNewURI("Термин_" + termTitle + "_" + termId);
                    String articleURI = createNewURI("Статья_" + articleTitle + "_" + articleId);
                    String annotationURI = createNewURI("Аннотация_" + articleId + "_" + termId);

                    Resource annotationResource = model.createResource(annotationURI);
                    Resource annClassResource = model.getResource(createNewURI(OntProperties.annotationClass.toString()));
                    Resource termResource = model.getResource(termURI);
                    Resource articleResource = model.getResource(articleURI);

                    Property instance = model.getProperty(OntProperties.instance.toString());
                    Statement statement = model.createStatement(annotationResource, instance, annClassResource);
                    model.add(statement);

                    Property boundary = model.getProperty(OntProperties.WWW.toString() + OntProperties.annotationStart);
                    statement = model.createLiteralStatement(annotationResource, boundary, start);
                    model.add(statement);
                    boundary = model.getProperty(OntProperties.WWW.toString() + OntProperties.annotationEnd);
                    statement = model.createLiteralStatement(annotationResource, boundary, end);
                    model.add(statement);

                    Property property = model.getProperty(OntProperties.WWW.toString() + OntProperties.annotationArticle);
                    statement = model.createStatement(annotationResource, property, articleResource);
                    model.add(statement);

                    property = model.getProperty(OntProperties.WWW.toString() + OntProperties.annotationTerm);
                    statement = model.createStatement(annotationResource, property, termResource);
                    model.add(statement);

                }
                System.out.println(i);
            }
            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static  void loadFormulas(String filePath) {
        try {
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonObject articles = new Gson().fromJson(reader, JsonObject.class);
            Resource formulaClassResource = model.getResource(createNewURI(OntProperties.formulaClass.toString()));
            for (String key: articles.keySet()) {
                String articleId = key.substring(key.lastIndexOf("_") + 1);
                JsonArray formulas = articles.get(key).getAsJsonArray();
                String articleURI = createNewURI("Article_" + key);
                Resource articleResource = model.getResource(articleURI);
                for (int i = 0; i < formulas.size(); ++i) {
                    String formula = formulas.get(i).getAsString();
                    String formulaURI = createNewURI("Formula_" + articleId + "_" + i);

                    Resource formulaResource = model.createResource(formulaURI);
                    Property instance = model.getProperty(OntProperties.instance.toString());
                    Statement statement = model.createStatement(formulaResource, instance, formulaClassResource);
                    model.add(statement);

                    Property definedBy = model.getProperty(OntProperties.isDefinedBy.toString());
                    statement = model.createStatement(formulaResource, definedBy, formula);
                    model.add(statement);

                    Property property = model.getProperty(OntProperties.WWW.toString() + OntProperties.formulaArticle);
                    statement = model.createStatement(formulaResource, property, articleResource);
                    model.add(statement);
                }
            }
            OutputStream out = new FileOutputStream(filename);
            model.write(out);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadRussianRelated(String filePath, int iteration) {
        try {
            JsonReader reader = new JsonReader(new FileReader(filePath));
//            System.out.println("1");
            JsonObject articles = new Gson().fromJson(reader, JsonObject.class);
//            System.out.println("2");

            int count = 0;
            int keyCount = 0;
            System.out.println("iteration: "+ iteration);
            for (String key : articles.keySet()) {
                keyCount++;
                JsonArray bind = articles.get(key).getAsJsonArray();
//                System.out.println("3");
                Resource articleResource = model.getResource(createNewURI("Статья_" + key));
//                    System.out.println(createNewURI("Статья_" + key));
                for (int i = iteration; i < bind.size() && i < iteration + 1; ++i) {
                    String related = bind.get(i).getAsString();
//                    System.out.println("4");
                    Resource relatedResource = model.getResource(createNewURI("Статья_" + related));
//                        System.out.println(createNewURI("Статья_" + related));
//                        Property property = model.getProperty(OntProperties.WWW.toString() + OntProperties.articlesRelate);
                    Property property = model.getProperty(OntProperties.seeAlso.toString());
//                        System.out.println(OntProperties.WWW.toString() + OntProperties.articlesRelate);
                    Statement statement = model.createStatement(articleResource, property, relatedResource);
//                    System.out.println("7");
                    model.add(statement);
                    ++count;
//                    System.out.println("8");
                }
                if (keyCount % 200 == 0) {
                    OutputStream out = new FileOutputStream(filename);
                    System.out.println("count: " + count);
//                System.out.println("9");
                    model.write(out);
                    out.close();
                    Thread.sleep(100);
                    setUpOntology();
                    count = 0;
                }
            }
            OutputStream out = new FileOutputStream(filename);
            System.out.println("iteration: "+ iteration);
            System.out.println("count: " + count);
//                System.out.println("9");
            model.write(out);
            out.close();
//                System.out.println("11");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
