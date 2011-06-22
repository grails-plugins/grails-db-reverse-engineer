grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'
grails.project.docs.output.dir = 'docs' // docs are checked into gh-pages branch

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
		compile('org.hibernate:hibernate-tools:3.2.4.GA') {
			transitive = false
		}
		compile('freemarker:freemarker:2.3.8') {
			transitive = false
		}
		compile('org.beanshell:bsh:2.0b4') {
			transitive = false
		}
		compile('org.hibernate:jtidy:r8-20060801') {
			transitive = false
		}
	}
}
