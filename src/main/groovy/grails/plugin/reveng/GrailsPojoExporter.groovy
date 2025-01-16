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

import freemarker.cache.TemplateLoader
import org.hibernate.tool.hbm2x.Cfg2HbmTool
import org.hibernate.tool.hbm2x.POJOExporter
import org.hibernate.tool.hbm2x.pojo.POJOClass

/**
 * Customizes the artifact name and source template, and customizes the tools.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class GrailsPojoExporter extends POJOExporter {

	final Cfg2HbmTool cfg2HbmTool = new Cfg2HbmTool()
	final GrailsCfg2JavaTool cfg2JavaTool

	protected boolean overwrite

	protected final String template = '''
${pojo.getPackageDeclaration()}
${pojo.findNewProperties()}
<#assign classbody>
${pojo.renderClassStart()}

${pojo.renderProperties()}

${pojo.renderHashCodeAndEquals()}

${pojo.renderMany()}

${pojo.renderMappedBy()}

${pojo.renderMapping()}

${pojo.renderConstraints()}
}
</#assign>

${pojo.generateImports()}${classbody}'''

	protected TemplateLoader templateLoader = new TemplateLoader() {
		def findTemplateSource(String name) { template }
		long getLastModified(Object templateSource) { System.currentTimeMillis() - 100000 }
		Reader getReader(templateSource, String encoding) { new StringReader(template) }
		void closeTemplateSource(Object templateSource) {}
	}

	GrailsPojoExporter(boolean overwrite, Map revengConfig) {
		this.overwrite = overwrite
		cfg2JavaTool = new GrailsCfg2JavaTool(cfg2HbmTool, configuration, revengConfig)
	}

	@Override
	protected void init() {
		templateName = getClass().getPackage().name.replace('.', '/') + '/DomainClass.ftl'
		filePattern = '{package-name}/{class-name}.groovy'
	}

	@Override
	protected void exportPOJO(Map additionalContext, POJOClass element) {
		templateHelper.freeMarkerEngine.templateLoader = templateLoader

		GrailsTemplateProducer producer = new GrailsTemplateProducer(templateHelper, artifactCollector, overwrite)
		additionalContext.pojo = element
		additionalContext.clazz = element.decoratedObject
		String filename = resolveFilename(element)
		producer.produce additionalContext, templateName, new File(outputDirectory, filename),
			templateName, element.toString()
	}
}
