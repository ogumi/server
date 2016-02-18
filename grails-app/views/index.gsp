<!DOCTYPE html>
<html>
<head>
    <title>Welcome to OGUMI Server</title>
    <meta name="layout" content="empty"/>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">
    <link rel="apple-touch-icon" href="${assetPath(src: 'apple-touch-icon.png')}">
    <link rel="apple-touch-icon" sizes="114x114" href="${assetPath(src: 'apple-touch-icon-retina.png')}">
</head>
<body>
    <div class="jumbotron">
        <h2>Welcome to OGUMI</h2>
        <p class="lead small">see <a href="http://www.ogumi.de" target="_blank">www.ogumi.de</a> for more information</p>
    </div>

    <div id="components" class="panel panel-default">
        <div class="panel-heading">
            <h2 class="panel-title">Components</h2>
        </div>
        <div class="panel-body">
            <div class="list-group">
                <a href="./admin" class="list-group-item"><strong>Admin Interface</strong></a>
                <a href="/webclient/" class="list-group-item"><strong>Web Client</strong></a>
            </div>
        </div>
    </div>
    <div id="status" class="panel panel-default">
        <div class="panel-heading">
            <h2 class="panel-title">Info</h2>
        </div>
        <div class="panel-body">
            <h3>Application Status</h3>
            <ul class="list-unstyled">
                <li>App version: <g:meta name="app.version"/></li>
                <li>Grails version: <g:meta name="app.grails.version"/></li>
                <li>Groovy version: ${GroovySystem.getVersion()}</li>
                <li>JVM version: ${System.getProperty('java.version')}</li>
                <li>Reloading active: ${grails.util.Environment.reloadingAgentEnabled}</li>
                <li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
                <li>Domains: ${grailsApplication.domainClasses.size()}</li>
                <li>Services: ${grailsApplication.serviceClasses.size()}</li>
                <li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
            </ul>
            <h3>Installed Plugins</h3>
            <ul class="list-unstyled">
                <g:each var="plugin" in="${applicationContext.getBean('pluginManager').allPlugins}">
                    <li>${plugin.name} - ${plugin.version}</li>
                </g:each>
            </ul>
        </div>
    </div>
</body>
</html>