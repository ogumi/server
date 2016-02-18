<%@ page defaultCodec="HTML" %>

<!DOCTYPE html>
<html>
	<head>
		<title>Live view - ${session.toString()}</title>
		<g:javascript src="amcharts/amcharts.js" />
		<g:javascript src="amcharts/plugins/export/export.min.js" />
        <link rel="stylesheet" href="${createLinkTo(dir:'js/amcharts/plugins/export',file:'export.css')}" />
		<g:javascript src="amcharts/serial.js" />
        <asset:javascript src="sockjs.min.js" />
        <asset:javascript src="stomp.min.js" />
		<asset:javascript src="jquery-1.11.2.min.js" />
		<asset:javascript src="experimentChart.js" />
		<link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">
		<link rel="apple-touch-icon" href="${assetPath(src: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${assetPath(src: 'apple-touch-icon-retina.png')}">
		<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'bootstrap.min.css')}" />
		<asset:stylesheet src="liveview.css"/>
	</head>
	<body>
    <script>
        var stompBase = "";
    </script>
		<div class="container">
			<nav role="navigation" class="navbar navbar-default navbar-fixed-top">
				<g:link mapping="grailsAdminDashboard" class="navbar-brand">
					<g:message code="grailsAdminPlugin.name" />
				</g:link>
				<div class="container-fluid">
					<ul class="nav navbar-nav">
						<li class="active">
							<a href="#">Live View</a>
						</li>
					</ul>
					<div class="pull-right logout">
						<sec:loggedInUserInfo field="username" />
					</div>
				</div>
			</nav>
			<div class="row">
				<div class="col-md-7">
					<ol class="breadcrumb">
							<li><g:link mapping="grailsAdminDashboard"><g:message code='grailsAdminPlugin.dashboard.title'/></g:link></li>
							<li class="active">Live view</li>
					</ol>
				</div>
			</div>
			<div>
				<h2>Live view of ${session.toString()}</h2>
				<div id="player" class="list-group">
					<h3>Player</h3>
					<g:each var="player" in="${session.players}">
						<li class="list-group-item">${player.toString().split('in')[0..-2].join('in')}</li>
					</g:each>
				</div>
				<div id="experiments">
					<g:each var="entry" in="${session.sequence}">
						<g:if test="${entry instanceof com.naymspace.ogumi.server.domain.Experiment}">
							<div id="experiment-${entry.id}">
								<h3>${entry.name}</h3>
								<p>Start: ${entry.experimentStart}, duration: ${entry.duration} minutes</p>
								<g:form method="post" controller="liveview" action="start">
									<g:hiddenField name="id" value="${session.id}" />
									<g:hiddenField name="expid" value="${entry.id}" />
									<g:if test="${entry.experimentStart.after(new Date())}">
										<g:actionSubmit action="start" value="Start now" class="btn btn-default"/>
									</g:if>
								</g:form>
								<div id="chartContainer" data-url="${createLinkTo(dir: '/stomp', absolute: true)}" data-authentication-token="${authentication_token}" data-session="${session.id}" data-experiment="${entry.id}" data-experiment-link="${entry.link}" data-chart-image-url="${createLinkTo(dir:'images/amcharts/')}">
									<div id="sessionChart-${entry.id}" class="sessionChart"></div>
								</div>
								<div id="user-inputs" style="margin-top:3em; max-width:800px;" class="panel panel-default">
									<div class="panel-heading">
										<h3 class="panel-title">User Inputs</h3>
									</div>
									<div class="panel-body">
										<pre id="user-inputs-json"></pre>
									</div>
								</div>
							</div>
						</g:if>
					</g:each>
				</div>
			</div>
		</div>
		<asset:javascript src="liveview.js" />
	</body>
</html>
