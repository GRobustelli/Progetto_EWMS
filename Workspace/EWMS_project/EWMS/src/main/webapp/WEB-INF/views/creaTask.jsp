<c:if test="${sessionScope.utente.ruolo} != 'Supervisore'"> <!--nel caso qualcuno per qualche strano motivo riesce ad accedere alla pagina senza essere un gestore-->
    <h1 align="center">Errore 401 - accesso non autorizzato</h1>
    <h2 align="center">Niente da vedere qui</h2>
</c:if>
<div class="crea">
    <form action="CreateTaskControl?action=validateData">
        <div>
            <div class="crea-2">
                <h3>Titolo Task</h3>
                <input type="text">
            </div>
            <div class="crea-2">
                <h3>Dipendente</h3>
                <select>
                    <c:forEach var = "utente" items="${userList}">
                        <option value="${utente.matricola}">${utente.nome} ${utente.cognome} - #${utente.matricola}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div>
            <div class="crea-2">
                <h3>Descrizione</h3>
                <textarea minlength="10" maxlength="500" placeholder="inserisci qui la descrizione del task (max. 500 caratteri)..."></textarea>
            </div>
        </div>
        <div>
            <div class="crea-2">
                <h3>Data di Scadenza</h3>
                <input type="date">
            </div>
            <div class="crea-2">
                <h3>Priorità</h3>
                <select>
                    <option value="Alta">ALTA</option>
                    <option value="Bassa">BASSA</option>
                </select>
            </div>
        </div>
        <a href="CreaTaskControl?action=homepage.jsp&id=${sessionScope.utente.matricola}">
            <button type="button" class="btn btn-danger">Annulla</button>
        </a>
        <input type="submit" class="btn btn-success" value="Crea Task">
    </form>
</div>