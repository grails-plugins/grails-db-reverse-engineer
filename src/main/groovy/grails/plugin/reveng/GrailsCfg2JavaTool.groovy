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
import org.hibernate.cfg.Configuration
import org.hibernate.mapping.PersistentClass
import org.hibernate.tool.hbm2x.Cfg2HbmTool
import org.hibernate.tool.hbm2x.Cfg2JavaTool
import org.hibernate.tool.hbm2x.pojo.POJOClass

/**
 * Subclass that creates GrailsEntityPOJOClass instances.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class GrailsCfg2JavaTool extends Cfg2JavaTool {

	protected Cfg2HbmTool c2h
	protected Configuration configuration
	protected Map revengConfig

	GrailsCfg2JavaTool(Cfg2HbmTool c2h, Configuration configuration, Map revengConfig) {
		this.c2h = c2h
		this.configuration = configuration
		this.revengConfig = revengConfig
	}

	@Override
	POJOClass getPOJOClass(PersistentClass comp) {
		new GrailsEntityPOJOClass(comp, this, c2h, configuration, revengConfig)
	}
}
