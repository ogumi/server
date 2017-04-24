# OGUMIServer

## Current Version

Version = 1.2

This is a modified and refactored fork of the origin OGUMI project.
To connect to this server, you will need an app or the webclient compatible with version 1.2
Feel free to contribute and raise github issues in case of problems.

## Requirements

- Database server or configured memory database
- JDK >= 1.7
- Grails 2.5
- you will need a Servletcontainer with websocket capability for production deployment (i.e. Tomcat 8)

## Configuration

configure the application using the grails configuration files

```
grails-app/conf/Config.groovy
grails-app/conf/DataSource.groovy
```

by default there is a in-memory h2 database configured for the dev environment

Also the default settings can be overwritten in properties file located at

```
classpath:OGUMIServer-config.properties
classpath:OGUMIServer-config.groovy
USER_HOME/.grails/OGUMIServer-config.properties
USER_HOME/.grails/OGUMIServer-config.groovy
```

or you can set the java property OGUMIServer.config.location to specify a config file

```
JVM option: -DOGUMIServer.config.location=./OGUMIServer-config.groovy
```
Some settings you should or must customize:

| property                 | description                               | default                           |
| ------------------------ | ----------------------------------------- | --------------------------------- |
| grails.app.context       | App context of this project               | /OGUMIServer                      |
| grails.ogumi.adminPath   | URL path of this project                  | /OGUMIServer                      |
| grails.ogumi.name        | The name of this project shown in the app | Ogumi on Localhost                |
| grails.ogumi.publish     | Publish this server on ogumi.de           | false                             |
| grails.ogumi.publishedContextPath | Context to publish               | /OGUMIServer                      |

## Development

The command `grails run-app` will start the server under
`http://localhost:8080/OGUMIServer/`

### Building

For displaying charts on the liveview and results page,
[Javascript Charts by amcharts](http://www.amcharts.com/javascript-charts/)
should be included. Note their license, see license section and the amcharts
website.

The command `grails dev war OGUMIServer.war` will build a war-file (in the
development mode `dev`) that can be deployed e.g. on a tomcat server.

For deployment on a tomcat, see the tomcat settings in
`grails-app/conf/Config.groovy` starting with `tomcat.`.

### Translations

Translation files can be added inside `grails-app/i18n` folder. For the registration of new users per e-mail the following keys are defined:

| Key                    | en                                                    |
| ---------------------- | ------------------------------------------------------------- |
| ogumi.name             | OGUMI                                                         |
| ogumi.register.title   | Registration                                                  |
| ogumi.register.message | Registration successfull. You can now login in the OGUMI app. |

The language of the user is detected by the settings of the browser used.

## License

The OGUMIServer is distributed under Apache-2.0, see LICENSE for more details.

Have a look into [THIRD-PARTY.txt](./THIRD-PARTY.txt) for informations about
the licenses of the dependencies of the OGUMIServer.

**Important notice**:
[Javascript Charts by amcharts](http://www.amcharts.com/javascript-charts/)
is only free to use and requires a commercial license if you do not include a
link to their website inside each chart.

To enable live preview, add your copy of Amcharts >= 3.19 to web-app/js/amcharts and copy the ./images - folder
to web-app/images/amcharts

The [favicon.ico](./grails-app/assets/images/favicon.ico),
[apple-touch-icon.png](./grails-app/assets/images/apple-touch-icon.png),
and [apple-touch-icon-retina.png](./grails-app/assets/images/apple-touch-icon-retina.png)
are licensed under CC0.
