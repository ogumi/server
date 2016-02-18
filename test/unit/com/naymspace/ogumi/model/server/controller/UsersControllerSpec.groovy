package com.naymspace.ogumi.model.server.controller

import com.naymspace.ogumi.server.domain.Role
import com.naymspace.ogumi.server.domain.User
import static javax.servlet.http.HttpServletResponse.*

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(UsersController)
@Mock([User, Role])
class UsersControllerSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "testing redirect to on index"(){
        when: "i call the index action"
            controller.index()
        then: "i should get a redirect to index page"
            response.redirectUrl == "/"
    }

    void "test invalid request method"() {
        given:
            request.method = 'GET'
            request.json = '{username: "testuser", password: "testpassword", email: "test@e-mail.com"}'

        when:
            controller.register()

        then:
            response.status == SC_METHOD_NOT_ALLOWED
    }

    void "test successfull user registration"() {
        given:
        new Role(authority: "ROLE_USER").save(flush:true) // User role exists
        request.method = "POST"
        request.json = '{username: "testuser", password: "testpassword", email: "test@e-mail.com"}'

        when:
        controller.register()

        then:
        response.status == 200
        response.json.username == "testuser"
        User.count() == 1
    }

    void "test failing user registration "(){
        given:
        request.method = "POST"

        when: "no password, email provided"
        request.json = "{username: 'bla'}"
        controller.register()

        then: "the action responds with status 500 and no User is persisted"
        response.status == 500
        User.count() == 0
    }

}
