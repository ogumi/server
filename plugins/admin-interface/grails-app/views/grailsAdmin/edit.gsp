<%@ page defaultCodec="HTML" %>

<!DOCTYPE html>
<html>
    <head>
        <title>${domain.className} - <g:message code="grailsAdminPlugin.edit.title" /></title>
        <meta name="layout" content="grailsAdmin/main" />
    </head>
    <body>
        <div class="main-container container tight">
            <div class="row">
                <div class="col-md-7">
                    <ol class="breadcrumb">
                        <li><g:link mapping="grailsAdminDashboard"><g:message code='grailsAdminPlugin.dashboard.title'/></g:link></li>
                        <li><g:link mapping="grailsAdminList" params="[slug: domain.slug]">${domain.className}</g:link></li>
                        <li class="active"><g:message code="grailsAdminPlugin.edit.title" /></li>
                    </ol>
                </div>
                <div class="col-md-5 object-nav">
                    <div class="btn-group">
                        <g:link mapping="grailsAdminAdd" params="[slug: domain.slug]" class="btn btn-default">
                            <span class="glyphicon glyphicon-plus"></span> <g:message code='grailsAdminPlugin.add.title' />
                        </g:link>
                        <g:link mapping="grailsAdminList" params="[slug: domain.slug]" class="btn btn-default">
                            <span class="glyphicon glyphicon-list"></span> <g:message code='grailsAdminPlugin.action.return'/>
                        </g:link>
                    </div>
                </div>
            </div>

            <g:render template="/grailsAdmin/editForm"/>
            <g:render plugin="grailsAdmin" template="/grailsAdmin/includes/delete" />
        </div>
    </body>
</html>
