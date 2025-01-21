package com.example.reveng.test

import grails.core.GrailsApplication
import org.grails.plugins.reveng.ReverseEngineeringEngine
import org.grails.plugins.reveng.ReverseEngineeringFactory
import groovy.util.logging.Slf4j

import java.nio.file.Path

@Slf4j
class BootStrap {
    GrailsApplication grailsApplication

    def init = { servletContext ->
        ReverseEngineeringEngine engine = ReverseEngineeringFactory.create(Path.of('').toAbsolutePath().toFile(), grailsApplication)
        log.info("Starting database reverse engineering, connecting to '${engine.properties.url}' as '${engine.properties.username}' ...")
        engine.execute()
        log.info('Finished database reverse engineering')
        System.exit(0)
    }
}