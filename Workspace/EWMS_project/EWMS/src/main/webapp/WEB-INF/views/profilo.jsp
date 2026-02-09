<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="mx-2 my-2">
    <c:if test="${error != null}">
        <div class="error-banner">
            <p>${error}</p>
        </div>
    </c:if>
    <div class="my-1">
        <h1>${sessionScope.utente.nome} ${sessionScope.utente.cognome}</h1>
        <span class="ms-4">Matricola: #${sessionScope.utente.matricolaFormattata}</span>
    </div>
    <div class="my-1">
        <ul>
            <li><strong>Nome: </strong>${sessionScope.utente.nome}</li>
            <li><strong>Cognome: </strong>${sessionScope.utente.cognome}</li>
            <li><strong>Data di nascita: </strong>${sessionScope.utente.dataNasc}</li>
            <li><strong>E-mail: </strong>${sessionScope.utente.email}</li>
            <li><strong>Ruolo: </strong>${sessionScope.utente.ruolo}</li>
        </ul>
    </div>
    <div class="my-2">

        <a href="${pageContext.request.contextPath}/homepage">
            <button type="button"  class="btn btn-info mx-1">Torna alla Home</button>
        </a>
        <a href="" disabled>
            <button type="button" class="btn btn-warning mx-1">Modifica password</button>
        </a>
        <a href="LogoutServlet">
            <button type="button" class="btn btn-danger mx-1">Logout <i class="bi bi-box-arrow-right"></i></button>

        </a>
    </div>
</div>