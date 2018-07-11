package edu.oregonstate.mist.textbooksapi.core

import com.fasterxml.jackson.annotation.JsonIgnore

class Textbook {
    @JsonIgnore
    Integer id
    String isbn
    String required
    String coverImageUrl
    String title
    String author
    String edition
    String copyright_year
    String priceNew
    String priceUsed
}
