/* Copyright 2012-2015 the original author or authors.
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
package org.grails.plugins.reveng

import grails.core.GrailsApplication
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class ReverseEngineeringFactory {
	static ReverseEngineeringEngine create(File baseDir, GrailsApplication application) {
		Map config = buildMergedConfig(baseDir, application)

		ReverseEngineeringEngine engine = init(config)
		engine
	}

	@CompileDynamic
	protected static Map buildMergedConfig(File baseDir, GrailsApplication application) {
		def config = application.config

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

		def pluginConfiguration = config.org.grails.plugins.reveng
		mergedConfig.packageName = pluginConfiguration.packageName ?: config.grails.codegen.defaultPackage ?: application.metadata.getApplicationName()
		mergedConfig.destDir = new File(baseDir, pluginConfiguration.destDir ?: 'grails-app/domain').canonicalPath
		if (pluginConfiguration.defaultSchema) {
			mergedConfig.defaultSchema = pluginConfiguration.defaultSchema
		}
		if (pluginConfiguration.defaultCatalog) {
			mergedConfig.defaultCatalog = pluginConfiguration.defaultCatalog
		}
		if (pluginConfiguration.overwriteExisting instanceof Boolean) {
			mergedConfig.overwriteExisting = pluginConfiguration.overwriteExisting
		}

		if (pluginConfiguration.alwaysMapManyToManyTables instanceof Boolean) {
			mergedConfig.alwaysMapManyToManyTables = pluginConfiguration.alwaysMapManyToManyTables
		}

		for (String name in ['versionColumns', 'manyToManyTables', 'manyToManyBelongsTos',
							 'includeTables', 'includeTableRegexes', 'includeTableAntPatterns',
							 'excludeTables', 'excludeTableRegexes', 'excludeTableAntPatterns',
							 'excludeColumns', 'excludeColumnRegexes', 'excludeColumnAntPatterns',
							 'mappedManyToManyTables']) {
			if (pluginConfiguration[name]) {
				mergedConfig[name] = pluginConfiguration[name]
			}
		}

		mergedConfig
	}

	/**
	 * Note: defaults are set in the DbReverseEngineerCommand instead of here
	 */
	@PackageScope
    static ReverseEngineeringEngine init(Map config) {
		ReverseEngineeringEngine engine = new ReverseEngineeringEngine(
			revengConfig: config,
			driverClass:  config.driverClassName as String,
			password:     config.password as String,
			username:     config.username as String,
			url:          config.url as String,
			dialect:      config.dialect as String,
			packageName:  config.packageName as String,
			destDir:      new File(config.destDir as String),
			overwrite:    config.overwriteExisting as boolean)

		if (config.defaultSchema) {
			engine.defaultSchema = config.defaultSchema
		}
		if (config.defaultCatalog) {
			engine.defaultCatalog = config.defaultCatalog
		}

		def strategy = engine.reverseEngineeringStrategy

		((Map<String, String>)config.versionColumns).each { String table, String column ->
			strategy.addVersionColumn table, column
		}

		((Collection<String>)config.manyToManyTables).each { String table ->
			strategy.addManyToManyTable table
		}

		((Map<String, String>)config.manyToManyBelongsTos).each { String manyTable, String belongsTable ->
			strategy.setManyToManyBelongsTo manyTable, belongsTable
		}

		((Collection<String>)config.includeTables).each { String table ->
			strategy.addIncludeTable table
		}

		((Collection<String>)config.includeTableRegexes).each { String pattern ->
			strategy.addIncludeTableRegex pattern
		}

		((Collection<String>)config.includeTableAntPatterns).each { String pattern ->
			strategy.addIncludeTableAntPattern pattern
		}

		((Collection<String>)config.excludeTables).each { String table ->
			strategy.addExcludeTable table
		}

		((Collection<String>)config.excludeTableRegexes).each { String pattern ->
			strategy.addExcludeTableRegex pattern
		}

		((Collection<String>)config.excludeTableAntPatterns).each { String pattern ->
			strategy.addExcludeTableAntPattern pattern
		}

		((Map<String, List<String>>)config.excludeColumns).each { String table, List<String> columns ->
			strategy.addExcludeColumns table, columns
		}

		((Map<String, List<String>>)config.excludeColumnRegexes).each { String table, List<String> patterns ->
			strategy.addExcludeColumnRegexes table, patterns
		}

		((Map<String, List<String>>)config.excludeColumnAntPatterns).each { String table, List<String> patterns ->
			strategy.addExcludeColumnAntPatterns table, patterns
		}

		((Collection<String>)config.mappedManyToManyTables).each { String table -> strategy.addMappedManyToManyTable table }

		strategy.alwaysMapManyToManyTables = config.alwaysMapManyToManyTables as boolean

		engine
	}
}
