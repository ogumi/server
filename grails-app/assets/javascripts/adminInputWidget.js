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

"use strict";

jQuery(document).ready(function(){

	var models = $('#models-data').attr('data-models').replace(/[']/g, '"');
	models = JSON.parse(models);
	var attribute = $('#models-data').attr('data-field');
	var hiddenEl  = $('#input-'+attribute+'-hidden');

	var addHiddenField = function(name, model){
		var val = $('input#input-'+name).val();
		val = JSON.stringify({name: name, value: val, model: model}).replace(/["]/g, "'");
		var str = '<input hidden="hidden" name="'+attribute+'" value="'+val+'" data-name="'+name+'">';
		hiddenEl.find('input[data-name="'+name+'"]').remove();
		hiddenEl.append(str);
	}

	var showAdminFields = function(){
		var modelId = $(this).val();
		var model = $.grep(models, function(e){
			return e.id == modelId;
		});
		if (model.length > 0) {
			model = model[0];
		}
		$('#models-data > div').empty();
		for(var i = 0; i < model.inputs.length; i++){
			var input = model.inputs[i];
			var value = hiddenEl.find('input[data-name="'+input.name+'"]').val();
			if(value !== undefined && value !== null && value !== ""){
				value = JSON.parse(value.replace(/[']/g, '"')).value;
			} else {
				value = "";
			}
			var step = 'any';
			var min = input.min;
			var max = input.max;
			var type = 'number decimal';
			if(input.type === 'int' || input.type === 'Integer'){
				type = 'number';
				step = 1;
			}
			if(input.displayAs === 'slider'){
				type = 'range';
			}
			var elem = '<div class="form-group">';
			elem += 	'<label for="input-'+input.name+'">'+input.name.replace(/[_]/g, ' ')
				+ ' * '
				+'<span class="glyphicon glyphicon-question-sign" title="min: '
				+ min
				+ ', max: '
				+ max
				+ '"></span>'
				+ '</label>';
			elem += 	'<input type="'+type+'" class="form-control" id="input-'+input.name+'" value="'+value+'" min="'+min+'" max="'+max+'" step="'+step+'" required';
			if(type === 'number decimal'){
				elem += ' data-parsley-type="number" data-parsley-min="'+min+'" data-parsley-max="'+max+'"';
			}
			elem += '>';
			elem += '</div>';
			var html = $($.parseHTML(elem));
			html.on('change', addHiddenField.bind(this, input.name, modelId));
			$('#models-data > div').append(html);
		}
	}

	var modelInput = $('label[for="model"]').parent('.form-group').find(':input[name="model"]');
	if(modelInput.length > 0){
		modelInput.on('change', showAdminFields);
		if(modelInput.val() !== ""){
			modelInput.trigger('change');
		}
	}
});
