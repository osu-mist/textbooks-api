package edu.oregonstate.mist.textbooksapi.resources

import com.google.common.base.Optional

import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.textbooksapi.TextbooksCollector
import edu.oregonstate.mist.textbooksapi.core.Textbook

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

    @GET
    Response getTextbooks(@QueryParam("term") String term,
                          @QueryParam("subject") String subject,
                          @QueryParam("courseNumber") String courseNumber,
                          @QueryParam("section") Optional<String> section) {
        if(!(term && subject && courseNumber)) {
            return badRequest("Query must contain term, subject, and courseNumber").build()
        }
        List<Textbook> textbooks
        if(section.isPresent()) {
            textbooks = TextbooksCollector.getTextbooks(term, subject, courseNumber, section.get())
        } else {
            textbooks = TextbooksCollector.getTextbooksNoSection(term, subject, courseNumber)
        }
        ok(textbooksResult(textbooks)).build()
    }

    ResourceObject textbooksResource(Textbook textbook) {
        new ResourceObject(
                id: textbook.id,
                type: "textbook",
                attributes: textbook,
                links: null
        )
    }

    ResultObject textbooksResult(List<Textbook> textbooks) {
        new ResultObject(
                links: null,
                data: textbooks.collect { textbooksResource(it) }
        )
    }
}
