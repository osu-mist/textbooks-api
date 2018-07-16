package edu.oregonstate.mist.textbooksapi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Optional

import edu.oregonstate.mist.textbooksapi.core.Textbook

import javax.ws.rs.core.UriBuilder

import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TextbooksCollector {

    private HttpClient httpClient
    private URI booksUri
    private URI coursesUri
    private static Logger logger = LoggerFactory.getLogger(this)
    ObjectMapper objectMapper = new ObjectMapper()

    TextbooksCollector(HttpClient httpClient, String verbaCompareUri) {
        this.httpClient = httpClient
        UriBuilder builder = UriBuilder.fromPath(verbaCompareUri)
        this.booksUri = builder.path("compare/books").build()
        builder.replacePath(verbaCompareUri)
        this.coursesUri = builder.path("compare/courses").build()
    }

    /**
     * Builds a list of textbooks in a single section of a course
     *
     * @param term
     * @param subject
     * @param courseNumber
     * @param section
     * @return
     */
    List<Textbook> getTextbooks(String term, String subject,
                                String courseNumber, Optional<String> section) {
        if(!section.isPresent()) {
            return getTextbooksNoSection(term, subject, courseNumber)
        }

        UriBuilder uriBuilder = UriBuilder.fromUri(booksUri)
        uriBuilder.queryParam("id", "${term}__${subject}__${courseNumber}__${section.get()}")

        def res = getResponse(uriBuilder.build())
        String rawTextbooksString = EntityUtils.toString(res.getEntity())
        List<RawTextbook> rawTextbooks
        try {
            rawTextbooks = objectMapper.readValue(
                    rawTextbooksString,
                    new TypeReference<List<RawTextbook>>() {}
            )
        } catch (JsonMappingException exception) {
            throw new Exception(exception)
        }
        List<Textbook> textbooks = rawTextbooks.collect { refineTextbook(it) }
        textbooks
    }

    /**
     * Builds a list of textbooks for all sections of a course
     *
     * @param term
     * @param department
     * @param course
     * @return
     */
    List<Textbook> getTextbooksNoSection(String term, String department, String course) {
        UriBuilder uriBuilder = UriBuilder.fromUri(coursesUri)
        uriBuilder.queryParam("term_id", "${term}")
        uriBuilder.queryParam("id", "${department}")

        def res = getResponse(uriBuilder.build())
        String coursesString = EntityUtils.toString(res.getEntity())
        List<Course> courses
        try {
            courses = objectMapper.readValue(
                    coursesString,
                    new TypeReference<List<Course>>(){}
            )
        } catch (JsonMappingException exception) {
            throw new Exception(exception)
        }
        Course courseObject = courses.find { it.id == course }
        List<Textbook> textbooks = []
        courseObject?.sections?.each { section ->
            List<Textbook> newBooks = getTextbooks(
                    term, department, course, Optional.of(section.name)
            )
            newBooks.each { newBook ->
                if (!textbooks.find { newBook.id == it.id }) {
                    textbooks.add(newBook)
                }
            }
        }
        textbooks
    }

    /**
     * Converts RawTextbook object to Textbook object
     *
     * @param rawTextbook
     * @return
     */
    Textbook refineTextbook(RawTextbook rawTextbook) {
        Float usedPrice = null
        Float newPrice = null
        rawTextbook.offers.each {
            if (it.condition == "new" && it.rental_days == null) {
                newPrice = Float.parseFloat(it.price)
            } else if (it.condition == "used" && it.rental_days == null) {
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
     * Queries Verba compare and returns the HTTP response
     *
     * @param uri
     * @return
     */
    HttpResponse getResponse(URI uri) {
        HttpGet req = new HttpGet(uri)
        HttpResponse res = httpClient.execute(req)
        int status = res.getStatusLine().getStatusCode()

        // Verba compare request should return a 200, regardless of parameters
        if (status != HttpStatus.SC_OK) {
            logger.error("""\
            Something went wrong with Verba compare request.
            Requested URI: ${uri}.
            HTTP response code: ${status}\
            """.stripIndent())
            throw new Exception("Something went wrong with Verba compare request")
        }
        res
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class RawTextbook {
    String isbn
    String cover_image_url
    String title
    String author
    String edition
    String copyright_year
    List<Offer> offers
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Offer {
    String price
    String condition
    String rental_days
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Course {
    String id
    List<Section> sections
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Section {
    String name
}