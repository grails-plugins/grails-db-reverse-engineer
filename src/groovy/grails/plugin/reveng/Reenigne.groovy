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
package grails.plugin.reveng

import grails.util.GrailsUtil

import org.hibernate.cfg.Environment
import org.hibernate.cfg.reveng.ReverseEngineeringSettings
import org.hibernate.tool.hbm2x.Exporter
import org.hibernate.tool.hbm2x.HibernateMappingExporter

/**
 * Main class, called from the reverse engineer script.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class Reenigne {

	File destDir
	String packageName
	String driverClass
	String password
	String username
	String url
	String dialect
	String defaultSchema
	String defaultCatalog

	boolean applyHibernateValidatorToDdl = false
	boolean preferBasicCompositeIds = true
	boolean detectOneToOne = true
	boolean detectManyToMany = true
	boolean detectOptimisticLock = true
	boolean ejb3 = false
	boolean jdk5 = true
	boolean overwrite = true

	ConfigObject revengConfig

	GrailsReverseEngineeringStrategy reverseEngineeringStrategy = GrailsReverseEngineeringStrategy.INSTANCE

	private GrailsPojoExporter pojoExporter
	private HibernateMappingExporter hbmXmlExporter = new HibernateMappingExporter()
	private GrailsJdbcMetaDataConfiguration configuration = new GrailsJdbcMetaDataConfiguration()
	private Properties properties = new Properties()

	void execute() {
		try {
			buildConfiguration()

			pojoExporter = new GrailsPojoExporter(overwrite, revengConfig)
			configureExporter pojoExporter
			pojoExporter.getProperties().setProperty('ejb3', ejb3.toString())
			pojoExporter.getProperties().setProperty('jdk5', jdk5.toString())

			configureExporter hbmXmlExporter

			pojoExporter.start()
//			hbmXmlExporter.start()
		}
		catch (e) {
			GrailsUtil.sanitize e
			e.printStackTrace()
			throw e
		}
	}

	private void configureExporter(Exporter exporter) {
		exporter.setProperties properties
		exporter.setConfiguration configuration
		exporter.setOutputDirectory destDir
	}

	private void buildConfiguration() {
		properties.putAll(configuration.getProperties())

		properties.put Environment.DRIVER, driverClass
		properties.put Environment.PASS, password
		properties.put Environment.URL, url
		properties.put Environment.USER, username
		if (dialect) {
			properties.put Environment.DIALECT, dialect
		}
		if (defaultSchema) {
			properties.put Environment.DEFAULT_SCHEMA, defaultSchema
		}
		if (defaultCatalog) {
			properties.put Environment.DEFAULT_CATALOG, defaultCatalog
		}
		
		properties.put("hibernate.validator.apply_to_ddl", applyHibernateValidatorToDdl.toString())

		configuration.setProperties(properties)

		configuration.setPreferBasicCompositeIds preferBasicCompositeIds

		ReverseEngineeringSettings settings = new ReverseEngineeringSettings(reverseEngineeringStrategy)
				.setDefaultPackageName(packageName)
				.setDetectManyToMany(detectManyToMany)
				.setDetectOneToOne(detectOneToOne)
				.setDetectOptimisticLock(detectOptimisticLock)
		reverseEngineeringStrategy.setSettings settings

		configuration.reverseEngineeringStrategy = reverseEngineeringStrategy
		configuration.readFromJDBC defaultCatalog, defaultSchema
		configuration.buildMappings()
	}
}
