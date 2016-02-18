package com.naymspace.ogumi.utils.marshallers

import com.naymspace.ogumi.server.domain.InformationStep
import com.naymspace.ogumi.server.domain.Questionnaire
import grails.converters.JSON

/**
 * Created by dennis on 15.06.15.
 */
class QuestionaireMarshaller {

    static def register(){
        JSON.registerObjectMarshaller( Questionnaire ) { Questionnaire questionnaire ->
            return [
                    id: questionnaire.id,
                    name: questionnaire.name,
                    cls: questionnaire.getClass().getName(),
                    fields: questionnaire.questions
            ]
        }
    }
}
