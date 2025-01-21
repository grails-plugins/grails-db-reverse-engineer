package com.example.reveng.test

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.testcontainers.containers.MySQLContainer

class Application extends GrailsAutoConfiguration {

    static void main(String[] args) {
        try (
			MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:9.1.0")
					.withDatabaseName("reveng")
					.withUsername("reveng")
					.withPassword("reveng")
                    .withInitScript("reveng.sql")
        ) {
            mysqlContainer.start()

            System.setProperty('dataSource.url', mysqlContainer.getJdbcUrl())
            GrailsApp.run Application, args
        }
    }
}
