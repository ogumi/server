package com.naymspace.ogumi.utils.marshallers

import com.naymspace.ogumi.server.domain.Session
import com.naymspace.ogumi.server.domain.Uploadable
import grails.converters.JSON
import grails.util.Holders

/**
 * Created by dennis on 2/3/16.
 */
class UploadableMarshaller {
    static def register(){
        JSON.registerObjectMarshaller( Uploadable ) { Uploadable uploadable ->
            return [
                    id: uploadable.id,
                    name: uploadable.name,
                    type: uploadable.type,
                    downloadble: uploadable.downloadable,
                    url: uploadable.url
            ]
        }
    }
}
