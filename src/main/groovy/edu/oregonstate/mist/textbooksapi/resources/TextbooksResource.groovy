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
    @Path("{id}")
    Response getTextbooksById() {
        Response.status(Response.Status.NOT_IMPLEMENTED).build()
    }

    @GET
    Response getTextbooks(@QueryParam("term") String term,
                          @QueryParam("department") String department,
                          @QueryParam("course") String course,
                          @QueryParam("section") Optional<String> section,
                          @QueryParam("isRequired") Optional<Boolean> isRequired) {
        if(!(term && department && course)) {
            return badRequest("Query must contain term, department, and course").build()
        }
        List<Textbook> textbooks
        if(section.isPresent()) {
            textbooks = TextbooksCollector.getTextbooks(
                    term, department, course, section.get(), isRequired.orNull())
        } else {
            textbooks = TextbooksCollector.getTextbooksNoSection(
                    term, department, course, isRequired.orNull()
            )
        }
        ok(textbooksResult(textbooks)).build()
    }

    ResourceObject textbooksResource(Textbook textbook) {
        new ResourceObject(
                id: textbook.isbn,
                type: "textbook",
                attributes: textbook,
                links: null
        )
    }

    ResultObject textbooksResult(Textbook textbook) {
        new ResultObject(
                links: null,
                data: textbooksResource(textbook)
        )
    }

    ResultObject textbooksResult(List<Textbook> textbooks) {
        new ResultObject(
                links: null,
                data: textbooks.collect { textbooksResource(it) }
        )
    }
}
