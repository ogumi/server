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

import grails.converters.XML

class Session {

	String name
	String password
	Integer maxParticipants
	Date start
	Date end
	boolean active = false
	List sequence
	Collection<Player> players

	static hasMany   = [players:Player, sequence:SequenceEntry]

	static mapping = {
		password column: '`password`'
		sequence cascade: 'save-update'
		players cascade: 'all-delete-orphan'
	}

	static constraints = {
		password        nullable: true
		maxParticipants nullable: true, min: 0
		end validator: { val, obj ->
			return !obj.start.after(val)
		}
    }

    static transients = ['secure']

    boolean getSecure() {
        password? true : false
    }

	String toString() {
		"$name (from $start to $end)"
	}

	def beforeUpdate() {
		if(!isDirty('active')){
			def now = new Date()
			if(!this.start.after(now)) {
				active = true
			}
			if(end.before(now)){
				active = false
			}
		}
		/*this.sequence.each {entry ->
			if(entry.instanceOf(Experiment)){
				entry.session = this
			}
		}*/
	}

	def beforeInsert() {
		def now = new Date()
		if(!this.start.after(now)) {
			this.active = true
		}
		/*this.sequence.each {entry ->
			if(entry.instanceOf(Experiment)){
				entry.session = this
			}
		}*/
	}

	def beforeDelete() {
		Session.withNewSession {
			Session.withTransaction { status ->
				QuestionResponse.findAllBySession(this)*.delete()
				ABQuestionResponse.findAllBySession(this)*.delete()
				IncentivizedTaskUser.findAllBySession(this)*.delete()
				SequenceEntryMoney.findAllBySession(this)*.delete()
				Player.findAllBySession(this)*.delete()
				/*
				//FIXME: The code below does not work.
				def seq = this.sequence.collect() as Set
				for(SequenceEntry se: seq){
					if(se instanceof Experiment){
						def experiment = (Experiment) se
						UserInput.findAllByExperiment(experiment)*.delete()
						experiment.delete()
					}
				}*/
				status.flush()
			}
		}
	}

}
