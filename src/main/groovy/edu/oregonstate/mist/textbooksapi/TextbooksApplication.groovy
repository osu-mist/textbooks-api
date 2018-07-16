package edu.oregonstate.mist.textbooksapi

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.textbooksapi.resources.TextbooksResource

import io.dropwizard.client.HttpClientBuilder
import io.dropwizard.setup.Environment

/**
 * Main application class.
 */
class TextbooksApplication extends Application<TextbooksConfiguration> {
    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    void run(TextbooksConfiguration configuration, Environment environment) {
        this.setup(configuration, environment)

        HttpClientBuilder httpClientBuilder = new HttpClientBuilder(environment)

        if(configuration.httpClient != null) {
            httpClientBuilder.using(configuration.httpClient)
        }

        TextbooksCollector textbooksCollector = new TextbooksCollector(
                httpClientBuilder.build(), configuration.textbooksApi.verbaCompareUri
        )

        environment.jersey().register(new TextbooksResource(textbooksCollector))
    }

    /**
     * Instantiates the application class with command-line arguments.
     *
     * @param arguments
     * @throws Exception
     */
    static void main(String[] arguments) throws Exception {
        new TextbooksApplication().run(arguments)
    }
}
