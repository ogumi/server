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

jQuery().ready(function(){

	// Sortable

	var excludeValues = [];
	if(jQuery('.sortable').attr('data-excludeIds') != null){
		excludeValues = JSON.parse(jQuery('.sortable').attr('data-excludeIds'));
	}

	var triggerSort = function($sortable, $li){
		$sortable.trigger('sortupdate', {
			startparent: $sortable,
			item: $li
		});
	};

	var addItem = function($sortable, id, text){
		var li = '<li class="list-group-item" data-id="'+id+'"><span class="text-left">'+text+'</span><a class="pull-right delete"><span class="glyphicon glyphicon-remove"></span></a></li>';
		var $li = jQuery(jQuery.parseHTML(li));
		$li.find('a.delete').on('click', deleteItem);
		$sortable.append($li);
		triggerSort($sortable, $li);
		$('.sortable').sortable('reload');
	};

	var deleteItem = function(ev){
		ev.preventDefault();
		ev.stopPropagation();
		var $li = jQuery(ev.target).parents('li');
		var $sortable = jQuery($li.parents('.sortable')[0]);
		$li.remove();
		triggerSort($sortable, $li);
		return false;
	};

	var $sortable = jQuery('.sortable').sortable({
		placeholder: '<li class="list-group-item"></li>'
	});
	$sortable.find('a.delete').on('click', deleteItem);

	$sortable.bind('sortupdate', function(e, ui) {
		var $hidden = jQuery('#' + $sortable.attr('data-inputid')+ '-hidden');
		$hidden.empty();
		var $list    = ui.startparent;
		var children = $list.children();
		for(var i=0; i < children.length; i++){
			var $child = jQuery(children[i]);
			var input = '<input type="hidden" name="'+ $list.attr('data-name') + '" value="' + $child.attr('data-id') + '">';
			$hidden.append(input);
		}
	});

	// Call relationPopupWidgetList from admin-interface plugin

	function addRelation (page) {
		page = page || 0;
		var $ths = jQuery(this);
		var $sortable = jQuery($ths.parents('div.form-group').find('.sortable'));
		var url_list  = $ths.data('url');
		var url_count = $ths.data('url-count');
		var template   = Injector.get('templateService')();
		var pagination = Injector.get('paginationService')(template);
		var list       = Injector.get('relationPopupWidgetList');
		list(template, pagination).open("Select", excludeValues, url_list, url_count)
			.done(function(id, text){
				addItem($sortable, id, text);
			});
	};
	$sortable.parent().find( ".js-relation-draggable-list").on("click", addRelation);

	function openNewPopup (event) {
		var target = $(event.currentTarget).data('target');
		var $sortable = jQuery(jQuery(this).parents('div.form-group').find('.sortable'));
		$(target).trigger('grailsadmin:relationPopupWidgetNew', function(id, text){
			excludeValues.push(id);
			addItem($sortable, id, text);
		});
	};
	$sortable.parent().find( ".js-relation-draggable-new").on("click", openNewPopup);
});
