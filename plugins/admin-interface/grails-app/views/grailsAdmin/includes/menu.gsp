<nav role="navigation" class="navbar navbar-default navbar-fixed-top">
		<g:link mapping="grailsAdminDashboard" class="navbar-brand"><g:message code="OGUMI Administration" /></g:link>
		<div class="container-fluid">
				<ul class="nav navbar-nav">
						<g:each var="group" in="${config.groups}">
							<g:if test="${group != 'Hidden'}">
								<li class="dropdown">
										<a href="#" class="dropdown-toggle" data-toggle="dropdown">${group} <b class="caret"></b></a>
										<ul class="dropdown-menu">
												<g:each var="domainClass" in="${config.getGroup(group)}">
														<li><g:link mapping="grailsAdminList" params="[slug: domainClass.slug]">${domainClass.className}</g:link></li>
												</g:each>
										</ul>
								</li>
							</g:if>
						</g:each>
				</ul>

				<div class="nav-text logout">
						<sec:ifLoggedIn>
							<g:link controller="logout"><sec:loggedInUserInfo field="username" /></g:link>
						</sec:ifLoggedIn>
						<sec:ifNotLoggedIn>
							<g:link controller='login' action='auth'>Login</g:link>
						</sec:ifNotLoggedIn>
				</div>
		</div>
</nav>
