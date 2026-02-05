<c:choose> <!--qui faccio la distinzione tra homepage di supervisore e dipendente (pressocché simili) da quella del gestore-->
    <c:when test="${sessionScope.utente.ruolo != 'Gestore'}">
        <div class="filtri_task">
            <ul class="nav nav-pills mb-3 nav-fill" id="pills-tab" role="tablist"><!--qui faccio i vari check per decidere cosa inserire nella navbar in base al tipo di utente che accede-->
                <c:if test="${sessionScope.utente.ruolo == SUPERVISORE}">
                    <li class="nav-item" role="presentation">
                        <a href="FilterControl?action=fetchTasks(done)">
                            <button class="nav-link" id="pills-done-tab" data-bs-toggle="pill" data-bs-target="#pills-done" type="button" role="tab" aria-controls="pills-done" aria-selected="true">Task completati</button>
                        </a>
                    </li>
                </c:if>

                <li class="nav-item" role="presentation">
                    <a href="FilterControl?action=fetchTasks(todo)">
                        <button class="nav-link" id="pills-todo-tab" data-bs-toggle="pill" data-bs-target="#pills-todo" type="button" role="tab" aria-controls="pills-todo" aria-selected="true">Task da completare</button>
                    </a>
                </li>
                <li class="nav-item active" role="presentation">
                    <a href="FilterControl?action=fetchTasks(ongoing)">
                        <button class="nav-link active" id="pills-ongoing-tab" data-bs-toggle="pill" data-bs-target="#pills-ongoing" type="button" role="tab" aria-controls="pills-ongoing" aria-selected="true">Task in esecuzione</button>
                    </a>
                </li>
                <li class="nav-item" role="presentation">
                    <a href="FilterControl?action=fetchTasks(hold)">
                        <button class="nav-link" id="pills-hold-tab" data-bs-toggle="pill" data-bs-target="#pills-hold" type="button" role="tab" aria-controls="pills-hold" aria-selected="true">Task in hold</button>
                    </a>
                </li>
                <li class="nav-item" role="presentation">
                    <a href="FilterControl?action=resetFilter">
                        <button class="nav-link" id="pills-reset-tab" data-bs-toggle="pill" data-bs-target="#pills-reset" type="button" role="tab" aria-controls="pills-reset" aria-selected="true">Annulla filtro</button>
                    </a>
                </li>
            </ul>
        </div>
        <c:if test="${error != null}">
            <div class="error-banner">
                <p>${error}</p>
            </div>
        </c:if>
        <div class="tab-content" id="pills-tabContent">
            <div class="tab-pane fade" id="pills-done" role="tabpanel" aria-labelledby="pills-done-tab" tabindex="0">
                <ul class="list-group">
                    <c:forEach var="task" items="${taskList}">
                        <li class="list-group-item">
                            <div class="badge text-bg-secondary">Task #${task.id}</div>
                            <div class="task-description">${task.descrizione}</div>
                            <a href="ViewTaskControl?action=taskDetail.jsp&id=${task.id}">
                                <div class="badge rounded-pill text-bg-primary">Dettagli</div>
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </div>
            <div class="tab-pane fade" id="pills-todo" role="tabpanel" aria-labelledby="pills-todo-tab" tabindex="0">
                <ul class="list-group">
                    <c:forEach var="task" items="${taskList}">
                        <c:if test="${empty taskList}">
                            <tr><td colspan="6">Nessun task trovato.</td></tr>
                        </c:if>
                        <li class="list-group-item">
                            <div class="badge text-bg-secondary">Task #${task.id}</div>
                            <div class="task-description">${task.descrizione}</div>
                            <a href="ViewTaskControl?action=taskDetail.jsp&id=${task.id}">
                                <div class="badge rounded-pill text-bg-primary">Dettagli</div>
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </div>
            <div class="tab-pane fade show active" id="pills-ongoing" role="tabpanel" aria-labelledby="pills-ongoing-tab" tabindex="0">
                <ul class="list-group">
                    <c:forEach var="task" items="${taskList}">
                        <c:if test="${empty taskList}">
                            <tr><td colspan="6">Nessun task trovato.</td></tr>
                        </c:if>
                        <li class="list-group-item">
                            <div class="badge text-bg-secondary">Task #${task.id}</div>
                            <div class="task-description">${task.descrizione}</div>
                            <a href="ViewTaskControl?action=taskDetail.jsp&id=${task.id}">
                                <div class="badge rounded-pill text-bg-primary">Dettagli</div>
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </div>
            <div class="tab-pane fade" id="pills-hold" role="tabpanel" aria-labelledby="pills-hold-tab" tabindex="0">
                <ul class="list-group">
                    <c:forEach var="task" items="${taskList}">
                        <c:if test="${empty taskList}">
                            <tr><td colspan="6">Nessun task trovato.</td></tr>
                        </c:if>
                        <li class="list-group-item">
                            <div class="badge text-bg-secondary">Task #${task.id}</div>
                            <div class="task-description">${task.descrizione}</div>
                            <a href="ViewTaskControl?action=taskDetail.jsp&id=${task.id}">
                                <div class="badge rounded-pill text-bg-primary">Dettagli</div>
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <ul class="list-group lista-utenti-gestore"><
            <c:forEach var="user" items="${userList}">
                <c:if test="${empty userList}">
                    <tr><td colspan="6">Nessun utente trovato.</td></tr>
                </c:if>
                <li class="list-group-item">
                    <div class="badge text-bg-secondary">${user.nome} ${user.cognome} - #${user.matricola} - ${user.ruolo}</div>
                    <a href="ModProfileControl?action=modProfile.jsp&id=${user.matricola}">
                        <div class="badge rounded-pill text-bg-primary">Visualizza Profilo</div>
                    </a>
                </li>
            </c:forEach>
        </ul>
    </c:otherwise>
</c:choose>

