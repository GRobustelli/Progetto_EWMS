/*
        <small id="errorTitolo" style="color: red;"></small> per messaggio errore titolo
        <small id="errorDipendente" style="color: red;"></small> per messaggio errore non selezione dipendente
        <small id="errorDataScadenza" style="color: red;"></small> per errore data
        <small id="errorDescrizione" style="color: red;"></small>
        <small id="errorPriorita" style="color: red;"></small>
 */

function isNotEmpty(value) {
    return value !== null && value.trim().length > 0;
}

function isValidTitolo(value) {
    const regex = /^[a-zA-Z0-9\s.,\-_!#'?àèéìòùÀÈÉÌÒÙ]{1,50}$/;
    return regex.test(value);
}

function isFutureOrToday(value) {
    if (!value) return false;
    const selectedDate = new Date(value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return selectedDate >= today;
}

document.addEventListener("DOMContentLoaded", function() {

    // 1. PRENDIAMO IL BOTTONE CHE APRE (Crea Task)
    const btnOpenTaskModal = document.getElementById('btnOpenTaskModal');

    if (btnOpenTaskModal) {
        btnOpenTaskModal.addEventListener('click', function() {

            let valid = true;

            // --- VALIDAZIONI (Copia-Incolla della tua logica) ---

            // TITOLO
            const titoloInput = document.getElementById('titoloTask');
            const errorTitolo = document.getElementById('errorTitolo');
            errorTitolo.innerText = "";
            titoloInput.style.borderColor = "";

            if (!isNotEmpty(titoloInput.value)) {
                errorTitolo.innerText = "Il titolo è obbligatorio.";
                titoloInput.style.borderColor = "red";
                valid = false;
            } else if (!isValidTitolo(titoloInput.value)) {
                errorTitolo.innerText = "Titolo non valido.";
                titoloInput.style.borderColor = "red";
                valid = false;
            }

            // DIPENDENTE
            const dipendenteSelect = document.getElementById('dipendenteSelect');
            const errorDipendente = document.getElementById('errorDipendente');
            errorDipendente.innerText = "";
            dipendenteSelect.style.borderColor = "";

            if (dipendenteSelect.value === "" || dipendenteSelect.value === null) {
                errorDipendente.innerText = "Seleziona un dipendente.";
                dipendenteSelect.style.borderColor = "red";
                valid = false;
            }

            // DATA
            const dataInput = document.getElementById('dataScadenza');
            const errorData = document.getElementById('errorDataScadenza');
            errorData.innerText = "";
            dataInput.style.borderColor = "";

            if (!dataInput.value) {
                errorData.innerText = "Data obbligatoria.";
                dataInput.style.borderColor = "red";
                valid = false;
            } else if (!isFutureOrToday(dataInput.value)) {
                errorData.innerText = "Data non valida (passato).";
                dataInput.style.borderColor = "red";
                valid = false;
            }

            // DESCRIZIONE
            const descInput = document.getElementById('descrizioneTask');
            const errorDesc = document.getElementById('errorDescrizione');
            const descValue = descInput.value.trim();
            errorDesc.innerText = "";
            descInput.style.borderColor = "";

            if (descValue.length === 0) {
                errorDesc.innerText = "Descrizione obbligatoria.";
                descInput.style.borderColor = "red";
                valid = false;
            } else if (descValue.length < 10) {
                errorDesc.innerText = "Minimo 10 caratteri.";
                descInput.style.borderColor = "red";
                valid = false;
            } else if (descValue.length > 2000) {
                errorDesc.innerText = "Massimo 2000 caratteri.";
                descInput.style.borderColor = "red";
                valid = false;
            }

            // PRIORITÀ
            const prioritaSelect = document.getElementById('prioritaSelect');
            const errorPriorita = document.getElementById('errorPriorita');
            errorPriorita.innerText = "";
            prioritaSelect.style.borderColor = "";

            if (!isNotEmpty(prioritaSelect.value)) {
                errorPriorita.innerText = "Seleziona priorità.";
                prioritaSelect.style.borderColor = "red";
                valid = false;
            }

            // --- FINE VALIDAZIONI ---

            // PUNTO CHIAVE: Se è valido, apriamo il modale MANUALMENTE
            if (valid) {
                const myModal = new bootstrap.Modal(document.getElementById('staticBackdrop'));
                myModal.show();
            } else {
                console.log("Validazione fallita, il modale non si apre.");
            }
        });
    }

    // 2. GESTIONE SUBMIT FINALE (Il bottone dentro il modal)
    const btnSubmitTask = document.getElementById('btnSubmitTask');
    const formTask = document.getElementById('formTask');

    if (btnSubmitTask && formTask) {
        btnSubmitTask.addEventListener('click', function() {
            formTask.submit(); // Spedisce il form al server
        });
    }
});