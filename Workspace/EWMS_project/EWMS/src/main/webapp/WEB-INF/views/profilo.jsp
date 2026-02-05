<div class="profilo-utente">
    <div class="profilo-utente1">
        <h1>${sessionScope.utente.nome} ${sessionScope.utente.cognome}</h1>
        <span class="ms-4">ID: #${sessionScope.utente.matricola}</span>
    </div>
    <div class="profilo-utente2">
        <ul>
            <li>Data di nascita: ${sessionScope.utente.dataNasc}</li>
            <li>E-mail: ${sessionScope.utente.email}</li>
        </ul>
    </div>
    <div class="profilo-utente3">
        <a href="" disabled>
            <button type="button" class="btn btn-info">Modifica password</button>
        </a>
        <a href="LogoutServlet"> <!--da vedere un attimo-->
            <button type="button" class="btn btn-danger">Logout</button>
            <i class="bi bi-box-arrow-right"></i>
        </a>
    </div>
</div>