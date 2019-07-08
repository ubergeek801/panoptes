package org.slaq.slaqworx.panoptes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Panoptes is a prototype system for investment portfolio compliance assurance.
 *
 * @author jeremy
 */
@SpringBootApplication
public class Panoptes {
    /**
     * The entry point for the Panoptes application. Currently the app doesn't do anything.
     *
     * @param args
     *            the program arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(Panoptes.class, args);
    }
}
