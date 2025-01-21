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
package org.grails.plugins.reveng

import grails.util.GrailsUtil
import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.util.logging.Slf4j
import org.hibernate.boot.Metadata
import org.hibernate.cfg.Environment
import org.hibernate.cfg.reveng.ReverseEngineeringSettings
import org.hibernate.tool.hbm2x.ArtifactCollector
import org.hibernate.tool.hbm2x.Exporter
import org.hibernate.tool.hbm2x.HibernateMappingExporter

/**
 * Main class, called from the reverse engineer script.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class ReverseEngineeringEngine {

	File destDir
	String packageName
	String driverClass
	String password
	String username
	String url
	String dialect
	String defaultSchema
	String defaultCatalog

	boolean preferBasicCompositeIds = true
	boolean detectOneToOne = true
	boolean detectManyToMany = true
	boolean detectOptimisticLock = true
	boolean ejb3 = false
	boolean jdk5 = true
	boolean overwrite = true

	Map revengConfig

	GrailsReverseEngineeringStrategy reverseEngineeringStrategy = GrailsReverseEngineeringStrategy.INSTANCE

	protected HibernateMappingExporter hbmXmlExporter = new HibernateMappingExporter()
	protected GrailsPojoExporter pojoExporter
	protected GrailsJdbcMetadataDescriptor metadataDescriptor

	void execute() {
		try {
			buildConfiguration()
			Metadata metadata = metadataDescriptor.createMetadata()

			pojoExporter = new GrailsPojoExporter(overwrite, revengConfig)
			configureExporter pojoExporter
			pojoExporter.properties.setProperty 'ejb3', ejb3 as String
			pojoExporter.properties.setProperty 'jdk5', jdk5 as String

			configureExporter hbmXmlExporter

			pojoExporter.start()
//			hbmXmlExporter.start()
		}
		catch (e) {
			GrailsUtil.deepSanitize(e)
			e.printStackTrace()
			throw e
		}
	}

	protected void configureExporter(Exporter exporter) {
		exporter.properties.putAll(metadataDescriptor.properties)
		exporter.artifactCollector = new ArtifactCollector()
		exporter.metadataDescriptor = metadataDescriptor
		exporter.outputDirectory = destDir
	}

	protected void buildConfiguration() {
		Properties properties = new Properties()
		properties[Environment.DRIVER] = driverClass
		properties[Environment.PASS] = password
		properties[Environment.URL] = url
		properties[Environment.USER] = username
		if (dialect) {
			properties[Environment.DIALECT] = dialect
		}
		if (defaultSchema) {
			properties[Environment.DEFAULT_SCHEMA] = defaultSchema
		}
		if (defaultCatalog) {
			properties[Environment.DEFAULT_CATALOG] = defaultCatalog
		}

		reverseEngineeringStrategy.settings = new ReverseEngineeringSettings(reverseEngineeringStrategy)
				.setDefaultPackageName(packageName)
				.setDetectManyToMany(detectManyToMany)
				.setDetectOneToOne(detectOneToOne)
				.setDetectOptimisticLock(detectOptimisticLock)

		metadataDescriptor = new GrailsJdbcMetadataDescriptor(reverseEngineeringStrategy, properties, preferBasicCompositeIds)
	}
}
