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

import org.hibernate.cfg.JDBCBinder
import org.hibernate.cfg.JDBCMetaDataConfiguration
import org.hibernate.cfg.JDBCReaderFactory
import org.hibernate.cfg.Mappings
import org.hibernate.cfg.Settings
import org.hibernate.cfg.reveng.DatabaseCollector
import org.hibernate.cfg.reveng.JDBCReader
import org.hibernate.cfg.reveng.MappingsDatabaseCollector
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy
import org.hibernate.cfg.reveng.dialect.MetaDataDialect

/**
 * Registers a ProgressListener to log status messages.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class GrailsJdbcBinder extends JDBCBinder {

	private Settings settings
	private JDBCMetaDataConfiguration cfg
	private Mappings mappings
	private ReverseEngineeringStrategy revengStrategy

	GrailsJdbcBinder(JDBCMetaDataConfiguration cfg, Settings settings, Mappings mappings,
			ReverseEngineeringStrategy revengStrategy) {
		super(cfg, settings, mappings, revengStrategy)
		this.settings = settings
		this.cfg = cfg
		this.mappings = mappings
		this.revengStrategy = revengStrategy
	}

	@Override
	DatabaseCollector readDatabaseSchema(String catalog, String schema) {
		catalog = catalog ?: settings.getDefaultCatalogName()
		schema = schema ?: settings.getDefaultSchemaName()

		MetaDataDialect mdd = JDBCReaderFactory.newMetaDataDialect(
				settings.dialect, cfg.getProperties())
		JDBCReader reader = new JDBCReader(mdd, settings.connectionProvider, settings.getSQLExceptionConverter(),
				settings.defaultCatalogName, settings.defaultSchemaName, revengStrategy)

		DatabaseCollector dbs = new MappingsDatabaseCollector(mappings, mdd)
		reader.readDatabaseSchema dbs, catalog, schema, new ReverseEngineerProgressListener()
		dbs
	}
}
