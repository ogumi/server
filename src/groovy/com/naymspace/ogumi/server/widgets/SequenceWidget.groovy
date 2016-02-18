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

import com.naymspace.ogumi.server.domain.*
import grails.util.Holders
import net.kaleidos.plugins.admin.widget.relation.RelationTableWidget
import groovy.xml.MarkupBuilder

class SequenceWidget extends RelationTableWidget {

	def app = Holders.getGrailsApplication()

	@Override
	String render() {
		if (htmlAttrs.disallowRelationships) {
			return "<p>Disabled relationship due to be inside an embedded dialog</p>"
		}
		def html = new StringBuilder()
		if (internalAttrs["relatedDomainClass"]) {
			def domainClass = internalAttrs["relatedDomainClass"]
			def prop = internalAttrs["propertyName"]

			def relationConfig = adminConfigHolder.getDomainConfig(domainClass)
			def slug
			def listUrl
			def countUrl
			def expListUrl
			def expCountUrl
			def waitingListUrl
			def waitingCountUrl
			if (relationConfig) {
				slug = relationConfig?.slug
				listUrl = grailsLinkGenerator.link(mapping: 'grailsAdminApiAction', params:[ 'slug': slug ])
				countUrl = grailsLinkGenerator.link(mapping:"grailsAdminCountApiAction", method:"get", params:[slug:slug])
				expListUrl = grailsLinkGenerator.link(mapping: 'grailsAdminApiAction', params:[ 'slug': 'experiment' ])
				expCountUrl = grailsLinkGenerator.link(mapping:"grailsAdminCountApiAction", method:"get", params:[slug:'experiment'])
				waitingListUrl = grailsLinkGenerator.link(mapping: 'grailsAdminApiAction', params:[ 'slug': 'waiting' ])
				waitingCountUrl = grailsLinkGenerator.link(mapping:"grailsAdminCountApiAction", method:"get", params:[slug:'waiting'])
			}

			def inputid = 'input-'+prop
			html.append('<div id="' + inputid + '-hidden" class="hidden">')

			def list = []
			def excludes = []
			def expList = Experiment.list()
			for (exp in expList) {
				excludes << exp.id
			}
			def waitingList = Waiting.list()
			for (wt in waitingList) {
				excludes << wt.id
			}
			if (value) {
				if (value instanceof List) {
					value.eachWithIndex{ id, index ->
						def entry = internalAttrs['relatedDomainClass'].get(id as Long)
						def editUrl = grailsLinkGenerator.link(mapping: 'grailsAdminAction', params:[slug: entry.class.simpleName.toLowerCase(), id:entry.id])
						list << '<li class="list-group-item" data-id="'+entry.id+'"><span class="text-left"><a href="'+editUrl+'" target="_blank">'+entry.toString()+'</a></span><a class="pull-right delete"><span class="glyphicon glyphicon-remove"></span></a></li>'
						html.append('<input type="hidden" name="'+ htmlAttrs.name + '" value="' + entry.id + '">')
					}
				}
			}
			html.append('</div>')
			html.append('<ol class="sortable list-group" data-inputid="'+inputid +'" data-name="'+internalAttrs["propertyName"]+'" data-excludeIds="'+excludes.toString()+'">')
			list.each {elem ->
				html.append(elem)
			}
			html.append('</ol>')
			html.append('<div class="seq-btn-group">')
			html.append('<button type="button" class="btn btn-default js-relation-draggable-list" data-url="'+listUrl+'" data-url-count="'+countUrl+'"><span class="glyphicon glyphicon-list"></span> Choose</button>')
			html.append('<button type="button" class="btn btn-default js-relation-draggable-new" data-toggle="modal" data-url="'+waitingListUrl+'" data-url-count="'+waitingCountUrl+'" data-target="#new-'+uuid+'-wt"><span class="glyphicon glyphicon-plus"></span> New Waiting</button>')
			html.append('<button type="button" class="btn btn-default js-relation-draggable-new" data-toggle="modal" data-url="'+expListUrl+'" data-url-count="'+expCountUrl+'" data-target="#new-'+uuid+'"><span class="glyphicon glyphicon-plus"></span> New Experiment</button>')
			html.append('</div>')
		}
		return html;
	}

	@Override
	String renderAfterForm() {
		def relationConfig = adminConfigHolder.getDomainConfig(Class.forName("com.naymspace.ogumi.server.domain.Experiment"))
		def writer = new StringWriter()
		def builder = new MarkupBuilder(writer)
		builder.div id:"new-$uuid", tabindex:"-1", view: "relationPopupWidgetNew", role:"dialog", "aria-labelledby":"confirmLabel", "aria-hidden":"true", "grailsadmin-remote": "enabled", class:"modal fade", "data-field":"${internalAttrs['propertyName']}", {
			div class:"modal-dialog", {
				div class:"modal-content", {
					div class:"modal-header", {
						buton type:"button", "data-dismiss":"modal", "aria-hidden":"true", class:"close", {
							mkp.yield "x"
						}
						h4 id:"confirmLabel", class:"modal-title", {
							mkp.yield "Add Experiment"
						}
					}
					div class:"modal-body", {
						mkp.yieldUnescaped groovyPageRenderer.render(template: '/grailsAdmin/addForm', model: [domain: relationConfig, embedded:true])
					}
					div class:"modal-footer", {
						button type:"button", "data-dismiss":"modal", class:"btn btn-default", { mkp.yield "Close" }
						button type:"button", class:"btn btn-plus btn-success js-relation-popup-widget-new-save-action", { mkp.yield "Save" }
					}
				}
			}
		}
		def relationConfigWt = adminConfigHolder.getDomainConfig(Class.forName("com.naymspace.ogumi.server.domain.Waiting"))
		builder.div id:"new-$uuid-wt", tabindex:"-1", view: "relationPopupWidgetNew", role:"dialog", "aria-labelledby":"confirmLabel", "aria-hidden":"true", "grailsadmin-remote": "enabled", class:"modal fade", "data-field":"${internalAttrs['propertyName']}", {
			div class:"modal-dialog", {
				div class:"modal-content", {
					div class:"modal-header", {
						buton type:"button", "data-dismiss":"modal", "aria-hidden":"true", class:"close", {
							mkp.yield "x"
						}
						h4 id:"confirmLabel", class:"modal-title", {
							mkp.yield "Add Waiting"
						}
					}
					div class:"modal-body", {
						mkp.yieldUnescaped groovyPageRenderer.render(template: '/grailsAdmin/addForm', model: [domain: relationConfigWt, embedded:true])
					}
					div class:"modal-footer", {
						button type:"button", "data-dismiss":"modal", class:"btn btn-default", { mkp.yield "Close" }
						button type:"button", class:"btn btn-plus btn-success js-relation-popup-widget-new-save-action", { mkp.yield "Save" }
					}
				}
			}
		}
		return writer.toString()
	}

	@Override
	List<String> getAssets() {
		[
			[ plugin: "admin-interface", file: "grails-admin/templates/grails-admin-modal.handlebars", absolute:true],
			[ plugin: "admin-interface", file: "grails-admin/templates/grails-admin-list.handlebars", absolute:true],
			[ plugin: "admin-interface", file: "grails-admin/templates/grails-admin-pagination.handlebars", absolute:true],
			[ plugin: "admin-interface", file: "grails-admin/js/widgets/relationPopupWidgetList.js", absolute:true],
			[ plugin: "admin-interface", file: "grails-admin/js/widgets/relationPopupWidgetNew.js", absolute:true],
			[ dir: "js", file: "html.sortable.min.js", absolute: true ],
			[ dir: "js", file: "sortable-list.js", absolute: true ],
			[ dir: "js", file: "adminInputWidget.js", absolute: true ],
			[ dir: "js", file: "outputWidget.js", absolute: true ],
			[ dir: "js", file: "selectProfitWidget.js", absolute: true ],
			[ dir: "css", file: "sortable-list.css", absolute: true ]
		]
	}

	public void updateValue() {
		def values = [];
		if (value) {
			if (value instanceof List) {
				value.each {
					values << internalAttrs['relatedDomainClass'].get(it as Long)
				}
			} else {
				values << internalAttrs['relatedDomainClass'].get(value as Long)
			}
		}
		updateValue(values)
	}

}
