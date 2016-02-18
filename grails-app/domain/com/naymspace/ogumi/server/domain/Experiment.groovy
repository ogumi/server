package com.naymspace.ogumi.server.domain
/*
 * Copyright (c) 2015 naymspace software (Dennis Nissen)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class Experiment extends SequenceEntry {

    enum ExperimentStatus{
        NOTACTIVE, PLANNED, RUNNING, ENDED, ERROR
    }

	String name
	Model model
    ExperimentStatus status = ExperimentStatus.NOTACTIVE
	Collection adminInput
	Collection outputfields
	OutputField profit
	Date experimentStart
	Integer timeStepSize = 1
	Integer duration = 30
	Integer clientUpdateInterval = 5
	BigDecimal terminationProbability = 0.0
	Integer terminatedStep
	Boolean showFuture = false
	String diagram = "linegraph"
	BigDecimal y0Min = 0.0
	BigDecimal y0Max = 200.0
	BigDecimal y1Min = 0.0
	BigDecimal y1Max = 100.0
	BigDecimal xPointsVisible = null
	Boolean blockUserInput = false
	Collection userInput
	String link
    ModelOutput modelOutput

	Date lastUpdated = new Date()

	static belongsTo = [model: Model]

    static hasOne = [
            modelOutput: ModelOutput
    ]

	static hasMany   = [adminInput:AdminInputFieldValue, userInput: UserInput, outputfields: OutputFieldValue]

	static constraints = {
        status blank: false
		link  nullable: true
		profit nullable: true
        modelOutput nullable: true
		terminatedStep nullable:true
		terminationProbability min:0.0, max:1.0
		clientUpdateInterval nullable:true, min:1
		xPointsVisible nullable:true, min:0.0, validator: { val, obj ->
			if(val == null){
				return true
			}
			return !obj.showFuture && obj.diagram.equals("linegraph")
		}
		showFuture validator:  {val, obj ->
			if(val == false){
				return true
			}
			return obj.diagram.equals("linegraph")
		}

        //@TODO: check if we need this contraint anyway or if we can find another way of "locking" changes to a running experiment
		/*experimentStart validator: {val, obj ->
			if (obj.id && obj.getPersistentValue('experimentStart')) {
				use(TimeCategory) {
					 if(1.minute.from.now.after(obj.getPersistentValue('experimentStart'))) {
						return false
					}
				}
			}
		}*/
		y0Max nullable:true
		y0Min nullable:true
		y1Max nullable:true
		y1Min nullable:true
		diagram inList: ["linegraph", "cylindergauge"]
	}

    def getShowedOutputValues(){
        outputfields.findAll{
            it.outputField.shouldShow == true
        }
    }

	String toString() {
		return "$label with $model"
	}

	def afterUpdate() {
		lastUpdated = new Date()
		if(link){
			if(isDirty('timeStepSize') || isDirty('duration') || isDirty('adminInput') || isDirty('experimentStart') || isDirty('terminationProbability') || isDirty('xPointsVisible') || isDirty('clientUpdateInterval')) {
				if(!xPointsVisible){
					xPointsVisible = null;
				}
			}
		} else {

		}
	}

	def beforeDelete() {
		Experiment.withNewSession {
			Experiment.withTransaction { status ->
				AdminInputFieldValue.findAllByExperiment(this)*.delete()
				OutputFieldValue.findAllByExperiment(this)*.delete()
				ModelOutput.findAllByExperiment(this)*.delete()
				def sessions = Session.executeQuery('select s from Session s where :experiment in elements(s.sequence)',[experiment: this])
				sessions.each {session ->
					def list = []
					for(SequenceEntry se: session.sequence){
						if(se instanceof Experiment){
							if(se.id != this.id) {
								list << se
							}
						} else {
							list << se
						}
					}
					session.sequence = list
					session.save()
				}
				status.flush()
			}
		}
	}

}
