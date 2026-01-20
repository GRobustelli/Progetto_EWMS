<c:if test="${sessionScope.utente.ruolo} != 'Gestore'"> <!--nel caso qualcuno per qualche strano motivo riesce ad accedere alla pagina senza essere un gestore-->
    <h1 align="center">Errore 401 - accesso non autorizzato</h1>
    <h2 align="center">Niente da vedere qui</h2>
</c:if>
<div class = "crea">
    <form action="AddUtenteControl?action=validateData" id="formAccount">
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
            <!-- Button trigger modal -->
            <button type="button" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#staticBackdrop">
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
                            <button type="submit" class="btn btn-outline-success" form="formAccount">Conferma</button>
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