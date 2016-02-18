package com.naymspace.ogumi.utils.marshallers
/**
 * Created by dennis on 20.05.15.
 */
class CustomMarshallers {

    def marshallers = []

    def register(){
        marshallers.each{
            it.register()
        }
    }
}
