/*
 * Copyright (c) 2014 Kaleidos Open Source
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

app.view('relationPopupOneWidgetField', ['$el', 'relationPopupWidgetList'], function ($el, relationPopupWidgetList) {
		"use strict";

		function setValue (objectId, objectText, show) {
				$el.find(".js-one-rel-text").text(objectText);
				var $input = $el.find(".js-one-rel-value");
				$input.val(objectId);
				$input.trigger('change');
		}

		function addOneElement (objectId, objectText) {
				setValue(objectId, objectText, true);
		}

		function removeElement (event) {
				event.preventDefault();
				setValue('', "<< empty >>", false);
		}

		function openListPopup (page) {
				page = page || 0;

				var input = $el.find(".js-one-rel-value");
				var currentValue = [];

				if (input.length) {
						currentValue.push(parseInt(input.val()));
				}

				var url_list = $(this).data('url');
				var url_count = $(this).data('url-count');

				relationPopupWidgetList
						.open("Select", [currentValue], url_list, url_count)
						.done(addOneElement);
		}

		$el.find(".js-relationpopuponewidget-list").on('click', openListPopup);
		$el.find(".js-relationpopuponewidget-delete").on('click', removeElement);
});
