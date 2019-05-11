/**
 * Created by Катерина on 06.03.2019.
 */
public enum OntProperties {
    WWW("http://www.mathEnc.ru#"),
    name("MathOnt.rdf"),
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
    annotationArticle("Содержится_в_тексте_аннотация"),
    annotationTerm("Ссылается_на_термин"),
    annotationStart("Начало_ссылки"),
    annotationEnd("Конец_ссылки"),
    formulaArticle("Содержится_в_тексте_формула"),
    seeAlso("http://www.w3.org/2000/01/rdf-schema#seeAlso"),
    instance("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
    label("http://www.w3.org/2000/01/rdf-schema#label"),
    comment("http://www.w3.org/2000/01/rdf-schema#comment"),
    isDefinedBy("http://www.w3.org/2000/01/rdf-schema#isDefinedBy");


    private String value;

    OntProperties(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }
}
