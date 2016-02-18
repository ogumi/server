package com.naymspace.ogumi.server.controller

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

import com.naymspace.ogumi.server.domain.*
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator

class LiveviewController {

    def TokenGenerator tokenGenerator
    def springSecurityService

	def session() {
		if(params?.id != null) {
            def currentUser = springSecurityService.currentUser

			def session = Session.get(params.id)
			if(session != null) {
				render(template: "liveview", model: [
                        session: session,
                        user: currentUser,
                        authentication_token: tokenGenerator.generateAccessToken(springSecurityService.principal).accessToken
                ])
			} else {
				render(status:404, contentType: 'text/plain'){
					"Cannot find a session with id ${params.id}"
				}
			}
		} else {
			render(status:400, contentType: 'text/plain'){
				"You must specify a session."
			}
		}

	}

	def index() {


	}

	def start() {
		if(params?.id != null) {
			def experiment = Experiment.get(params.expid)
			if(experiment != null) {
				Date now = new Date()
				experiment.experimentStart = now
				experiment.save(flush: true)
			}
			redirect(action: "session", params: [id: params.id])
		}
	}

}
