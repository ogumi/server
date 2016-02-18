package com.naymspace.ogumi.model.server.controller

import com.naymspace.ogumi.model.server.ModelApiController

class DefaultController implements ModelApiController{

    def index() {
        RenderJson([
                version: grailsApplication.metadata['app.version'],
                status: 'healthy', // not implemented yet
                capabilities: [], // not implemented yet
                availableStepTypes: [] // not implemented yet
        ])
    }
}
