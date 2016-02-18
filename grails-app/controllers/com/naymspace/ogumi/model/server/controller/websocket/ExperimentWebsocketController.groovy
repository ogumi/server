package com.naymspace.ogumi.model.server.controller.websocket

import com.naymspace.ogumi.model.server.TimedModel
import com.naymspace.ogumi.model.server.communication.WebsocketResultMessage
import com.naymspace.ogumi.server.domain.Experiment
import com.naymspace.ogumi.server.domain.Player
import com.naymspace.ogumi.server.domain.Session
import com.naymspace.ogumi.server.domain.User
import com.naymspace.ogumi.server.domain.UserInput
import com.naymspace.ogumi.server.domain.Variable
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.annotation.SendToUser

import java.security.Principal

class ExperimentWebsocketController {

    class SubscribeResult extends WebsocketResultMessage{
        enum Status{
            UNKNOWN, RUNNING, PLANNED, STOPPED
        }

        def reason, identifier, status=Status.UNKNOWN , startAt

        def getStatus(){
            status.name()
        }
    }

    class InputResult extends WebsocketResultMessage{

    }

    public static class Input{
        def input

        Input(){}

        Input(double[] input){
            this.input = input as double[]
        }

        def setInput(input){
            this.input = input as double[]
        }
    }

    def experimentService
    def PersistenceContextInterceptor persistenceInterceptor

    def index() {
        [experiment: Experiment.first()]
    }

    /**
     * actually does not subscribe anything anywhere, but let the client be sure that the experiment exists and return
     * status information
     */
    @MessageMapping("/experiment/subscribe/{identifier}")
    @SendToUser("/queue/experiment/updates")
    protected SubscribeResult subscribeExperiment(@DestinationVariable String identifier, Principal user){
        log.info("User ${user.principal.username} tries to subscribe websocket for experiment with identifier: ${identifier}")
        TimedModel timedModel
        try {
             timedModel = experimentService.getActiveModel(identifier);
        }
        catch (Exception e) {
            log.info("unable to get active model with id: ${identifier}")
            e.printStackTrace()
            return new SubscribeResult(
                    resultStatus: WebsocketResultMessage.RESULTSTATUS.FAILURE,
                    reason: 'experiment not found',
                    identifier: identifier,
                    status: SubscribeResult.Status.UNKNOWN
            )
        }
        def experimentStatus = timedModel.isRunning() ? SubscribeResult.Status.RUNNING : timedModel.isStopped() ? SubscribeResult.Status.STOPPED : timedModel.start.after(new Date()) ? SubscribeResult.Status.PLANNED : SubscribeResult.Status.UNKNOWN

        def result = new SubscribeResult(
                resultStatus: WebsocketResultMessage.RESULTSTATUS.SUCCESS,
                identifier: identifier,
                status: experimentStatus,
                startAt: timedModel.start
        )
        log.info("User ${user.principal.username} has successfully subscribed to websocket for experiment: ${identifier}, start: ${timedModel.start}")
        return result
    }

    /**
     * user sends input to model
     */
    @MessageMapping("/experiment/update/{identifier}")
    @SendToUser("/topic/experiment/update")
    protected InputResult input(@DestinationVariable String identifier, Input input, Principal principal){
        persistenceInterceptor.init()
        log.info("User ${principal.principal.username} submits data for experiment ${identifier}")
        def currentUser = User.findByUsername(principal.principal.username);
        TimedModel timedModel
        try {
            timedModel = experimentService.getActiveModel(identifier);
        }
        catch (Exception e) {
            log.error("unable to get active model with id: ${identifier}")
            e.printStackTrace()
            return new InputResult(
                    result: SubscribeResult.RESULTSTATUS.FAILURE,
            )
        }

        def currentPlayer = Player.findByUserAndSession(currentUser, timedModel.session)


        if (timedModel.model.checkUserInput(input.input)){
            timedModel.model.updateUserInput(currentPlayer.id, input.input)
            def userInput = new UserInput(player: currentPlayer.id, experiment: timedModel.experiment)
            userInput.addToEffort(new Variable(field: 'effort', type: 'double[]', step: timedModel.getCurrStep() * timedModel.model.getTStep(), value: input.input.toString()))
            userInput.save()
            return new InputResult(
                    resultStatus: WebsocketResultMessage.RESULTSTATUS.SUCCESS
            )
        }
        else return new InputResult(
                resultStatus: WebsocketResultMessage.RESULTSTATUS.FAILURE
        )
    }
}



