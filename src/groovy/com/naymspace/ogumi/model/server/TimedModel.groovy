package com.naymspace.ogumi.model.server

import com.naymspace.ogumi.model.ModelOutput

import com.naymspace.ogumi.model.interfaces.Model
import com.naymspace.ogumi.server.domain.Experiment
import com.naymspace.ogumi.server.domain.Session
import com.naymspace.ogumi.server.services.ExperimentService
import grails.util.Holders
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NumberIsTooSmallException

import org.apache.commons.logging.LogFactory;

import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimedModel {
    private static final log = LogFactory.getLog(this)

    protected Model model;
    protected Date start;
    protected Date stop;
    protected int currStep;
    protected BigDecimal xPoints;
    protected Long interval;
    protected Timer timer;
    protected BigDecimal terminationProbalility;
    protected ModelOutput lastModelOutput;
    protected boolean isFirstRun;
    protected int clientUpdateInterval;
    protected ExperimentService experimentService
    def Experiment experiment
    def Session session


    public stepListeners = [:]

    public TimedModel(ExperimentService experimentService, Experiment experiment,  Model model, Date start, Date stop, Long interval, BigDecimal terminationProbalility, BigDecimal xPoints, int clientUpdateInterval) {
        this.experimentService = experimentService
        this.experiment = experiment
        this.session = experiment.session
        this.model      = model;
        this.start      = start;
        this.stop       = stop;
        this.terminationProbalility = terminationProbalility;
        this.currStep   = 0;
        this.xPoints    = xPoints;
        this.isFirstRun = true;
        this.clientUpdateInterval  = clientUpdateInterval;
        this.timer      = new Timer();
        timer.scheduleAtFixedRate(new UpdateTime(this), start, interval);
    }



    public int getCurrStep() {
        return currStep;
    }

    public Model getModel() {
        return model;
    }

    public Date getStart() {
        return start;
    }

    public Date getStop() {
        return stop;
    }

    public ModelOutput getLastOutput() {
        return this.lastModelOutput;
    }

    public BigDecimal getXPoints() {
        return this.xPoints;
    }

    public void setCurrStep(int currStep) {
        this.currStep = currStep;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public void setStop(Date stop) {
        this.stop = stop;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    public boolean isRunning() {
        Date now = Calendar.getInstance().getTime();
        if(this.start.before(now) && this.stop.after(now)){
            return true;
        }
        return false;
    }

    public boolean isStarted() {
        Date now = Calendar.getInstance().getTime();
        if(this.start.before(now)){
            return true;
        }
        return false;
    }

    public boolean isStopped() {
        return isStarted() && !isRunning();
    }

    public void setXPoints(BigDecimal xpoints) {
        this.xPoints = xpoints;
    }

    class UpdateTime extends TimerTask {

        TimedModel timedModel;

        def grailsApplication
        def persistenceInterceptor

        public UpdateTime(TimedModel timedModel) {
            this.timedModel = timedModel;
            initPersistence()
        }

        protected def initPersistence(){
            grailsApplication = Holders.grailsApplication
            persistenceInterceptor = grailsApplication.mainContext.getBean("persistenceInterceptor");
        }

        @Override
        public void run() {
            persistenceInterceptor.init()
            def experiment = Experiment.get(timedModel.experiment.id)
            if (!experiment) throw new Exception("Experiment is gone")
            //update experiment from database
            //experiment.refresh()

            if(timedModel.isFirstRun){
                // Initialize Model
                timedModel.model.integrateIntermediate();
                def sessionPlayers = experiment.session.players
                def players = sessionPlayers?.size() > 0 ? sessionPlayers*.id.toArray() as long[] : new long[0]
                Logger.getLogger(TimedModel.class.getName()).info("initiating ${sessionPlayers.size()} players for experiment ${experiment.name}(ID:${experiment.id})")
                model.initUserInput(players);
                experiment.status = Experiment.ExperimentStatus.RUNNING
                experiment.save()

                timedModel.isFirstRun = false;
            }
            boolean terminate = false;
            Date now = Calendar.getInstance().getTime();
            try {
                timedModel.lastModelOutput = timedModel.model.calculateOutput(timedModel.currStep * timedModel.model.getTStep())
                Integer upperBound = timedModel.terminationProbalility.multiply(new BigDecimal(100)).setScale(0, RoundingMode.FLOOR).intValue();
                terminate = new Random().nextInt(100) < upperBound;
            } catch (NumberIsTooSmallException | DimensionMismatchException | MaxCountExceededException | NoBracketingException | NumberFormatException ex){
                Logger.getLogger(TimedModel.class.getName()).log(Level.SEVERE, null, ex);
                timedModel.experimentService.sendExperimentError(timedModel, ex.getMessage())
                terminate = true;
            }
            if(terminate){
                experiment.terminatedStep = timedModel.currStep
            }
            if(now.after(timedModel.stop) || terminate){
                log.info("timedModel should terminate - canceling the timer, sending ExperimentEnd and deleting active model")
                timedModel.timer.cancel();
                timedModel.experimentService.endExperiment(experiment, this.timedModel);

            } else {
                timedModel.currStep++;
                if (timedModel.currStep % timedModel.clientUpdateInterval == 0)
                    timedModel.experimentService.experimentCommunicator.sendExperimentUpdate(experiment, this.timedModel)
            }

            persistenceInterceptor.flush()
            persistenceInterceptor.destroy()
        }
    }
}
