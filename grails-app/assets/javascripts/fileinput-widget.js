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

jQuery().ready(function(){
	Object.keys(window.fileinputWidget).forEach(function(k) {
		var v = window.fileinputWidget[k];
		var e = jQuery('#' + k).fileinput(v);
		var hidden = jQuery('#' + k + '-hidden');
		e.on('fileuploaded', function(event, data, previewId, index) {
			var input = jQuery('<input>').attr({
				'hidden': true,
				'name':   v.inputName,
				'value':  data.response.id
			});
			if(hidden.attr('data-replace') != null) {
				hidden.empty();
			}
			hidden.append(input);
		});
		e.on('filedeleted', function(event, key) {
			hidden.find('input[value="' + key + '"]').remove();
		});
	});
});
