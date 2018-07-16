package edu.oregonstate.mist.textbooksapi.resources

import com.google.common.base.Optional

import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.textbooksapi.TextbooksCollector
import edu.oregonstate.mist.textbooksapi.core.Textbook

import java.util.regex.Pattern

import javax.annotation.security.PermitAll
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/textbooks")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
class TextbooksResource extends Resource {

    private TextbooksCollector textbooksCollector
    private Pattern validPattern = ~"[A-Za-z0-9-]+"

    TextbooksResource(TextbooksCollector textbooksCollector) {
        this.textbooksCollector = textbooksCollector
    }

    /**
     * GET textbooks by parameters
     *
     * @param term Term of course. Example: 2018-Fall
     * @param subject Subject of course. Example: HST
     * @param courseNumber Course number. Example: 101
     * @param section Section number. Example: 001
     * @return Response
     */
    @GET
    Response getTextbooks(@QueryParam("term") String term,
                          @QueryParam("subject") String subject,
                          @QueryParam("courseNumber") String courseNumber,
                          @QueryParam("section") Optional<String> section) {
        def params = [term: term, subject: subject, courseNumber: courseNumber]
        def badResponse = null
        params.findAll() { key, value ->
            if(!value) {
                badResponse = badRequest("Query must contain ${key}").build()
            } else if(!(value ==~ validPattern)) {
                badResponse = badRequest("${key} must match pattern ${validPattern}").build()
            }
        }
        if(badResponse) {
            return badResponse
        }
        if(section.isPresent() && !(section.get() ==~ validPattern)) {
            return badRequest("section must match pattern ${validPattern}").build()
        }
        List<Textbook> textbooks = textbooksCollector.getTextbooks(
                term, subject, courseNumber, section
        )
        ok(textbooksResult(textbooks)).build()
    }

    /**
     * Builds a jsonapi ResourceObject from a Textbook object
     *
     * @param textbook
     * @return ResourceObject
     */
    ResourceObject textbooksResource(Textbook textbook) {
        new ResourceObject(
                id: textbook.id,
                type: "textbook",
                attributes: textbook,
                links: null
        )
    }

    /**
     * Builds a jsonapi ResultObject from a list of Textbook objects
     *
     * @param textbooks
     * @return ResultObject
     */
    ResultObject textbooksResult(List<Textbook> textbooks) {
        new ResultObject(
                links: null,
                data: textbooks.collect { textbooksResource(it) }
        )
    }
}
