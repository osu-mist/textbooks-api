package edu.oregonstate.mist.textbooksapi

import edu.oregonstate.mist.api.Application
import edu.oregonstate.mist.api.Configuration
import edu.oregonstate.mist.textbooksapi.resources.TextbooksResource
import io.dropwizard.setup.Environment

/**
 * Main application class.
 */
class TextbooksApplication extends Application<Configuration> {
    /**
     * Parses command-line arguments and runs the application.
     *
     * @param configuration
     * @param environment
     */
    @Override
    void run(Configuration configuration, Environment environment) {
        this.setup(configuration, environment)
        environment.jersey().register(new TextbooksResource())
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
