<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${error != null}">
    <div class="error-banner mx-2">
        <p>${error}</p>
    </div>
</c:if>

<c:choose>

    <c:when test="${sessionScope.utente.ruolo != 'GESTORE'}">

        <div class="filtri_task">
            <ul class="nav nav-pills mb-3 nav-fill" id="pills-tab" role="tablist">

                <c:if test="${sessionScope.utente.ruolo == 'SUPERVISORE'}">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link" id="pills-done-tab" data-bs-toggle="pill" data-bs-target="#pills-done" type="button" role="tab" aria-controls="pills-done" aria-selected="false">
                            Task completati
                        </button>
                    </li>
                </c:if>

                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="pills-todo-tab" data-bs-toggle="pill" data-bs-target="#pills-todo" type="button" role="tab" aria-controls="pills-todo" aria-selected="false">
                        Task da completare
                    </button>
                </li>

                <li class="nav-item" role="presentation">
                    <button class="nav-link active" id="pills-ongoing-tab" data-bs-toggle="pill" data-bs-target="#pills-ongoing" type="button" role="tab" aria-controls="pills-ongoing" aria-selected="true">
                        Task in esecuzione
                    </button>
                </li>

                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="pills-hold-tab" data-bs-toggle="pill" data-bs-target="#pills-hold" type="button" role="tab" aria-controls="pills-hold" aria-selected="false">
                        Task in hold
                    </button>
                </li>

                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="pills-all-tab" data-bs-toggle="pill" data-bs-target="#pills-all" type="button" role="tab" aria-controls="pills-all" aria-selected="false">
                        Tutti i Task
                    </button>
                </li>
            </ul>
        </div>

        <div class="tab-content" id="pills-tabContent">

            <c:if test="${sessionScope.utente.ruolo == 'SUPERVISORE'}">
                <div class="tab-pane fade" id="pills-done" role="tabpanel" aria-labelledby="pills-done-tab" tabindex="0">
                    <ul class="list-group">
                        <c:forEach var="task" items="${taskList}">
                            <c:if test="${task.stato == 'COMPLETATO'}">
                                <li class="list-group-item list-group-item-success">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <span class="badge bg-success">Completato</span>
                                            <span class="badge bg-secondary">#${task.id}</span>
                                            <span class="fw-bold ms-2">${task.titolo}</span>
                                        </div>
                                        <a href="task-detail?action=taskDetail.jsp&id=${task.id}" class="btn btn-sm btn-primary rounded-pill">Dettagli</a>
                                    </div>
                                    <div class="mt-2 text-muted small">${task.istruzioni}</div>
                                </li>
                            </c:if>
                        </c:forEach>
                    </ul>
                </div>
            </c:if>

            <div class="tab-pane fade" id="pills-todo" role="tabpanel" aria-labelledby="pills-todo-tab" tabindex="0">
                <ul class="list-group">
                    <c:forEach var="task" items="${taskList}">
                        <c:if test="${task.stato == 'DA_COMPLETARE'}">
                            <li class="list-group-item">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <span class="badge bg-warning text-dark">Da Completare</span>
                                        <span class="badge bg-secondary">#${task.id}</span>
                                        <span class="fw-bold ms-2">${task.titolo}</span>
                                    </div>
                                    <a href="task-detail?action=taskDetail.jsp&id=${task.id}" class="btn btn-sm btn-primary rounded-pill">Dettagli</a>
                                </div>
                                <div class="mt-2">${task.istruzioni}</div>
                            </li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>

            <div class="tab-pane fade show active" id="pills-ongoing" role="tabpanel" aria-labelledby="pills-ongoing-tab" tabindex="0">
                <ul class="list-group">
                    <c:forEach var="task" items="${taskList}">
                        <c:if test="${task.stato == 'IN_ESECUZIONE'}">
                            <li class="list-group-item border-primary">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <span class="badge bg-primary">In Corso</span>
                                        <span class="badge bg-secondary">#${task.id}</span>
                                        <span class="fw-bold ms-2">${task.titolo}</span>
                                    </div>
                                    <a href="task-detail?action=taskDetail.jsp&id=${task.id}" class="btn btn-sm btn-primary rounded-pill">Dettagli</a>
                                </div>
                                <div class="mt-2">${task.istruzioni}</div>
                            </li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>

            <div class="tab-pane fade" id="pills-hold" role="tabpanel" aria-labelledby="pills-hold-tab" tabindex="0">
                <ul class="list-group">
                    <c:forEach var="task" items="${taskList}">
                        <c:if test="${task.stato == 'IN_SOSPENSIONE'}">
                            <li class="list-group-item list-group-item-warning">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <span class="badge bg-danger">Sospeso</span>
                                        <span class="badge bg-secondary">#${task.id}</span>
                                        <span class="fw-bold ms-2">${task.titolo}</span>
                                    </div>
                                    <a href="task-detail?action=taskDetail.jsp&id=${task.id}" class="btn btn-sm btn-primary rounded-pill">Dettagli</a>
                                </div>
                                <div class="mt-2 text-muted">${task.istruzioni}</div>
                            </li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>

            <div class="tab-pane fade" id="pills-all" role="tabpanel" aria-labelledby="pills-all-tab" tabindex="0">
                <ul class="list-group">
                    <c:forEach var="task" items="${taskList}">
                        <li class="list-group-item">
                            <div class="d-flex justify-content-between align-items-center">
                                <div>
                                    <span class="badge bg-dark">${task.stato}</span>
                                    <span class="badge bg-secondary">#${task.id}</span>
                                    <span class="fw-bold ms-2">${task.titolo}</span>
                                </div>
                                <a href="task-detail?action=taskDetail.jsp&id=${task.id}" class="btn btn-sm btn-outline-primary rounded-pill">Dettagli</a>
                            </div>
                            <div class="mt-2">${task.istruzioni}</div>
                        </li>
                    </c:forEach>
                </ul>
            </div>

        </div>
    </c:when>

    <c:otherwise>
        <ul class="list-group lista-utenti-gestore">
            <c:choose>
                <c:when test="${empty userList}">
                    <li class="list-group-item">Nessun utente trovato.</li>
                </c:when>
                <c:otherwise>
                    <c:forEach var="user" items="${userList}">
                        <li class="list-group-item">
                            <div class="badge text-bg-secondary">${user.nome} ${user.cognome} - #${user.matricolaFormattata} - ${user.ruolo}</div>
                            <a href="${pageContext.request.contextPath}/mod-profilo?action=modProfile.jsp&id=${user.matricola}">
                                <div class="badge rounded-pill text-bg-primary">Visualizza Profilo</div>
                            </a>
                        </li>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </ul>
    </c:otherwise>
</c:choose>

