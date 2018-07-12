package edu.oregonstate.mist.textbooksapi.core

import com.fasterxml.jackson.annotation.JsonIgnore

class Textbook {
    @JsonIgnore
    String id
    Boolean isRequired
    String coverImageUrl
    String title
    String author
    Integer edition
    Integer copyright_year
    Float priceNewUSD
    Float priceUsedUSD
}
