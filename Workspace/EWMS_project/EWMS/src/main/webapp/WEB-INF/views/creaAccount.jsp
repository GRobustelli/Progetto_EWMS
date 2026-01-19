<c:if test="${sessionScope.utente.ruolo} != 'Gestore'"> <!--nel caso qualcuno per qualche strano motivo riesce ad accedere alla pagina senza essere un gestore-->
    <h1 align="center">Errore 401 - accesso non autorizzato</h1>
    <h2 align="center">Niente da vedere qui</h2>
</c:if>
<div class = "crea">
    <form action="AddUtenteControl?action=validateData">
        <div>
            <div class="crea-2">
                <h3>Nome</h3>
                <input type="text">
            </div>
            <div class="crea-2">
                <h3>Cognome</h3>
                <input type="text">
            </div>
            <div class="crea-2">
                <h3>Cognome</h3>
                <input type="text">
            </div>
        </div>
        <div>
            <div class="crea-2">
                <h3>Data di nascita</h3>
                <input type="date">
            </div>
            <div class="crea-2">
                <h3>E-mail</h3>
                <input type="text">
            </div>
        </div>
        <div>
            <div class="crea-2">
                <h3>Matricola</h3>
                <input type="text">
            </div>
            <div class="crea-2">
                <h3>Ruolo</h3>
                <select>
                    <option value="Dipendente">Dipendente</option>
                    <option value="Supervisore">Supervisore</option>
                </select>
            </div>
        </div>
        <div>
            <div class="crea-2">
                <h3>Password</h3>
                <input type="text" disabled>
                <a href = "AddUtenteControl?action=generatePwd">
                    <button type="button" class="btn btn-info">Genera</button>
                </a>
            </div>
        </div>
        <div>
            <input type="submit" class="btn btn-success" value="Crea account">
            <a href="AddUtenteControl?action=homepage.jsp&id=${sessionScope.utente.matricola}">
                <button type="button" class="btn btn-danger">Annulla</button>
            </a>
        </div>
    </form>
</div>