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
import com.naymspace.ogumi.server.domain.OutputField
import com.naymspace.ogumi.server.domain.OutputFieldValue
import groovy.json.*

class OutputWidget extends Widget {

	def app = Holders.getGrailsApplication()

	String render() {
		def domainClass = internalAttrs["relatedDomainClass"]
		def domainObject = internalAttrs["domainObject"]
		def html = ''
		def inputid = 'input-'+internalAttrs["propertyName"]
		html += '<div id="' + inputid + '-hidden" class="hidden">'
		value.each {id ->
			def obj = domainClass.get(id)
			def of = obj.outputField
			def name = of.name.replace(' ', '_')
			def jsonVal = [
				name: name,
				shouldshow: '' + obj.shouldshow,
				model: Long.toString(of.model.id)
			]
			def valueStr = new JsonBuilder(jsonVal).toString().replace('"', "'")
			html += '<input type="hidden" name="'+ htmlAttrs.name + '" value="' + valueStr + '" data-name="' + name + '">';
		}
		html += '</div>'

		def json = []
		def models = Model.list()
		models.each {model ->
			def inputs = []
			model.outputfields.each {o ->
				def name = o.name.replace(' ', '_')
				inputs << [
					name: name
				]
			}
			json << [
				id: model.id,
				inputs: inputs
			]
		}
		def jsonStr = new JsonBuilder(json).toString().replace('"', "'")
		html += '<div id="models-output-data" class="panel panel-default" data-models="'+jsonStr+'" data-field="'+htmlAttrs.name+'">'
		html += '<div class="panel-body"></div>'
		html += '</div>'
	}

	List<Map> getAssets() {
		[
			[ dir: "js", file: "outputWidget.js", absolute: true ]
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
		value.findAll{it != null}.each{ obj ->
			def elem = slurper.parseText(obj.replace("'", '"'))
			def model = Model.get(elem.model)
			def of = OutputField.findAllByNameAndModel(elem.name.replace('_', ' '), model)
			def ovl = OutputFieldValue.findAllByOutputField(of)
			def ov
			def found = false;
			if(ovl.size() > 0){
				ovl.each {val ->
					if(val.experiment.equals(internalAttrs["domainObject"])){
						ov = val
						found = true
					}
				}
			}
			if(!found){
				ov = new OutputFieldValue(outputField:of)
				ov.experiment = (Experiment) internalAttrs["domainObject"]
			}
			ov.shouldshow = elem.shouldshow.toBoolean()
			values << ov
		}
		internalAttrs["domainObject"]."${internalAttrs['propertyName']}" = values
	}

}
