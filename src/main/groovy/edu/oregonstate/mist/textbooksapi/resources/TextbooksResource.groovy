package edu.oregonstate.mist.textbooksapi.resources

import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject
import edu.oregonstate.mist.textbooksapi.TextbooksURIBuilder
import edu.oregonstate.mist.textbooksapi.core.Textbook

import javax.annotation.security.PermitAll
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/textbooks")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
class TextbooksResource extends Resource {

    private URI baseURI
    private TextbooksURIBuilder uriBuilder

    TextbooksResource(URI baseURI) {
        this.baseURI = baseURI
        this.uriBuilder = new TextbooksURIBuilder(baseURI)
    }

    @GET
    @Path("{id}")
    Response getTextbooksById(@PathParam("id") String id) {
        null
    }

    @GET
    Response getTextbooks(@QueryParam("term") String term,
                          @QueryParam("department") String department,
                          @QueryParam("course") String course,
                          @QueryParam("section") String section,
                          @QueryParam("isRequired") boolean isRequired) {
        null
    }

    ResourceObject textbooksResource(Textbook textbook) {
        new ResourceObject(
                id: textbook.id,
                type: "textbook",
                attributes: textbook,
                links: uriBuilder.textbooksURI(textbook.id)
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
