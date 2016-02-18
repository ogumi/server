<%@ page defaultCodec="HTML" %>

<!DOCTYPE html>
<html>
	<head>
		<title>Results - ${session.toString()}</title>
		<g:javascript src="amcharts.js" />
		<g:javascript src="amexport/amexport.js" />
		<g:javascript src="amexport/canvg.js" />
		<g:javascript src="amexport/filesaver.js" />
		<g:javascript src="amexport/rgbcolor.js" />
		<g:javascript src="serial.js" />
		<g:javascript src="jquery-1.11.2.min.js" />
		<asset:javascript src="experimentChart.js" />
		<g:javascript src="bootstrap.min.js" />
		<link rel="shortcut icon" href="${assetPath(src: 'favicon.ico')}" type="image/x-icon">
		<link rel="apple-touch-icon" href="${assetPath(src: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${assetPath(src: 'apple-touch-icon-retina.png')}">
		<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'bootstrap.min.css')}" />
		<asset:stylesheet src="liveview.css"/>
	</head>
	<body>
		<div class="container">
			<nav role="navigation" class="navbar navbar-default navbar-fixed-top">
				<g:link mapping="grailsAdminDashboard" class="navbar-brand">
					<g:message code="grailsAdminPlugin.name" />
				</g:link>
				<div class="container-fluid">
					<ul class="nav navbar-nav">
						<li class="active">
							<a href="#">Results</a>
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
							<li class="active">Results</li>
					</ol>
				</div>
			</div>
			<div>
				<h2>Results of ${session.toString()}</h2>
				<div class="export">
					<a href="${exportLink}">Export as CSV</a>
				</div>
				<div id="player" class="list-group">
					<h3>Player</h3>
					<g:each var="player" in="${session.players}">
						<li class="list-group-item">${player.user.username}</li>
					</g:each>
				</div>
				<div id="userdata" class="panel-group">
					<h3>Questionnaires</h3>
					<g:each var="player" in="${session.players}">
						<div class="panel panel-default">
							<div class="panel-heading">
								<h4 class="panel-title">
									<a data-toggle="collapse" data-parent="#userdata" href="#user-${player.user.id}">${player.user.username}</a>
								</h4>
							</div>
							<div id="user-${player.user.id}" class="panel-collapse collapse">
								<div class="panel-body">
									<div class="panel-group" id="user-${player.user.id}-steps">
										<g:each var="step" status="istep" in="${session.sequence}">
											<g:if test="${(step instanceof com.naymspace.ogumi.server.domain.Questionnaire) || (step instanceof com.naymspace.ogumi.server.domain.IncentivizedTask)}">
												<div class="panel panel-default">
													<div class="panel-heading">
														<h4 class="panel-title">
															<a data-toggle="collapse" data-parent="#user-${player.user.id}-steps" href="#user-${player.user.id}-step-${istep}">${step.name}</a>
														</h4>
													</div>
													<div id="user-${player.user.id}-step-${istep}" class="panel-collapse collapse">
														<div class="panel-body">
															<dl class="dl-horizontal">
																<g:each var="question" in="${step.questions}">
																	<g:each var="answer" in="${data}">
																		<g:if test="${(answer.question == question) && (answer.user == player.user) && (answer.step == istep)}">
																			<dt>${question.question}</dt>
																			<dd>${answer.response}</dd>
																		</g:if>
																	</g:each>
																</g:each>
															</dl>
														</div>
													</div>
												</div>
											</g:if>
										</g:each>
									</div>
								</div>
							</div>
						</div>
					</g:each>
				</div>
				<div id="experiment-output" class="panel-group">
					<h3>Experiments</h3>
					<g:each var="step" status="istep" in="${session.sequence}">
						<g:if test="${(step instanceof com.naymspace.ogumi.server.domain.Experiment)}">
							<div id="experimentResultCharts">
							<g:each var="res" in="${experimentResults}">
									<g:if test="${(res.experiment.id == step.id)}">
										<h4>${step.name}</h4>
										<div id="chartContainer" data-results="${res.data}" data-chart-image-url="${createLinkTo(dir:'images/amcharts/')}">
											<div id="sessionChart-${step.id}" class="sessionChart"></div>
										</div>
									</g:if>
							</g:each>
							</div>
							<div class="panel panel-default">
								<div class="panel-heading">
									<h4 class="panel-title">
										<a data-toggle="collapse" data-parent="#experiment-output" href="#experiment-${step.id}">User Input</a>
									</h4>
								</div>
								<div id="experiment-${step.id}" class="panel-collapse collapse">
									<div class="panel-body">
										<ol id="experiment-user-input" class="list-group">
											<g:each var="input" in="${experimentData}">
												<g:if test="${(input.experiment.id == step.id)}">
													<li class="list-group-item" data-userinputid="${input.id}">
														<span>${input.player.username}: </span>
														<g:each var="variable" in="${input.effort}">
															<span>${variable.toString()}</span>
														</g:each>
													</li>
												</g:if>
											</g:each>
										</ol>
									</div>
								</div>
							</div>
						</g:if>
					</g:each>
				</div>
			</div>
		</div>
		<asset:javascript src="results.js" />
	</body>
</html>
