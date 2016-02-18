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

class Variable {

	String field
	String type = "Object"
	String value
	double step = 0

	static belongsTo = [userInput: UserInput]

	static constraints = {
		field     blank: false
		value     nullable: true
		type      nullable: true
		userInput nullable: true
		step      nullable: true, validator: { val ->
			return val >= 0.0
		}
	}

	String toString() {
		def str = "$field ($type)"
		if(value){
			str += ": $value (t=$step)"
		}
		return str
	}

}
