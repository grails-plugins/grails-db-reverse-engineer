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

import grails.core.GrailsApplication
import grails.dev.commands.ApplicationCommand
import grails.dev.commands.ExecutionContext

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class DbReverseEngineerCommand implements ApplicationCommand {

	final String description = 'Reverse-engineers a database and creates domain classes'

	boolean handle(ExecutionContext executionContext) {
		RevengRunner runner = new RevengRunner()
		def mergedConfig = buildMergedConfig(executionContext)

		println "Starting database reverse engineering, connecting to '$mergedConfig.url' as '$mergedConfig.username' ..."

		runner.run mergedConfig

		println 'Finished database reverse engineering'

		true
	}

	protected Map buildMergedConfig(ExecutionContext ctx) {

		GrailsApplication grailsApplication = applicationContext.getBean(GrailsApplication)
		def config = grailsApplication.config

		def mergedConfig = [
			alwaysMapManyToManyTables: false,
			defaultCatalog: '',
			defaultSchema: '',
			excludeColumnAntPatterns: [:],
			excludeColumnRegexes: [:],
			excludeColumns: [:],
			excludeTableAntPatterns: [],
			excludeTableRegexes: [],
			excludeTables: [],
			includeTableAntPatterns: [],
			includeTableRegexes: [],
			includeTables: [],
			manyToManyBelongsTos: [:],
			manyToManyTables: [],
			mappedManyToManyTables: [],
			overwriteExisting: true,
			versionColumns: [:]
		]

		def dsConfig = config.dataSource

		mergedConfig.driverClassName = dsConfig.driverClassName ?: 'org.h2.Driver'
		mergedConfig.password = dsConfig.password ?: ''
		mergedConfig.username = dsConfig.username ?: 'sa'
		mergedConfig.url = dsConfig.url ?: 'jdbc:h2:mem:testDB'
		if (dsConfig.dialect instanceof CharSequence) {
			mergedConfig.dialect = dsConfig.dialect.toString()
		}
		else if (dsConfig.dialect instanceof Class) {
			mergedConfig.dialect = dsConfig.dialect.name
		}

		def revengConfig = config.grails.plugin.reveng
		mergedConfig.packageName = revengConfig.packageName ?:
				config.grails.codegen.defaultPackage ?:
				grailsApplication.metadata.getApplicationName()
		mergedConfig.destDir = new File(ctx.baseDir, revengConfig.destDir ?: 'grails-app/domain').canonicalPath
		if (revengConfig.defaultSchema) {
			mergedConfig.defaultSchema = revengConfig.defaultSchema
		}
		if (revengConfig.defaultCatalog) {
			mergedConfig.defaultCatalog = revengConfig.defaultCatalog
		}
		if (revengConfig.overwriteExisting instanceof Boolean) {
			mergedConfig.overwriteExisting = revengConfig.overwriteExisting
		}

		if (revengConfig.alwaysMapManyToManyTables instanceof Boolean) {
			mergedConfig.alwaysMapManyToManyTables = revengConfig.alwaysMapManyToManyTables
		}

		for (String name in ['versionColumns', 'manyToManyTables', 'manyToManyBelongsTos',
		                     'includeTables', 'includeTableRegexes', 'includeTableAntPatterns',
		                     'excludeTables', 'excludeTableRegexes', 'excludeTableAntPatterns',
		                     'excludeColumns', 'excludeColumnRegexes', 'excludeColumnAntPatterns',
		                     'mappedManyToManyTables']) {
			if (revengConfig[name]) {
				mergedConfig[name] = revengConfig[name]
			}
		}

		mergedConfig
	}
}
