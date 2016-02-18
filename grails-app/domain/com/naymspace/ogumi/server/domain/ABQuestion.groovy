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

class ABQuestion {

	IncentivizedTask incentivizedTask
	String question = ""
	String a = "A"
	BigDecimal aActive
	BigDecimal aPassive
	String b = "B"
	BigDecimal bActive
	BigDecimal bPassive
	Date lastUpdated = new Date()

	static belongsTo = [incentivizedTask:IncentivizedTask]

	static constraints = {
		incentivizedTask nullable: true
		question nullable:true
		a nullable:true
		b nullable:true
	}

	static mapping = {
		a defaultValue: "'A'"
		b defaultValue: "'B'"
		question defaultValue: "''"
	}

	def beforeInsert(){
		aActive = Math.round(aActive * 100) / 100
		aPassive = Math.round(aPassive * 100) / 100
		bActive = Math.round(bActive * 100) / 100
		bPassive = Math.round(bPassive * 100) / 100
		lastUpdated = new Date()
		if(!a){
			a = "A"
		}
		if(!b){
			b = "B"
		}
	}

	def beforeUpdate() {
		lastUpdated = new Date()
		if(!a){
			a = "A"
		}
		if(!b){
			b = "B"
		}
	}

	String toString() {
		def str = "($aActive<>$aPassive | $bActive<>$bPassive)"
		if(question){
			str = "$question: "+str
		}
		return str
	}

}
