<c:if test="${sessionScope.utente.ruolo} != 'Dipendente'">
    <h1 align="center">Errore 401 - accesso non autorizzato</h1>
    <h2 align="center">Niente da vedere qui</h2>
</c:if>
<div class="mod-pwd">
    <form action="ChangePwdControl" method="post" id="formPwd">
        <h2>Vecchia password</h2>
        <input type="text" placeholder="Inserisci la vecchia password" name="old">

        <h2>Nuova password</h2>
        <input type="text" placeholder="Inserisci la nuova password" name="new">

        <h2>Ripeti nuova password</h2>
        <input type="text" placeholder="Ripeti la vecchia password" name="repeat">
    </form>
</div>
<div class="profilo-utente3"><!--riutilizzo la stessa classe css, tanto sono una serie justified di buttons-->
    <a href="ChangePwdControl?action=profilo.jsp&id=${sessionScope.utente.matricola}">
        <button type="button" class="btn btn-danger">Annulla</button>
    </a>

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
                    Confermi di voler modificare la tua password?
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-danger" data-bs-dismiss="modal">Annulla</button>

                    <a href="ChangePwdControl?action=modifyPwd">
                        <button type="submit" class="btn btn-outline-success" form="formPwd">Conferma</button>
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>