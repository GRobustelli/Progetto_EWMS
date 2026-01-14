<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!Doctype html>
<html lang="it">
    <head>
        <meta charset="UTF-8">
        <title>EWMS - Login</title>
        <link rel = "stylesheet" href = "style.css">
    </head>
    <body class="login_page">
        <nav class="navbar">
            <div class="logo">
                <img src="logo_EWMS_crop.jpg" alt="logo"> <!--qui poi verrà inserita l'img del logo (croppato) di EWMS-->
                EWMS
            </div>
        </nav>

        <div class="login_form">
            <%String error = "Credenziali errate";%>
            <c:if test="${not empty error}">
                <div class="error-banner">
                    <p>${error}</p>
                </div>
            </c:if>
            <form name="form1" action="LoginControl" method="post">
                <div class="form-group">
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