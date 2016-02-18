package com.naymspace.ogumi.utils.marshallers

import com.naymspace.ogumi.server.domain.Session
import grails.converters.JSON
import grails.util.Holders

/**
 * Created by dennis on 20.05.15.
 */
class SessionMarshaller {
    static def register(){
        def linkGenerator = Holders.grailsApplication.mainContext.getBean("grailsLinkGenerator")
        JSON.registerObjectMarshaller( Session ) { Session session ->
            return [
                    id: session.id,
                    name: session.name,
                    password: session.password,
                    secure: session.getSecure(),
                    stompEntry: linkGenerator.serverBaseURL + "/stomp"
            ]
        }
    }

}
