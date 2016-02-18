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
import groovy.xml.MarkupBuilder

class RelationNewWidget extends RelationTableWidget {

	@Override
	String render() {
		if (htmlAttrs.disallowRelationships) {
			return "<p>Disabled relationship due to be inside an embedded dialog</p>"
		}

		def writer = new StringWriter()
		def builder = new MarkupBuilder(writer)

		def options = [:]

		if (internalAttrs["relatedDomainClass"]) {
			def domainClass = internalAttrs["relatedDomainClass"]
			def otherSideProperty = internalAttrs["grailsDomainClass"].getPropertyByName(internalAttrs['propertyName']).getOtherSide()
			def optional = otherSideProperty?otherSideProperty.isOptional():true

			def relationConfig = adminConfigHolder.getDomainConfig(domainClass)
			def slug
			def listUrl
			def countUrl

			if (relationConfig) {
				slug = relationConfig?.slug
				listUrl = grailsLinkGenerator.link(mapping: 'grailsAdminApiAction', params:[ 'slug': slug ])
				countUrl = grailsLinkGenerator.link(mapping:"grailsAdminCountApiAction", method:"get", params:[slug:slug])
			}

			value.each {id ->
				def element = domainClass.get(id)
				options[id] = element.toString()
			}

			builder.div class:"relationtablewidget clearfix", view:"relationTableWidget", {
				options.each { key, value ->
					input type: "hidden", name:htmlAttrs['name'], value: key
				}
				_elementsTable(delegate, domainClass, options, optional, slug)
				div {
					def attrs = [class:"btn btn-default js-relationtablewidget-new", "data-url": listUrl, "data-url-count": countUrl, href:"#", "data-toggle":"modal","data-target":"#new-$uuid"]
					if (! relationConfig){
						attrs['disabled'] = 'disabled'
					}
					a (attrs) {
						span(class:"glyphicon glyphicon-plus", "")
						mkp.yield " New"
					}
				}
			}
		}
		return writer.toString()
	}

}
