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
            <!--<li>Supervisore: ${utenteTrovato.supervisore}</li> chiedimelo di persona questo-->
        </ul>
    </div>
    <div class="profilo-utente3">
        <a href="ReplacePwdControl?action=generatePwd">
            <button type="button" class="btn-info">Genera password</button>
        </a>

        <a href="ChangeRoleControl?action=modifyRole">
            <button type="button" class="btn-warning">Cambia ruolo</button>
        </a>

        <a href="DeleteAccountControl?action=delete&delID=${utenteTrovato.matricola}">
            <button type="button" class="btn-info">Genera password</button>
        </a>
    </div>
</div>