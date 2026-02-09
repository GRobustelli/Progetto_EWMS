<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page isErrorPage="true"%>
<div>
    <p class="error-banner">Errore 404 - pagina inesistente</p>

    <a href="${pageContext.request.contextPath}/homepage">
        <button type="button"  class="btn btn-info mx-1">Torna alla Home</button>
    </a>
</div>

