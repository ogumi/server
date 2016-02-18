package com.naymspace.ogumi.util.tasks

import com.naymspace.ogumi.server.domain.Experiment
import com.naymspace.ogumi.server.services.ExperimentService
import com.naymspace.ogumi.server.services.ModelService
import grails.util.Holders
import groovy.util.logging.Log4j
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service

import javax.xml.ws.Holder

/**
 * Created by dennis on 10/14/15.
 */
@Log4j
class ExperimentActivatorTask extends TimerTask{
    def grailsApplication
    def persistenceInterceptor
    def ExperimentService experimentService

    public ExperimentActivatorTask(){
        grailsApplication = Holders.grailsApplication
        persistenceInterceptor = grailsApplication.mainContext.getBean('persistenceInterceptor')
        experimentService = grailsApplication.mainContext.getBean('experimentService')
    }

    def experimentsToStart = {
        Experiment.findByStatus(Experiment.ExperimentStatus.NOTACTIVE)
    }

    def startPlannedExperiments() {
        experimentsToStart().each{ Experiment it ->
            log.info("found experiment that is notactive and has to be planned: ${it.name}-${it.id}" )
            experimentService.planExperiment(it)
            log.info("planned experiment: ${it.name}-${it.id}, new status is: ${it.status}")
        }
    }

    @Override
    void run() {
        persistenceInterceptor.init()
        startPlannedExperiments()
        persistenceInterceptor.flush()
        persistenceInterceptor.destroy()
    }
}
