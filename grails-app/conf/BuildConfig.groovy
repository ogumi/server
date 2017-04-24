/*
 * Copyright (c) 2015 naymspace software (Dennis Nissen)
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

grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.fork = [
		// configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
		//  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

		// configure settings for the test-app JVM, uses the daemon by default
		test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
		// configure settings for the run-app JVM
		run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
		// configure settings for the run-war JVM
		war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
		// configure settings for the Console UI JVM
		console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256],

        //enable debugger during development
        test: false,
        run: false
]

grails.plugin.location.'admin-interface' = "plugins/admin-interface"

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
		// inherit Grails' default dependencies
		inherits("global") {
				// specify dependency exclusions here; for example, uncomment this to disable ehcache:
				// excludes 'ehcache'
		}
		log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
		checksums true // Whether to verify checksums on resolve
		legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

		repositories {
				inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        mavenLocal()
        grailsCentral()
        mavenCentral()
        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
		}

		dependencies {
				// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.
				// runtime 'org.postgresql:postgresql:9.3-1101-jdbc41'
				//runtime 'mysql:mysql-connector-java:5.1.29'
				runtime 'org.mariadb.jdbc:mariadb-java-client:1.1.9'
				test "org.grails:grails-datastore-test-support:1.0.2-grails-2.4"
				runtime('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1') {
					excludes 'xalan'
					excludes 'xml-apis'
					excludes 'groovy'
				}
				compile 'com.opencsv:opencsv:3.3',
                        'org.apache.commons:commons-math3:3.4.1',
                        'com.naymspace.ogumi:ogumi-model:1.2-SNAPSHOT'
		}

		plugins {
				// plugins for the build system only
				build ":tomcat:8.0.22"
				build ":release:3.0.1", { export = false }

				// plugins for the compile step
				compile ":scaffolding:2.1.2"
				compile ':cache:1.1.8'
				compile ":asset-pipeline:2.1.5"

				compile ':spring-security-core:2.0-RC5'
                compile ":spring-security-rest:1.5.1"
                compile ":spring-websocket:1.3.0"
				// plugins needed at runtime but not for compilation
				runtime ":cors:1.1.8"
				runtime ":hibernate4:4.3.6.1" // or ":hibernate:3.6.10.18"
				runtime ":database-migration:1.4.0"
				runtime ":jquery:1.11.1"
				//runtime ":admin-interface:0.6.5-OGUMI-SNAPSHOT"

				// Uncomment these to enable additional asset-pipeline capabilities
				compile ":sass-asset-pipeline:2.3.0"
				//compile ":less-asset-pipeline:1.10.0"
				compile ":coffee-asset-pipeline:2.0.7"
				//compile ":handlebars-asset-pipeline:1.3.0.3"
		}
}
