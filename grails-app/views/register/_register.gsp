<%@ page defaultCodec="HTML" %>

<!DOCTYPE html>
<html>
	<head>
		<title><g:message code="ogumi.name"/> - <g:message code="ogumi.register.title"/></title>
		<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'bootstrap.min.css')}" />
		<asset:stylesheet src="register.css"/>
	</head>
	<body>
		<div class="container">
			<nav role="navigation" class="navbar navbar-default navbar-fixed-top">
				<a href="#" class="navbar-brand"><g:message code="ogumi.name"/></a>
				<div class="container-fluid">
					<ul class="nav navbar-nav">
						<li class="active">
							<a href="#"><g:message code="ogumi.register.title"/></a>
						</li>
					</ul>
				</div>
			</nav>
			<div>
				<h2><g:message code="ogumi.register.title"/></h2>
				<p><g:message code="ogumi.register.message"/></p>
			</div>
		</div>
	</body>
</html>
