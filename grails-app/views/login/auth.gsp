<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="empty"/>
  <title>Login</title>
</head>
<body class="login">
    <form class="form-signin" method="POST" action="${resource(file: 'j_spring_security_check')}">
        <g:if test="${params.login_error}">
            <div class="alert alert-danger" role="alert">
                <strong>Invalid username and password.</strong>
            </div>
        </g:if>
        <h2 class="form-signin-heading">Please sign in</h2>
        <label for="j_username" class="sr-only">Username</label>
        <input type="text" name="j_username" id="j_username" class="form-control" placeholder="Username" required autofocus>
        <label for="j_password" class="sr-only">Password</label>
        <input type="password" name="j_password" id="j_password" class="form-control" placeholder="Password" required>
        <div class="checkbox">
            <label>
                <input type="checkbox" name="_spring_security_remember_me" value="remember-me"> Remember me
            </label>
        </div>
        <button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
    </form>
</body>
</html>