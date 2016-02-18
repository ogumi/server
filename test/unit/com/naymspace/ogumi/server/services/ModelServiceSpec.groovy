package com.naymspace.ogumi.server.services

import com.naymspace.ogumi.server.domain.Model
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import helpers.MockService
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ModelService)
@Mock([Model])
class ModelServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "fromJar should create a new Model with correct admin attributes"() {
        def createdModel;
        when: "i call it with a path of a model jar file"
            createdModel = ModelService.fromJar(name: "testmodel", MockService.TESTMODELLOCATION)
        then: "a new model should be created with admininput fields and class name"
            createdModel.adminInput.size == 5
            createdModel.claz == "com.naymspace.ogumi.example.OgumiModel"
            createdModel.name == "testmodel"
            (['catchability', 'cost', 'p', 'growth rate', 'capacity']).eachWithIndex { String entry, int i ->
                createdModel.adminInput[i].name == entry
            }
    }

    void 'getUserInput fields should extract the user input fields from jar' (){
        def fields
        when: "i call the it with a path to a model file"
            fields = ModelService.getUserInputFieldsFrom(MockService.TESTMODELLOCATION)
        then: "i should get a list of UserInputFields of the model"
            fields[0].with {
                name == 'effort'
                displayAs == 'slider'
                min == "0.0"
                max == '10.0'
                type == "double"
            }
    }
}
