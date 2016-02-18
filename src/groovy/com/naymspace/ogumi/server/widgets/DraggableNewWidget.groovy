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

import net.kaleidos.plugins.admin.widget.relation.RelationTableWidget

class DraggableNewWidget extends RelationTableWidget {

	@Override
	String render() {
		if (htmlAttrs.disallowRelationships) {
			return "<p>Disabled relationship due to be inside an embedded dialog</p>"
		}
		def html = new StringBuilder()
		if (internalAttrs["relatedDomainClass"]) {
			def domainClass = internalAttrs["relatedDomainClass"]
			def prop = internalAttrs["propertyName"]
			def inputid = 'input-'+prop
			html.append('<div id="' + inputid + '-hidden" class="hidden">')

			def list = []
			if (value) {
				if (value instanceof List) {
					value.eachWithIndex{ id, index ->
						def entry = internalAttrs['relatedDomainClass'].get(id as Long)
						list << '<li class="list-group-item" data-id="'+entry.id+'"><span class="text-left">'+entry.toString()+'</span><a class="pull-right delete"><span class="glyphicon glyphicon-remove"></span></a></li>'
						html.append('<input type="hidden" name="'+ htmlAttrs.name + '" value="' + entry.id + '">')
					}
				}
			}
			html.append('</div>')
			html.append('<ol class="sortable list-group" data-inputid="'+inputid +'" data-name="'+internalAttrs["propertyName"]+'">')
			list.each {elem ->
				html.append(elem)
			}
			html.append('</ol>')

			def relationConfig = adminConfigHolder.getDomainConfig(domainClass)
			def slug
			def listUrl
			def countUrl
			if (relationConfig) {
				slug = relationConfig?.slug
				listUrl = grailsLinkGenerator.link(mapping: 'grailsAdminApiAction', params:[ 'slug': slug ])
				countUrl = grailsLinkGenerator.link(mapping:"grailsAdminCountApiAction", method:"get", params:[slug:slug])
			}

			html.append('<div><a href="#" class="btn btn-default js-relation-draggable-new" data-toggle="modal" data-url="'+listUrl+'" data-url-count="'+countUrl+'" data-target="#new-'+uuid+'"><span class="glyphicon glyphicon-plus"></span> New</a></div>')
		}
		return html;
	}

	@Override
	List<String> getAssets() {
		[
			[ plugin: "admin-interface", file: "grails-admin/js/widgets/relationPopupWidgetNew.js", absolute:true],
			[ dir: "js", file: "html.sortable.min.js", absolute: true ],
			[ dir: "js", file: "sortable-list.js", absolute: true ],
			[ dir: "css", file: "sortable-list.css", absolute: true ]
		]
	}

}
