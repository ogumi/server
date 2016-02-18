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

import net.kaleidos.plugins.admin.widget.relation.RelationPopupOneWidget
import groovy.xml.MarkupBuilder

class RelationPopupOneListWidget extends RelationPopupOneWidget {

		@Override
		def _buttons(slug, relationObject, builder) {
				def listApi = ''
				def countApi = ''

				if (slug) {
						listApi = grailsLinkGenerator.link(mapping:"grailsAdminApiAction", method:"get", params:[slug:slug])
						countApi = grailsLinkGenerator.link(mapping:"grailsAdminCountApiAction", method:"get", params:[slug:slug])
				}

				String display = (relationObject)?"block":"none"

				builder.div class:"btn-group", {
						def attrs = [href:"#", class:"btn btn-default js-relationpopuponewidget-list", "data-toggle":"modal", "data-url":listApi, "data-url-count": countApi]
						if (!slug) {
								attrs['disabled']='disabled'
						}
						a (attrs) {
								span class:"glyphicon glyphicon-list", { mkp.yield " "}
								mkp.yield " List"
						}

						attrs = [href:"#", class:"btn btn-default js-relationpopuponewidget-delete", style:"display:${display};"]
						if (!slug) {
								attrs['disabled']='disabled'
						}
						a (attrs) {
								span class:"glyphicon glyphicon-trash", { mkp.yield " "}
								mkp.yield " Delete"

						}
				}
		}

		@Override
		List<String> getAssets() {
				def results = [
						'grails-admin/css/widgets/relationpopuponewidget.css',
						'grails-admin/js/widgets/relationpopup.js',
						'grails-admin/js/widgets/relationPopupWidgetList.js',
						'grails-admin/templates/grails-admin-modal.handlebars',
						'grails-admin/templates/grails-admin-list.handlebars',
						'grails-admin/templates/grails-admin-pagination.handlebars'
				]
				results = results.collect { ["plugin":"admin-interface", "absolute":true, "file":it]  }
				results << [ dir: "js", file: "relationPopupOneListWidgetField.js", absolute: true ]
				return results
		}
}
