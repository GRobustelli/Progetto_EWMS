<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!--Questi sono gli scheletri delle jsp, puoi ovviamente modificare qualsiasi cosa non ti permetta di procedere
fluidamente con la programmazione, se hai dubbi chiedimi.

in particolare, come avevamo discusso, ho creato layout.jsp per evitare di dover riscrivere sempre la stessa pagina -->

<!Doctype html>
<html lang="it">
    <head>
        <meta charset="UTF-8">
        <title>EWMS</title>
        <!--<link rel = "stylesheet" href = "style.css">-->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
    </head>
    <body>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js" integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
        <nav class="navbar">

            <div class="logo">
                <img src="logo_EWMS_crop.jpg" alt="logo"> <!--qui poi verrà inserita l'img del logo (croppato) di EWMS-->
                EWMS
            </div>

            <div class="menu-links">

                <c:if test="${viewPath != 'views/homepage.jsp'}">
                <a href="LoginControl?action=homepage.jsp&id=${sessionScope.utente.matricola}" class="btn-create"><!--nell'href ho scritto roba a caso per quando farai il backend-->
                    <i class="bi bi-house-door"></i>                                                              <!--non hanno senso logico definito, sono li giusto per aiutarti a capire cosa dovrebbe fare-->
                </a>
                </c:if>

                <c:if test="${sessionScope.utente.ruolo == 'Supervisore'}">
                    <c:if test="${viewPath != 'views/creaTask.jsp'}">
                        <a href="CreaTaskControl?action=creaTask.jsp" class="btn-create" >
                            <i class="bi bi-plus-circle"></i>
                        </a>
                    </c:if>
                </c:if>

                <c:if test="${sessionScope.utente.ruolo != 'Gestore'}">
                    <a href="NotificationControl?action=notifications" class="btn-create">
                        <i class="bi bi-bell"></i>
                    </a>
                </c:if>
                </div>

                <c:if test="${sessionScope.utente.ruolo == 'Gestore'}">
                    <c:if test="${viewPath != 'views/creaAccount.jsp'}">
                        <a href="AddUtenteControl?action=addAccount" class="btn-create">
                            <i class="bi bi-plus-circle"></i>
                        </a>
                    </c:if>
                </c:if>

                <c:if test="${viewPath != 'views/profilo.jsp'}">
                    <a href="ViewProfileControl?action=profilo.jsp&id=${sessionScope.utente.matricola}">
                        <i class="bi bi-person-square"></i>
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
        <p>&copy; 2025 EWMS</p>
    </footer>
</html>