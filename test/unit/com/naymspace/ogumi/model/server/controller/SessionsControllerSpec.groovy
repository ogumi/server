package com.naymspace.ogumi.model.server.controller

import com.naymspace.ogumi.server.domain.ABQuestion
import com.naymspace.ogumi.server.domain.ABQuestionResponse
import com.naymspace.ogumi.server.domain.AdminInputField
import com.naymspace.ogumi.server.domain.AdminInputFieldValue
import com.naymspace.ogumi.server.domain.Experiment
import com.naymspace.ogumi.server.domain.IncentivizedTask
import com.naymspace.ogumi.server.domain.IncentivizedTaskUser
import com.naymspace.ogumi.server.domain.InformationStep
import com.naymspace.ogumi.server.domain.Model
import com.naymspace.ogumi.server.domain.OutputField
import com.naymspace.ogumi.server.domain.Player
import com.naymspace.ogumi.server.domain.Question
import com.naymspace.ogumi.server.domain.QuestionResponse
import com.naymspace.ogumi.server.domain.Questionnaire
import com.naymspace.ogumi.server.domain.SequenceEntry
import com.naymspace.ogumi.server.domain.SequenceEntryMoney
import com.naymspace.ogumi.server.domain.Session
import com.naymspace.ogumi.server.domain.Uploadable
import com.naymspace.ogumi.server.domain.User
import com.naymspace.ogumi.server.domain.Variable
import com.naymspace.ogumi.server.services.ExperimentService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import helpers.MockService
import spock.lang.Specification


/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(SessionsController)
@Mock([Session, Uploadable, Experiment, AdminInputField, AdminInputFieldValue, Model, User, Player, InformationStep, SequenceEntry, SequenceEntryMoney, Question, Questionnaire, ABQuestion, ABQuestionResponse, IncentivizedTask, IncentivizedTaskUser, OutputField, Variable, Question, QuestionResponse])
class SessionsControllerSpec extends Specification {

    def setup(){}

    def cleanup() {
    }

    void "test index action with no active session (GET /sessions)"(){
        when: "i call the index action and there's no active session"
            controller.index()
        then: "i should get an empty json object as response"
            response.status == 200
            response.json == []

    }

    void "test index action with active session (GET /sessions)"() {
        mockSession("")
        when: "i call the index action and there're active sessions"
            controller.index()
        then: "i should get a list with running sessions"
            response.status == 200
            response.json[0].id == 1
            response.json[0].name == "session_name"
            response.json[0].secure == false
    }

    void "test index action with active session with password (GET /sessions)"(){
        mockSession("testpassword")
        when: "i call the index action and there're active secure sessions"
            controller.index()
        then: "i should get a list with running sessions including the secure property and a password"
            response.status == 200
            response.json[0].id == 1
            response.json[0].name == "session_name"
            response.json[0].password == "testpassword"
            response.json[0].secure == true
    }

    void "test adding player to running session (POST /sessions/id)"(){
        def session = mockSession("testpassword")
        mockSpringSecurityService(controller)

        when: "i POST to the single action with parameter id set to an active session id and a logged in user"
            request.method = "POST"
            params.sessionId = session.id
            controller.addPlayerToSession()
        then: "the user should be added to the session and i should get the current step for the user as a json response"
            response.status == 200
            response.json.step == 0
            Player.count() == 1
            Session.findById(session.id).players.first().id == 1 //see mockSpringSecurityService
    }

    void "test adding player to running session, but the player already participates (POST /sessions/id)"(){
        def session = mockSession("testpassword")
        mockSpringSecurityService(controller)
        new Player(session: session, user: controller.springSecurityService.currentUser, money: 0, step: 1, version:0).save(flush: true)

        when: "i POST to the single action with id of an active session, where i already participate it"
            request.method = "POST"
            params.sessionId = session.id
            controller.addPlayerToSession()
        then: "i should get the current step in that session but should not be added to the session twice"
            response.status == 200
            response.json.step == 1
            Player.count() == 1
            Session.findById(session.id).players.size() == 1
    }


    void "test adding player to running session, that exceeds the maxParticipants number (POST /sessions/id)"(){
        def session = mockSession("testpassword")
        session.maxParticipants = 5
        session.save(flush: true)
        mockSpringSecurityService(controller)

        //adding 5 players to session
        [1,2,3,4,5].each {
            def newUser = new User(username: "user"+it, password: "password", email:"me@you.com").save(flush: true)
            new Player(session: session, user: newUser, money: 0, step: 1, version:0).save(flush: true)
        }

        when: "i POST to the single action with id of an active session, where maxParticipants number is reached"
            request.method = "POST"
            params.sessionId = session.id
            controller.addPlayerToSession()
        then: "i should get an 401 response and an error message"
            response.status == 401
            response.json.failure == "max participants reached"
    }

    void "test getting step info  (GET /sessions/id?step)"(){
        def user = mockSpringSecurityService(controller)
        def step
        def session = MockService.makeSession {
            addPlayer user
            step = addInformationStep()
        }

        when: "i GET request the single action with a user, a session id and a step id"
            request.method = "GET"
            params.stepIndex      = 0
            params.sessionId      = session.id
            controller.getSessionStep()
        then: "the controller should respond with json response containing my so far earnings and step information"
            response.status == 200
            response.json.currentStep.name == step.name

    }

    void "test getting info for a information step"(){
        def user = mockSpringSecurityService(controller)
        def uploadable = MockService.makeUploadable{
            name = "myname"
            type = "image"
            url  = "myUrl"
        }

        InformationStep step

        def session = MockService.makeSession {
            addPlayer user
            step = addInformationStep().addToMedia(uploadable).save(flush: true)
        }

        when: "i request info about an information step (including media)"
            request.method = "GET"
            params.stepIndex      = 0
            params.sessionId      = session.id
            controller.getSessionStep()
        then: "the controller should respond with all information about the step (including media)"
            response.status == 200
            response.json.currentStep.id == step.id
            response.json.currentStep.name == step.name
            response.json.currentStep.info == step.information
            response.json.currentStep.cls == "com.naymspace.ogumi.server.domain.InformationStep"
            response.json.currentStep.fields[0].name == "myname"
            response.json.currentStep.fields[0].type == "image"
    }


    void "test get info for a questionaire step" (){
        def user = mockSpringSecurityService(controller)
        def step
        def session = MockService.makeSession {
            addPlayer user
            step = addQuestionnaireStep()
        }

        when: "i wanna get information about a questionnaire step"
            request.method = "GET"
            params.stepIndex      = 0
            params.sessionId      = session.id
            controller.getSessionStep()
        then: "i should get all infos about the questionnaire step including questions"
            response.status == 200
            response.json.currentStep.cls == step.getClass().getName()
            response.json.currentStep.fields[0].question == "question 1"
            response.json.currentStep.fields[1].question == "question 2"
    }

    //@TODO: test fails, since there's only one player in the database - handle this case and rewrite tests
    void "test get info for a incentiviceTask step"(){
        def user = mockSpringSecurityService(controller)
        def step
        def session = MockService.makeSession {
            addPlayer user
            addPlayerCount 1
            step = addIncentiviceTaskStep()
        }

        when: "i wanna get information about an incentivizedTask step"
            request.method  = "GET"
            params.stepIndex      = 0
            params.sessionId      = session.id
            controller.getSessionStep()
        then: "i should get all infos about that step and my fellow player"
            response.status == 200
            response.json.currentStep.cls == step.getClass().getName()
            response.json.currentStep.fields[0].question == "question 1"
    }

    void "test get information about an experiment step"(){

        /*defineBeans {
            experimentService(ExperimentService)
        }*/

        def user = mockSpringSecurityService(controller)
        def step
        def session = MockService.makeSession {
            addPlayer user
            step = addExperimentStep()
        }

        when: "i wanna get information about an experiment step"
            request.method = "GET"
            params.stepIndex      = 0
            params.sessionId      = session.id
            controller.getSessionStep()
        then: "i should get all infos about that experiment including translations and userInput fields"
            response.status == 200
            response.json.currentStep.cls == Experiment.class.getName()

            response.json.currentStep.translations.size() == 2
            response.json.currentStep.translations[0].name == "de.txt"

            response.json.currentStep.userInput[0].with{
                min == 0.0
                name == "effort"
                displayAs == "slider"
            }
    }

    void "test saveSessionStep for Questionnaire step"(){
        def user = mockSpringSecurityService(controller)
        def step, step2
        def session = MockService.makeSession {
            addPlayer user
            step = addQuestionnaireStep()
            step2 = addInformationStep()
            addPlayerCount(1)
        }

        when: "i send a POST to saveSessionStep with a stepId of a questionnaire Step and data with answers for questions"
            request.method = "POST"
            params.stepIndex = 0
            params.sessionId      = session.id
            request.json = [
                    cls: step.getClass().getName(),
                    data: [
                        [
                                id: "1",
                                answer: "answer1"

                        ],
                        [
                                id: "2",
                                answer: "answer2"

                        ]
                    ]
            ]
            controller.saveSessionStep()
        then: "the provided answers should be saved correctly and i should get the information if the next step"
            response.status == 200
            response.json.currentStep.cls == 'com.naymspace.ogumi.server.domain.InformationStep'
            1..2.each(){
                QuestionResponse.findByUserAndQuestionAndSession(
                        user,
                        Question.get(it),
                        session
                ).response == "answer"+it
            }
    }




    def mockSequenceInSession = {Session session ->
        [
            withSequence: { sequence ->
                session.sequence = sequence
            }
        ]
    }

    def mockSpringSecurityService(SessionsController controller){
        def loggedInUser = new User(id: 1, username: "dennis", password: "testpassword", email: "me@you.com").save(flush: true)
        controller.springSecurityService = [
                encodePassword: 'password',
                reauthenticate: { String u -> true },
                loggedIn: true,
                currentUser:  loggedInUser
        ]
        loggedInUser
    }

    def mockSession(String password) {
        mockDomain(Session, [[
                     name  : "session_name",
                     start : new Date(),
                     end   : new Date().plus(1),
                     active: true,
                     password: password
                 ]
        ])
        assert Session.count() == 1
        Session.findAll().first()
    }


}
