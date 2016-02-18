package com.naymspace.ogumi.server

import com.naymspace.ogumi.model.server.ActiveIncentivizedTask
import com.naymspace.ogumi.server.domain.ABQuestion
import com.naymspace.ogumi.server.domain.ABQuestionResponse
import com.naymspace.ogumi.server.domain.IncentivizedTask
import com.naymspace.ogumi.server.domain.IncentivizedTaskUser
import com.naymspace.ogumi.server.domain.Player
import com.naymspace.ogumi.server.domain.SequenceEntry
import com.naymspace.ogumi.server.domain.Session
import com.naymspace.ogumi.server.domain.User
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import helpers.MockService
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by dennis on 2/16/16.
 */
@TestMixin(GrailsUnitTestMixin)
@Mock([IncentivizedTask, Player, User, Session, ABQuestion, SequenceEntry, ABQuestionResponse, IncentivizedTaskUser])
class ActiveIncentivizedTaskSpec extends Specification{

    static def ITERATIONS = 100

    def step
    def session
    def activeIncentivizedTask

    def setup(){
        session = MockService.makeSession {
            step = addIncentiviceTaskStep()
            addPlayerCount(3)
        }
        activeIncentivizedTask = new ActiveIncentivizedTask(incentivizedTask: step)
    }

    def cleanup() {
    }

    void "Test Matching"(){

        when: "I add three players and match them"
            
            (1..3).each{
                activeIncentivizedTask.addPlayer(Player.get(it))
            }
            activeIncentivizedTask.matchPlayers()
        then: "two players should always be matched with each other and one player should be matched with null"
            (1..3).each {
                activeIncentivizedTask.isMatched(Player.get(it)) == true
            }
            activeIncentivizedTask.matchedPlayer(Player.get(1)) == [status: "PASSIVE", player: Player.get(2)]
            activeIncentivizedTask.matchedPlayer(Player.get(3)) == [status: "PASSIVE", player: null]
    }

    void "Test answer submission"(){
        (1..3).each{
            activeIncentivizedTask.addPlayer(Player.get(it))
        }
        activeIncentivizedTask.matchPlayers()
        when: "two of three players submit answers"
            (1..2).each {
                def answer = new ABQuestionResponse(
                        question: ABQuestion.find(1),
                        response: ABQuestionResponse.AB.A,
                        user: Player.get(it).user,
                        session: session
                ).save()
                activeIncentivizedTask.addAnswer(Player.get(it), answer)
            }
        then: "These two players should be done, player 3 not"
            activeIncentivizedTask.getDonePlayers().containsAll([Player.get(1), Player.get(2)])
            !activeIncentivizedTask.getDonePlayers().contains(Player.get(3))
    }

    @Unroll("Test Profit selection - #i")
    void "Test Profit selection"(){
        (1..3).each{
            activeIncentivizedTask.addPlayer(Player.get(it))
        }
        activeIncentivizedTask.matchPlayers()
        when: "three players submit answers"
            (1..3).each {
                def answer = new ABQuestionResponse(
                        question: ABQuestion.get(1),
                        response: ABQuestionResponse.AB.A,
                        user: Player.get(it).user,
                        session: session,
                        step: 0
                ).save()
                activeIncentivizedTask.addAnswer(Player.get(it), answer)
            }
        then: "profitAnswers should contain the correct choosen answer for player 1 and 2, all 3 players should have a selected answers"
            def profitAnswers = activeIncentivizedTask.selectProfitAnswers()
            profitAnswers.size() == 2
            profitAnswers[0].activePlayer == Player.get(1)
            profitAnswers[0].passivePlayer == Player.get(2)
            profitAnswers[0].choosenAnswer == ABQuestionResponse.get(1) || profitAnswers[0].choosenAnswer == ABQuestionResponse.get(2)
            profitAnswers[1].choosenAnswer == ABQuestionResponse.get(3)
            profitAnswers[1].activePlayer == Player.get(3)
            profitAnswers[1].passivePlayer == null

        where:
            i << (1..ITERATIONS)

    }


    @Unroll("Random Player and Answer Count - Profit selection - #i (#playerCount/#answerCount)")
    void "Test Profit selection with random player and answer count"(){
        (1..playerCount).each{
            activeIncentivizedTask.addPlayer(Player.get(it))
        }
        activeIncentivizedTask.matchPlayers()
        when: "#playerCount players submit #answerCount answers"
        (1..playerCount).each { playerIndex ->
            (1..answerCount).each{ answerIndex ->
                def answer = new ABQuestionResponse(
                        question: ABQuestion.get(answerIndex),
                        response: ABQuestionResponse.AB.A,
                        user: Player.get(playerIndex).user,
                        session: session,
                        step: 0
                ).save()
                activeIncentivizedTask.addAnswer(Player.get(playerIndex), answer)
            }
        }
        then: "profitAnswers should contain the correct choosen answer for player 1 and 2, all 3 players should have a selected answers"
        def profitAnswers = activeIncentivizedTask.selectProfitAnswers()
        profitAnswers.size() == Math.ceil(playerCount / 2)

        where:
            i << (1..ITERATIONS)
            playerCount = Math.floor(Math.random() * 3) + 1
            answerCount = Math.floor(Math.random() * 3) + 1

    }


}
