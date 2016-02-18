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
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class Waiting extends SequenceEntry {

	String name
	Session session
	Date waitUntil
	Date lastUpdated = new Date()

	static belongsTo = [session:Session]

	static constraints = {
		session nullable: true
	}

	String toString() {
		return "$label until $waitUntil"
	}

	def beforeUpdate() {
		lastUpdated = new Date()
	}


	def beforeDelete() {
		Waiting.withNewSession {
			Waiting.withTransaction { status ->
				def sessions = Session.executeQuery('select s from Session s where :wts in elements(s.sequence)',[wts: this])
				sessions.each {session ->
					def list = []
					for(SequenceEntry se: session.sequence){
						if(se instanceof Waiting){
							if(se.id != this.id) {
								list << se
							}
						} else {
							list << se
						}
					}
					session.sequence = list
					session.save()
				}
				status.flush()
			}
		}
	}

	def app = Holders.getGrailsApplication()

}
