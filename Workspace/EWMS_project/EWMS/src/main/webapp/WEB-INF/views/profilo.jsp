<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="profilo-utente">
    <c:if test="${error != null}">
        <div class="error-banner">
            <p>${error}</p>
        </div>
    </c:if>
    <div class="profilo-utente1">
        <h1>${sessionScope.utente.nome} ${sessionScope.utente.cognome}</h1>
        <span class="ms-4">Matricola: #${sessionScope.utente.matricolaFormattata}</span>
    </div>
    <div class="profilo-utente2">
        <ul>
            <li>Nome: ${sessionScope.utente.nome}</li>
            <li>Cognome: ${sessionScope.utente.cognome}</li>
            <li>Data di nascita: ${sessionScope.utente.dataNasc}</li>
            <li>E-mail: ${sessionScope.utente.email}</li>
            <li>Ruolo: ${sessionScope.utente.ruolo}</li>
        </ul>
    </div>
    <div class="profilo-utente3">

        <a href="${pageContext.request.contextPath}/homepage">
            <button type="button"  class="btn btn-danger">Torna alla Home</button>
        </a>
        <a href="" disabled>
            <button type="button" class="btn btn-info">Modifica password</button>
        </a>
        <a href="LogoutServlet"> <!--da vedere un attimo-->
            <button type="button" class="btn btn-danger">Logout</button>
            <i class="bi bi-box-arrow-right"></i>
        </a>
    </div>
</div>