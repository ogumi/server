package helpers

import com.naymspace.ogumi.server.domain.ABQuestion
import com.naymspace.ogumi.server.domain.AdminInputField
import com.naymspace.ogumi.server.domain.AdminInputFieldValue
import com.naymspace.ogumi.server.domain.Experiment
import com.naymspace.ogumi.server.domain.IncentivizedTask
import com.naymspace.ogumi.server.domain.IncentivizedTaskUser
import com.naymspace.ogumi.server.domain.InformationStep
import com.naymspace.ogumi.server.domain.Model
import com.naymspace.ogumi.server.domain.Player
import com.naymspace.ogumi.server.domain.Question
import com.naymspace.ogumi.server.domain.Questionnaire
import com.naymspace.ogumi.server.domain.SequenceEntry
import com.naymspace.ogumi.server.domain.Session
import com.naymspace.ogumi.server.domain.Uploadable
import com.naymspace.ogumi.server.domain.User
import com.naymspace.ogumi.server.services.ExperimentService
import com.naymspace.ogumi.server.services.ModelService
import grails.test.mixin.Mock
import grails.util.Holders


/**
 * Created by dennis on 20.05.15.
 */
class MockService {

    static def TESTMODELLOCATION = new File('test/unit/resources/ogumi-model.jar').absolutePath

    static class UploadableMock{

        def Uploadable uploadable

        def name    = "uploadable"
        def type    = "filetype"
        def url     = "myurl"
        def path    = "filesystem/path"

        def getUploadable(){
            uploadable = new Uploadable(
                    name: name,
                    type: type,
                    url: url,
                    path: path
            ).save(flush: true)
            uploadable
        }

        def static make(closure){
            def mockService = new UploadableMock();
            closure.delegate = mockService
            mockService.getUploadable()
            closure()
            mockService.uploadable
        }
    }

    static class SessionMock{

        def static make(closure){
            def mockService = new SessionMock()
            mockService.getSession()
            closure.delegate = mockService
            closure()
            mockService.session
        }

        def Session session
        def getSession(){
            session = session ?: new Session(
                    name    : "session_name",
                    start   : new Date(),
                    end     : new Date().plus(1),
                    active  : true
            ).save(flush: true)
            session
        }

        def password(String password){
            session.password = password
            session.save(flush: true)
        }

        def addPlayer(User user){
            new Player(
                    session: session,
                    user: user,
                    money: 0,
                    step: 1,
                    version: 0
            ).save(flush:true)
        }

        def addPlayers(Collection users){
            users.each{
                new Player(session: session, user: user, money: 0, step: 1, version:0).save(flush: true)
            }
            session
        }

        def addPlayerCount(int numPlayers){
            (1..numPlayers).each{
                def newUser = new User(username: "user"+it, password: "password", email:"me@you.com").save(flush: true)
                new Player(session: session, user: newUser, money: 0, step: 1, version:0).save(flush: true)
            }
            session
        }

        def addIncentiviceTaskStep(){
            def iTask = new IncentivizedTask(
                    name: "incentivizedtask step",
                    label: "incentivizedtask step",
                    session: session
            )
            def ArrayList<ABQuestion> questions = new ArrayList<ABQuestion>()
            (1..3).each{
                def newQuestion = new ABQuestion(
                        incentivizedTask: iTask,
                        question: "question "+it,
                        aActive: 1,
                        bActive: 2,
                        aPassive: 1,
                        bPassive: 2
                )
                newQuestion.save(flush: true)
                questions.add(newQuestion)
            }
            iTask.questions = questions
            iTask.save(flush: true)
            session.addToSequence(SequenceEntry.get(iTask.id))
            iTask
        }

        def createIncentivizedTaskUserEntry(User activeUser, User passiveUser, IncentivizedTask iTask){
            new IncentivizedTaskUser(
                    activeUser: activeUser,
                    passiveUser: passiveUser,
                    session: session,
                    step: 1,
                    incentivizedTask: iTask,
                    date: new Date()
            ).save(flush: true)
        }

        def addQuestionnaireStep(){
            def questionnaire = new Questionnaire(name: "questionnaire step", label: "questionnaire")
            def questions = new ArrayList<Question>()
            (1..4).each{
                def newQuestion = new Question(
                        questionnaire: questionnaire,
                        question: "question "+it,
                ).save(flush: true)
                questions.push(newQuestion)
            }
            questionnaire.questions = questions
            questionnaire.session = session
            questionnaire.save(flush: true)
            session.addToSequence(SequenceEntry.get(questionnaire.id))
            questionnaire
        }

        def addExperimentStep(){
            def uploadable = new Uploadable(
                    path: TESTMODELLOCATION,
                    name: "ogumi-model.jar",
                    url:"doesnotmatter"
            ).save(flush: true)
            def model = ModelService.fromJar(name: "TestModel", jar: uploadable, uploadable.path)

            model.addToTranslations(
                path: new File('test/unit/resources/translation1.txt').absolutePath,
                name: "de.txt",
                url:"language1"
            )

            model.addToTranslations(
                    path: new File('test/unit/resources/translation2.txt').absolutePath,
                    name: "en.txt",
                    url:"language1"
            )
            model.save(flush: true)

            //donno why i have to do this explicitly
            model.adminInput.each{
                it.save(flush: true)
            }

            def adminInput = []
            ['catchability', 'cost', 'p', 'growth rate', 'capacity'].each{
                def field = AdminInputField.findByName(it)
                adminInput << new AdminInputFieldValue(
                        adminInputField: field,
                        value: 1.0
                ).save(flush: true)
            }

            def experiment = new Experiment(
                    name: "testExperiment",
                    label: "testExperiment",
                    model: model,
                    experimentStart: new Date(),
                    session: session,
                    adminInput: adminInput,
            ).save(flush: true)
            experiment.link = "SERVER NOT RUNNING"
            experiment
        }

        def addInformationStep(){
            def step = new InformationStep(
                    name: "test_informationstep",
                    label: 'test informationstep',
                    information: "Des is a information",
                    //media: [new Uploadable(url: "http://myurl.com", path: "mypath", name: "myname", type: "image").save(flush: true)]
            ).save(flush: true)
            session.addToSequence(SequenceEntry.get(step.id))
            step
        }

    }

    def static makeSession(closure){
        SessionMock.make(closure)
    }

    def static makeUploadable(closure){
        UploadableMock.make(closure)
    }

    def demoExperimentWithModel(){
        def model = demoModel()
        demoExperiment(model)
    }

    static def mockSession(String password){

    }

    def demoModel(){
        def uploadable = new Uploadable(
                path: new File('test/unit/resources/ogumi-model.jar').absolutePath,
                name: "ogumi-model.jar",
                url:"doesnotmatter"
        ).save(flush: true)
        def model = new Model(jar: uploadable, name: "TestModel").save(flush: true);
        assert Model.count() == 1
        return model
    }

    def demoExperiment(Model model){
        def adminInput = []

        ['catchability', 'cost', 'p', 'growth rate', 'capacity'].each{
            def field = AdminInputField.findByName(it)
            adminInput << new AdminInputFieldValue(
                    adminInputField: field,
                    value: 1.0
            ).save()
        }

        def experiment = new Experiment(
                name: "testExperiment",
                model: model,
                experimentStart: new Date(),
                adminInput: adminInput
        ).save(flush: true)

        assert AdminInputField.count() == 5
        assert AdminInputFieldValue.count() == 5
        assert Experiment.count() == 1
    }
}
