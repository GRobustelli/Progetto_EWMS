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

    const regex = /^[a-zA-Z0-9\s.,\-_!]{1,50}$/;
    return regex.test(value);
}

function isFutureOrToday(value) {
    if (!value) return false;
    const selectedDate = new Date(value);
    const today = new Date();

    // Azzeriamo l'orario di "oggi" per fare un confronto solo sulla data (giorno/mese/anno)
    today.setHours(0, 0, 0, 0);

    return selectedDate >= today;
}



document.addEventListener("DOMContentLoaded", function() {

    const form = document.getElementById('formTask');

    if (form) {
        form.addEventListener('submit', function(event) {
            let valid = true;

            // --- 1. VALIDAZIONE TITOLO ---
            const titoloInput = document.getElementById('titoloTask');
            const errorTitolo = document.getElementById('errorTitolo');
            const titoloValue = titoloInput.value;

            // Reset stile
            errorTitolo.innerText = "";
            titoloInput.style.borderColor = "";

            if (!isNotEmpty(titoloValue)) {
                errorTitolo.innerText = "Il titolo è obbligatorio.";
                titoloInput.style.borderColor = "red";
                valid = false;
            }

            if (!isValidTitolo(titoloValue)) {
                errorTitolo.innerText = "Titolo non valido (Max 50 caratteri, solo lettere, numeri e .,-_!).";
                titoloInput.style.borderColor = "red";
                valid = false;
            }


            const dipendenteSelect = document.getElementById('dipendenteSelect');
            const errorDipendente = document.getElementById('errorDipendente');

            // Reset Dipendente
            errorDipendente.innerText = "";
            dipendenteSelect.style.borderColor = "";

            // Controlla se il valore è vuoto (cioè se è rimasto su "-- Seleziona --")
            if (dipendenteSelect.value === "" || dipendenteSelect.value === null) {
                errorDipendente.innerText = "Devi selezionare un dipendente.";
                dipendenteSelect.style.borderColor = "red";
                valid = false;
            }


            const dataInput = document.getElementById('dataScadenza');
            const errorData = document.getElementById('errorDataScadenza');
            const dataValue = dataInput.value;

            // Reset stile
            errorData.innerText = "";
            dataInput.style.borderColor = "";

            // Controllo 1: Obbligatorio
            if (!dataValue) {
                errorData.innerText = "La data di scadenza è obbligatoria.";
                dataInput.style.borderColor = "red";
                valid = false;
            }
            // Controllo 2: Non nel passato
            else if (!isFutureOrToday(dataValue)) {
                errorData.innerText = "La data non può essere nel passato.";
                dataInput.style.borderColor = "red";
                valid = false;
            }


            // --- 4. VALIDAZIONE DESCRIZIONE ---
            const descInput = document.getElementById('descrizioneTask');
            const errorDesc = document.getElementById('errorDescrizione');
            const descValue = descInput.value.trim(); // Usiamo trim() per ignorare spazi vuoti iniziali/finali

            // Reset dello stile e del messaggio
            errorDesc.innerText = "";
            descInput.style.borderColor = "";

            // Controllo 1: Obbligatorio
            if (descValue.length === 0) {
                errorDesc.innerText = "La descrizione è obbligatoria.";
                descInput.style.borderColor = "red";
                valid = false;
            }
            // Controllo 2: Lunghezza minima
            else if (descValue.length < 10) {
                errorDesc.innerText = "La descrizione deve contenere almeno 10 caratteri.";
                descInput.style.borderColor = "red";
                valid = false;
            }
            // Controllo 3: Lunghezza massima
            else if (descValue.length > 2000) {
                errorDesc.innerText = "La descrizione non può superare i 2000 caratteri.";
                descInput.style.borderColor = "red";
                valid = false;
            }

            const prioritaSelect = document.getElementById('prioritaSelect');
            const errorPriorita = document.getElementById('errorPriorita');
            if (!isNotEmpty(prioritaSelect.value)) {
                errorPriorita.innerText = "Seleziona la priorità del task.";
                prioritaSelect.style.borderColor = "red";
                valid = false;
            } else {
                errorPriorita.innerText = "";
                prioritaSelect.style.borderColor = "";
            }

            // Blocco invio se non valido
            if (!valid) {
                event.preventDefault();
            }
        });
    }
});