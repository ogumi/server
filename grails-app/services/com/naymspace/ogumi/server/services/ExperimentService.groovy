package com.naymspace.ogumi.server.services

import com.naymspace.ogumi.model.ModelInput
import com.naymspace.ogumi.model.interfaces.Model
import com.naymspace.ogumi.model.server.ModelLoader
import com.naymspace.ogumi.model.server.TimedModel
import com.naymspace.ogumi.server.domain.AdminInputFieldValue
import com.naymspace.ogumi.server.domain.Experiment
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by dennis on 7/13/15.
 */
class ExperimentService {

    static transactional = false

    def activeModels =[:]

    @Autowired
    def experimentCommunicator

    def planExperiment(Experiment experiment){
        // load model from jar
        // otherwise there could be a "ClassNotFoundException" if the model was uploaded before last server start
        ModelService.loadModel(experiment.model.jar.path)

        experiment.link = createActiveModel(
                experiment,
                experiment.model.claz,
                experiment.duration,
                experiment.experimentStart,
                experiment.terminationProbability,
                experiment.timeStepSize,
                experiment.adminInput,
                experiment.xPointsVisible,
                experiment.clientUpdateInterval
        )

        experiment.status = Experiment.ExperimentStatus.PLANNED
        log.debug("scheduled modelServer (Link: ${experiment.link})")
        experiment
    }

    def endExperiment(experiment, timedModel){
        experimentCommunicator.sendExperimentEnd(experiment, timedModel)
        deleteActiveModel(experiment.link)
        experiment.status = Experiment.ExperimentStatus.ENDED
    }

    //@TODO: implement !!
    def updateActiveModel(String id, Integer duration, Date experimentStart, BigDecimal terminationPropability, Integer timeStepSize, Collection adminInput, BigDecimal xPointsVisible, Integer clientUpdateInterval){
        //throw new Exception ("not yet implemented")
    }

    def deleteActiveModel(String id){
        def activeModel = getActiveModel(id)
        if (activeModel.isRunning()){
            activeModel.getTimer().cancel()
        }
        activeModels.remove(id)
    }

    def TimedModel getActiveModel(String id) throws Exception{
        def model = activeModels.get(id)
        if (model) return model
        throw new Exception("could not find active model with id "+id)
    }

    protected def createActiveModel(Experiment experiment, String model, Integer duration, Date experimentStart, BigDecimal terminationProbability, Integer timeStepSize, Collection<AdminInputFieldValue> adminInput, BigDecimal xPointsVisible, Integer clientUpdateInterval){
        Class cls = Class.forName(model, true, ModelLoader.classLoader)
        def modl = (Model) cls.newInstance()
        createActiveModel(experiment, modl, duration, experimentStart, terminationProbability, timeStepSize, adminInput, xPointsVisible, clientUpdateInterval)
    }

    protected def createActiveModel(Experiment experiment, Model model, Integer duration, Date experimentStart, BigDecimal terminationProbability, Integer timeStepSize, Collection<AdminInputFieldValue> adminInput, BigDecimal xPointsVisible, Integer clientUpdateInterval){
        def id = model.getUuid().toString();
        def mi = new ModelInput();
        adminInput.each{
            mi.addFieldAndValue(it.adminInputField.name, it.value)
        }

        mi.setT_step(timeStepSize);

        boolean canSet = model.checkAdminInput(mi);
        if(canSet){
            model.setAdminInput(mi);
        } else {
            throw new Exception("Wrong values for admin input.");
        }
        model.setTMax(duration*60+1.0); //set tmax = duration in seconds + 1

        int durMSec     = duration*60000;
        Date end        = new Date(experimentStart.getTime() + durMSec);

        double interval = 1000 * timeStepSize; //one second * timestepsize

        TimedModel timedModel = new TimedModel(this, experiment, model, experimentStart, end, (long) interval, terminationProbability, xPointsVisible, clientUpdateInterval);
        activeModels.put(id, timedModel);
        experiment.link = id;
        return id;
    }
}
