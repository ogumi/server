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

// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

grails.config.locations =   [   "classpath:${appName}-config.properties",
                                "classpath:${appName}-config.groovy",
                                "file:${userHome}/.grails/${appName}-config.properties",
                                "file:${userHome}/.grails/${appName}-config.groovy"
                            ]

if (System.properties["${appName}.config.location"]) {
		grails.config.locations << "file:" + System.properties["${appName}.config.location"]
}

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination

// The ACCEPT header will not be used for content negotiation for user agents containing the following strings (defaults to the 4 major rendering engines)
grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
grails.mime.types = [ // the first one is the default format
		all:           '*/*', // 'all' maps to '*' or the first available format in withFormat
		atom:          'application/atom+xml',
		css:           'text/css',
		csv:           'text/csv',
		form:          'application/x-www-form-urlencoded',
		html:          ['text/html','application/xhtml+xml'],
		js:            'text/javascript',
		json:          ['application/json', 'text/json'],
		multipartForm: 'multipart/form-data',
		rss:           'application/rss+xml',
		text:          'text/plain',
		zip:           'application/octet-stream',
		hal:           ['application/hal+json','application/hal+xml'],
		xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// Legacy setting for codec used to encode data with ${}
grails.views.default.codec = "html"

// The default scope for controllers. May be prototype, session or singleton.
// If unspecified, controllers are prototype scoped.
grails.controllers.defaultScope = 'singleton'

// GSP settings
grails {
		views {
				gsp {
						encoding = 'UTF-8'
						htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
						codecs {
								expression = 'html' // escapes values inside ${}
								scriptlet = 'html' // escapes output from scriptlets in GSPs
								taglib = 'none' // escapes output from taglibs
								staticparts = 'none' // escapes output from static template parts
						}
				}
				// escapes all not-encoded output at final stage of outputting
				// filteringCodecForContentType.'text/html' = 'html'
		}
}


grails.converters.encoding = "UTF-8"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = ['com.naymspace.ogumi.model.server.websockets']
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

// configure passing transaction's read-only attribute to Hibernate session, queries and criterias
// set "singleSession = false" OSIV mode in hibernate configuration after enabling
grails.hibernate.pass.readonly = false
// configure passing read-only to OSIV session by default, requires "singleSession = false" OSIV mode
grails.hibernate.osiv.readonly = false

tomcat.deploy.username=""
tomcat.deploy.password=""
tomcat.deploy.url="http://localhost:8080/manager/text"

grails.tomcat.nio = true
grails.tomcat.scan.enabled = true

// log4j configuration
log4j = {
		// Example of changing the log pattern for the default console appender:
		//
		appenders {
            console name: 'stdout', layout: pattern(conversionPattern: '%d{yyyy-MM-dd HH:mm:ss,SSS Z} [%t] %-5p %c{1}:%L %x - %m%n')

		}
        root {
            info()
        }
        error   'org.codehaus.groovy.grails.web.pages' //  GSP
                'org.codehaus.groovy.grails.web.servlet' //controller

        info    'grails.app'
                'com.naymspace.ogumi.util.tasks'
                'com.naymspace.ogumi.model.server.communication'
}


// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
grails.plugin.springsecurity.password.algorithm = 'SHA-256'
grails.plugin.springsecurity.password.hash.iterations = 1
grails.plugin.springsecurity.logout.postOnly = false
grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.naymspace.ogumi.server.domain.User'
grails.plugin.springsecurity.authority.className = 'com.naymspace.ogumi.server.domain.Role'
grails.plugin.springsecurity.rejectIfNoRule = true
grails.plugin.springsecurity.fii.rejectPublicInvocations = false

grails.plugin.springsecurity.rest.login.active = true
grails.plugin.springsecurity.rest.login.endpointUrl = '/modelapi/users/login'

grails.plugin.springwebsocket.useCustomConfig = true

grails.plugin.springsecurity.interceptUrlMap = [
        '/':                                ['ROLE_ADMIN'],
        '/index':                           ['permitAll'],
        '/index.gsp':                       ['permitAll'],
        '/**/js/**':                        ['permitAll'],
        '/**/css/**':                       ['permitAll'],
        '/**/images/**':                    ['permitAll'],
        '/**/favicon.ico':                  ['permitAll'],
        '/assets/**':                       ['permitAll'],
        '/login/**':                        ['permitAll'],
        '/logout/**':                       ['permitAll'],
        '/userrole/*':                      ['ROLE_ADMIN'],
        '/liveview/**':                     ['ROLE_ADMIN'],
        '/results/**':                      ['ROLE_ADMIN'],
        '/uploads/**':                       ['permitAll'],
       	'/uploads/models/*':                ['ROLE_ADMIN'],
    	'/upload/**':                       ['ROLE_ADMIN'],
        '/upload':                          ['ROLE_ADMIN'],

        //Admin Area
        '/admin/**':                        ['ROLE_ADMIN'],
        '/dbconsole/**':                    ['ROLE_ADMIN'],

        //DES SHOULD NOT BE DE WAY
        '/plugins/admin-interface-0.6.5-OGUMI-SNAPSHOT/grails-admin/libs/**': ['permitAll'],

        //modelAPI
        '/modelapi':                        ['permitAll'],
        '/modelapi/users/login':            ['permitAll'],
        '/modelapi/users/register':         ['permitAll'],
        '/modelapi/users/status':           ['ROLE_USER', 'ROLE_ADMIN'],
        '/modelapi/**':                     ['ROLE_USER', 'ROLE_ADMIN'],
        '/experimentWebsocket/**':          ['ROLE_USER', 'ROLE_ADMIN'],
        '/stomp/info':                      ['permitAll'],
        '/stomp/**':                        ['ROLE_USER', 'ROLE_ADMIN'],

]

//CORS
cors.url.pattern = ['/modelapi/*','/stomp/*', '/uploads/*']
cors.headers = [
	'Access-Control-Allow-Methods': 'GET, POST, PUT, HEAD, OPTIONS',
    'Access-Control-Allow-Headers': 'x-auth-token, origin, authorization, accept, content-type, x-requested-with'
]

// Admin Interface
admin.base = "admin"
grails.plugin.admin.accessRoot = "/" + admin.base

grails.plugin.springsecurity.filterChain.chainMap = [
	 "/modelapi/**":    'JOINED_FILTERS,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter',
     "/stomp/**":       'JOINED_FILTERS,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter',
	 '/**':             'JOINED_FILTERS,-basicAuthenticationFilter,-basicExceptionTranslationFilter,-restTokenValidationFilter,-restExceptionTranslationFilter'
]

grails.plugin.admin.domains."User Management" = [
	"com.naymspace.ogumi.server.domain.User",
]

grails.plugin.admin.domains."Session" = [
	"com.naymspace.ogumi.server.domain.Session"
]

grails.plugin.admin.domains."Stages" = [
	"com.naymspace.ogumi.server.domain.Questionnaire",
	"com.naymspace.ogumi.server.domain.IncentivizedTask",
	"com.naymspace.ogumi.server.domain.InformationStep",
	"com.naymspace.ogumi.server.domain.Model"
]

//Domain Classes in this group will not be shown, neither in the menu not in the dashboard
grails.plugin.admin.domains."Hidden" = [
	"com.naymspace.ogumi.server.domain.Question",
	"com.naymspace.ogumi.server.domain.ABQuestion",
	"com.naymspace.ogumi.server.domain.QuestionResponse",
	"com.naymspace.ogumi.server.domain.ABQuestionResponse",
	"com.naymspace.ogumi.server.domain.Experiment",
	"com.naymspace.ogumi.server.domain.Waiting",
	"com.naymspace.ogumi.server.domain.SequenceEntry",
	"com.naymspace.ogumi.server.domain.Variable",
	"com.naymspace.ogumi.server.domain.AdminInputFieldValue",
	"com.naymspace.ogumi.server.domain.OutputFieldValue",
	"com.naymspace.ogumi.server.domain.Uploadable",
	"com.naymspace.ogumi.server.domain.Role",
]

grails.plugin.admin.domain.InformationStep = {
	create excludes: ["session"]
	edit   excludes: ["session"]
    list   includes: ['label', 'name']
    groups{
        "General" fields: ['label','name']
        "Content" fields: ['information', 'media']
    }
	widget "information", "net.kaleidos.plugins.admin.widget.TextAreaWidget"
	widget "media", "com.naymspace.ogumi.server.widgets.UploadWidget"
	widget "label", help: "The label used only in the admin area."
	widget "name", help: "The name displayed in the app."
}

grails.plugin.admin.domain.Questionnaire = {
    create excludes: ['session']
    edit excludes: ['session']
    list excludes: ['session']
    groups {
        "General" fields: ['label','name']
        "Content" fields: ['questions']
    }
	widget "questions", "com.naymspace.ogumi.server.widgets.DraggableNewWidget"
	widget "label", help: "The label used only in the admin area."
	widget "name", help: "The name displayed in the app."
}

grails.plugin.admin.domain.IncentivizedTask = {
	create includes: ["label", "name", "randomized", "questions"]
	edit   includes: ["label", "name", "randomized", "questions"]
	list   includes: ["label", "name", "randomized", "questions"]
    groups {
        "General" fields: ['label', 'name', 'randomized']
        "Content" fields: ['questions']
    }
	widget "questions", "com.naymspace.ogumi.server.widgets.DraggableNewWidget"
	widget "label", help: "The label used only in the admin area."
	widget "name", help: "The name displayed in the app."
}

grails.plugin.admin.domain.Question = {
	create includes: ["question", "pattern"]
	edit   includes: ["question", "pattern"]
	widget "pattern", "net.kaleidos.plugins.admin.widget.SelectWidget", options: [ ".*": "any", "[0-9]*": "Number", "[A-Za-z\\-., _\u00c0-\u00ff]*": "Text"]
}

grails.plugin.admin.domain.ABQuestion = {
	create includes: ["aActive", "aPassive", "bActive", "bPassive", "question", "a", "b"]
	edit   includes: ["aActive", "aPassive", "bActive", "bPassive", "question", "a", "b"]
	widget "aActive", "net.kaleidos.plugins.admin.widget.DecimalInputWidget"
	widget "aPassive", "net.kaleidos.plugins.admin.widget.DecimalInputWidget"
	widget "bActive", "net.kaleidos.plugins.admin.widget.DecimalInputWidget"
	widget "bPassive", "net.kaleidos.plugins.admin.widget.DecimalInputWidget"
	widget "a", help: "Default is A if you keep this field empty."
	widget "b", help: "Default is B if you keep this field empty."
}

grails.plugin.admin.domain.Session = {
	create includes: ["name", "start", "end", "sequence", "maxParticipants", "password"]
	edit includes: ["name", "start", "end", "active", "sequence", "maxParticipants", "password"]
	list includes: ["name", "active", "start", "end", "password"]
    groups {
        "General" fields: ['name', 'start', 'end', 'active']
        "Content" fields: ['sequence']
        "Access" style: 'collapse', fields: ['maxParticipants', 'password']
    }
	widget "sequence", "com.naymspace.ogumi.server.widgets.SequenceWidget"
	widget "start", "com.naymspace.ogumi.server.widgets.DateTimeWidget"
	widget "end", "com.naymspace.ogumi.server.widgets.DateTimeWidget"
}

grails.plugin.admin.domain.Experiment = {
	create includes: ["label", "name", "model", "adminInput", "outputfields", "profit", "experimentStart", "timeStepSize", "clientUpdateInterval", "duration", "terminationProbability", "diagram", "showFuture", "xPointsVisible", "y0Min", "y0Max", "y1Min", "y1Max", "blockUserInput"]
	edit includes: ["label", "name", "model", "adminInput", "outputfields", "profit", "experimentStart", "timeStepSize", "clientUpdateInterval", "duration", "terminationProbability", "diagram", "showFuture", "xPointsVisible", "y0Min", "y0Max", "y1Min", "y1Max", "blockUserInput"]
	list includes: ["label", "name", "model", "adminInput", "outputfields", "profit", "experimentStart", "timeStepSize",  "clientUpdateInterval", "duration", "terminationProbability", "diagram", "showFuture", "xPointsVisible", "y0Min", "y0Max", "y1Min", "y1Max", "blockUserInput"]

    groups{
        "General" fields: ['label','name', 'experimentStart', 'duration', 'terminationProbability']
        "Model" fields: ['model','adminInput', 'timeStepSize']
        "Client output" fields: ['outputfields', 'profit',  'clientUpdateInterval', 'blockUserInput']
        "Graph display" style: 'collapse', fields: ['diagram', 'showFuture', 'xPointsVisible', 'y0Min', 'y0Max', 'y1Min', 'y1Max']
    }

    widget "label", help: "The label used only in the admin area."
	widget "name", help: "The name displayed in the app."
	widget "model", "net.kaleidos.plugins.admin.widget.relation.RelationSelectWidget"
	widget "adminInput", "com.naymspace.ogumi.server.widgets.AdminInputWidget"
	widget "outputfields", "com.naymspace.ogumi.server.widgets.OutputWidget",  help: "Counter shown in the 'Profit'-field."
	widget "profit", "com.naymspace.ogumi.server.widgets.SelectProfitWidget", help: "Value that is paid out to the participants."
	widget "experimentStart", "com.naymspace.ogumi.server.widgets.DateTimeWidget"
	widget "timeStepSize", help: "Calculation step width in seconds."
	widget "duration", help: "The duration of the experiment in minutes."
	widget "clientUpdateInterval", help: "The interval in seconds the server sends output to the clients."
	widget "y0Min", "net.kaleidos.plugins.admin.widget.DecimalInputWidget", help: "The minimal value to display in the first y-axis."
	widget "y0Max", "net.kaleidos.plugins.admin.widget.DecimalInputWidget", help: "The maximal value to display in the first y-axis."
	widget "y1Min", "net.kaleidos.plugins.admin.widget.DecimalInputWidget", help: "The minimal value to display in the second y-axis."
	widget "y1Max", "net.kaleidos.plugins.admin.widget.DecimalInputWidget", help: "The maximal value to display in the second y-axis."
	widget "xPointsVisible", "net.kaleidos.plugins.admin.widget.DecimalInputWidget", help: "The points that should be visible in one page of the diagram. Only for linegraphs with showFuture disabled."
	widget "diagram", "net.kaleidos.plugins.admin.widget.SelectWidget", options: ["Line Graph":"linegraph", "Cylinder Gauge":"cylindergauge"]
	widget "terminationProbability", "net.kaleidos.plugins.admin.widget.DecimalInputWidget"
	widget "showFuture", help: "Show calculated future timesteps in the client's graph. Only for linegraphs and respected only if xPointsVisible is set"
	widget "blockUserInput", help: "Disable the send button in the client in the current timestep after sending data."
}

grails.plugin.admin.domain.Waiting = {
	create includes: ["label", "name", "waitUntil"]
	edit includes: ["label", "name", "waitUntil"]
	list includes: ["label", "name", "waitUntil"]
	widget "waitUntil", "com.naymspace.ogumi.server.widgets.DateTimeWidget"
	widget "label", help: "The label used only in the admin area."
	widget "name", help: "The name displayed in the app."
}

grails.plugin.admin.domain.Model = {
	create includes: ["name", "jar", "translations"]
	edit   includes: ["name", "jar", "translations"]
	list   includes: ["name", "jar", "translations"]
    groups {
        "General" fields: ['name', 'jar', 'translations']
    }
	widget "jar", "com.naymspace.ogumi.server.widgets.UploadJarWidget"
	widget "translations", "com.naymspace.ogumi.server.widgets.UploadWidget"
}

grails.plugin.admin.domain.Variable = {
	create includes: ["field", "value"]
	edit   includes: ["field", "value"]
}

grails.plugin.admin.domain.AdminInputFieldValue = {
	create includes: ["value"]
	edit   includes: ["value"]
}

grails.plugin.admin.domain.User = {
	list   includes: ["username", "enabled", "roles"]
	create includes: ["username", "password", "roles"]
	edit   includes: ["username", "password", "enabled", "email", "roles"]
    widget "password", "net.kaleidos.plugins.admin.widget.PasswordInputWidget"
}