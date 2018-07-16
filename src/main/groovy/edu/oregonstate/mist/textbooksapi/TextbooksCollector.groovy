package edu.oregonstate.mist.textbooksapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory

import edu.oregonstate.mist.textbooksapi.core.Textbook
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils

import javax.ws.rs.core.UriBuilder

class TextbooksCollector {

    private HttpClient httpClient
    private URI booksUri
    private URI coursesUri

    TextbooksCollector(HttpClient httpClient, String verbaCompareUri) {
        this.httpClient = httpClient
        UriBuilder builder = UriBuilder.fromPath(verbaCompareUri)
        this.booksUri = builder.path("compare/books").build()
        this.coursesUri = builder.path("compare/courses").build()
    }

    /**
     * Queries Verba compare and builds a list of textbooks
     *
     * @param term
     * @param subject
     * @param courseNumber
     * @param section
     * @return
     */
    List<Textbook> getTextbooks(String term, String subject,
                                       String courseNumber, String section) {
        String urlString = "http://osu.verbacompare.com/compare/books/?id="
        urlString += "${term}__${subject}__${courseNumber}__${section}"
        List<Object> rawTextbooks = objectListCollector(urlString)
        List<Textbook> textbooks = rawTextbooks.collect { refineTextbook(it) }
        textbooks
    }

    /**
     * Queries Verba compare and builds a list of textbooks for all sections of a course
     *
     * @param term
     * @param department
     * @param course
     * @return
     */
    List<Textbook> getTextbooksNoSection(String term, String department, String course) {
        String urlString = "http://osu.verbacompare.com/compare/courses/?term_id="
        urlString += "${term}&id=${department}"
        List<Object> courses = objectListCollector(urlString)
        Object courseObject = courses.find { it.id == course }
        List<Textbook> textbooks = []
        courseObject?.sections?.each { section ->
            List<Textbook> newBooks = getTextbooks(
                    term, department, course, section.name
            )
            newBooks.each { newBook ->
                if(!textbooks.find { newBook.id == it.id }) {
                    textbooks.add(newBook)
                }
            }
        }
        textbooks
    }

    /**
     * Converts verba compare JSON response to Textbook object
     *
     * @param rawTextbook
     * @return
     */
    Textbook refineTextbook(Object rawTextbook) {
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
                id: rawTextbook.isbn,
                coverImageUrl: rawTextbook.cover_image_url,
                title: rawTextbook.title,
                author: rawTextbook.author,
                edition: bookEdition,
                copyrightYear: copyrightYear,
                priceNewUSD: newPrice,
                priceUsedUSD: usedPrice
        )
    }

    /**
     * Queries Verba compare and returns the JSON list response
     *
     * @param urlString
     * @return
     */
    List<Object> objectListCollector(String urlString) {
        HttpGet req = new HttpGet(urlString)
        HttpResponse res = httpClient.execute(req)
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
