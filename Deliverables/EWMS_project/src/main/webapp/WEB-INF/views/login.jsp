<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<script src="${pageContext.request.contextPath}/js/validazioneFormLogin.js"></script>

<div>
    <div class="login_form">
        <c:if test="${error != null}">
            <div class="error-banner mx-2">
                <p>${error}</p>
            </div>
        </c:if>
        <form name="form1" action="LoginServlet" method="post" onSubmit="return validateForm()">
            <div class="form-group">
                <div align="center" class="my-sm-1">Email/Username</div>
                <input type="text" id="username" name="username"  placeholder="Inserisci il tuo username">
                <small id="error-username" class="error-msg" style="color: red"></small>
            </div>
            <div class="form-group">
                <div align="center" class="my-sm-1">Password</div>
                <input type="password" id="password" name="password"  placeholder="Inserisci la tua password">
                <small id="error-password" class="error-msg" style="color: red"></small>
            </div>

            <button type="submit" class="btn-login">Accedi</button>
        </form>
    </div>
</div>