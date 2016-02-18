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
import groovy.json.*

class UploadJarWidget extends Widget {

	def app = Holders.getGrailsApplication()

	String render() {

        def g = app.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        def uploadLink = g.createLink(uri: '/upload/jar')

		def domainClass = internalAttrs["relatedDomainClass"]
		def html = new StringBuilder()
		def inputid = 'input-'+internalAttrs["propertyName"]
		html.append('<div id="' + inputid + '-hidden" class="hidden" data-replace="true">');
		def prev = []
		def conf = []
		value.each {id ->
			def upload = domainClass.get(id)
			prev << '<div class="file-preview-other" style="min-width:160px;"><i class="glyphicon glyphicon-file"></i></div>'
			conf << [ caption: upload.getName(), url: uploadLink,
				key: upload.getId()
			]
			html.append('<input type="hidden" name="'+ htmlAttrs.name + '" value="' + id + '">');
		}
		html.append('</div>');
		html.append('<input id="' + inputid + '" type="file">')
		def opts = new JsonBuilder()

		opts uploadUrl:  uploadLink,
				showRemove: true,
				initialPreviewShowDelete: true,
				overwriteInitial: false,
				initialPreview: prev,
				initialPreviewConfig: conf,
				inputName: htmlAttrs.name,
				allowedFileExtensions: ['jar'],
				value: value
		html.append('<script type="text/javascript">')
		html.append('window.fileinputWidget = window.fileinputWidget || {};')
		html.append('window.fileinputWidget["' + inputid + '"] = ')
		html.append(opts.toString() + ';')
		html.append('</script>')
	}

	List<Map> getAssets() {
		[
			[ dir: "js", file: "fileinput.min.js", absolute: true ],
			[ dir: "js", file: "fileinput-widget.js", absolute: true ],
			[ dir: "css", file: "fileinput.min.css", absolute: true ]
		]
	}

	public void updateValue() {
		def upload;
		if (value) {
			upload = internalAttrs['relatedDomainClass'].get(value as Long)
		}
		updateValue(upload)
	}

}
