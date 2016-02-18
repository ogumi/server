package com.naymspace.ogumi.server.tags

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

import net.kaleidos.plugins.admin.GrailsAdminPluginTagLib
import grails.util.Holders

class GrailsAdminPluginExtTagLib extends GrailsAdminPluginTagLib {

	static namespace = 'gape'

	def listSessionLine = { attrs ->
		def domain = adminConfigHolder.getDomainConfig(attrs.object)
		def dataUrl = ""
		if (attrs.object.id) {
			dataUrl = g.createLink(absolute:true, mapping:"grailsAdminEdit", params:[slug: domain.slug, id: attrs.object.id])
		}
		def grailsApplication = Holders.getGrailsApplication()
		def contextLink  = servletContext.contextPath
		def sessionLink  = "session/" + attrs.object.id
		def resultsLink  = contextLink + "/results/"  + sessionLink
		def liveViewLink = contextLink + "/liveview/" + sessionLink
		out << "<tr data-url='$dataUrl'>"
		out << "<td class=\"js-list-delete\">"
		if (attrs.object.id) {
			out << "<input type=\"checkbox\" class=\"js-list-delete\" data-element-id=\"${attrs.object.id}\"/>"
		}
		out << "</td>"
		out << grailsAdminPluginHtmlRendererService.renderListLine(attrs.className, attrs.object)
		if(attrs.object.active){
			out << "<td><a href=\"${liveViewLink}\">Live View</a></td>"
		} else {
			def now = new Date()
			if(attrs.object.end?.before(now)){
				out << "<td><a href=\"${resultsLink}\">Results</a></td>"
			} else {
				out << "<td></td>"
			}
		}
		out << "</tr>"
	}
}
