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
package grails.plugin.reveng

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class RevengRunner {

	void run(Map config) {

		Reenigne reenigne = new Reenigne(
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
			reenigne.defaultSchema = config.defaultSchema
		}
		if (config.defaultCatalog) {
			reenigne.defaultCatalog = config.defaultCatalog
		}

		def strategy = reenigne.reverseEngineeringStrategy

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

		reenigne.execute()
	}
}
