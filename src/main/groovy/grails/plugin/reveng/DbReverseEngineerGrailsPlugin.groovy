/* Copyright 2010-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.reveng

import grails.plugins.Plugin
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@CompileStatic
@Slf4j
class DbReverseEngineerGrailsPlugin extends Plugin {
	String grailsVersion = '3.0.0 > *'
	String author = 'Burt Beckwith'
	String authorEmail = 'burt@burtbeckwith.com'
	String title = 'Grails Database Reverse Engineering Plugin'
	String description = 'Reverse-engineers a database to Grails domain classes.'
	String documentation = 'http://grails-plugins.github.io/grails-db-reverse-engineer/'
	String license = 'APACHE'
	def organization = [name: 'Grails', url: 'http://www.grails.org/']
	def issueManagement = [url: 'https://github.com/grails-plugins/grails-db-reverse-engineer/issues']
	def scm = [url: 'https://github.com/grails-plugins/grails-db-reverse-engineer']
}
