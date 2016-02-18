package com.naymspace.ogumi.utils.marshallers

import com.naymspace.ogumi.server.domain.Uploadable
import com.naymspace.ogumi.server.domain.Waiting
import grails.converters.JSON

/**
 * Created by dennis on 2/3/16.
 */
class WaitingMarshaller {
    static def register(){
        JSON.registerObjectMarshaller( Waiting ) { Waiting waiting ->
            return [
                    id: waiting.id,
                    name: waiting.name,
                    label: waiting.label,
                    cls: waiting.getClass().getName(),
                    waitUntil: waiting.waitUntil
            ]
        }
    }
}
