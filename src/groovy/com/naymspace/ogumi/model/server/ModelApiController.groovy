package com.naymspace.ogumi.model.server

/**
 * Created by dennis on 17.05.15.
 */
trait ModelApiController {
    static namespace = 'modelapi'

    def RenderJson(what){
        render(contentType: "application/json"){
            what
        }
    }
}