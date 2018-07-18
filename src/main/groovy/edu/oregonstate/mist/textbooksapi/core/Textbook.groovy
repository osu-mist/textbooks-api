package edu.oregonstate.mist.textbooksapi.core

import com.fasterxml.jackson.annotation.JsonIgnore

class Textbook {
    @JsonIgnore
    String id
    String coverImageUrl
    String title
    String author
    Integer edition
    Integer copyrightYear
    Float priceNewUSD
    Float priceUsedUSD
}
