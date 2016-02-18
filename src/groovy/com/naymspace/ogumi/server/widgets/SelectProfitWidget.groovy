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
import net.kaleidos.plugins.admin.widget.relation.RelationSelectWidget
import com.naymspace.ogumi.server.domain.Model
import com.naymspace.ogumi.server.domain.Experiment
import com.naymspace.ogumi.server.domain.OutputField
import groovy.json.*

class SelectProfitWidget extends RelationSelectWidget {

	def app = Holders.getGrailsApplication()

	String render() {
		def domainClass = internalAttrs["relatedDomainClass"]
		def domainObject = internalAttrs["domainObject"]
		def html = ''
		def inputid = 'input-'+internalAttrs["propertyName"]
		html += '<div id="' + inputid + '-hidden" class="hidden">'

		if(value != null){
			html += '<input type="hidden" name="'+ htmlAttrs.name + '" value="' + value + '">';
		}
		html += '</div>'

		def json = []
		def models = Model.list()
		models.each {model ->
			def inputs = []
			model.outputfields.each {o ->
				def name = o.name.replace(' ', '_')
				inputs << [
				name: name,
				id: o.id
				]
			}
			json << [
			id: model.id,
			inputs: inputs
			]
		}
		def jsonStr = new JsonBuilder(json).toString().replace('"', "'")
		html += '<div id="models-profit-data" data-models="'+jsonStr+'" data-field="'+htmlAttrs.name+'">'
		html += '<div></div>'
		html += '</div>'
	}

	List<Map> getAssets() {
		[
		[ dir: "js", file: "selectProfitWidget.js", absolute: true ]
		]
	}

}
