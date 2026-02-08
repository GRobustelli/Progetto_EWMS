<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %> <div class="crea-2">
<c:if test="${sessionScope.utente.ruolo} != 'SUPERVISORE'"> <!--nel caso qualcuno per qualche strano motivo riesce ad accedere alla pagina senza essere un gestore-->
    <h1 align="center">Errore 401 - accesso non autorizzato</h1>
    <h2 align="center">Niente da vedere qui</h2>
</c:if>
<div class="crea">
    <form action="crea-task" id="formTask" method="post">
        <input type="hidden" name="action" value="insertTask">
        <div>
            <div class="crea-2">
                <h3>Titolo Task</h3>
                <input type="text" name="titolo">
            </div>
            <div class="crea-2">
                <h3>Dipendente</h3>
                <select name = "dipendente">
                    <c:forEach var = "dipendente" items="${userList}">
                        <option value="${dipendente.matricola}">${dipendente.nome} ${dipendente.cognome} - #${dipendente.matricolaFormattata}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div>
            <div class="crea-2">
                <h3>Descrizione</h3>
                <textarea minlength="10" maxlength="2000" name = "istruzioni" placeholder="inserisci qui la descrizione del task (max. 2000 caratteri)..."></textarea>
            </div>
        </div>
        <div>
            <div class="crea-2">
                <h3>Data di Scadenza</h3>
                <input type="date" name = "dataScadenza">
            </div>
            <div class="crea-2">
                <h3>Priorità</h3>
                <select name = "priorita">
                    <option value="ALTA">ALTA</option>
                    <option value="MEDIA">MEDIA</option>
                    <option value="BASSA">BASSA</option>
                </select>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/homepage">
            <button type="button"  class="btn btn-danger">Annulla</button>
        </a>

        <!-- Button trigger modal -->
        <button type="button" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#staticBackdrop">
            Crea Task
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
                        Confermi di voler creare una nuova task?
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-danger" data-bs-dismiss="modal">Annulla</button>
                        <button type="submit" class="btn btn-outline-success" form="formTask">Conferma</button>
                    </div>
                </div>
            </div>
        </div>
    </form>
</div>