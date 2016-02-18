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
import com.naymspace.ogumi.server.services.ModelService
import com.naymspace.ogumi.util.tasks.ExperimentActivatorTask
import com.naymspace.ogumi.util.tasks.SessionDeactivatorTask
import grails.util.Holders

import javax.servlet.ServletContext
import groovyx.net.http.HTTPBuilder
import groovy.time.TimeCategory
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*


class BootStrap {
    def grailsApplication

	def http = new HTTPBuilder('http://ogumi.de/')


    def init = { ServletContext ctx ->
        environments {
            production {
                userSetup()
                initProduction()
                startExperimentActivatorTask(10)
                startSessionDeactivatorTask(30)
            }
            test {
                userSetup()
            }
            development {
                userSetup()
                demoUser()
                def model = demoModel()
                demoExperiment(model)

                demoIncentivizedTask()

                startExperimentActivatorTask(10)
                startSessionDeactivatorTask(5)
            }
        }
    }

    /**
     * starts the experiment activator task
     * responsible for activating planned experiments
     * Delays the first run for 10 seconds
     *
     * @param interval interval in seconds
     * @return
     */
    def startExperimentActivatorTask(int interval){
        use (TimeCategory) {
            //1000 * interval = interval Seconds
            new Timer().scheduleAtFixedRate(new ExperimentActivatorTask(), new Date() + 10.seconds, 1000 * interval)
        }
    }

    /**
     * starts the session deactivator task
     * responsible for deactivating active sessions if they have
     * a end date in the past
     * Delays the first run for 10 seconds
     *
     * @param interval interval in seconds
     * @return
     */
    def startSessionDeactivatorTask(int interval){
        use ( TimeCategory ) {
            new Timer().scheduleAtFixedRate(new SessionDeactivatorTask(), new Date() + 10.seconds, 1000 * interval)
        }
    }

    def initProduction(){
        if(grailsApplication.config.grails.ogumi.publish) {
            def linkGenerator = Holders.grailsApplication.mainContext.getBean("grailsLinkGenerator")
            http.request(GET, TEXT) {
                uri.path = 'serverlist-1.2.json'
                uri.query = [ action: 'register', name: grailsApplication.config.grails.ogumi.name, url: "${linkGenerator.serverBaseURL}/${grailsApplication.config.grails.ogumi.publishedContextPath}"]
                response.success = { resp ->
                    println "Registered on ogumi.de"
                }
                response.failure = { resp ->
                    println "Registration on ogumi.de failed with status ${resp.status}"
                }
            }
        }
    }


    def userSetup(){
        if (!Role.findByAuthority("ROLE_ADMIN")){
            def adminRole = new Role(authority: 'ROLE_ADMIN').save(flush: true)
        }
        if (!Role.findByAuthority("ROLE_USER")){
            new Role(authority: 'ROLE_USER').save(flush: true)
        }
        if (!User.findByUsername("admin")){
            def testUser = new User(username: 'admin', password: 'ogumi', email: 'admin@example.com')
            testUser.addToRoles(Role.findByAuthority("ROLE_ADMIN"))
            testUser.save(flush: true)
        }
    }

    def demoUser(){
        def testUser = new User(username: 'user', password: 'darfnichtrein', email: 'user@example.com')
        testUser.addToRoles(Role.findByAuthority('ROLE_USER'))
        testUser.save(flush: true)
    }

    def demoModel(){
        def uploadable = new Uploadable(
                path: grailsApplication.getParentContext().getResource("/testfiles/OgumiModel-OpenCampus-With-AvarageTotalCumulatedReturn-TwoGraphs-1.2-SNAPSHOT.jar").file.absolutePath,
                name: "ogumi-model.jar",
                url:"doesnotmatter"
        ).save(flush: true)
        def model = ModelService.fromJar(jar: uploadable, name: "TestModel", label: "TestModel", uploadable.path)
        model.addToTranslations(
                path: grailsApplication.getParentContext().getResource("/testfiles/OpenCampus-with-avarageTotalCumulatedReturn-twographs-translations/de.json").file.absolutePath,
                name: "de.json",
                downloadable: true
        )
        model.addToTranslations(
                path: grailsApplication.getParentContext().getResource("/testfiles/OpenCampus-with-avarageTotalCumulatedReturn-twographs-translations/en.json").file.absolutePath,
                name: "en.json",
                downloadable: true
        )
        model.save(flush: true)
        assert Model.count() == 1
        return model
    }

    def demoIncentivizedTask(){
        def iTask = new IncentivizedTask(
                name: "incentivizedtask step",
                label: "incentivizedtask step",
                session: testSession,
                randomized: false

        )
        def ArrayList<ABQuestion> questions = new ArrayList<ABQuestion>()
        (1..9).each{
            def newQuestion = new ABQuestion(
                    incentivizedTask: iTask,
                    question: "question "+it,
                    aActive: 1+it,
                    bActive: 1,
                    aPassive: 1,
                    bPassive: 1+it
            )
            newQuestion.save(flush: true)
            questions.add(newQuestion)
        }
        iTask.questions = questions
        iTask.save(flush: true)

        def informationStep = new InformationStep(
                name: 'informationstep',
                label: 'informationstep',
                information: 'Das ist eine Information'
        ).save(flush: true)
        testSession.addToSequence(SequenceEntry.get(informationStep.id))
        testSession.addToSequence(SequenceEntry.get(iTask.id))
        iTask
    }


    def testSession = new Session(
            name: "testsession",
            start: new Date(),
            end: new Date().plus(1)
    ).save(flush: true)

    def demoExperiment(Model model){


        def adminInput = []

        def values = [
                "catchability": 0.02,
                "cost": 0,
                "p": 1,
                "growth rate": 0.5,
                "capacity": 100,
                "InitialResourceLevel": 10
        ]
        ['catchability', 'cost', 'p', 'growth rate', 'capacity'].each{
                def field = AdminInputField.findByName(it)
                adminInput << new AdminInputFieldValue(
                        adminInputField: field,
                        value: values[it]
                ).save(flush: true)
        }

        def session = testSession

        def experimentStart = new Date()
        use (TimeCategory){
            experimentStart = experimentStart + 30.seconds
        }

        def experiment = new Experiment(
                timeStepSize: 1,
                clientUpdateInterval: 5,
                name: "testExperiment",
                label: "testExperiment",
                model: model,
                experimentStart: experimentStart,
                duration: 2,
                showFuture: false,
                xPointsVisible: 100
        )

        OutputField.all.each{
            experiment.addToOutputfields(new OutputFieldValue(outputField: it, shouldshow: true))
        }

        experiment.profit = OutputField.findByName("cumulated return")

        adminInput.each {
            experiment.addToAdminInput(it)
        }
        experiment.save(flush: true)

        def player = new Player(user: User.findByUsername('admin'), step: 0, session: session, money: 0).save(flush: true)

        def informationStep = new InformationStep(
                name: 'informationstep',
                label: 'informationstep',
                information: 'Das ist eine Information'
        )

        // Questionnaire Step
        def question = new Question(
                question: "whaaat?",
        ).save()

        def questionnaire = new Questionnaire(
                name: "gimmi da antwort",
                label: 'alta, a questionnaire step'
        )
        questionnaire.addToQuestions(question)
        questionnaire.save(flush: true)
        // ----------------------

        session.addToSequence(informationStep).save(flush: true)
        session.addToSequence(questionnaire).save(flush: true)
        session.addToSequence(experiment).save(flush: true)
        session.addToPlayers(player).save(flush: true)

        assert Experiment.count() == 1
        assert Experiment.first().session != null
    }


	def destroy = {
		if(grailsApplication.config.grails.ogumi.publish){
			http.request(GET, TEXT) {
				uri.path = 'serverlist-1.2.json'
				uri.query = [ action: 'unregister', name: grailsApplication.config.grails.ogumi.name, url: "${linkGenerator.serverBaseURL}/${grailsApplication.config.grails.ogumi.publishedContextPath}" ]
				response.success = { resp ->
					println "Unregistered on ogumi.de"
				}
				response.failure = { resp ->
					println "Unregistration on ogumi.de failed with status ${resp.status}"
				}
			}
		}
	}
}
