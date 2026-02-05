<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
            <form name="form1" action="LoginServlet" method="post"> <!--anche qui nel campo action intendo quale controller utilizzare-->
                <div class="form-group">                            <!--come prima, non hanno senso, sono dei placeholder-->
                    <label for="username">Email/Username</label>
                    <input type="text" id="username" name="username" required placeholder="Inserisci il tuo username">
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" required placeholder="Inserisci la tua password">
                </div>

                <button type="submit" class="btn-login">Accedi</button>
            </form>
        </div>
    </body>
</html>