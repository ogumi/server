package com.naymspace.ogumi.util.tasks

import com.naymspace.ogumi.server.domain.Session
import grails.util.Holders
import groovy.util.logging.Log4j

/**
 * Created by dennis on 2/3/16.
 */
@Log4j
class SessionDeactivatorTask extends TimerTask{
    def grailsApplication
    def persistenceInterceptor

    public SessionDeactivatorTask(){
        grailsApplication = Holders.grailsApplication
        persistenceInterceptor = grailsApplication.mainContext.getBean('persistenceInterceptor')
    }

    @Override
    void run() {
        persistenceInterceptor.init()
        Session.findByActive(true).each {
            if (it.end.before(new Date())){
                it.active = false
                it.save()
            }
        }
        persistenceInterceptor.flush()
        persistenceInterceptor.destroy()
    }
}
