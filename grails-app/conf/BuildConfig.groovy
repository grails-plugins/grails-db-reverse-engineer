grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'docs/manual' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()

		mavenRepo 'https://repository.jboss.org/nexus/content/repositories/central/'
	}

	dependencies {
		compile('org.hibernate:hibernate-tools:3.2.4.GA') { transitive = false }

		compile('org.hibernate:hibernate-core:3.3.1.GA') { transitive = false }
		compile('dom4j:dom4j:1.6.1') { transitive = false }

		compile('freemarker:freemarker:2.3.8') { transitive = false }
		compile('org.beanshell:bsh:2.0b4') { transitive = false }
		compile('org.hibernate:jtidy:r8-20060801') { transitive = false }
	}

	plugins {
		build(':release:1.0.0.RC3') { export = false }
	}
}
