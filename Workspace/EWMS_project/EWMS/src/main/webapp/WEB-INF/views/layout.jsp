<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!Doctype html>
<html lang="it">
    <head>
        <meta charset="UTF-8">
        <title>EWMS</title>
        <link rel = "stylesheet" href = "style.css">
    </head>
    <body>
        <nav class="navbar">

            <div class="logo">
                <img src="logo_EWMS_crop.jpg" alt="logo"> <!--qui poi verrà inserita l'img del logo (croppato) di EWMS-->
                EWMS
            </div>

            <div class="menu-links">

                <c:if test="${viewPath != 'views/homepage.jsp'}">
                <a href="LoginControl?action=home" class="btn-create">
                    <img src="imgs/home.jpg" alt="home">
                </a>
                </c:if>

                <!--dopo inserirò le immagini nella cartella imgs-->
                <c:if test="${sessionScope.utente.ruolo == 'Supervisore'}">
                    <c:if test="${viewPath != 'views/crea_task.jsp'}"></c:if>
                    <a href="CreaTaskControl?action=viewCreate" class="btn-create" >
                        <img src="imgs/create_task.jpg" alt="crea_task">
                    </a>
                </c:if>


                <a href="NotificationControl?action=notifications" class="btn-create">
                    <img src="imgs/bell.jpg" alt="notifiche">
                </a>

                <c:if test="${viewPath != 'views/profilo.jsp'}">
                    <a href="ViewProfileControl?action=profile">
                        <img src="imgs/profile.jpg" alt="profilo">
                    </a>
                </c:if>

            </div>
        </nav>


        <c:choose>
            <c:when test="${not empty viewPath}">
                <jsp:include page="${viewPath}" />
            </c:when>
            <c:otherwise>
                <div class="alert alert-warning">Nessun contenuto da visualizzare.</div> <!--Error 404-->
            </c:otherwise>
        </c:choose>
    </body>
    <footer>
        <p>&copy; 2025 EWMS System</p>
    </footer>
</html>