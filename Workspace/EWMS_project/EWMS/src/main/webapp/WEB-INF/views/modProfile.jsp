<c:if test="${sessionScope.utente.ruolo} != 'Gestore'"> <!--nel caso qualcuno per qualche strano motivo riesce ad accedere alla pagina senza essere un gestore-->
    <h1 align="center">Errore 401 - accesso non autorizzato</h1>
    <h2 align="center">Niente da vedere qui</h2>
</c:if>
<div class="profilo-utente">
    <div class="profilo-utente1">
        <h1>${utenteTrovato.nome} ${utenteTrovato.cognome}</h1><!--dopo che ti passo l'id dell'utente da homepage.jsp come gestore,-->
        <span class="ms-4">ID: #${utenteTrovato.matricola}</span> <!--poi da backend con il DAO mi fai la ricerca nel DB dell'utente e me lo passi come utenteTrovato-->
    </div>
    <div class="profilo-utente2">
        <ul>
            <li>Data di nascita: ${utenteTrovato.dataNasc}</li>
            <li>E-mail: ${utenteTrovato.email}</li>
            <li>Ruolo: ${utenteTrovato.ruolo}</li>
        </ul>
    </div>
    <div class="profilo-utente3">
        <a href="ReplacePwdControl?action=generatePwd">
            <button type="button" class="btn-info">Genera password</button>
        </a>

        <a href="ChangeRoleControl?action=modifyRole">
            <button type="button" class="btn-warning">Cambia ruolo</button>
        </a>

        <!-- Button trigger modal -->
        <button type="button" class="btn btn-warning" data-bs-toggle="modal" data-bs-target="#staticBackdrop2">
            Cambia ruolo
        </button>

        <!-- Modal -->
        <div class="modal fade" id="staticBackdrop2" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1 class="modal-title fs-5" id="staticBackdropLabel">Conferma</h1>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        Confermi di voler modificare il ruolo di questo utente?
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-danger" data-bs-dismiss="modal">Annulla</button>
                        <a href="ChangeRoleControl?action=modifyRole">
                            <button type="button" class="btn btn-outline-success">Conferma</button>
                        </a>
                    </div>
                </div>
            </div>
        </div>



        <!-- Button trigger modal -->
        <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#staticBackdrop">
            Elimina account
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
                        Confermi di voler eliminare questo account?
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-danger" data-bs-dismiss="modal">Annulla</button>
                        <a href="DeleteAccountControl?action=delete&delID=${utenteTrovato.matricola}">
                            <button type="button" class="btn btn-outline-success">Conferma</button>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>