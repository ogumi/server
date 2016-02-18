package com.naymspace.ogumi.server.controller

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

import com.naymspace.ogumi.server.domain.*
import com.opencsv.CSVWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import groovy.json.*

class ResultsController {

	def session() {
		if(params?.id != null) {
			def session = Session.get(params.id)
			if(session != null) {
				def now = new Date()
				if(!session.active && session.end.before(now)){
					Set answers = []
					def experiments = []
					def expresults  = []
					session.sequence.each() { step ->
						if ((step instanceof Questionnaire) || (step instanceof IncentivizedTask)) {
							step.questions?.each () { question ->
								if(question instanceof Question)
									answers.addAll(QuestionResponse.findAllByQuestionAndSession(question, session))
								else if (question instanceof ABQuestion)
									answers.addAll(ABQuestionResponse.findAllByQuestionAndSession(question, session))
							}
						} else if(step instanceof Experiment){
							experiments.addAll(UserInput.findAllByExperiment(step).sort{it.date})
							expresults.addAll(ModelOutput.findAllByExperiment(step))
						}
					}
					def exportLink  = servletContext.contextPath + '/results/export/' + params.id
					render(template: "results", model: [session: session, data: answers, experimentData: experiments, experimentResults: expresults, exportLink: exportLink])
				} else {
					render(status:403, contentType: 'text/plain'){
						"This session is still running."
					}
				}
			} else {
				render(status:404, contentType: 'text/plain'){
					"Cannot find a session with id ${params.id}"
				}
			}
		} else {
			render(status:400, contentType: 'text/plain'){
				"You must specify a session."
			}
		}
	}

	def export() {
		def filename = "session-$params.id-export.zip"
		response.setContentType(grailsApplication.config.grails.mime.types['zip'])
		response.setHeader("Content-Disposition", "attachment; filename=$filename")
		ZipOutputStream zip = new ZipOutputStream(response.outputStream)
		def session = Session.get(params.id)
		byte[] buf = new byte[1024];
		//Session CSV
		ByteArrayOutputStream output = new ByteArrayOutputStream()
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(output))
		def heads = ["start", "end", "public", "maxParticipants"]
		writer.writeNext(heads as String[]);
		def publicSession = session.password
		if(publicSession == null){
			publicSession = true;
		} else {
			publicSession = false;
		}
		def entries = [session.start, session.end, publicSession, session.maxParticipants]
		writer.writeNext(entries as String[]);
		writer.close();
		byte[] bytes = output.toByteArray()
		def fileEntry = new ZipEntry('session.csv');
		zip.putNextEntry(fileEntry);
		zip.write(bytes);
		//Player CSV
		output = new ByteArrayOutputStream()
		writer = new CSVWriter(new OutputStreamWriter(output))
		heads = ["username", "email"]
		writer.writeNext(heads as String[]);
		session.players.each {player ->
			entries = [player.user.username, player.user.email]
			writer.writeNext(entries as String[]);
		}
		writer.close();
		bytes = output.toByteArray()
		fileEntry = new ZipEntry('player.csv');
		zip.putNextEntry(fileEntry);
		zip.write(bytes);
		//Money Overview
		output = new ByteArrayOutputStream()
		writer = new CSVWriter(new OutputStreamWriter(output))
		heads = ["username", "money"]
		writer.writeNext(heads as String[]);
		session.players.each {player ->
			def sum = Math.round(player.money * 100) / 100
			entries = [player.user.username, sum]
			writer.writeNext(entries as String[]);
		}
		writer.close();
		bytes = output.toByteArray()
		fileEntry = new ZipEntry('money-overview.csv');
		zip.putNextEntry(fileEntry);
		zip.write(bytes);
		//'Bill' for each player
		session.players.each {player ->
			output = new ByteArrayOutputStream()
			writer = new CSVWriter(new OutputStreamWriter(output))
			heads = ["step", "info", "amount"]
			writer.writeNext(heads as String[]);
			def items = SequenceEntryMoney.findAllByUserAndSession(player.user, session, [sort: "step", order: "asc"])
			items.each{ item ->
				def rounded = Math.round(item.money * 100) / 100
				entries = [item.step, item.info, rounded]
				writer.writeNext(entries as String[]);
			}
			def sum = Math.round(player.money * 100) / 100
			entries = ["", "Sum", sum]
			writer.writeNext(entries as String[]);
			writer.close();
			bytes = output.toByteArray()
			fileEntry = new ZipEntry('bill-'+player.user.username+'.csv');
			zip.putNextEntry(fileEntry);
			zip.write(bytes);
		}
		//Sequence CSV
		output = new ByteArrayOutputStream()
		writer = new CSVWriter(new OutputStreamWriter(output))
		heads = ["step", "name", "type"]
		writer.writeNext(heads as String[]);
		session.sequence.eachWithIndex {se, index ->
			def type = se.getClass().getName()
			entries = [index, se.name, type]
			writer.writeNext(entries as String[]);
		}
		writer.close();
		bytes = output.toByteArray()
		fileEntry = new ZipEntry('sequence.csv');
		zip.putNextEntry(fileEntry);
		zip.write(bytes);
		//Sequence Entries
		session.sequence.eachWithIndex {se, index ->
			output = new ByteArrayOutputStream()
			writer = new CSVWriter(new OutputStreamWriter(output))
			if(se.class.equals(com.naymspace.ogumi.server.domain.Questionnaire)){
				se = Questionnaire.load(se.id)
				heads = ["username"]
				entries = []
				se.questions.each {question ->
					heads << question.question
				}
				writer.writeNext(heads as String[]);
				session.players.each{ player ->
					def entry = [player.user.username]
					def answers = QuestionResponse.findAllByUserAndSessionAndStep(player.user, session, index)
					se.questions.each {question ->
						def a = null
						answers.each{answer ->
							if(answer.question.equals(question)){
								a = answer.response
							}
						}
						entry << a
					}
					entries << entry
				}
				entries.each {entry ->
					writer.writeNext(entry as String[]);
				}
			} else if(se.class.equals(com.naymspace.ogumi.server.domain.IncentivizedTask)){
				se = IncentivizedTask.load(se.id)
				heads = ["username"]
				entries = []
				se.questions.each {question ->
					heads << question.id
				}
				writer.writeNext(heads as String[]);
				session.players.each{ player ->
					def entry = [player.user.username]
					def answers = ABQuestionResponse.findAllByUserAndSessionAndStep(player.user, session, index)
					se.questions.each {question ->
						def a = null
						answers.each{answer ->
							if(answer.question.equals(question)){
								a = answer.response
							}
						}
						entry << a
					}
					entries << entry
				}
				entries.each {entry ->
					writer.writeNext(entry as String[]);
				}
				//Write user mapping
				def output2 = new ByteArrayOutputStream()
				def writer2 = new CSVWriter(new OutputStreamWriter(output2))
				heads = ["user_1", "user_2", "answer_set", "chosen_question", "random_response"]
				entries =
				writer2.writeNext(heads as String[]);
				def mapping = IncentivizedTaskUser.findAllByIncentivizedTaskAndSessionAndStep(se, session, index)
				mapping.each {data ->
					def passive = data.passiveUser?.username
					def randomResponse = ""
					if(passive == null){
						passive = ""
						randomResponse = data.randomResponse
					}
					writer2.writeNext([data.activeUser.username, passive, data.activeSet, data.question, randomResponse] as String[]);
				}
				writer2.close();
				bytes = output2.toByteArray()
				fileEntry = new ZipEntry('sequence-'+index+'-usermapping.csv');
				zip.putNextEntry(fileEntry);
				zip.write(bytes)
				//Write incentivizedTask Info
				output2 = new ByteArrayOutputStream()
				writer2 = new CSVWriter(new OutputStreamWriter(output2))
				heads = ["a", "a_active", "a_passive", "b", "b_active", "b_passive",  "id", "question"]
				entries =
				writer2.writeNext(heads as String[]);
				se.questions.each {question ->
					writer2.writeNext([question.a, question.aActive, question.aPassive, question.b, question.bActive, question.bPassive, question.id, question.question] as String[]);
				}
				writer2.close();
				bytes = output2.toByteArray()
				fileEntry = new ZipEntry('sequence-'+index+'-questions.csv');
				zip.putNextEntry(fileEntry);
				zip.write(bytes)
			} else if(se.class.equals(com.naymspace.ogumi.server.domain.InformationStep)){
				se = InformationStep.load(se.id)
				heads = ["information"]
				entries = [se.information]
				se.media.eachWithIndex {media, i ->
					heads << "media-$i-name"
					entries << media.name
					FileInputStream inp = new FileInputStream(media.path);
					zip.putNextEntry(new ZipEntry("media-$i-$index "+media.name));
					int len;
					while((len = inp.read(buf)) > 0) {
						zip.write(buf, 0, len);
					}
					zip.closeEntry();
					inp.close();
				}
				writer.writeNext(heads as String[]);
				writer.writeNext(entries as String[]);
			} else if(se.class.equals(com.naymspace.ogumi.server.domain.Experiment)){
				se = Experiment.load(se.id)
				heads = ["name", "modelname", "modelclass"]
				entries = [se.name, se.model.name, se.model.claz]
				se.model.adminInput.each {field ->
					heads << field.name
					se.adminInput.each{ai ->
						if(ai.adminInputField.equals(field)){
							entries << ai.value
						}
					}
				}
				heads.addAll(["timeStepSize", "start", "duration", "terminationProbability", "terminatedStep", "clientUpdateInterval", "diagram", "showFuture", "xPointsVisible", "y0Min", "y0Max", "y1Min", "y1Max", "blockUserInput"])
				writer.writeNext(heads as String[]);
				entries.addAll([se.timeStepSize, se.experimentStart, se.duration, se.terminationProbability, se.terminatedStep, se.clientUpdateInterval, se.diagram, se.showFuture, se.xPointsVisible, se.y0Min, se.y0Max, se.y1Min, se.y1Max, se.blockUserInput])
				writer.writeNext(entries as String[]);
				//Write Model Output Graph Data
				def mo = ModelOutput.findAllByExperiment(se)
				def moJson
				mo.each {data ->
					moJson = new JsonSlurper().parseText(data.data)
				}
				def output2 = new ByteArrayOutputStream()
				def writer2 = new CSVWriter(new OutputStreamWriter(output2))
				heads = ["time"]
				moJson.label.each{ label ->
					heads << label
				}
				writer2.writeNext(heads as String[]);
				moJson.data.each {step ->
					def data = [step.time]
					moJson.label.eachWithIndex {label, i ->
						def i1 = i + 1
						data[i1] = step[''+i]
					}
					writer2.writeNext(data as String[]);
				}
				writer2.close();
				bytes = output2.toByteArray()
				fileEntry = new ZipEntry('sequence-'+index+'-modeloutput-graphs.csv');
				zip.putNextEntry(fileEntry);
				zip.write(bytes)
				//Write Model Output Cumulated Data
				output2 = new ByteArrayOutputStream()
				writer2 = new CSVWriter(new OutputStreamWriter(output2))
				heads = ["field", "value"]
				writer2.writeNext(heads as String[]);
				moJson.cumulatedData.each {entry ->
					def obj = entry.entrySet() as List
					obj.each{ ent ->
						writer2.writeNext([ent.key, ent.value] as String[]);
					}
				}
				writer2.close();
				bytes = output2.toByteArray()
				fileEntry = new ZipEntry('sequence-'+index+'-modeloutput-cumulated.csv');
				zip.putNextEntry(fileEntry);
				zip.write(bytes)
				//Write User Input
				output2 = new ByteArrayOutputStream()
				writer2 = new CSVWriter(new OutputStreamWriter(output2))
				heads = ["username", "step", "field", "value", "date"]
				writer2.writeNext(heads as String[]);
				session.players.each{ player ->
					def input = UserInput.findAllByExperimentAndPlayer(se, player.user)
					input.each {data ->
						def variables = data.effort
						variables.each{ variable ->
							writer2.writeNext([player.user.username, variable.step, variable.field, variable.value, data.date] as String[]);
						}
					}
				}
				writer2.close();
				bytes = output2.toByteArray()
				fileEntry = new ZipEntry('sequence-'+index+'-userinput.csv');
				zip.putNextEntry(fileEntry);
				zip.write(bytes)
			}
			writer.close();
			bytes = output.toByteArray()
			fileEntry = new ZipEntry('sequence-'+index+'.csv');
			zip.putNextEntry(fileEntry);
			zip.write(bytes)
		}
		//Close
		zip.close();
	}

	def index() {
		render "Results"
	}

}
