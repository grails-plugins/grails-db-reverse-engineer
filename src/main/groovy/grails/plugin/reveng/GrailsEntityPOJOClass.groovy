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

import groovy.util.logging.Slf4j
import org.hibernate.cfg.Configuration
import org.hibernate.mapping.Column
import org.hibernate.mapping.ForeignKey
import org.hibernate.mapping.ManyToOne
import org.hibernate.mapping.PersistentClass
import org.hibernate.mapping.Property
import org.hibernate.mapping.UniqueKey
import org.hibernate.tool.hbm2x.Cfg2HbmTool
import org.hibernate.tool.hbm2x.Cfg2JavaTool
import org.hibernate.tool.hbm2x.pojo.EntityPOJOClass
import org.hibernate.type.CalendarDateType
import org.hibernate.type.CalendarType
import org.hibernate.type.DateType
import org.hibernate.type.IntegerType
import org.hibernate.type.LongType
import org.hibernate.type.TimeType
import org.hibernate.type.TimestampType
import org.hibernate.type.Type

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@Slf4j
class GrailsEntityPOJOClass extends EntityPOJOClass {

	private static final String QUOTE = '"'
	protected static final Map<String, String> typeNameReplacements = [
		'boolean': 'Boolean',
		'byte':    'Byte',
		'char':    'Character',
		'double':  'Double',
		'int':     'Integer',
		'float':   'Float',
		'long':    'Long',
		'short':   'Short']

	protected PersistentClass clazz
	protected Cfg2HbmTool c2h
	protected Configuration configuration
	protected Map revengConfig
	protected String newline = System.getProperty('line.separator')
	protected List<Property> newProperties = []

	GrailsEntityPOJOClass(PersistentClass clazz, Cfg2JavaTool cfg, Cfg2HbmTool c2h,
			Configuration configuration, Map revengConfig) {
		super(clazz, cfg)
		this.clazz = clazz
		this.c2h = c2h
		this.configuration = configuration
		this.revengConfig = revengConfig
	}

	@Override
	String getPackageDeclaration() { super.packageDeclaration - ';' }

	@Override
	Iterator getAllPropertiesIterator() {
		def props = []
		def idProperty = identifierProperty
		def versionProperty = versionProperty

		super.allPropertiesIterator.each { Property property ->
			if (property == versionProperty) {
				return
			}

			if (property == idProperty) {
				if (c2j.isComponent(property)) {
					property.value.propertyIterator.each { props << it }
					return
				}
				if (property.name == 'id' || property.type instanceof LongType || property.type instanceof IntegerType) {
					return
				}
			}

			if (property.value instanceof ManyToOne || property.value instanceof org.hibernate.mapping.Set) {
				return
			}

			props << property
		}

		props.iterator()
	}

	String renderId() {
		def idProperty = getIdentifierProperty()
		if (c2j.isComponent(idProperty)) {
			def idDef = new StringBuilder('\t\tid composite: [')
			String delimiter = ''
			idProperty.value.propertyIterator.each {
				idDef << delimiter << QUOTE << findRealIdName(it) << QUOTE
				delimiter = ', '
			}
			idDef << ']' << newline
			return idDef
		}

		if (idProperty.name != 'id' || idProperty.value.identifierGeneratorStrategy == 'assigned') {
			def idDef = new StringBuilder('\t\tid ')
			String delimiter = ''

			if (idProperty.name != 'id') {
				idDef << delimiter
				if (idProperty.type instanceof LongType || idProperty.type instanceof IntegerType) {
					String colName = idProperty.columnIterator.next().name
					idDef << 'column: ' << QUOTE << colName << QUOTE
				}
				else {
					idDef << 'name: ' << QUOTE << idProperty.name << QUOTE
				}
				delimiter = ', '
			}

			if (idProperty.value.identifierGeneratorStrategy == 'assigned') {
				idDef << delimiter << 'generator: "assigned"'
				delimiter = ', '
			}

			idDef << newline
			return idDef
		}

		''
	}

	String renderVersion() {
		if (hasVersionProperty()) {
			if (versionProperty.name != 'version') {
				return '\t\tversion "' + versionProperty.value.columnIterator.next().name +
						  QUOTE + newline
			}
			return ''
		}

		'\t\tversion false' + newline
	}

	String renderTable() {
//		if (clazz.table.schema || clazz.table.catalog) {
//			def tableDef = new StringBuilder('\t\ttable ')
//			String delimiter = ''
//
//			if (clazz.table.schema) {
//				tableDef << delimiter << "schema: \"$clazz.table.quotedSchema\""
//				delimiter = ', '
//			}
//
//			if (clazz.table.catalog) {
//				tableDef << delimiter << "catalog: \"$clazz.table.catalog\""
//				delimiter = ', '
//			}
//
//			tableDef << newline
//			return tableDef
//		}

		''
	}

	@Override
	String generateImports() {
		def fixed = new StringBuilder()
		String imports = super.generateImports().trim()
		String delimiter = ''
		imports.eachLine { String line ->
			line -= ';'
			if (isValidImport(line - 'import ')) {
				fixed << delimiter << line
				delimiter = newline
			}
		}

		if (needsEqualsHashCode()) {
			fixed << delimiter << 'import org.apache.commons.lang.builder.EqualsBuilder'
			delimiter = newline
			fixed << delimiter << 'import org.apache.commons.lang.builder.HashCodeBuilder' << delimiter
		}

		imports = fixed
		if (imports) {
			return imports + newline + newline
		}

		''
	}

	protected boolean isValidImport(String candidate) {
		if ('java.math.BigDecimal' == candidate || 'java.math.BigInteger' == candidate) {
			return false
		}

		if (['java.io', 'java.net', 'java.util'].any { isInPackage(candidate, it) }) {
			return false
		}

		true
	}

	protected boolean isInPackage(String candidate, String pkg) {
		if (!candidate.contains(pkg)) {
			return false
		}

		int index = candidate.lastIndexOf('.')
		if (index == -1) {
			return false
		}

		String candidatePackage = candidate[0..index - 1]
		candidatePackage == pkg
	}

	String renderHashCodeAndEquals() {
		if (!needsEqualsHashCode()) {
			return ''
		}

		def hashCodeDef = new StringBuilder('\tint hashCode() {')
		hashCodeDef << newline << '\t\tdef builder = new HashCodeBuilder()' << newline

		def equalsDef = new StringBuilder('\tboolean equals(other) {')
		equalsDef << newline << '\t\tif (other == null) return false' << newline
		equalsDef << '\t\tdef builder = new EqualsBuilder()' << newline

		allPropertiesIterator.each { Property property ->
			if (!c2j.getMetaAsBool(property, 'use-in-equals')) {
				return
			}

			String name = findRealIdName(property)
			if (name != property.name) {
				name += '?.id'
			}
			hashCodeDef << '\t\tbuilder.append ' << name << newline

			equalsDef << '\t\tbuilder.append ' << name << ', other.' << name << newline
		}

		hashCodeDef << '\t\tbuilder.toHashCode()' << newline << '\t}' << newline

		equalsDef << '\t\tbuilder.isEquals()' << newline << '\t}'

		newline + hashCodeDef + newline + equalsDef
	}

	String renderConstraints() {

		def constraints = new StringBuilder()

		allPropertiesIterator.each { Property property ->
			if (!getMetaAttribAsBool(property, 'gen-property', true)) {
				return
			}

			def values = [:]

			if (!property.type.collectionType && property.nullable) {
				values.nullable = true
			}

			if (property.columnSpan == 1) {
				Column column = property.columnIterator.next()
				if (column.length && column.length != Column.DEFAULT_LENGTH && !isDateType(property.type)) {
					values.maxSize = column.length
				}

				if (column.scale != 0 && column.scale != Column.DEFAULT_SCALE) {
					values.scale = column.scale
				}

				if (property != identifierProperty && column.unique) {
					values.unique = true
				}

				clazz.table.uniqueKeyIterator.each { UniqueKey key ->
					if (key.columnSpan == 1 || key.name == clazz.table.primaryKey?.name) return
					if (key.columns[-1] == column) {
						def otherNames = key.columns[0..-2].collect { QUOTE + it.name + QUOTE }
						values.unique = '[' + otherNames.reverse().join(', ') + ']'
					}
				}
			}

			if (values) {
				constraints << '\t\t' << property.name << ' '
				String delimiter = ''
				values.each { k, v ->
					constraints << delimiter << k << ': ' << v
					delimiter = ', '
				}
				constraints << newline
			}
		}

		constraints.length() ? "\tstatic constraints = {$newline$constraints\t}" : ''
	}

	protected boolean isDateType(Type type) {
		(type instanceof DateType) || (type instanceof TimestampType) || (type instanceof TimeType) ||
		(type instanceof CalendarType) || (type instanceof CalendarDateType)
	}

	void findNewProperties() {

		def idProperty = identifierProperty
		def versionProperty = getVersionProperty()

		super.allPropertiesIterator.each { Property property ->
			if (property == versionProperty || property == idProperty) {
				return
			}

			if (property.value instanceof ManyToOne) {
				newProperties << property
			}
		}
	}

	String renderMany() {

		Set<String> belongs = new TreeSet()
		Set<String> hasMany = new TreeSet()
		findBelongsToAndHasMany belongs, hasMany

		if (!belongs && !hasMany) {
			return ''
		}

		def many = new StringBuilder()
		if (hasMany) {
			many << combine('static hasMany = [', ', ', ']', hasMany, true) << newline
		}
		if (belongs) {
			many << combine('static belongsTo = [', ', ', ']', belongs) << newline
		}
		many
	}

	String renderMappedBy() {
		Set<String> belongs = new TreeSet()
		Set<String> hasMany = new TreeSet()
		findBelongsToAndHasMany belongs, hasMany

		if (!hasMany) {
			return ''
		}

		Map<String, List<String>> grouped = [:]
		for (many in hasMany) {
			String[] parts = many.split(':')
			String className = parts[1].trim()
			def propNames = grouped[className]
			if (!propNames) {
				propNames = grouped[className] = propNames = []
			}
			propNames << parts[0].trim()
		}

		Set<String> mappedBy = new TreeSet()
		Set<String> classNames = []
		grouped.each { className, propNames ->
			if (propNames.size() > 1) {
				for (propName in propNames) {
					mappedBy << propName + ': "TODO"'
				}
				classNames << className
			}
		}

		if (!classNames) {
			return ''
		}

		"\t// TODO you have multiple hasMany references for class(es) $classNames " + newline +
		"\t//      so you'll need to disambiguate them with the 'mappedBy' property:" + newline +
		combine('static mappedBy = [', ', ', ']', mappedBy, true) + newline
	}

	protected void findBelongsToAndHasMany(Set<String> belongs, Set<String> hasMany) {

		boolean bidirectionalManyToOne = revengConfig.bidirectionalManyToOne instanceof Boolean ?
				revengConfig.bidirectionalManyToOne : true
		boolean mapManyToManyJoinTable = revengConfig.mapManyToManyJoinTable instanceof Boolean ?
				revengConfig.mapManyToManyJoinTable : false

		def idProperty = identifierProperty
		def versionProperty = getVersionProperty()
		def strategy = GrailsReverseEngineeringStrategy.INSTANCE

		super.allPropertiesIterator.each { Property property ->
			if (property == versionProperty || property == idProperty) {
				return
			}

			if (bidirectionalManyToOne && property.value instanceof ManyToOne) {
				if (!isPartOfPrimaryKey(property)) {
					belongs << classShortName(property.value.referencedEntityName)
				}
			}

			if (property.value instanceof org.hibernate.mapping.Set) {
				boolean isManyToMany = strategy.isManyToManyTable(property.value.collectionTable)
				boolean isReallyManyToMany = strategy.isReallyManyToManyTable(property.value.collectionTable)
				if ((bidirectionalManyToOne && !isManyToMany && !isReallyManyToMany) || isManyToMany) {
					String classShortName = classShortName(property.value.element.type.name)
					hasMany << property.name + ': ' + classShortName
					if (isManyToMany) {
						if (strategy.isManyToManyBelongsTo(property.value.collectionTable, property.persistentClass.table)) {
							belongs << findManyToManyOtherSide(property)
						}
					}
				}
			}
		}

		belongs.remove classShortName(mappedClassName)
	}

	protected boolean isPartOfPrimaryKey(Property prop) {
		if (c2j.isComponent(identifierProperty) &&
					GrailsReverseEngineeringStrategy.INSTANCE.isReallyManyToManyTable(clazz.table)) {
			String propClassShortName = classShortName(prop.value.referencedEntityName)
			for (newProp in newProperties) {
				if (classShortName(newProp.value.referencedEntityName) == propClassShortName) {
					return true
				}
			}
		}

		false
	}

	protected String findRealIdName(Property prop) {
		if (c2j.isComponent(identifierProperty) &&
					GrailsReverseEngineeringStrategy.INSTANCE.isReallyManyToManyTable(clazz.table)) {

			for (newProp in newProperties) {
				if (newProp.name + 'Id' == prop.name) {
					return newProp.name
				}
			}
		}

		prop.name
	}

	String renderProperties() {
		def props = new StringBuilder()
		allPropertiesIterator.each { Property property ->
			if (!getMetaAttribAsBool(property, 'gen-property', true) || findRealIdName(property) != property.name) {
				return
			}

			props << '\t' << getJavaTypeName(property, true) << ' ' << property.name << newline
		}

		for (prop in newProperties) {
			props << '\t' << classShortName(prop.value.referencedEntityName) << ' ' << prop.name << newline
		}

		props
	}

	String renderMapping() {
		def mapping = new StringBuilder()
		mapping << renderId() << renderVersion() << renderTable()
		mapping.length() ? "\tstatic mapping = {$newline$mapping\t}" : ''
	}

	String renderClassStart() {
		"class $declarationName${renderImplements()}{"
	}

	String renderImplements() {
		identifierProperty.columnSpan > 1 ? ' implements Serializable ' : ' '
	}

	String getJavaTypeName(Property p, boolean useGenerics) {
		String name = super.getJavaTypeName(p, useGenerics)
		typeNameReplacements[name] ?: name
	}

	protected String findManyToManyOtherSide(Property prop) {
		String belongsTo
		prop.value.collectionTable.foreignKeyIterator.each { ForeignKey fk ->
			if (prop.value.table != fk.referencedTable) {
				belongsTo = classShortName(fk.referencedEntityName)
			}
		}
		belongsTo
	}

	protected String classShortName(String className) {
		if (className.indexOf('.') > -1) {
			return className.substring(className.lastIndexOf('.') + 1)
		}
		className
	}

	protected String combine(String start, String delim, String end, things, boolean lineUp = false) {
		def buffer = new StringBuilder('\t')

		String pad
		if (lineUp) {
			def bufferPad = new StringBuilder()
			bufferPad << newline << '\t'
			start.length().times { bufferPad << ' ' }
			pad = bufferPad
		}

		buffer << start
		String delimiter = ''
		things.each {
			buffer << delimiter << it
			delimiter = lineUp ? delim.trim() + pad : delim
		}
		buffer << end
		buffer
	}
}
