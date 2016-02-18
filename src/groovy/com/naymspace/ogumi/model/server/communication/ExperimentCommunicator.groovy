package com.naymspace.ogumi.model.server.communication

import com.naymspace.ogumi.model.server.TimedModel
import com.naymspace.ogumi.server.domain.Experiment
import com.naymspace.ogumi.server.domain.Player

/**
 * Created by dennis on 7/29/15.
 */
interface ExperimentCommunicator {

    void sendExperimentEnd(Experiment experiment, TimedModel timedModel)
    void sendExperimentUpdate(Experiment experiment, TimedModel timedModel)
    void sendExperimentError(Experiment experiment, TimedModel timedModel)

}
