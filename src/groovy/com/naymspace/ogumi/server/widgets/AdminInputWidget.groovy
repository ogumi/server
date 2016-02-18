package com.naymspace.ogumi.server.widgets

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
import net.kaleidos.plugins.admin.widget.Widget
import com.naymspace.ogumi.server.domain.Model
import com.naymspace.ogumi.server.domain.Experiment
import com.naymspace.ogumi.server.domain.AdminInputField
import com.naymspace.ogumi.server.domain.AdminInputFieldValue
import groovy.json.*

class AdminInputWidget extends Widget {

	def app = Holders.getGrailsApplication()

	String render() {
		def domainClass = internalAttrs["relatedDomainClass"]
		def domainObject = internalAttrs["domainObject"]
		def html = ''
		def inputid = 'input-'+internalAttrs["propertyName"]
		html += '<div id="' + inputid + '-hidden" class="hidden">'
		value.each {id ->
			def obj = domainClass.get(id)
			def aif = obj.adminInputField
			def name = aif.name.replace(' ', '_')
			def jsonVal = [
				name: name,
				value: Double.toString(obj.value),
				model: Long.toString(aif.model.id)
			]
			def valueStr = new JsonBuilder(jsonVal).toString().replace('"', "'")
			html += '<input type="hidden" name="'+ htmlAttrs.name + '" value="' + valueStr + '" data-name="' + name + '">';
		}
		html += '</div>'

		def json = []
		def models = Model.list()
		models.each {model ->
			def inputs = []
			model.adminInput.each {ai ->
				def name = ai.name.replace(' ', '_')
				inputs << [
					name: name,
					displayAs: ai.displayAs,
					type: ai.type,
					min: ai.min,
					max: ai.max
				]
			}
			json << [
				id: model.id,
				inputs: inputs
			]
		}
		def jsonStr = new JsonBuilder(json).toString().replace('"', "'")
		html += '<div id="models-data" class="panel panel-default" data-models="'+jsonStr+'" data-field="'+htmlAttrs.name+'">'
		html += '<div class="panel-body"></div>'
		html += '</div>'
	}

	List<Map> getAssets() {
		[
			[ dir: "js", file: "adminInputWidget.js", absolute: true ]
		]
	}

	private boolean isCollectionOrArray(object) {
		[Collection, Object[]].any { it.isAssignableFrom(object.getClass()) }
	}

	public void updateValue() {
		def slurper = new JsonSlurper()
		def values = []
		if(!isCollectionOrArray(value)){
			value = [value]
		}
		value.each{ obj ->
			def elem = slurper.parseText(obj.replace("'", '"'))
			def model = Model.get(elem.model)
			def aif = AdminInputField.findAllByNameAndModel(elem.name.replace('_', ' '), model)
			def aivl = AdminInputFieldValue.findAllByAdminInputField(aif)
			def aiv
			def found = false;
			if(aivl.size() > 0){
				aivl.each {val ->
					if(val.experiment.equals(internalAttrs["domainObject"])){
						aiv = val
						found = true
					}
				}
			}
			if(!found){
				aiv = new AdminInputFieldValue(adminInputField:aif)
				aiv.experiment = (Experiment) internalAttrs["domainObject"]
			}
			aiv.value = Double.parseDouble(elem.value)
			values << aiv
		}
		internalAttrs["domainObject"]."${internalAttrs['propertyName']}" = values
	}

}
