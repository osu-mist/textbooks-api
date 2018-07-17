package edu.oregonstate.mist.textbooksapi

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Optional

import edu.oregonstate.mist.textbooksapi.core.Textbook
import edu.oregonstate.mist.api.Resource

import javax.ws.rs.core.UriBuilder
import javax.ws.rs.core.Response

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
        List<RawTextbook> rawTextbooks = getListFromResponse(res, RawTextbook.class)
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
        List<Course> courses = getListFromResponse(res, Course.class)
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
            if(it.condition == "new" && it.rentalDays == null) {
                newPrice = Float.parseFloat(it.price)
            } else if(it.condition == "used" && it.rentalDays == null) {
                usedPrice = Float.parseFloat(it.price)
            }
        }
        Integer bookEdition
        Integer copyrightYearInt
        rawTextbook.with {
            bookEdition = edition ? Integer.parseInt(edition) : null
            copyrightYearInt = copyrightYear ? Integer.parseInt(copyrightYear) : null
        }

        new Textbook(
                id: rawTextbook.isbn,
                coverImageUrl: rawTextbook.coverImageUrl,
                title: rawTextbook.title,
                author: rawTextbook.author,
                edition: bookEdition,
                copyrightYear: copyrightYearInt,
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

    Response validateTerm(String academicYear, String term) {
        String termString = "${academicYear}-${term}"
        def res = getResponse(coursesUri)
        List<Term> termList = getListFromResponse(res, Term.class)
        if(termList.find { it.id == termString }) {
            null
        } else {
            Resource.badRequest("Invalid term: '${termString}'. Valid terms are [" +
                    "${termList.id.join(", ")}]").build()
        }
    }

    def getListFromResponse(HttpResponse res, Class objectType) {
        String listString = EntityUtils.toString(res.getEntity())
        def list
        def listType = objectMapper.getTypeFactory().constructCollectionType(
                List.class, objectType
        )
        try {
            list = objectMapper.readValue(listString, listType)
        } catch (JsonMappingException exception) {
            throw new Exception(exception)
        }
        list
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class RawTextbook {
    String isbn
    @JsonProperty("cover_image_url")
    String coverImageUrl
    String title
    String author
    String edition
    @JsonProperty("copyright_year")
    String copyrightYear
    List<Offer> offers
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Offer {
    String price
    String condition
    @JsonProperty("rental_days")
    String rentalDays
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

@JsonIgnoreProperties(ignoreUnknown = true)
class Term {
    String id
}