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
import org.hibernate.tool.hbm2x.ArtifactCollector
import org.hibernate.tool.hbm2x.ExporterException
import org.hibernate.tool.hbm2x.TemplateHelper
import org.hibernate.tool.hbm2x.TemplateProducer

/**
 * Doesn't overwrite existing classes if configured not to.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class GrailsTemplateProducer extends TemplateProducer {

	private static final String newline = System.getProperty('line.separator')

	protected TemplateHelper templateHelper
	protected ArtifactCollector artifactCollector
	protected boolean overwrite

	GrailsTemplateProducer(TemplateHelper helper, ArtifactCollector collector, boolean overwrite) {
		super(helper, collector)
		this.overwrite = overwrite
		templateHelper = helper
		artifactCollector = collector
	}

	@Override
	void produce(Map additionalContext, String templateName, File destination,
			String identifier, String fileType, String rootContext) {

		if (!overwrite && destination.exists()) {
			log.warn 'Not overwriting {}', destination
			return
		}

		String content = produceToString(additionalContext, templateName, rootContext)
		if (!content.trim()) {
			log.warn 'Generated output is empty. Skipped creation for file {}', destination
			return
		}

		FileWriter fileWriter
		try {
			templateHelper.ensureExistence destination
			artifactCollector.addFile destination, fileType
			log.debug 'Creating {}', destination.absolutePath
			fileWriter = new FileWriter(destination)
			fileWriter.write content
		}
		catch (e) {
			throw new ExporterException('Error while writing result to file', e)
		}
		finally {
			try {
				fileWriter?.flush()
				fileWriter?.close()
			}
			catch (IOException e) {
				log.warn "Exception while flushing/closing $destination", e
			}
		}
	}

	protected String produceToString(Map<String, Object> additionalContext, String templateName, String rootContext) {
		additionalContext.each { String k, v -> templateHelper.putInContext k, v }

		StringWriter writer = new StringWriter()
		BufferedWriter bufferedWriter = new BufferedWriter(writer)
		templateHelper.processTemplate(templateName, bufferedWriter, rootContext)

		additionalContext.each { k, v -> templateHelper.removeFromContext k, v }

		try {
			bufferedWriter.flush()
		}
		catch (IOException e) {
			throw new RuntimeException('Error while flushing to string', e)
		}

		fixWhitespace writer.toString()
	}

	protected String fixWhitespace(String content) {
		List<String> lines = []
		content.eachLine { lines << (it as String) }
		lines << newline

		def fixed = new StringBuilder()
		int count = lines.size()
		for (int i = 0; i < count - 1; i++) {
			String line = lines[i]
			fixed << line << newline
			if (isBlankLine(line)) {
				while (isBlankLine(lines[i + 1]) && i < count - 1) {
					i++
				}
			}
		}

		content = fixed

		// 2nd pass to remove extra blank at end
		lines = []
		content.eachLine { lines << (it as String) }
		lines << newline
		if (!lines[-3]) {
			lines.remove lines.size() - 3
		}

		lines.join(newline).trim() + newline
	}

	protected boolean isBlankLine(String line) {
		!line || !line.trim()
	}
}
