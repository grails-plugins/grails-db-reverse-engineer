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

import grails.core.GrailsApplication
import grails.dev.commands.ApplicationCommand
import grails.dev.commands.ExecutionContext
import org.grails.plugins.reveng.ReverseEngineeringEngine
import org.grails.plugins.reveng.ReverseEngineeringFactory

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class DbReverseEngineerCommand implements ApplicationCommand {

	final String description = 'Reverse-engineers a database and creates domain classes'

	boolean handle(ExecutionContext executionContext) {
		GrailsApplication grailsApplication = applicationContext.getBean(GrailsApplication)
		ReverseEngineeringEngine engine = ReverseEngineeringFactory.create(executionContext.baseDir, grailsApplication)

		println "Starting database reverse engineering, connecting to '${engine.properties.url}' as '${engine.properties.username}' ..."

		engine.execute()

		println 'Finished database reverse engineering'

		true
	}
}
