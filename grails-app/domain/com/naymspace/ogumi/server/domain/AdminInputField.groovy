package com.naymspace.ogumi.server.domain

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

import com.naymspace.ogumi.model.annotations.AdminInput
import java.lang.annotation.Annotation
import java.lang.reflect.Field

class AdminInputField {

	String name
	String displayAs="inputfield"
	String type="double"
	double min
	double max

	AdminInputField(Object json, Model m){
		model = m
		name = json.name
		displayAs = json.displayAs
		type = json.type
		min = json.min
		max = json.max
	}

    AdminInputField(String name, String displayAs, String type, double min, double max){
        this.name = name;
        this.displayAs = displayAs
        this.type = type
        this.min = min
        this.max = max
    }

	static belongsTo = [model: Model]

	String toString() {
		return name + ' ('+model+')'
	}

    protected static AdminInputField fromField(Field field){
        Annotation annotation = field.getAnnotation(AdminInput.class);
        AdminInput aia = (AdminInput) annotation;
        new AdminInputField(aia.name(), aia.displayAs(), field.getType().getName(), aia.min(), aia.max());
    }

}
