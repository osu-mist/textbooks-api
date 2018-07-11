package edu.oregonstate.mist.textbooksapi.resources

import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.api.jsonapi.ResultObject

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

    TextbooksResource(URI baseURI) {
        this.baseURI = baseURI
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
}
