package com.naymspace.ogumi.server.domain

import com.naymspace.ogumi.model.annotations.AdminInput
import com.naymspace.ogumi.model.annotations.Output

/*
 * Copyright (c) 2015 naymspace software (Dennis Nissen)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import groovy.json.*

import java.lang.annotation.Annotation
import java.lang.reflect.Field

class OutputField {

	String name

	OutputField(Object json, Model m){
		model = m
		name = json.name
	}

	static belongsTo = [model: Model]

	String toString() {
		return name + ' ('+model+')'
	}

    public static OutputField fromField(Field field){
        Annotation annotation = field.getAnnotation(Output.class);
        Output aia = (Output) annotation;
        new OutputField(name: aia.name());
    }

}
