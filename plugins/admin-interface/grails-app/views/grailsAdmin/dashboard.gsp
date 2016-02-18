<%@ page defaultCodec="HTML" %>

<!DOCTYPE html>
<html>
		<head>
				<title><g:message code="grailsAdminPlugin.dashboard.title" /></title>
				<meta name="layout" content="grailsAdmin/main" />
		</head>
		<body>
				<div class="main-container container">
								<g:each var="group" in="${config.groups}">
                                    <div class="panel panel-default">
									<g:if test="${group != 'Hidden'}">
                                        <div class="panel-heading">${group}</div>
                                        <ul class="list-group">
                                            <g:each var="domainClass" in="${config.getGroup(group)}">

                                                        <li class="list-group-item">${domainClass.className}
                                                                    <div class="btn-group">
                                                                            <g:link class="btn btn-link" mapping="grailsAdminList" params="[slug: domainClass.slug]">
                                                                                    <span class="glyphicon glyphicon-list"></span>
                                                                            </g:link>
                                                                            <g:link class="btn btn-link" mapping="grailsAdminAdd" params="[slug: domainClass.slug]">
                                                                                    <span class="glyphicon glyphicon-plus"></span>
                                                                            </g:link>
                                                                    </div>
                                                        </li>
                                            </g:each>
                                        </ul>
									</g:if>
                                    </div>
								</g:each>
				</div>
		</body>
</html>
