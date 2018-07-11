package edu.oregonstate.mist.textbooksapi

import edu.oregonstate.mist.textbooksapi.core.Textbook

import javax.ws.rs.core.UriBuilder

class TextbooksURIBuilder {

    URI baseURI

    TextbooksURIBuilder(URI baseURI) {
        this.baseURI = baseURI
    }

    URI textbooksURI(String id) {
        UriBuilder.fromUri(baseURI)
                  .path("textbooks/${id}")
                  .build(id)
    }
}
