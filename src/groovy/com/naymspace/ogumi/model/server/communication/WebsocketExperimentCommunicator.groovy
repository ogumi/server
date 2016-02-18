package com.naymspace.ogumi.model.server.communication

import com.naymspace.ogumi.model.ModelOutput
import com.naymspace.ogumi.model.interfaces.Model
import com.naymspace.ogumi.model.server.TimedModel
import com.naymspace.ogumi.server.domain.Experiment
import com.naymspace.ogumi.server.domain.Player
import com.naymspace.ogumi.server.domain.Role
import com.naymspace.ogumi.server.domain.SequenceEntryMoney
import com.naymspace.ogumi.server.domain.User
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.stereotype.Service

/**
 * Created by dennis on 7/29/15.
 */
@Service
@Log4j
class WebsocketExperimentCommunicator implements ExperimentCommunicator {

    @Autowired
    def MessageSendingOperations brokerMessagingTemplate

    /**
     * Notify all players about the experiments end
     * @param experiment
     * @param timedModel
     */
    @Override
    void sendExperimentEnd(Experiment experiment, TimedModel timedModel) {
        log.info("session ${experiment.link} stopped, sending players the update")

        String data = timedModel.lastModelOutput.toJson().toString() //@TODO: get rid of this json stuff
        experiment.setModelOutput(new com.naymspace.ogumi.server.domain.ModelOutput(
            step: timedModel.currStep,
            data: data,
            experiment: experiment
        ))

        experiment.modelOutput.save(flush: true)

        allPlayers(experiment,timedModel){ Player player, username ->
            def usersProfitFromExperiment = timedModel.getLastOutput().cumulated?.find{
                it.key.contains("player[${player.id}]") && it.key.contains(experiment.profit.name)
            }?.value
            player.money += usersProfitFromExperiment
            new SequenceEntryMoney(
                session: timedModel.session,
                sequenceentry: experiment,
                user: player.user,
                step: player.step,
                money: usersProfitFromExperiment
            ).save()
            player.save()
            brokerMessagingTemplate.convertAndSendToUser(
                username,
                "/topic/experiment/control/"+experiment.link,
                new ControlMessage(
                    reason: ControlMessage.REASON.EXPERIMENT_ENDED,
                    message: "Experiment ended",
                    profit: usersProfitFromExperiment
                )
            )

        }
    }

    /**
     * Send updates from the experiment model (model data) to the clients
     * @param experiment
     * @param timedModel
     */
    @Override
    void sendExperimentUpdate(Experiment experiment, TimedModel timedModel) {
        Model model = timedModel.getModel()
        def output = timedModel.getLastOutput()
        int curStep = timedModel.getCurrStep()
        double time = curStep * timedModel.model.getTStep()
        Integer xPoints = timedModel.getXPoints()?.toInteger()
        if (experiment.session.players){
            log.debug("sending experiment updates to users ${experiment.session.players*.user.username}")
            def adminModelOutput = generateLegacyModelOutput(output)
            def globalModelOutput = xPoints ? extractDataWindow(adminModelOutput, curStep, xPoints ) : adminModelOutput


            brokerMessagingTemplate.convertAndSend(
                "/queue/experiment/updates/admin/" + experiment.link,
                new ExperimentUpdate(
                    model: adminModelOutput,
                    step: curStep,
                    time: time
                )
            )


            // send message to all users
            allPlayers(experiment, timedModel){ player, username ->

                def modelUpdate = [data: legacyPlayerDataFilter(globalModelOutput.data, player), cumulatedData: null, label: globalModelOutput.label]

                //Filter selected outputfields from experiment output (may contain player[x]. prefix)
                modelUpdate.cumulatedData = globalModelOutput.cumulatedData.findAll {
                    experiment.outputfields.collect{ i -> i.outputField.name }
                            .contains(it.key.replaceFirst(/^player\[[\d]*\]./,"" ))
                }.findAll{ //filter player specific entries, that are not for this player
                    !it.key.contains("player[") || it.key.contains("player[${player.id}]")
                }

                brokerMessagingTemplate.convertAndSendToUser(
                        username, "/queue/experiment/updates/"+experiment.link,
                        new ExperimentUpdate(
                                model: modelUpdate,
                                step: curStep,
                                time: time
                        )
                )
            }
        }
        else {
            log.info("session ${experiment.session} has no players, don't sending any updates")
        }
    }

    /**
     * Notify all clients, that an unresolvable error occurred within the running experiment
     * @param experiment
     * @param timedModel
     */
    @Override
    void sendExperimentError(Experiment experiment, TimedModel timedModel) {
        log.info("session ${experiment.link} had an error, sending players the update")
        allUsersnames(experiment, timedModel){
            brokerMessagingTemplate.convertAndSendToUser(
                    it,
                    "/topic/experiment/control/"+experiment.link,
                    new ControlMessage(
                            reason: ControlMessage.REASON.EXPERIMENT_ERROR,
                            message: "Experiment had an unknown error" //@TODO: send more detailed error information
                    )
            )
        }
    }

    /**
     * convenience methods to apply a closure with every sessions user username
     */
    def allUsersnames = { Experiment experiment, TimedModel tm, Closure cl ->
        if (experiment.session.players) {
            experiment.session.players*.user.each {
                cl(it.username)
            }
        }
    }

    def allPlayers = { Experiment experiment, TimedModel tm, Closure cl ->
        if (experiment.session.players){
            experiment.session.players.each{
                cl (it, it.user.username)
            }
        }
    }


    /**
     * needed to convert the legacy output of ogumi model 1.2
     * @TODO: this should be done in the ModelOutput class of Ogumi Model
     */
    protected def generateLegacyModelOutput(ModelOutput modelOutput){
        def graphIndex = 0
        def label = []
        def data = [:]

        data["time"] = modelOutput.time

        // Fill label array
        label.push(modelOutput.yNames[graphIndex])
        for (int v=0 ; v < modelOutput.otherNames.size() ; v++) {
            label.push(modelOutput.otherNames.getAt(v))
        }

        //fill data array
        data[(modelOutput.yNames[graphIndex])] = flattenYs(modelOutput.ys)[0]

        //Add other values
        for (int v=0 ; v < modelOutput.otherNames.size() ; v++){
            data[modelOutput.otherNames.getAt(v)] = modelOutput.other.get(v)
        }
        [
            data: data,
            label: label,
            cumulatedData: modelOutput.cumulated
        ]
    }

    def extractDataWindow(LinkedHashMap<String,LinkedHashMap> output,currentStep, windowSize){
        def windowedData = [
                data: new LinkedHashMap<String,LinkedHashMap>(),
                label: output.label,
                cumulatedData: output.cumulatedData
        ]
        output.data.each{
            windowedData.data.putAt(it.key,new Double[windowSize])
            for (def i=Math.max(currentStep-windowSize,0); i < currentStep && i < it.value.length ; i++){
                windowedData.data.getAt(it.key)[i-currentStep] = it.value.getAt(i);
            }
        }
        windowedData
    }

    /**
     * Filter for legacy data
     * Filters every data that is not for given player
     * @param data
     * @param player
     * @return
     */
    def legacyPlayerDataFilter(LinkedHashMap<String,Double> data, Player player){
        def removeKeys = []
        data.keySet().each {
            if (it.contains("player[")){
                if (!it.contains("player[${player.id}]")){
                    removeKeys.add(it)
                }
            }
        }
        data.findAll{!removeKeys.contains(it)}
    }

    // legacy
    // converts ys[time]sch;n[graph] to ys[graph][time]
    def flattenYs(double[][] ys){
        double[][] flat = new double[ys[0].length][ys.length]
        for (int i=0; i < ys.length; i++){
            for (int v=0; v < ys[i].length; v++){
                flat[v][i] = ys[i][v]
            }
        }
        flat
    }

    public static class ControlMessage{
        public static enum REASON{
            EXPERIMENT_ENDED, EXPERIMENT_ERROR
        }

        def REASON reason
        def message
        def profit

        def getReason(){
            return reason.name()
        }
    }

    public static class ExperimentUpdate{
        def model
        def step
        def time
    }
}
