package edu.oregonstate.mist.textbooksapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory

import edu.oregonstate.mist.textbooksapi.core.Textbook

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

class TextbooksCollector {

    static List<Textbook> getTextbooks(String term,
                                       String department,
                                       String course,
                                       String section,
                                       Boolean isRequired) {
        String urlString = "http://osu.verbacompare.com/compare/books/?id="
        String sectionId = "${term}__${department}__${course}__${section}"
        urlString += sectionId
        List<Object> rawTextbooks = objectListCollector(urlString)
        List<Textbook> textbooks = rawTextbooks.collect { refineTextbook(it, sectionId) }
        if(isRequired != null) {
            textbooks.removeIf { it.isRequired != isRequired }
        }
        textbooks
    }

    static List<Textbook> getTextbooksNoSection(String term,
                                                String department,
                                                String course,
                                                Boolean isRequired) {
        String urlString = "http://osu.verbacompare.com/compare/courses/?term_id="
        urlString += "${term}&id=${department}"
        List<Object> courses = objectListCollector(urlString)
        Object courseObject = courses.find { it.id == course }
        List<Textbook> textbooks = []
        courseObject.sections.each { section ->
            List<Textbook> newBooks = getTextbooks(
                    term, department, course, section.name, isRequired
            )
            newBooks.each { newBook ->
                if(!textbooks.find { newBook.isbn == it.isbn }) {
                    textbooks.add(newBook)
                }
            }
        }
        textbooks
    }

    static Textbook refineTextbook(Object rawTextbook, String sectionId) {
        Float usedPrice = null
        Float newPrice = null
        rawTextbook.offers.each {
            if(it.condition == "new" && it.rental_days == null) {
                newPrice = Float.parseFloat(it.price)
            } else if(it.condition == "used" && it.rental_days == null) {
                usedPrice = Float.parseFloat(it.price)
            }
        }
        Integer bookEdition
        Integer copyrightYear
        rawTextbook.with {
            bookEdition = edition ? Integer.parseInt(edition) : null
            copyrightYear = copyright_year ? Integer.parseInt(copyright_year) : null
        }

        new Textbook(
                id: sectionId + "__${rawTextbook.isbn}",
                isbn: rawTextbook.isbn,
                isRequired: rawTextbook.required == "Required",
                coverImageUrl: rawTextbook.cover_image_url,
                title: rawTextbook.title,
                author: rawTextbook.author,
                edition: bookEdition,
                copyright_year: copyrightYear,
                priceNewUSD: newPrice,
                priceUsedUSD: usedPrice
        )
    }

    static List<Object> objectListCollector(String urlString) {
        HttpClient client = HttpClients.createDefault()
        HttpGet req = new HttpGet(urlString)
        HttpResponse res = client.execute(req)
        int status = res.getStatusLine().getStatusCode()

        // Verba compare request should return a 200, regardless of parameters
        if(status != 200) {
            throw new Exception("Something went wrong with Verba compare request")
        }
        ObjectMapper objectMapper = new ObjectMapper()
        TypeFactory typeFactory = objectMapper.getTypeFactory()
        String rawTextbooksString = EntityUtils.toString(res.getEntity())
        List<Object> objectList = objectMapper.readValue(
                rawTextbooksString,
                typeFactory.constructCollectionType(List.class, Object.class)
        )
        objectList
    }
}
