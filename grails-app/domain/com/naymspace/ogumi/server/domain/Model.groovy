package com.naymspace.ogumi.server.domain

import com.naymspace.ogumi.server.services.ModelService

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

class Model {

	String name
	String claz
	Uploadable jar
	Collection translations
	Collection adminInput
	Collection outputfields

	static hasMany = [adminInput:AdminInputField, outputfields: OutputField, translations: Uploadable]

    static mapping = {
        adminInput cascade: 'all-delete-orphan'
        outputfields cascade: 'all-delete-orphan'
    }

	static constraints = {
		name            blank:false, unique: true
		claz            nullable: true
		translations    nullable: true
	}


    def beforeInsert(){
        updateModelInfo()
        true
    }
    def beforeUpdate(){
        updateModelInfo()
        true
    }

    protected def updateModelInfo(){
        adminInput?.clear()
        outputfields?.clear()
        def ogumiModel = ModelService.loadModel(jar.path)
        def modelInfo = ModelService.getAdminFieldsFrom(ogumiModel)
        claz = modelInfo.claz
        modelInfo.fields.each{
            addToAdminInput(AdminInputField.fromField(it))
        }
        ModelService.getOutputFieldsFrom(ogumiModel).each{
            addToOutputfields(OutputField.fromField(it))
        }
    }

	String toString() {
		return name
	}

}
