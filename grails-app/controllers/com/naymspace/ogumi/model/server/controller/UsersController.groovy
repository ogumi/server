package com.naymspace.ogumi.model.server.controller

import com.naymspace.ogumi.model.server.ModelApiController
import com.naymspace.ogumi.server.domain.User
import com.naymspace.ogumi.server.domain.Role

class UsersController implements ModelApiController{

    static allowedMethods = [login: 'POST', register: 'POST']

    def authenticationManager
    def springSecurityService

    def index() {
        redirect uri: "/"
    }

    def status(){
        def user = springSecurityService.currentUser
        RenderJson([
                id: user.id,
                accountExpired: user.accountExpired,
                accountLocked: user.accountLocked,
                username: user.username
        ])
    }

    def register(){
        def requested = request.JSON

        // see if that username already exists
        if (User.findByUsername(requested.username)){
            response.status = 500
            RenderJson([
                    reason: "DUPLICATE_USERNAME"
            ])
            return
        }

        // try to save
        def user = new User(requested)
        if (user.validate()){
            log.info("User ${requested.username} successfully registered")
            user.addToRoles(Role.findByAuthority("ROLE_USER"))
            user.save flush: true
            RenderJson(user)//SAVE
        }
        else {
            //respond with errors if field validation fails
            response.status = 500
            RenderJson([
                    reason: "ERROR_IN_FIELDS",
                    fields: user.errors.fieldErrors.collect {
                                it.field
                            }
            ])
        }
    }
}
