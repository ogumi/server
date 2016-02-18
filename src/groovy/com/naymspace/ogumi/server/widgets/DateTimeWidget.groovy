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

import net.kaleidos.plugins.admin.widget.DateInputWidget
import groovy.xml.MarkupBuilder

class DateTimeWidget extends DateInputWidget {

	static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm"

	@Override
	String render() {
		def writer = new StringWriter()
		def builder = new MarkupBuilder(writer)
		def format = _getFormat()
		def attrs = htmlAttrs.clone()
		attrs << ["type": "datetime-local"]
		attrs << ["data-date-format": format.toLowerCase()]
		attrs << ["class": "form-control"]
		attrs << ["value": value?value.format(format):""]
		builder.input(attrs)
		return writer
	}


	List<Map> getAssets() {
		[
			[ dir: "js", file: "polyfiller.js", absolute: true ],
			[ dir: "js", file: "datetime-widget.js", absolute: true ],
			[ dir: "css", file: "datetime.css", absolute: true ]
		]
	}

	@Override
	String _getFormat(){
		return internalAttrs["dateFormat"]?:DEFAULT_DATE_FORMAT
	}

}
