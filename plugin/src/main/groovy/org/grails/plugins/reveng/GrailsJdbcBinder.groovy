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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.hibernate.boot.spi.InFlightMetadataCollector
import org.hibernate.boot.spi.MetadataBuildingContext
import org.hibernate.cfg.AvailableSettings
import org.hibernate.cfg.JDBCBinder
import org.hibernate.cfg.JDBCReaderFactory
import org.hibernate.cfg.reveng.DatabaseCollector
import org.hibernate.cfg.reveng.JDBCReader
import org.hibernate.cfg.reveng.MappingsDatabaseCollector
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy
import org.hibernate.service.ServiceRegistry

/**
 * Registers a ProgressListener to log status messages.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class GrailsJdbcBinder extends JDBCBinder {

	protected ServiceRegistry serviceRegistry
	protected Properties properties
	protected InFlightMetadataCollector metadataCollector
	protected ReverseEngineeringStrategy revengStrategy

	GrailsJdbcBinder(ServiceRegistry serviceRegistry, Properties properties, MetadataBuildingContext mdbc,
					 ReverseEngineeringStrategy revengStrategy, boolean preferBasicCompositeIds) {
		super(serviceRegistry, properties, mdbc, revengStrategy, preferBasicCompositeIds)
		this.serviceRegistry = serviceRegistry
		this.properties = properties
		this.metadataCollector = mdbc.metadataCollector
		this.revengStrategy = revengStrategy
	}

	@Override
	DatabaseCollector readDatabaseSchema(String catalog, String schema) {
		catalog = catalog ?: properties.getProperty(AvailableSettings.DEFAULT_CATALOG)
		schema = schema ?: properties.getProperty(AvailableSettings.DEFAULT_SCHEMA)

		JDBCReader reader = JDBCReaderFactory.newJDBCReader(properties, revengStrategy, serviceRegistry)
		DatabaseCollector dbs = new MappingsDatabaseCollector(metadataCollector, reader.metaDataDialect)
		reader.readDatabaseSchema dbs, catalog, schema, new ReverseEngineerProgressListener()
		dbs
	}
}
