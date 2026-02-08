/*  <small id="errorNome" style="color: red;"></small> per messaggio errore nome
    <small id="errorCognome" style="color: red;"></small> per messaggio errore cognome
    <small id="errorData" style="color: red;"></small> per messaggio errore data
    <small id="errorEmail" style="color: red;"></small> per messaggio errore email

*/
function isNotEmpty(value) {
    return value !== null && value.trim().length > 0;
}

function hasMaxLength(value, max) {
    return value.length <= max;
}

function isValidDate(value) {
    // Crea un oggetto data
    const date = new Date(value);

    return !isNaN(date.getTime());
}

function isValidEmail(value) {

    const regex = /^[a-zA-Z0-9]{1,50}\.[a-zA-Z0-9]{1,50}@azienda\.it$/;
    return regex.test(value);
}

document.addEventListener("DOMContentLoaded", function() {

    const form = document.getElementById('formAccount');

    if (form) {
        form.addEventListener('submit', function(event) {
            let valid = true;

            // --- VALIDAZIONE NOME ---
            const nomeInput = document.getElementById('nome');
            const errorNome = document.getElementById('errorNome');
            const nomeValue = nomeInput.value;

            errorNome.innerText = "";
            nomeInput.style.borderColor = "";

            // Regola 1: Non vuoto
            if (!isNotEmpty(nomeValue)) {
                errorNome.innerText = "Il campo nome è obbligatorio.";
                nomeInput.style.borderColor = "red";
                valid = false;
            }
            // Regola 2: Max 50 caratteri
            else if (!hasMaxLength(nomeValue, 50)) {
                errorNome.innerText = "Il nome è troppo lungo (max 50).";
                nomeInput.style.borderColor = "red";
                valid = false;
            }

            // --- VALIDAZIONE COGNOME (Nuova) ---
            const cognomeInput = document.getElementById('cognome');
            const errorCognome = document.getElementById('errorCognome');
            const cognomeValue = cognomeInput.value;

            // Reset stile Cognome
            errorCognome.innerText = "";
            cognomeInput.style.borderColor = "";

            if (!isNotEmpty(cognomeValue)) {
                errorCognome.innerText = "Il cognome è obbligatorio.";
                cognomeInput.style.borderColor = "red";
                valid = false;
            } else if (!hasMaxLength(cognomeValue, 50)) {
                errorCognome.innerText = "Il cognome è troppo lungo (max 50).";
                cognomeInput.style.borderColor = "red";
                valid = false;
            }

            // --- VALIDAZIONE DATA DI NASCITA ---
            const dataInput = document.getElementById('data');
            const errorData = document.getElementById('errorData');
            const dataValue = dataInput.value;

            // Reset stile Data
            errorData.innerText = "";
            dataInput.style.borderColor = "";

            if (!isNotEmpty(dataValue)) {
                errorData.innerText = "La data di nascita è obbligatoria.";
                dataInput.style.borderColor = "red";
                valid = false;
            } else if (!isValidDate(dataValue)) {
                errorData.innerText = "Inserisci una data valida.";
                dataInput.style.borderColor = "red";
                valid = false;
            }

            // --- VALIDAZIONE EMAIL ---
            const emailInput = document.getElementById('email');
            const errorEmail = document.getElementById('errorEmail');
            const emailValue = emailInput.value;

            // Reset stile Email
            errorEmail.innerText = "";
            emailInput.style.borderColor = "";

            if (!isNotEmpty(emailValue)) {
                errorEmail.innerText = "L'email è obbligatoria.";
                emailInput.style.borderColor = "red";
                valid = false;
            } else if (!isValidEmail(emailValue)) {
                // Messaggio specifico per aiutare l'utente a capire il formato richiesto
                errorEmail.innerText = "Formato richiesto: nome.cognome@azienda.it";
                emailInput.style.borderColor = "red";
                valid = false;
            }

            // Se c'è anche solo un errore, blocchiamo l'invio del form
            if (!valid) {
                event.preventDefault();
            }
        });
    }
});