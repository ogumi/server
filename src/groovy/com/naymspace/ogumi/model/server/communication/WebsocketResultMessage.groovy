package com.naymspace.ogumi.model.server.communication

/**
 * Created by dennis on 8/15/15.
 */
class WebsocketResultMessage {
    enum RESULTSTATUS{
        FAILURE, SUCCESS
    }

    def resultStatus

    def getResultStatus(){
        resultStatus.name()
    }

}
