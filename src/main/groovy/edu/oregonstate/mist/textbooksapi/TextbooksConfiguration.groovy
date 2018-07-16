package edu.oregonstate.mist.textbooksapi

import edu.oregonstate.mist.api.Configuration
import io.dropwizard.client.HttpClientConfiguration

class TextbooksConfiguration extends Configuration {

    HttpClientConfiguration httpClient

    Map<String, String> textbooksApi

}
