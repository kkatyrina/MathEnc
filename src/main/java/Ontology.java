/**
 * Created by Катерина on 06.03.2019.
 */

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.*;
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

    private static String createNewResource(String name) {
        return OntProperties.WWW.toString() + name;
    }

    public static void main(String[] argv) {
        setUpOntology();

        loadEnglish(basePath + FilePath.englishArticles.toString());
        loadRussian(basePath + FilePath.russianArticlesAnnotated.toString());

        loadIndexes(basePath + FilePath.MSCConcepts.toString(),
                basePath + FilePath.MSCRelations.toString());

        loadArticleMatch(basePath + FilePath.articleMatch.toString());

        loadIndexArticle(basePath + FilePath.englishArticlesParsed,
                basePath + FilePath.articleIndexes);
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
            String articleClassURI = createNewResource(OntProperties.articleEnClass.toString());
            String termClassURI = createNewResource(OntProperties.termClass.toString());
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

                String articleURI = createNewResource("Article_" + title+ "_" + i);
                String termURI = createNewResource("Term_" + title+ "_" + i);
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
            String articleClassURI = createNewResource(OntProperties.articleRuClass.toString());
            String termClassURI = createNewResource(OntProperties.termClass.toString());
            System.out.println(articleClassURI);
            System.out.println(termClassURI);
            JsonReader reader = new JsonReader(new FileReader(filePath));
            JsonArray array = new Gson().fromJson(reader, JsonArray.class);

            for (int i = 0; i < array.size(); ++i) {
                JsonObject object = array.get(i).getAsJsonObject();
                String preTitle = object.get("title").getAsString();
                String text = object.get("text").getAsString();
                String title = preTitle.replaceAll(" ", "_");

                String articleURI = createNewResource("Статья_" + title + "_" + i);
                String termURI = createNewResource("Термин_" + title + "_" + i);
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
        String classURI = createNewResource(OntProperties.MSCClass.toString());
        Resource className = model.getResource(classURI);

        for (int i = 0; i < MSCIndexes.size(); ++i) {
            String index = MSCIndexes.get(i);
            String category = MSCCategories.get(i);

//            System.out.println("index: " + index);
//            System.out.println("category: " + category);
            Resource indexResource = model.createResource(createNewResource(index));

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
                String indexURI = createNewResource(index);

                if (OntologyUtils.ifInstance(inf, classURI, indexURI)) {

                    Elements parents = item.getElementsByTag("parent");
                    if (!parents.isEmpty()) {
                        String parent = parents.first().ownText();
//                        index = index.replaceAll("http://libmeta.ru/taxon/msc#", "");
                        parent = parent.replaceAll("http://libmeta.ru/taxon/msc#", "");
                        parent = parent.replaceAll("--", "-").toUpperCase();

//                        System.out.println("index: " + index);
//                        System.out.println("parent: " + parent);
                        String parentURI = createNewResource(parent);

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
                        String relatedURI = createNewResource(relatedOne);

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
                key = key.replaceAll(" ", "_");
//                String idEn = value.substring(value.indexOf('_') + 1);
                value = value.replaceAll(" ", "_");
                value = value.replaceAll("\\u2013", "-");

                String ruURI = createNewResource("Статья_" + key);
                String enURI = createNewResource("Article_" + value);
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
                String articleURI = createNewResource("Article_" + key.replaceAll(" ", "_") + "_" + id);
//                System.out.println(articleURI);
                Resource articleResource = model.getResource(articleURI);
                JsonArray indexes = articles.get(key).getAsJsonArray();
                for (int i = 0; i < indexes.size(); ++i) {
                    String index = indexes.get(i).getAsString().toUpperCase();
                    index = index.replaceAll("\\.", "-");
//                    System.out.println(index);
                    String nearest = OntologyUtils.findNearest(inf, index);
//                    String nearest = index;
                    String articleClassURI = createNewResource(OntProperties.articleEnClass.toString());
                    String indexClassURI = createNewResource(OntProperties.MSCClass.toString());
                    String indexURI = createNewResource(nearest.toUpperCase());

                    if (OntologyUtils.ifInstance(inf, articleClassURI, articleURI)) {
                        Resource indexResource = model.getResource(indexURI);
                        if (!OntologyUtils.ifInstance(inf, indexClassURI, indexURI)) {
                            System.out.println(index);
                            Resource classResource = model.getResource(indexClassURI);
                            indexResource = model.getResource(createNewResource(index));
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
}
