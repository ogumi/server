import com.naymspace.ogumi.model.server.IncentivicedTaskBroker
import com.naymspace.ogumi.model.server.communication.IncentivicedTaskCommunicator
import com.naymspace.ogumi.model.server.communication.WebsocketExperimentCommunicator
import com.naymspace.ogumi.model.server.config.WebSocketConfig
import com.naymspace.ogumi.server.services.ExperimentService
import com.naymspace.ogumi.util.tasks.ExperimentActivatorTask
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor

// Place your Spring DSL code here
beans = {
    websocketConfig(WebSocketConfig)
    experimentService(ExperimentService){
        experimentCommunicator = ref("experimentCommunicator")
    }

    experimentCommunicator(WebsocketExperimentCommunicator)
    incentivicedTaskCommunicator(IncentivicedTaskCommunicator)
    incentivizedTaskBroker(IncentivicedTaskBroker)
}