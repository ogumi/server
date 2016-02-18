package com.naymspace.ogumi.utils.marshallers

import com.naymspace.ogumi.server.domain.Experiment
import com.naymspace.ogumi.server.domain.InformationStep
import com.naymspace.ogumi.server.services.ModelService
import grails.converters.JSON
import grails.util.Holders

/**
 * Created by dennis on 20.05.15.
 */
class ExperimentMarshaller {
    static def register(){
        JSON.registerObjectMarshaller( Experiment ) { Experiment experiment ->
            def userInputFields = ModelService.getUserInputFieldsFrom(experiment.model.jar.path)
            return [
                    id: experiment.id,
                    link: experiment.link,
                    diagram: experiment.diagram,
                    y0min: experiment.y0Min,
                    y0max: experiment.y0Max,
                    y1min: experiment.y1Min,
                    y1max: experiment.y1Max,
                    xPointsVisible: experiment.xPointsVisible,
                    start: experiment.experimentStart,
                    tStep: experiment.timeStepSize,
                    displayFuture: experiment.showFuture,
                    blockStep: experiment.blockUserInput,
                    cls: experiment.getClass().getName(),
                    userInput: userInputFields,
                    translations: experiment.model.translations,
                    status: experiment.status

            ]
        }
    }

}
