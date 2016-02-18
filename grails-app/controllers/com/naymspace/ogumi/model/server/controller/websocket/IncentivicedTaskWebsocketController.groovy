package com.naymspace.ogumi.model.server.controller.websocket

import com.naymspace.ogumi.model.server.communication.WebsocketResultMessage
import com.naymspace.ogumi.server.domain.ABQuestion
import com.naymspace.ogumi.server.domain.ABQuestionResponse
import com.naymspace.ogumi.server.domain.IncentivizedTask
import com.naymspace.ogumi.server.domain.Player
import com.naymspace.ogumi.server.domain.User
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.annotation.SendToUser

import java.security.Principal

class IncentivicedTaskWebsocketController {

    class SubscribeResult extends WebsocketResultMessage {}

    class ControlMessage extends WebsocketResultMessage {
        def reason
    }

    class InputResult extends WebsocketResultMessage {
        def answer
    }

    // Wrapper class for ABQuestionInput from Client
    public static class Input extends ABQuestion {
        def answer
    }

    def incentivicedTaskCommunicator
    def incentivizedTaskBroker
    def PersistenceContextInterceptor persistenceInterceptor

    @MessageMapping("/incentivizedTask/subscribe/{incentivizedTaskId}")
    @SendToUser("/topic/incentivizedTask/control/")
    protected ControlMessage subscribeIncentivizedTask(@DestinationVariable String incentivizedTaskId, Principal user) {
        persistenceInterceptor.init()
        log.info("User ${user.principal.username} subscribes IncentivizedTask ${incentivizedTaskId}")
        def currentUser = User.findByUsername(user.principal.username);

        def incentivizedTask = IncentivizedTask.findById(incentivizedTaskId)
        def currentPlayer = Player.findByUserAndSession(currentUser, incentivizedTask.session)

        incentivizedTaskBroker.activeTask(incentivizedTask).subscribeForPlayer(currentPlayer)
        return new ControlMessage(resultStatus: WebsocketResultMessage.RESULTSTATUS.SUCCESS, reason: "SUBSCRIPTION")
    }

    @MessageMapping("/incentivizedTask/update/{incentivizedTaskId}")
    @SendToUser("/topic/incentivizedTask/control/")
    protected ControlMessage input(@DestinationVariable int incentivizedTaskId, Input[] input, Principal user) {
        persistenceInterceptor.init()
        log.info("User ${user.principal.username} added input to  IncentivizedTask ${incentivizedTaskId}")
        def currentUser = User.findByUsername(user.principal.username);

        def incentivizedTask = IncentivizedTask.findById(incentivizedTaskId)
        def currentPlayer = Player.findByUserAndSession(currentUser, incentivizedTask.session)

        def activeIncentivizedTask = incentivizedTaskBroker.activeTask(incentivizedTask)

        input.each {
            activeIncentivizedTask.addAnswer(
                    currentPlayer,
                    new ABQuestionResponse(
                            question: ABQuestion.findById(it.id),
                            response: it.answer,
                            session: incentivizedTask.session,
                            user: currentUser,
                            step: currentPlayer.step
                    ).save(flush: true)
            )
        }

        incentivicedTaskCommunicator.notifyDonePlayersFor activeIncentivizedTask

        persistenceInterceptor.flush();
        persistenceInterceptor.destroy();
        new ControlMessage(reason: "RECEIVEDINPUT", resultStatus: WebsocketResultMessage.RESULTSTATUS.SUCCESS)
    }
}
