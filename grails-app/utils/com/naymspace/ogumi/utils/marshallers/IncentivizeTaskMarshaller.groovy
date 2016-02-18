package com.naymspace.ogumi.utils.marshallers

import com.naymspace.ogumi.server.domain.IncentivizedTask
import com.naymspace.ogumi.server.domain.InformationStep
import grails.converters.JSON

/**
 * Created by dennis on 20.05.15.
 */
class IncentivizeTaskMarshaller {
    static def register(){
        JSON.registerObjectMarshaller( IncentivizedTask ) { IncentivizedTask iTask ->
            return [
                    id: iTask.id,
                    name: iTask.name,
                    cls: iTask.getClass().getName(),
                    fields: iTask.questions
            ]
        }
    }

}
