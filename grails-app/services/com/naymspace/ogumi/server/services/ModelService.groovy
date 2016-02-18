package com.naymspace.ogumi.server.services

import com.naymspace.ogumi.model.annotations.AdminInput
import com.naymspace.ogumi.model.annotations.UserInput
import com.naymspace.ogumi.model.interfaces.Model
import com.naymspace.ogumi.model.server.ModelLoader
import com.naymspace.ogumi.server.domain.AdminInputField
import com.naymspace.ogumi.server.domain.OutputField

import java.lang.annotation.Annotation
import java.lang.reflect.Field

class ModelService {

    static def fromJar(params, path){
        def model = new com.naymspace.ogumi.server.domain.Model()
        def ogumiModel = loadModel(path)

        // AdminInputFields & Params
        def modelInfo = getAdminFieldsFrom(ogumiModel)
        def claz = modelInfo.claz
        def adminInput = []
        for (Field field: modelInfo.fields){
            adminInput << AdminInputField.fromField(field as Field)
        }
        model.setProperties(params + [claz: claz])
        adminInput.each {
            model.addToAdminInput(it)
        }

        // Outputfields
        for (Field field: getOutputFieldsFrom(ogumiModel)){
            model.addToOutputfields(OutputField.fromField(field))
        }

        model
    }

    /**
     * Convenience method for getUserInputFieldsFrom(Model)
     * @param path
     * @return
     */
    static def getUserInputFieldsFrom(String path){
        def model = loadModel(path)
        getUserInputFieldsFrom(model)
    }

    /**
     * Extracts userinputfields from model
     * @param model
     * @return fields
     */
    static def getUserInputFieldsFrom(Model model){
        def fields = []
        for(Field obj: model.getUserInputFields()){
            Annotation annotation = obj.getAnnotation(UserInput.class);
            UserInput ui = (UserInput) annotation;
            obj.setAccessible(true)
            def defaultValue = (Number) obj.get(model)
            fields << [
                    name: ui.name(),
                    displayAs: ui.displayAs(),
                    min: ui.min(),
                    max: ui.max(),
                    default: defaultValue,
                    type: obj.getType().getName()
            ]
        }
        fields
    }

    /**
     * Extracts outputfields from model
     * @param model
     * @return
     */
    static def getOutputFieldsFrom(Model model){
        def fields = []
        for(Field obj: model.getOutputFields()){
            fields << obj
        }
        fields
    }

    /**
     * Extracts admininputfields from model
     * @param model
     * @return
     */
    static def getAdminFieldsFrom(Model model){
        def fields = []
        for(Field obj: model.getAdminInputFields()){
            fields << obj
        }
        [claz: model.getClass().getName() , fields: fields]
    }

    static def loadModel(String path){
        def model
        try {
            File f = new File(path)
            String cls = ModelLoader.loadModel(f);
            Class clz = Class.forName(cls, true, ModelLoader.classLoader);
            model = (Model) clz.newInstance();

        }
        catch (Exception e){
            e.printStackTrace()
        }
        model
    }

}
