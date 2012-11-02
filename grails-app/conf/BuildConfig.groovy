//grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'docs/manual' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()
		mavenCentral()
	}

	dependencies {
		compile('org.hibernate:hibernate-tools:3.6.0.CR1')
	}

	plugins {
		build(':release:2.0.4') { export = false }
	}
	
}
