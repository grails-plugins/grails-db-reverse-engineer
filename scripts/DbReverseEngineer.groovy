/* Copyright 2010-2011 SpringSource.
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

import grails.util.BuildSettings
import grails.util.GrailsUtil

import org.apache.ivy.core.report.ResolveReport
import org.codehaus.groovy.grails.compiler.Grailsc
import org.codehaus.groovy.grails.resolve.IvyDependencyManager
import org.codehaus.groovy.util.ReleaseInfo

includeTargets << grailsScript('_GrailsBootstrap')

USAGE = 'grails db-reverse-engineer'

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
target(dbReverseEngineer: 'Reverse-engineers a database and creates domain classes') {
	depends packageApp, loadApp

	try {
		createConfig()
		
		def mergedConfig = buildMergedConfig()
		
		event('StatusUpdate', ["Starting database reverse engineering, connecting to '$mergedConfig.url' as '$mergedConfig.username' ..."])
		
		try {
			classLoader.loadClass('grails.plugin.reveng.RevengRunner').newInstance().run(mergedConfig, metadata['app.name'])
		}
		catch (e) {
			event('StatusError', ["Error running forked reverse-engineer script: $e.message"])
			throw e
		}
		
		event('StatusUpdate', ['Finished database reverse engineering'])
	}
	catch (e) {
		GrailsUtil.deepSanitize e
		throw e
	}
}

private Map buildMergedConfig() {

	def mergedConfig = [:]

	def dsConfig = config.dataSource

	mergedConfig.driverClassName = dsConfig.driverClassName ?: 'org.h2.Driver'
	mergedConfig.password = dsConfig.password ?: ''
	mergedConfig.username = dsConfig.username ?: 'sa'
	mergedConfig.url = dsConfig.url ?: 'jdbc:h2:mem:testDB'
	if (dsConfig.dialect instanceof String) {
		mergedConfig.dialect = dsConfig.dialect
	}
	else if (dsConfig.dialect instanceof Class) {
		mergedConfig.dialect = dsConfig.dialect.name
	}

	def revengConfig = config.grails.plugin.reveng
	mergedConfig.packageName = revengConfig.packageName ?: metadata['app.name']
	mergedConfig.destDir = new File(basedir, revengConfig.destDir ?: 'grails-app/domain').canonicalPath
	if (revengConfig.defaultSchema) {
		mergedConfig.defaultSchema = revengConfig.defaultSchema
	}
	if (revengConfig.defaultCatalog) {
		mergedConfig.defaultCatalog = revengConfig.defaultCatalog
	}
	if (revengConfig.overwriteExisting instanceof Boolean) {
		mergedConfig.overwriteExisting = revengConfig.overwriteExisting
	}
	else {
		mergedConfig.overwriteExisting = true
	}

	if (revengConfig.alwaysMapManyToManyTables instanceof Boolean) {
		mergedConfig.alwaysMapManyToManyTables = revengConfig.alwaysMapManyToManyTables
	}
	else {
		mergedConfig.alwaysMapManyToManyTables = false
	}

	for (String name in ['versionColumns', 'manyToManyTables', 'manyToManyBelongsTos',
	                     'includeTables', 'includeTableRegexes', 'includeTableAntPatterns',
	                     'excludeTables', 'excludeTableRegexes', 'excludeTableAntPatterns',
	                     'excludeColumns', 'excludeColumnRegexes', 'excludeColumnAntPatterns',
	                     'mappedManyToManyTables']) {
		mergedConfig[name] = revengConfig[name]
	}

	mergedConfig
}

setDefaultTarget dbReverseEngineer
