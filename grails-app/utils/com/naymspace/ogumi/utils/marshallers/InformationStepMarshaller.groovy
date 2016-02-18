package com.naymspace.ogumi.utils.marshallers

import com.naymspace.ogumi.server.domain.InformationStep
import grails.converters.JSON

/**
 * Created by dennis on 20.05.15.
 */
class InformationStepMarshaller {
    static def register(){
        UploadableMarshaller.register()
        JSON.registerObjectMarshaller( InformationStep ) { InformationStep infoStep ->
            return [
                    id: infoStep.id,
                    name: infoStep.name,
                    info: infoStep.information,
                    fields: infoStep.media,
                    cls: infoStep.getClass().getName()

            ]
        }
    }

}
