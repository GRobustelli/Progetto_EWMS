<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %> <div class="crea-2">

    <script src="${pageContext.request.contextPath}/js/validazioneFormUtente.js"></script>

<c:if test="${sessionScope.utente.ruolo} != 'GESTORE'"> <!--nel caso qualcuno per qualche strano motivo riesce ad accedere alla pagina senza essere un gestore-->
    <h1 align="center">Errore 401 - accesso non autorizzato</h1>
    <h2 align="center">Niente da vedere qui</h2>
</c:if>
<div class = "crea">
    <form action="add-utente" id="formAccount" method="post">
        <input type="hidden" name="action" value="insertUtente">
        <div>
            <div class="crea-2">
                <h3>Nome</h3>
                <input type="text" name="nome" id="nome" placeholder="Nome">
                <small id="errorNome" style="color: red;"></small>
            </div>
            <div class="crea-2">
                <h3>Cognome</h3>
                <input type="text" name="cognome" id="cognome" placeholder="Cognome">
                <small id="errorCognome" style="color: red;"></small>
            </div>
        </div>
        <div>
            <div class="crea-2">
                <h3>Data di nascita</h3>
                <input type="date" name="data" id="data">
                <small id="errorData" style="color: red;"></small>
            </div>
            <div class="crea-2">
                <h3>E-mail</h3>
                <input type="text" name="email" id ="email" placeholder="nome.cognome@azienda.it">
                <small id="errorEmail" style="color: red;"></small>
            </div>
        </div>

        <div class="crea-2">
            <h3>Ruolo</h3>
            <select name="ruolo" id="ruoloSelect">
                <option value="DIPENDENTE">Dipendente</option>
                <option value="SUPERVISORE">Supervisore</option>
                <option value="GESTORE">Gestore</option>
            </select>
        </div>

        <div class="crea-2" id="divSupervisore" style="display: none;">
            <h3>Supervisore</h3>
            <select name="supervisore" id="supervisoreSelect">
                <option value="">-- Seleziona Supervisore --</option>
                <c:forEach var="sup" items="${informazioniSup}">
                    <option value="${sup.matricola}">${sup.nome} ${sup.cognome} - #${sup.matricolaFormattata}</option>
                </c:forEach>
            </select>
            <small id="errorSupervisore" style="color: red;"></small>
        </div>

        <div>
            <div class="crea-2">
                <h3>Password</h3>
                <input type="text" readonly name="password" id="passwordField">
                    <button type="button" class="btn btn-info" onclick="generaPassword()">Genera</button>
                    <small id="errorPassword" style="color: red;"></small>
            </div>
        </div>
        <div>
            <!-- Button trigger modal -->
            <button type="button" class="btn btn-success" id="btnOpenModal" data-bs-target="#staticBackdrop">
                Conferma
            </button>

            <!-- Modal -->
            <div class="modal fade" id="staticBackdrop" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h1 class="modal-title fs-5" id="staticBackdropLabel">Conferma</h1>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            Confermi di voler creare un nuovo account?
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-outline-danger" data-bs-dismiss="modal">Annulla</button>
                            <button type="button" class="btn btn-success" id="btnOpenModal" form="formAccount">Conferma</button>
                        </div>
                    </div>
                </div>
            </div>
            <a href="AddUtenteControl?action=homepage.jsp&id=${sessionScope.utente.matricola}">
                <button type="button" class="btn btn-danger">Annulla</button>
            </a>
        </div>
    </form>
</div>