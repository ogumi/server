package com.naymspace.ogumi.model.server.controller

import com.naymspace.ogumi.model.server.ModelApiController
import com.naymspace.ogumi.model.server.TimedModel
import com.naymspace.ogumi.server.domain.ABQuestion
import com.naymspace.ogumi.server.domain.ABQuestionResponse
import com.naymspace.ogumi.server.domain.Experiment
import com.naymspace.ogumi.server.domain.IncentivizedTask
import com.naymspace.ogumi.server.domain.IncentivizedTaskUser
import com.naymspace.ogumi.server.domain.InformationStep
import com.naymspace.ogumi.server.domain.Player
import com.naymspace.ogumi.server.domain.Question
import com.naymspace.ogumi.server.domain.QuestionResponse
import com.naymspace.ogumi.server.domain.Questionnaire
import com.naymspace.ogumi.server.domain.SequenceEntry
import com.naymspace.ogumi.server.domain.SequenceEntryMoney
import com.naymspace.ogumi.server.domain.Session
import com.naymspace.ogumi.server.domain.Waiting
import com.naymspace.ogumi.utils.marshallers.ExperimentMarshaller
import com.naymspace.ogumi.utils.marshallers.IncentivizeTaskMarshaller
import com.naymspace.ogumi.utils.marshallers.InformationStepMarshaller
import com.naymspace.ogumi.utils.marshallers.QuestionaireMarshaller
import com.naymspace.ogumi.utils.marshallers.SessionMarshaller
import com.naymspace.ogumi.utils.marshallers.WaitingMarshaller
import grails.converters.JSON

class SessionsController implements ModelApiController{

    def springSecurityService
    def experimentService

    def incentivicedTaskCommunicator

    def index() {
        SessionMarshaller.register()
        def sessions = Session.findAllByActive(true)
        RenderJson(sessions)
    }

    // POST: /modelapi/$sessionId
    def addPlayerToSession(){
        def sessionId = params.sessionId
        def currentUser = springSecurityService.currentUser
        def currentSession = Session.get(sessionId);

        log.info("User ${currentUser.username} wants to join session ${sessionId}")

        //See if the player is already registered to that session and return current step if he is
        def player = Player.findBySessionAndUser(currentSession, currentUser)
        if (player){
            log.info("User ${currentUser.username} already joined session ${sessionId} - sending his current step (${player.step})")
            return RenderJson([step: player.step]) //just return the step for the current user
        }

        //see if there's a where maxParticipants is reached
        def participantsInSession = currentSession.players?.size() ?: 0
        def session = Session.where {
            id == sessionId && (maxParticipants <= participantsInSession && maxParticipants > 0)
        }
        if (session) {
            log.info("User ${currentUser.username} is unable to join session ${sessionId} - maxParticipants reached!")
            response.status = 401
            RenderJson([failure: "max participants reached"])
        }
        //and return 401 with failure json response

        //otherwise add the player to a session
        def addedPlayer = new Player(session: currentSession, user: currentUser, money: 0, step: 0, version: 0).save();
        if (addedPlayer){
            log.info("User ${currentUser.username} was added to session ${sessionId} - sending his current step (0)")
            return RenderJson([step: 0])
        }
        else{
            response.status = 500;
            log.error("ERROR while adding User ${currentUser.username} to session ${sessionId}")
            return RenderJson([failure: "unknown error while registering player to session"])
        }
    }

    // return information about a step
    // GET: /modelapi/$sessionId/step/$stepIndex
    def getSessionStep(){
        def stepIndex = params.stepIndex as int
        def sessionId = params.sessionId
        def currentUser = springSecurityService.currentUser
        def currentSession = Session.get(sessionId)

        def currentPlayer = Player.findByUserAndSession(currentUser,currentSession)
        if (stepIndex >= currentPlayer.step) stepIndex = currentPlayer.step

        // return "no content" if the step index is greater than the sessions step count
        if (stepIndex >= currentSession.sequence.size()) {
            def currentStepMoney = SequenceEntryMoney.findBySessionAndUserAndStep(currentSession, currentUser, stepIndex-1)
            log.info("User ${currentUser.username} has reached end of session ${sessionId}")
            render(contentType: "application/json", status: 200){
                [
                    stepProfit: currentStepMoney ? currentStepMoney.money : 0,
                    stepIndex: stepIndex,
                    sessionEnd: true,
                    player: currentPlayer
                ]
            }
            return
        }

        def currentStep = currentSession.sequence.get(stepIndex as int)
        def currentStepMoney = SequenceEntryMoney.findBySessionAndUserAndStep(currentSession, currentUser, stepIndex)

        InformationStepMarshaller.register()
        IncentivizeTaskMarshaller.register()
        QuestionaireMarshaller.register()
        ExperimentMarshaller.register()
        WaitingMarshaller.register()

        log.info("User ${currentUser.username} gets info about his current step (${stepIndex}) - ${currentStep.getClass().getName()}")
        RenderJson([
                stepProfit: currentStepMoney ? currentStepMoney.money : 0,
                stepIndex: stepIndex,
                currentStep: currentStep,
                player: currentPlayer
        ])
    }

    // save input for a step
    // POST: /modelapi/$sessionId/step/$stepIndex
    def saveSessionStep(){
        def currentUser = springSecurityService.currentUser
        def currentSession = Session.get(params.sessionId)
        def currentStep = currentSession.sequence.get(params.stepIndex as int)

        def player = Player.findByUserAndSession(currentUser,currentSession)
        log.info("User ${currentUser.username} posts step results for step ${params.stepIndex}(${currentStep.getClass().getName()})")

        def data = request.JSON.data

        switch (currentStep.getClass()){

            case Waiting.class:
                if ((currentStep as Waiting).waitUntil.after(new Date())){
                    log.info("User ${currentUser.username} still has to wait")
                    return getSessionStep()
                }
                break
            case Questionnaire.class:
                log.info("User ${currentUser.username} posts answers to questionnaire (step:${params.stepIndex}")
                data.each(){
                    def answered = QuestionResponse.findByUserAndQuestionAndSession(
                            currentUser,
                            Question.get(it.id),
                            currentSession
                    )
                    if (!answered){
                        new QuestionResponse(
                                user: currentUser,
                                question: Question.get(it.id),
                                session: currentSession,
                                response: it.answer,
                                step: params.stepIndex as int
                        ).save()
                    }
                }
        }
        log.info("User ${currentUser.username} is send to the next step (${player.step + 1})")
        player.step += 1
        player.save(flush: true)
        params.stepIndex = player.step
        getSessionStep()
    }


}
