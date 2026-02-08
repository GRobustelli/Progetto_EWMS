<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<script src="${pageContext.request.contextPath}/js/validazioneFormLogin.js"></script>


<!Doctype html>
<html lang="it">
    <head>
        <meta charset="UTF-8">
        <title>EWMS - Login</title>
        <link rel = "stylesheet" href = "style.css">
    </head>
    <body class="login_page">

        <div class="login_form">
            <c:if test="${error != null}">
                <div class="error-banner">
                    <p>${error}</p>
                </div>
            </c:if>
            <form name="form1" action="LoginServlet" method="post" onsubmit="return validateForm()"> <!--anche qui nel campo action intendo quale controller utilizzare-->
                <div class="form-group">                            <!--come prima, non hanno senso, sono dei placeholder-->
                    <label for="username">Email/Username</label>
                    <input type="text" id="username" name="username"  placeholder="Inserisci il tuo username">
                    <small id="error-username" class="error-msg" style="color: red"></small>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password"  placeholder="Inserisci la tua password">
                    <small id="error-password" class="error-msg" style="color: red"></small>
                </div>

                <button type="submit" class="btn-login">Accedi</button>
            </form>
        </div>
    </body>
</html>