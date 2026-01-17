<c:if test="${sessionScope.utente.ruolo} != 'Dipendente'">
    <h1 align="center">Errore 401 - accesso non autorizzato</h1>
    <h2 align="center">Niente da vedere qui</h2>
</c:if>
<div class="mod-pwd">
    <form action="ChangePwdControl" method="post" name="form">
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
    <a href="ChangePwdControl?action=modifyPwd">
        <button type="submit" class="btn btn-success">Conferma</button>
    </a>
</div>