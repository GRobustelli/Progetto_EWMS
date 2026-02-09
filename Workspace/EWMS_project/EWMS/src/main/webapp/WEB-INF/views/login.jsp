<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div>
    <div class="login_form">
        <c:if test="${error != null}">
            <div class="error-banner mx-2">
                <p>${error}</p>
            </div>
        </c:if>
        <form name="form1" action="LoginServlet" method="post">
            <div class="form-group">
                <div align="center" class="my-sm-1">Email/Username</div>
                <input type="text" id="username" name="username" required placeholder="Inserisci il tuo username">
            </div>
            <div class="form-group">
                <div align="center" class="my-sm-1">Password</div>
                <input type="password" id="password" name="password" required placeholder="Inserisci la tua password">
            </div>

            <button type="submit" class="btn-login">Accedi</button>
        </form>
    </div>
</div>