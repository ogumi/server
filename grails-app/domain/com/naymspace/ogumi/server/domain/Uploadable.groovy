package com.naymspace.ogumi.server.domain

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

import grails.util.Holders

class Uploadable {

	String path
	String name
	String type
	Date lastUpdated = new Date()
    boolean downloadable = false

	static belongsTo = [info: InformationStep]

	static constraints = {
		path blank: false
		info nullable: true
		type nullable: true
	}

    def getUrl(){
        if (!downloadable) "/not_a_downloadable_file"
        def linkGenerator = Holders.grailsApplication.mainContext.getBean("grailsLinkGenerator")
        linkGenerator.link(action: "get", controller: "uploads", params:[id: id], absolute: true)
    }

	def beforeUpdate() {
		lastUpdated = new Date()
	}

	def beforeDelete = {
		Model.withNewSession {
			Model.findAllByJar(this).each {
				it.jar = null
				it.save(flush:true)
			}
		}
		new File(path).delete()
	}

	String toString() {
		"<a href=\"${url}\">${name}</a>"
	}
}
