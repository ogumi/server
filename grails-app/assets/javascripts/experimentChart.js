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

function ExperimentChart(container, images) {
	this.container = container || "sessionChart";
	this.images = images || "";
	this.chart = null;
}

ExperimentChart.prototype.parseData = function(ret){
	var model = ret.model;
	var chartData = model.data;
	if(this.chart == null){
		var getBallonText = function (prop, graphDataItem, graph) {
			var value = graphDataItem.values.value;
			var category = graphDataItem.category;
			return 'Step '+category+'<br/><b><span>'+prop+': '+Math.round(value * 100) /100+'</span></b>';
		};
		var graphs = [];
		for(var i = 0; i < model.label.length; i++){
			var prop = model.label[i];
			graphs.push({
				"id": prop,
				"balloonFunction": getBallonText.bind(this, prop),
				"legendValueText": "[[description]]",
				"bullet": "round",
				"bulletBorderAlpha": 1,
				"bulletColor":"#FFFFFF",
				"hideBulletsCount": 50,
				"title": prop,
				"valueField": prop,
				"useLineColorForBulletBorder":true
			});
		}
		this.chart = AmCharts.makeChart(this.container, {
			"type": "serial",
			"theme": "none",
            "export" : {
              "enabled": true,
                "libs": "../libs/"
            },
			"pathToImages": this.images,
			"legend": {
				"useGraphSettings": true
			},
			"dataProvider": chartData,
			"valueAxes": [{
				"axisAlpha": 0.2,
				"dashLength": 1,
				"position": "left"
			}],
			"mouseWheelZoomEnabled":true,
			"graphs": graphs,
			"chartScrollbar": {
				"autoGridCount": true,
				"graph":  model.label[0],
				"scrollbarHeight": 40
			},
			"chartCursor": {
				"cursorPosition": "mouse"
			},
			"categoryField": "time",
			"categoryAxis": {
				"axisColor": "#DADADA",
				"dashLength": 1,
				"minorGridEnabled": true,
				"title": "Time in seconds"
			}
		});
	} else {
		this.chart.dataProvider = chartData;
	}
	this.chart.categoryAxis.guides = [{
		category: ret.time,
		lineColor: "#CC0000",
		lineAlpha: 1,
		fillAlpha: 0.2,
		fillColor: "#CC0000",
		dashLength: 2,
		inside: true,
		labelRotation: 90,
		label: "current"
	}]
	this.chart.validateData();
}

window.ExperimentChart = ExperimentChart;
