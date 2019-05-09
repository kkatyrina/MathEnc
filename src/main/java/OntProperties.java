/**
 * Created by Катерина on 06.03.2019.
 */
public enum OntProperties {
    WWW("http://www.mathEnc.ru#"),
    name("MathOnt(new).rdf"),
    articleRuClass("Статья_рус"),
    articleEnClass("Статья_англ"),
    MSCClass("Раздел_MSC"),
    termClass("Термин"),
    annotationClass("Аннотация"),
    formulaClass("Формула"),
    articleTermRu("Содержится_в_названии_рус"),
    articleTermEn("Содержится_в_названии_англ"),
    subIndex("Подраздел_MSC"),
    relatedIndex("Связан_с_разделом"),
    articleMatch("Имеет_перевод"),
    MSCArticle("Относится_к_разделу"),
    CLASS("http://www.w3.org/2002/07/owl#Class"),
    SUBCLASS("http://www.w3.org/2000/01/rdf-schema#subClassOf"),
    instance("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
    LANGSTRING("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"),
    OBJECTPROPERTY("http://www.w3.org/2002/07/owl#ObjectProperty"),
    RDFS("http://www.w3.org/2000/01/rdf-schema"),
    label("http://www.w3.org/2000/01/rdf-schema#label"),
    OWL("http://www.w3.org/2002/07/owl"),

    comment("http://www.w3.org/2000/01/rdf-schema#comment"),
    isDefinedBy("http://www.w3.org/2000/01/rdf-schema#isDefinedBy"),
    SAMEAS("http://www.w3.org/2002/07/owl#sameAs");


    private String value;

    OntProperties(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
