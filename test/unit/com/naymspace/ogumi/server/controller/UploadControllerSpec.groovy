package com.naymspace.ogumi.server.controller

import com.naymspace.ogumi.server.domain.Uploadable
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(UploadController)
@Mock([Uploadable])
class UploadControllerSpec extends Specification {
    File testModelJarFile


    def setup() {
        testModelJarFile = new File('test/unit/resources/ogumi-model.jar')
    }

    def cleanup() {
    }

    void 'test file upload'() {
        when: "uploading a valid file"
        def file = new GrailsMockMultipartFile('file_data',"ogumi-model.jar", "application/jar", testModelJarFile.bytes)
        request.addFile file
        controller.index()

        then: "the action saves an uploadable and responds with status 201"
        Uploadable.count() == 1
        response.status == 201
        response.json.origName == "ogumi-model.jar"
        file.targetFileLocation.path == Uploadable.list().first().path
    }

    void 'test jar file upload'(){
        when: "uploading a valid jar file"
        def file = new GrailsMockMultipartFile('file_data',"ogumi-model.jar", "application/jar", testModelJarFile.bytes)
        request.addFile file
        controller.jar()

        then: "the action saves an uploadable and responds with status 201"
        Uploadable.count() == 1
        response.status == 201
        response.json.origName == "ogumi-model.jar"
        file.targetFileLocation.path == Uploadable.list().first().path
        file.targetFileLocation.path.contains("/models/")
    }
}
