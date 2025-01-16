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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy
import org.hibernate.cfg.reveng.TableIdentifier
import org.hibernate.mapping.Table
import org.springframework.util.AntPathMatcher

import java.util.regex.Pattern

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class GrailsReverseEngineeringStrategy extends DefaultReverseEngineeringStrategy {

	public static final GrailsReverseEngineeringStrategy INSTANCE = new GrailsReverseEngineeringStrategy()

	protected Set<String> excludeTables = []
	protected Set<Pattern> excludeTableRegexes = []
	protected Set<String> excludeTableAntPatterns = []
	protected Set<String> includeTables = []
	protected Set<Pattern> includeTableRegexes = []
	protected Set<String> includeTableAntPatterns = []
	protected Map<String, List<String>> excludeColumns = [:].withDefault {[]}
	protected Map<String, List<Pattern>> excludeColumnRegexes = [:].withDefault {[]}
	protected Map<String, List<String>> excludeColumnAntPatterns = [:].withDefault {[]}
	protected Map<String, String> versionColumnNames = [:]
	protected Set<String> manyToManyTables = []
	protected Set<String> mappedManyToManyTables = []
	protected boolean alwaysMapManyToManyTables
	protected Map<String, String> belongsTos = [:]
	protected AntPathMatcher antMatcher = new AntPathMatcher()

	@Override
	boolean excludeTable(TableIdentifier ti) {

		String name = ti.name

		if (includeTables || includeTableRegexes || includeTableAntPatterns) {
			return isNotIncluded(name)
		}

		isExcluded name
	}

	protected boolean isNotIncluded(String name) {
		if (!includeTables.contains(name)) {
			log.debug 'table {} not included by name', name
			return true
		}

		for (Pattern pattern : includeTableRegexes) {
			if (!pattern.matcher(name).matches()) {
				log.debug 'table {} not included by regex {}', name, pattern
				return true
			}
		}

		for (String pattern : includeTableAntPatterns) {
			if (!antMatcher.match(pattern, name)) {
				log.debug 'table {} not included by pattern {}', name, pattern
				return true
			}
		}

		false
	}

	protected boolean isExcluded(String name) {
		if (excludeTables.contains(name)) {
			log.debug 'table {} excluded by name', name
			return true
		}

		for (Pattern pattern : excludeTableRegexes) {
			if (pattern.matcher(name).matches()) {
				log.debug 'table {} excluded by regex {}', name, pattern
				return true
			}
		}

		for (String pattern : excludeTableAntPatterns) {
			if (antMatcher.match(pattern, name)) {
				log.debug 'table {} excluded by pattern {}', name, pattern
				return true
			}
		}

		false
	}

	@Override
	boolean excludeColumn(TableIdentifier identifier, String columnName) {

		List<String> excludeNames = excludeColumns[identifier.name]
		if (excludeNames?.contains(columnName)) {
			log.debug 'column {} in table {} excluded by name', columnName, identifier.name
			return true
		}

		for (Pattern pattern in excludeColumnRegexes[identifier.name]) {
			if (pattern.matcher(columnName).matches()) {
				log.debug 'column {} in table {} excluded by regex {}', columnName, identifier.name, pattern
				return true
			}
		}

		for (String pattern in excludeColumnAntPatterns[identifier.name]) {
			if (antMatcher.match(pattern, columnName)) {
				log.debug 'column {} in table {} excluded by pattern {}', columnName, identifier.name, pattern
				return true
			}
		}

		false
	}

	@Override
	String getOptimisticLockColumnName(TableIdentifier identifier) {
		String name = versionColumnNames[identifier.name]
		if (name) {
			log.debug 'using "{}" for version in table {}', name, identifier.name
		}
		name
	}

	@Override
	boolean isManyToManyTable(Table table) {
		if (alwaysMapManyToManyTables || mappedManyToManyTables.contains(table.name)) {
			return false
		}

		isReallyManyToManyTable table
	}

	boolean isReallyManyToManyTable(Table table) {
		if (manyToManyTables.contains(table.name)) {
			log.debug 'using {} as many-to-many table', table.name
			return true
		}
		super.isManyToManyTable table
	}

	/**
	 * Register a table name to exclude.
	 * @param name the name
	 */
	void addExcludeTable(String name) { excludeTables << name }

	/**
	 * Register a regex pattern for table names to ignore.
	 * @param pattern the pattern
	 */
	void addExcludeTableRegex(String pattern) { excludeTableRegexes << Pattern.compile(pattern) }

	/**
	 * Register an Ant-style pattern for table names to ignore.
	 * @param pattern the pattern
	 */
	void addExcludeTableAntPattern(String pattern) { excludeTableAntPatterns << pattern }

	/**
	 * Register a table name to include.
	 * @param name the name
	 */
	void addIncludeTable(String name) { includeTables << name }

	/**
	 * Register a regex pattern for table names to include.
	 * @param pattern the pattern
	 */
	void addIncludeTableRegex(String pattern) { includeTableRegexes << Pattern.compile(pattern) }

	/**
	 * Register an Ant-style pattern for table names to include.
	 * @param pattern the pattern
	 */
	void addIncludeTableAntPattern(String pattern) { includeTableAntPatterns << pattern }

	/**
	 * Register one or more column names to exclude.
	 * @param table the table name
	 * @param names the column names
	 */
	void addExcludeColumns(String table, List<String> names) {
		getOrCreateList(excludeColumns, table).addAll names
	}

	/**
	 * Register one or more regex patterns for column names to ignore.
	 * @param table the table name
	 * @param patterns the column name patterns
	 */
	void addExcludeColumnRegexes(String table, List<String> patterns) {
		def list = getOrCreateList(excludeColumnRegexes, table)
		for (String pattern in patterns) {
			list << Pattern.compile(pattern)
		}
	}

	/**
	 * Register one or more Ant-style patterns for column names to ignore.
	 * @param table the table name
	 * @param patterns the column name patterns
	 */
	void addExcludeColumnAntPatterns(String table, List<String> patterns) {
		getOrCreateList(excludeColumnAntPatterns, table).addAll patterns
	}

	protected List getOrCreateList(Map map, String key) {
		List list = map[key] as List
		if (list == null) {
			map[key] = list = []
		}
		list
	}

	/**
	 * Set the name of the optimistic lock version column if it's different from 'version'.
	 * @param table the table name
	 * @param column the column name
	 */
	void addVersionColumn(String table, String column) {
		versionColumnNames[table] = column
	}

	/**
	 * Add a table name that should be considered a many-to-many join table;
	 * useful if the table has more properties than the two foreign keys since
	 * otherwise Hibernate will ignore it.
	 *
	 * @param name the table name
	 */
	void addManyToManyTable(String name) {
		manyToManyTables << name
	}

	/**
	 * Add a table name that should be mapped as a domain class rather than creating
	 * standard hasMany/belongsTo.
	 *
	 * @param name the table name
	 */
	void addMappedManyToManyTable(String name) {
		mappedManyToManyTables << name
	}

	/**
	 * Set whether to always map the many-to-many join tables (instead of individually
	 * specifying them with addMappedManyToManyTable()).
	 * @param map if true always map join tables
	 */
	void setAlwaysMapManyToManyTables(boolean map) {
		alwaysMapManyToManyTables = map
	}

	/**
	 * Register the 'belongsTo' end of a many-to-many; needs to be set since it otherwise
	 * can't be inferred.
	 * @param joinTable the database join table name
	 * @param name the table name of the 'owned' domain class
	 */
	void setManyToManyBelongsTo(String joinTable, String name) {
		belongsTos[joinTable] = name
	}

	boolean isManyToManyBelongsTo(Table joinTable, Table table) {
		table.name.equals(belongsTos[joinTable.name])
	}
}
