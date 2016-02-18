###
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
###
jQuery().ready ->
  jQuery('div#experiments > div').each (index, el) ->
    $el             = jQuery(el)
    $startForm      = $el.find('form')
    $container      = $el.find('div#chartContainer')
    session         = $container.attr('data-session')
    identifier      = $container.attr('data-experiment-link')
    modelurl        = $container.attr('data-url')
    images          = $container.attr('data-chart-image-url')
    chartEl         = $container.find('.sessionChart').attr('id')
    authentication_token = $container.attr('data-authentication-token')
    experimentChart = new ExperimentChart(chartEl, images);

    updateData =  (data) ->
     	if data.model != undefined
        $startForm.hide()
        $container.show()
        experimentChart.parseData(data)

      if data.userInput != undefined
          $el.find('div#user-inputs pre#user-inputs-json').html(JSON.stringify(data.userInput).replace(/{/gi, '\n{'));

    openWebsocket = ->
      socket = new SockJS(modelurl+"?access_token="+authentication_token);
      socket.onopen = ->
          stomp = Stomp.over(socket);
          stomp.debug = null
          stomp.connect();
          socket.retries = 0;
          $startForm.show();
          $container.hide();
          stomp.subscribe '/queue/experiment/updates/admin/' + identifier, (message)->
            json = JSON.parse message.body
            # reformat model data to fit to legacy code
            newData = []
            for i in [0..json.model.data.time.length]
              curStep = {}
              for label, values of json.model.data
                curStep[label] = values[i]
              newData.push(curStep)
            json.model.data = newData
            #########################################
            updateData  json

    openWebsocket()
