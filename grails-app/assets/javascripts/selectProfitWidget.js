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

	var models = $('#models-profit-data').attr('data-models').replace(/[']/g, '"');
	models = JSON.parse(models);
	var attribute = $('#models-profit-data').attr('data-field');
	var hiddenEl  = $('#input-'+attribute+'-hidden');

	var addHiddenField = function(select, model){
		var val = select.find('option:selected').val();
		if(val != null){
			var str = '<input hidden="hidden" name="'+attribute+'" value="'+val+'" data-name="'+attribute+'">';
			hiddenEl.find('input').remove();
			hiddenEl.append(str);
		}
	}

	var showProfitSelect = function(){
		var modelId = $(this).val();
		var model = $.grep(models, function(e){
			return e.id == modelId;
		});
		if (model.length > 0) {
			model = model[0];
		}
		$('#models-profit-data > div').empty();
		var elem = '<div class="form-group">';
		elem += '<select class="form-control">';
		elem += '<option value="">--</option>';
		var value = hiddenEl.find('input').val();
		for(var i = 0; i < model.inputs.length; i++){
			var input = model.inputs[i];
			elem += '<option id="input-'+input.name+'" value="'+input.id+'"';
			if(value == input.id){
				elem += ' selected'
			}
			elem += '>'+input.name.replace(/[_]/g, ' ')+'</option>';
		}
		elem += '</select></div>';
		var html = $($.parseHTML(elem));
		var select = html.find('select');
		select.on('change', addHiddenField.bind(this, select, modelId));
		$('#models-profit-data > div').append(html);
	}

	var modelInput = $('label[for="model"]').parent('.form-group').find(':input[name="model"]');
	if(modelInput.length > 0){
		modelInput.on('change', showProfitSelect);
		if(modelInput.val() !== ""){
			modelInput.trigger('change');
		}
	}
});
