/*
 * Created by Катерина on 06.03.2019.
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OntologyUtils {

    private static String createNewResource(String name) {
        return OntProperties.WWW.toString() + name;
    }

    public static void load(Model model, String articleURI, String termURI, String preTitle, String text, String link, boolean russianFlag) {
        Property bind;
        Resource articleClassResource;
        if (russianFlag) {
            String articleClassURI = createNewResource(OntProperties.articleRuClass.toString());
            articleClassResource = model.getResource(articleClassURI);
            bind = model.getProperty(OntProperties.WWW.toString() + OntProperties.articleTermRu.toString());
        }
        else {
            String articleClassURI = createNewResource(OntProperties.articleEnClass.toString());
            articleClassResource = model.getResource(articleClassURI);
            bind = model.getProperty(OntProperties.WWW.toString() + OntProperties.articleTermEn.toString());
        }
        String termClassURI = createNewResource(OntProperties.termClass.toString());
        Resource termClassResource = model.getResource(termClassURI);
        Resource articleResource = model.createResource(articleURI);
        Resource termResource = model.createResource(termURI);

        Property instanceOf = model.getProperty(OntProperties.instance.toString());
        Statement statement = model.createStatement(articleResource, instanceOf, articleClassResource);
        model.add(statement);
        statement = model.createStatement(termResource, instanceOf, termClassResource);
        model.add(statement);

        Property definedBy = model.getProperty(OntProperties.isDefinedBy.toString());
        statement = model.createStatement(articleResource, definedBy, text);
        model.add(statement);

        Property label = model.getProperty(OntProperties.label.toString());
        statement = model.createStatement(articleResource, label, preTitle);
        model.add(statement);
        statement = model.createStatement(termResource, label, preTitle);
        model.add(statement);

        Property comment = model.getProperty(OntProperties.comment.toString());
        statement = model.createStatement(articleResource, comment, link);
        model.add(statement);

        statement = model.createStatement(termResource, bind, articleResource);
        model.add(statement);
    }

    public static void readMSCIndexes(String filePath, List<String> MSCIndexes, List<String> MSCCategories) {
        Document xmlFileMSC = null;
        try {
            xmlFileMSC = Jsoup.parse(new File(filePath), "UTF-8");
            Elements items = xmlFileMSC.getElementsByTag("taxon");
            for (Element item : items) {
                String index = item.getElementsByTag("code").first().ownText();
                MSCIndexes.add(index.replaceAll("--", "-").toUpperCase());

                String category = item.getElementsByTag("name").first().ownText();
                MSCCategories.add(category.replaceAll("�", ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean ifInstance(InfModel inf, String classURI, String instanceURI) {

        String query = "SELECT ?s {<" + instanceURI + "> ?s <" + classURI +">}";

//        System.out.println("SparqlQuery for InstanceCheck: "+query);

        Query jenaquery = QueryFactory.create(query) ;
        QueryExecution qexec = QueryExecutionFactory.create(jenaquery, inf);
        ResultSet jenaresults = qexec.execSelect();
        if (jenaresults.hasNext()) {
            String result = jenaresults.next().toString();
//            System.out.println("result: " + result);
            if (result.equalsIgnoreCase("( ?s = rdf:type )")) {
                return true;
            }
        }
        return false;
    }

    public static String findNearest(InfModel inf, String index) {
        try {
            String classURI = createNewResource(OntProperties.MSCClass.toString());
            if (ifInstance(inf, classURI, createNewResource(index))) return index;
            index = index.replaceAll("--", "-");
            String first = index.substring(0, 3) + "XX";
//        System.out.println("first: "+first);
            if (ifInstance(inf, classURI, createNewResource(first))) return first;
            String second = index.substring(0, 2) + "-" + index.substring(3, 5);
//        System.out.println("second: "+second);
            if (ifInstance(inf, classURI, createNewResource(second))) return second;
            String third = index.substring(0, 2) + "-XX";
//        System.out.println("third: "+third);
            if (ifInstance(inf, classURI, createNewResource(third))) return third;

        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(index);
        }
        return "";
    }
}
