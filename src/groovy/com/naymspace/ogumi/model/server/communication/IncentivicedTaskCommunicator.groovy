package com.naymspace.ogumi.model.server.communication

import com.naymspace.ogumi.model.server.ActiveIncentivizedTask
import com.naymspace.ogumi.server.domain.ABQuestion
import com.naymspace.ogumi.server.domain.IncentivizedTaskUser
import com.naymspace.ogumi.server.domain.Player
import com.naymspace.ogumi.server.domain.SequenceEntryMoney
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.stereotype.Service

/**
 * Created by dennis on 8/15/15.
 */
@Service
@Log4j
class IncentivicedTaskCommunicator {

    @Autowired
    def MessageSendingOperations brokerMessagingTemplate

    /**
     * Notify all done and connected players about that they're done with this task
     *
     * @param activeIncentivizedTask
     * @return
     */
    def notifyDonePlayersFor(ActiveIncentivizedTask activeIncentivizedTask){
        def donePlayers = activeIncentivizedTask.donePlayers
        def profits = activeIncentivizedTask.selectProfitAnswers()
        donePlayers.each { player ->
            def profitEntry = profits.find{
                it.activePlayer.id == player.id || it.passivePlayer?.id == player.id
            }

            def profit = 0
            switch (profitEntry.choosenFrom){
                case "ACTIVE":
                    profit = profitEntry.activePlayer.id == player.id ?
                                profitEntry.choosenAnswer.question.getProperty("${profitEntry.choosenAnswer.response.toString().toLowerCase()}Active") :
                                profitEntry.choosenAnswer.question.getProperty("${profitEntry.choosenAnswer.response.toString().toLowerCase()}Passive")
                    break
                case "PASSIVE":
                    profit = profitEntry.passivePlayer?.id == player.id ?
                            profitEntry.choosenAnswer.question.getProperty("${profitEntry.choosenAnswer.response.toString().toLowerCase()}Passive") :
                            profitEntry.choosenAnswer.question.getProperty("${profitEntry.choosenAnswer.response.toString().toLowerCase()}Active")
            }

            // @TODO: we seem to have a problem within the database sessions and entity relations
            // this is a workaround to update to correct player entity
            if (!SequenceEntryMoney.findByUserAndStepAndSession(Player.get(player.id).user, Player.get(player.id).step,activeIncentivizedTask.incentivizedTask.session)){
                def j = Player.get(player.id)
                j.money += profit
                j.save()
                new SequenceEntryMoney(
                        session: activeIncentivizedTask.incentivizedTask.session,
                        sequenceentry: activeIncentivizedTask.incentivizedTask,
                        user: Player.get(player.id).user,
                        step: Player.get(player.id).step,
                        money: profit
                ).save()
            }
            //


            log.info("notifying user ${player.user.username} of incentivicedTask with UUID:${activeIncentivizedTask.uuid} that he's done")
            brokerMessagingTemplate.convertAndSendToUser(
                    player.user.username,
                    "/topic/incentivizedTask/control/",
                    new ControlMessage(
                            reason: ControlMessage.REASON.IS_DONE,
                            data: [incentivizedTaskId: activeIncentivizedTask.incentivizedTask.id],
                            message: "player and matched player are done",
                            profit: profit
                    )
            )
        }
    }

    public static class ControlMessage{
        public static enum REASON{
            IS_DONE, IS_ERROR
        }

        def REASON reason
        def message
        def data
        def profit

        def getReason(){
            return reason.name()
        }
    }
}
