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
import com.opencsv.CSVWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class RegisterController {

	def index() {
		if(params?.id != null) {
			def user = User.findByActivationLink(params.id)
			if(user != null){
				user.enabled = true
				user.save(flush: true)
				render(template: "register", model: [user: user])
				return
			}
		}
		render(status:400, contentType: 'text/plain'){
			"Registration did not work."
		}
	}

}
