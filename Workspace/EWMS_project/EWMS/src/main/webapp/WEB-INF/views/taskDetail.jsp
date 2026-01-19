<div class = "task-detail">
    <div class = "task1">
        <h1>${task.titolo}</h1>
        <span>ID: #${task.id}</span>
        <c:if test="${sessionScope.utente.ruolo != 'Supervisore'}"}>
            <h3>Supervisore: ${task.supervisore}</h3>
        </c:if>
    </div>
    <hr>
    <div class = "task2">
        <h2>Descrizione: </h2>
        <div class = "task-descrizione">${task.istruzioni}</div>
        <h3>Data assegnazione: ${task.dataCreazione}</h3>
        <h3>Data scadenza: ${task.dataDiScadenza}</h3>
    </div>
    <div class = "task-btns"><!--rendere i button responsive rispetto al context del task-->
        <c:choose>
            <c:when test="${sessionScope.utente.ruolo == 'Dipendente'}">
                <a href="InizializzaControl?action=inizializzaTask&idTask=${task.id}">
                    <button type="button" class = "btn btn-success" <c:if test = "${task.stato != 'DA_COMPLETARE'}">disabled</c:if>>Avvia Task</button>
                </a>
            </c:when>
            <c:otherwise>
                <c:if test = "${task.stato != 'COMPLETATO'}">
                    <a href="WarningControl?action=sendWarning">
                        <button class="btn btn-warning">Comunica</button>
                    </a>
                </c:if>
            </c:otherwise>
        </c:choose>

        <a href="HoldControl?action=holdTask&idTask=${task.id}" <c:if test = "${task.stato != 'IN_ESECUZIONE'}">disabled</c:if>>
            <button type="button" class = "btn btn-warning" >Sospendi Task</button>
        </a>

        <c:choose>
            <c:when test="${sessionScope.utente.ruolo == 'Dipendente'}">
                <a href="CompleteTaskControl?action=completeTask&idTask=${task.id}" <c:if test = "${task.stato == 'COMPLETATO'}">hidden</c:if>>
                    <button type="button" class = "btn btn-danger">Completa Task</button>
                </a>
            </c:when>
            <c:otherwise>
                <c:if test = "${task.stato != 'COMPLETATO'}">
                    <a href="DeleteTaskControl?action=deleteTask&idTask=${task.id}">
                        <button type="button" class="btn btn-warning">Elimina Task</button>
                    </a>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
</div>