package edu.oregonstate.mist.textbooksapi

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.textbooksapi.health.TextbooksHealthCheck
import edu.oregonstate.mist.textbooksapi.resources.TextbooksResource
import io.dropwizard.client.HttpClientBuilder
import io.dropwizard.setup.Environment
import org.apache.http.client.HttpClient

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

        // The Verbacompare url currently does not send its certificates in the right way
        // so this system property allows intermediate certificates to be automatically downloaded
        System.setProperty("com.sun.security.enableAIAcaIssuers", "true")

        HttpClientBuilder httpClientBuilder = new HttpClientBuilder(environment)
        if(configuration.httpClient != null) {
            httpClientBuilder.using(configuration.httpClient)
        }
        HttpClient httpClient = httpClientBuilder.build()

        TextbooksCollector textbooksCollector = new TextbooksCollector(
                httpClient, configuration.textbooksApi.verbaCompareUri
        )
        TextbooksHealthCheck textbooksHealthCheck = new TextbooksHealthCheck(
                httpClient, configuration.textbooksApi.verbaCompareUri
        )
        environment.healthChecks().register("TextbooksHealthCheck", textbooksHealthCheck)

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
