package com.naymspace.ogumi.model.server

import com.naymspace.ogumi.server.domain.ABQuestionResponse
import com.naymspace.ogumi.server.domain.IncentivizedTask
import com.naymspace.ogumi.server.domain.IncentivizedTaskUser
import com.naymspace.ogumi.server.domain.Player
import groovy.util.logging.Log4j
import org.springframework.stereotype.Service

/**
 * Created by dennis on 1/25/16.
 */
@Service
@Log4j
class IncentivicedTaskBroker {
    // keep track of all running incentivized tasks
    static LinkedHashMap<String, ActiveIncentivizedTask> activeIncentivizedTasks = new LinkedHashMap<>()

    /**
     * return the entry with the corresponding uuid from
     * the list of active incentivized tasks or null if its not found
     * @param uuid
     * @return ActiveIncentivizedTask | NULL
     */
    def ActiveIncentivizedTask taskIsActive(String uuid) {
        activeIncentivizedTasks.get(uuid)
    }

    /**
     * Ensures that for given incentivizedTask is an ActiveIncentivizesTask running
     * by either returning an active one or creating a new active one and returning it.
     * @param incentivicedTask
     * @return ActiveIncentivicedTask
     */
    def ActiveIncentivizedTask activeTask(IncentivizedTask incentivicedTask){
        taskIsActive(ActiveIncentivizedTask.uuid(incentivicedTask)) ?: startTask(incentivicedTask)
    }

    /**
     * Creates an ActiveIncentivizedTask for a given IncentivizedTask and
     * adds all players from the IncentivizedTask session to the list of players.
     * Calls matching function of ActiveIncentivicedTask.
     * @param incentivizedTask
     * @return ActiveIncentivizedTask
     */
    def startTask(IncentivizedTask incentivizedTask){
        if (taskIsActive(ActiveIncentivizedTask.uuid(incentivizedTask))){
            log.error("incentiviced task ${incentivizedTask.name}(ID:${incentivizedTask.id}) from session ${incentivizedTask.session.name}(ID: ${incentivizedTask.session.id}) is already active")
            throw new Exception("this incentivizedTask is already active")
        }
        log.info("activating incentivized task ${incentivizedTask.name}(ID:${incentivizedTask.id}) from session ${incentivizedTask.session.name}(ID: ${incentivizedTask.session.id})")

        def activatedTask = new ActiveIncentivizedTask(incentivizedTask: incentivizedTask)

        activeIncentivizedTasks.put(activatedTask.getUuid(), activatedTask)
        incentivizedTask.session.players.each {
            activatedTask.addPlayer(it)
        }

        activatedTask.matchPlayers()
        activatedTask
    }

}

@Service
@Log4j
class ActiveIncentivizedTask {
    public IncentivizedTask incentivizedTask

    public boolean matched = false

    // list containing all player, unmatched
    protected List<Player> players = new LinkedList<>()

    // list containing players, that subscribed this task
    protected List<Player> subscribedPlayers = new LinkedList()

    // hashmap containing player matching
    // player that is key is an active player, its value is its assigned passive player
    protected LinkedHashMap<Player, Player> matchedPlayers = new LinkedHashMap<>()

    // hashmap containing answers for player
    protected LinkedHashMap<Player, List<ABQuestionResponse>> answers = new LinkedHashMap<>()


    protected choosenAnswers = []

    /**
     * adds a player to the list of players
     * @param player
     * @return
     */
    def addPlayer(Player player){
        if (!players.find{it.id == player.id})
            players.add(player)
        else log.error("player ${player.user.username} was already in list of players for this active task")
    }

    /**
     * add player that subscribed (should have been actively subscribed) this incentivizedTask
     * @param player
     * @return
     */
    def subscribeForPlayer(Player player){
        subscribedPlayers.add(player)
    }

    /**
     * do player matching,
     * will not do matching if players were matched before
     * @return
     */
    def matchPlayers(){
        if (matched) {
            // check if we have additional subscribed players (players that came when this task was already active)
            if (subscribedPlayers.size() > players.size()){
                def unmatched = subscribedPlayers.findAll { subscribedPlayer -> !players.find{ it.id == subscribedPlayer.id}}
                for (def i = 0; i < unmatched.size(); i += 2) {
                    def p1 = unmatched.get(i);
                    def p2 = null
                    // catch last loop cycle if we have an uneven count of players
                    if (!(unmatched.size() % 2 != 0 && i == unmatched.size() - 1)) {
                        p2 = unmatched.get(i + 1)
                    }
                    log.info("matching ${p1.user.username}(ID:${p1.id}) with ${p2?.user?.username}")
                    players.add(p1)
                    if (p2) players.add(p2)

                    matchedPlayers.put(p1, p2)
                }

            }
            return matched
        }
        matchedPlayers.clear()
        for (def i = 0; i <players.size(); i += 2){
            def p1 = players.get(i);
            def p2 = null
            // catch last loop cycle if we have an uneven count of players
            if (!(players.size() % 2 != 0 && i == players.size()-1)){
                p2 = players.get(i+1)
            }
            log.info("matching ${p1.user.username}(ID:${p1.id}) with ${p2?.user?.username}")
            matchedPlayers.put(p1, p2)
        }
        matched = true
    }

    /**
     * adds an answer for player
     * @param player
     * @param answer
     * @return
     */
    def addAnswer(Player player, ABQuestionResponse answer){
        if (!isReady()) throw new Exception("ActiveIncentivizedTask ${incentivizedTask.name}(ID:${incentivizedTask.id}) is not ready for Answers")
        matchPlayers() // in case we have new players since startup of this incentivized task
        if (!answers.find{ it.key.id == player.id}){
            answers.put(player, new LinkedList<ABQuestionResponse>())
        }
        answers.find{it.key.id == player.id}.value.add(answer)
    }

    /**
     * get all players that answered all questions and have
     * either a fellow that answered all his questions or a null fellow, which indicates
     * that the player is not matched.
     * @return List<Player>
     */
    def getDonePlayers(){
        matchedPlayers.findAll {
            // find player matchings where both players posted answers or matched player is null (not matched)
            answers.find{ i -> i.key.id == it.key.id } && (answers.find {i -> i.key.id == it.value?.id} || it.value == null)
        }.collect {
            // return list of both matched players
            [it.key, it.value]
        }.flatten().findAll{it != null} //flatten lists and filter null players
    }

    /**
     * check if all players are done with this task
     * @return boolean
     */
    def getAllDone(){
        donePlayers.size() == players.size()
    }

    def playerIsDone(Player p){
        donePlayers.find{ it.id == p.id } == null ? false : true
    }

    def selectProfitAnswers(){
        matchedPlayers.each{
            if (playerIsDone(it.key) && !profitChoosenForPlayer(it.key)){
                def matchedPlayersAnswers = answers.collect{ playerAnswer ->
                    // Collect answers from active and passive players
                    if (playerAnswer.key.id == it.key.id) [
                            activePlayer: playerAnswer.key,
                            activeAnswers: playerAnswer.value,
                            passiveAnswers: answers.find{ answer -> answer.key.id == it.value?.id}?.value,
                            passivePlayer: it.value
                    ]
                }.findAll{it != null}

                Random rand = new Random()
                def answerCount = 0
                answerCount += matchedPlayersAnswers.first().activeAnswers.size()
                if (matchedPlayersAnswers.first().passiveAnswers)
                    answerCount += matchedPlayersAnswers.first().passiveAnswers?.size()

                def choosenAnswerIndex = answerCount == 1 ? 0 : rand.nextInt(answerCount)

                def choosen
                if (choosenAnswerIndex >= matchedPlayersAnswers.first().activeAnswers.size()){
                    choosen = [
                            choosenFrom: "PASSIVE",
                            choosenAnswer: matchedPlayersAnswers.first().passiveAnswers.get(choosenAnswerIndex - (matchedPlayersAnswers.first().activeAnswers.size())),
                            activePlayer: matchedPlayersAnswers.first().activePlayer,
                            passivePlayer: matchedPlayersAnswers.first().passivePlayer
                    ]

                }
                else {
                    choosen = [
                            choosenFrom: "ACTIVE",
                            choosenAnswer: matchedPlayersAnswers.first().activeAnswers.get(choosenAnswerIndex),
                            activePlayer: matchedPlayersAnswers.first().activePlayer,
                            passivePlayer: matchedPlayersAnswers.first().passivePlayer
                    ]
                }
                choosenAnswers.add(choosen)
                // persist this choosen answer
                def g = new IncentivizedTaskUser(
                        session: incentivizedTask.session,
                        incentivizedTask: incentivizedTask.id,
                        step: incentivizedTask.session.sequence.indexOf(incentivizedTask),
                        activeUser: choosen.activePlayer.user,
                        passiveUser: choosen.passivePlayer?.user,
                        question: choosen.choosenAnswer.question.id,
                        activeSet: choosen.choosenFrom == "ACTIVE" ? true : false,
                        date: new Date()

                )
                g.save(flush: true)
            }
        }
        choosenAnswers
    }

    def profitChoosenForPlayer(Player p){
        choosenAnswers.findAll{
            it.activePlayer.id == p.id || it.passivePlayer?.id == p.id
        }.size() > 0
    }

    /**
     * get player matched with given player
     * @param p
     * @return
     */
    def matchedPlayer(Player p){
        def matchedPassive = matchedPlayers.find { it.key.id == p.id }
        def matchedActive = matchedPlayers.find { it.value?.id == p.id }
        matchedPassive ? [status: "PASSIVE", player: matchedPassive.value] : [status: "ACTIVE", player: matchedActive.key]
    }

    def isMatched(Player p){
        matchedPlayers.find{ it.key.id == p.id || it.value?.id == p.id}
    }

    def isReady() {
        matched
    }

    /* auxilary functions to get uuid for session and task */
    def getUuid(){
        "${incentivizedTask.session.id}_${incentivizedTask.id}".encodeAsMD5()
    }

    static def uuid(incentivizedTask){
        "${incentivizedTask.session.id}_${incentivizedTask.id}".encodeAsMD5()
    }

}
