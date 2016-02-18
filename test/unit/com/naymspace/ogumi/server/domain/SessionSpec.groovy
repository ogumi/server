package com.naymspace.ogumi.server.domain

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class SessionSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test secure property"() {
        when:
            def session = new Session(name: "testsession")
        then:
            session.secure == false

        when:
            session = new Session(name: "testsession", password: "testpassword")
        then:
            session.secure == true

    }
}
