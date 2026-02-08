<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${error != null}">
    <div class="error-banner">
        <p>${error}</p>
    </div>
</c:if>

<div class = "task-detail">
    <div class = "task1">
        <h1>${task.titolo}</h1>
        <span>ID: #${task.id}</span>
        <c:choose>
        <c:when test="${sessionScope.utente.ruolo != 'SUPERVISORE'}">
            <h3>Supervisore: #${task.supervisoreFormattato}</h3>
        </c:when>
            <c:otherwise>
                <h3>Dipendente: #${task.dipendenteFormattato}</h3>
            </c:otherwise>

        </c:choose>

    </div>
    <hr>
    <div class = "task2">
        <h2>Descrizione: </h2>
        <div class = "task-descrizione">${task.istruzioni}</div>
        <h3>Data assegnazione: ${task.dataCreazione}</h3>
        <h3>Data scadenza: ${task.dataDiScadenza}</h3>
        <h3>Stato: ${task.stato}</h3>
    </div>
    <div class = "task-btns"><!--rendere i button responsive rispetto al context del task-->
        <c:choose>
            <c:when test="${sessionScope.utente.ruolo == 'DIPENDENTE'}">

                <!-- Button trigger modal -->
                <button type="button" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#staticBackdrop5" <c:if test = "${task.stato == 'COMPLETATO'}">disabled</c:if>>
                    Avvia Task
                </button>

                <!-- Modal -->
                <div class="modal fade" id="staticBackdrop5" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel5" aria-hidden="true">
                    <div class="modal-dialog modal-dialog-centered modal-lg">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h1 class="modal-title fs-5" id="staticBackdropLabel5">Conferma</h1>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                Confermi di voler avviare questo task?
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-outline-danger" data-bs-dismiss="modal">Annulla</button>
                                <a href="init-task?id=${task.id}">
                                    <button type="button" class="btn btn-outline-success">Conferma</button>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <c:if test = "${task.stato != 'COMPLETATO'}">
                    <!-- Button trigger modal
                    <button type="button" class="btn btn-outline-warning" data-bs-toggle="modal" data-bs-target="#staticBackdrop2" disabled> --bottone gambizzato
                        Comunica
                    </button>
                    +-------------------------------------------------------------------------------------+
                    --qui sotto si trova il modal per l'invio dei warning, lo commento per ignorarlo
                    +-------------------------------------------------------------------------------------+

                    -- Modal | non si apre
                    <div class="modal fade" id="staticBackdrop2" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel2" aria-hidden="true">
                        <div class="modal-dialog modal-dialog-centered modal-lg">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h1 class="modal-title fs-5" id="staticBackdropLabel2">Comunicazione al dipendente ${task.dipendente}</h1>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                </div>
                                <div class="modal-body">
                                    <div class="form-floating">
                                        <form action="" method="post" id="formWarning"> --levo l'action per sicurezza, perché essendo priorità media non lo implementiamo
                                            <textarea class="form-control" placeholder="Inserisci il corpo della comunicazione" id="floatingTextarea2" style="height: 200px"></textarea>
                                            <label for="floatingTextarea2">Descrizione</label>
                                        </form>
                                    </div>

                                    -- Button trigger modal
                                    <button type="button" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#staticBackdrop4" disabled> --bottone gambizzato
                                        Invia comunicazione
                                    </button>

                                    -- Modal | tanto non appare
                                    <div class="modal fade" id="staticBackdrop4" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel4" aria-hidden="true">
                                        <div class="modal-dialog modal-dialog-centered modal-lg">
                                            <div class="modal-content">
                                                <div class="modal-header">
                                                    <h1 class="modal-title fs-5" id="staticBackdropLabel4">Conferma</h1>
                                                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                </div>
                                                <div class="modal-body">
                                                    Confermi di voler inviare questa comunicazione sul task?
                                                </div>
                                                <div class="modal-footer">
                                                    <button type="button" class="btn btn-outline-danger" data-bs-dismiss="modal">Annulla</button>
                                                    <button type="submit" class="btn btn-outline-success" form="formWarning">Conferma</button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-outline-danger" data-bs-dismiss="modal">Annulla</button>
                                </div>
                            </div>
                        </div>
                    </div>-->

                </c:if>
            </c:otherwise>
        </c:choose>

        <!-- Button trigger modal -->
        <button type="button" class="btn btn-warning" data-bs-toggle="modal" data-bs-target="#staticBackdrop1" <c:if test = "${task.stato != 'IN_ESECUZIONE'}">disabled</c:if>>
            Sospendi task
        </button>

        <!-- Modal -->
        <div class="modal fade" id="staticBackdrop1" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1 class="modal-title fs-5" id="staticBackdropLabel1">Conferma</h1>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        Confermi di voler sospendere questa task?
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-danger" data-bs-dismiss="modal">Annulla</button>
                        <a href="hold-task?id=${task.id}">
                            <button type="submit" class="btn btn-outline-success" form="formTask">Conferma</button>
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <c:choose>
            <c:when test="${sessionScope.utente.ruolo == 'DIPENDENTE'}">
                <!-- Button trigger modal -->
                <button type="button" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#staticBackdrop6" <c:if test = "${task.stato == 'IN_SOSPENSIONE'}">disabled</c:if>>
                    Completa Task
                </button>

                <!-- Modal -->
                <div class="modal fade" id="staticBackdrop6" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel6" aria-hidden="true">
                    <div class="modal-dialog modal-dialog-centered modal-lg">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h1 class="modal-title fs-5" id="staticBackdropLabel6">Conferma</h1>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                Confermi di voler completare questa task?
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-outline-danger" data-bs-dismiss="modal">Annulla</button>
                                <a href="complete-task?id=${task.id}" <c:if test = "${task.stato == 'COMPLETATO'}">hidden</c:if>>
                                    <button type="button" class = "btn btn-outline-success">Conferma</button>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <c:if test = "${task.stato != 'COMPLETATO'}">
                    <!-- Button trigger modal -->
                    <button type="button" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#staticBackdrop7">
                        Elimina Task
                    </button>

                    <!-- Modal -->
                    <div class="modal fade" id="staticBackdrop7" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel7" aria-hidden="true">
                        <div class="modal-dialog modal-dialog-centered modal-lg">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <h1 class="modal-title fs-5" id="staticBackdropLabel7">Conferma</h1>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                </div>
                                <div class="modal-body">
                                    Confermi di voler eliminare questa task?
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-outline-danger" data-bs-dismiss="modal">Annulla</button>
                                    <a href="delete-task?id=${task.id}">
                                        <button type="button" class="btn btn-outline-success">Elimina Task</button>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </div>
</div>