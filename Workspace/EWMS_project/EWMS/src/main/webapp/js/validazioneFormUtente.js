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
    const date = new Date(value);
    return !isNaN(date.getTime());
}

function isValidEmail(value) {
    const regex = /^[a-zA-Z0-9]{1,50}\.[a-zA-Z0-9]{1,50}@azienda\.it$/;
    return regex.test(value);
}

/**
 * NUOVA FUNZIONE: Genera Password sicura secondo la Regex
 * ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,16}$
 */
function generaPassword() {
    const length = 12; // Lunghezza scelta (tra 8 e 16)
    const lowerChars = "abcdefghijklmnopqrstuvwxyz";
    const upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    const numberChars = "0123456789";
    const specialChars = "@$!%*?&"; // Solo caratteri ammessi dalla regex

    const allChars = lowerChars + upperChars + numberChars + specialChars;
    let password = "";

    // Garantiamo almeno un carattere per tipo per soddisfare la regex
    password += lowerChars.charAt(Math.floor(Math.random() * lowerChars.length));
    password += upperChars.charAt(Math.floor(Math.random() * upperChars.length));
    password += numberChars.charAt(Math.floor(Math.random() * numberChars.length));
    password += specialChars.charAt(Math.floor(Math.random() * specialChars.length));

    // Riempiamo il resto
    for (let i = password.length; i < length; i++) {
        password += allChars.charAt(Math.floor(Math.random() * allChars.length));
    }

    // Mescoliamo i caratteri
    password = password.split('').sort(() => 0.5 - Math.random()).join('');

    // Inseriamo nel campo input
    const passwordField = document.getElementById('passwordField');
    if (passwordField) {
        passwordField.value = password;
        passwordField.style.borderColor = ""; // Rimuovi errore visivo
        // Rimuovi messaggio errore testuale se presente
        const errorPwd = document.getElementById('errorPassword');
        if (errorPwd) errorPwd.innerText = "";
    }
}

// =========================================================
// 2. LOGICA DOMContentLoaded
// =========================================================

document.addEventListener("DOMContentLoaded", function() {

    // --- A. GESTIONE COMPARSA/SCOMPARSA SUPERVISORE ---
    const ruoloSelect = document.getElementById('ruoloSelect');
    const divSupervisore = document.getElementById('divSupervisore');
    const supervisoreSelect = document.getElementById('supervisoreSelect');

    function checkRuolo() {
        if (!ruoloSelect || !divSupervisore) return;

        if (ruoloSelect.value === 'DIPENDENTE') {
            divSupervisore.style.display = 'block';
        } else {
            divSupervisore.style.display = 'none';
            // Resetta la selezione se nascondi
            if(supervisoreSelect) supervisoreSelect.value = "";
        }
    }

    if (ruoloSelect) {
        // Controlla al cambio
        ruoloSelect.addEventListener('change', checkRuolo);
        // Controlla subito (utile se ricarichi la pagina)
        checkRuolo();
    }


    // --- B. VALIDAZIONE AL CLICK DI "CONFERMA" (PRIMA DEL MODAL) ---
    // Usiamo questo listener invece del 'submit' classico per gestire il modale
    const btnOpenModal = document.getElementById('btnOpenModal');

    if (btnOpenModal) {
        btnOpenModal.addEventListener('click', function() {
            let valid = true;

            // 1. Validazione Nome
            const nomeInput = document.getElementById('nome');
            const errorNome = document.getElementById('errorNome');
            errorNome.innerText = "";
            nomeInput.style.borderColor = "";

            if (!isNotEmpty(nomeInput.value)) {
                errorNome.innerText = "Il campo nome è obbligatorio.";
                nomeInput.style.borderColor = "red";
                valid = false;
            } else if (!hasMaxLength(nomeInput.value, 50)) {
                errorNome.innerText = "Il nome è troppo lungo (max 50).";
                nomeInput.style.borderColor = "red";
                valid = false;
            }

            // 2. Validazione Cognome
            const cognomeInput = document.getElementById('cognome');
            const errorCognome = document.getElementById('errorCognome');
            errorCognome.innerText = "";
            cognomeInput.style.borderColor = "";

            if (!isNotEmpty(cognomeInput.value)) {
                errorCognome.innerText = "Il cognome è obbligatorio.";
                cognomeInput.style.borderColor = "red";
                valid = false;
            } else if (!hasMaxLength(cognomeInput.value, 50)) {
                errorCognome.innerText = "Il cognome è troppo lungo (max 50).";
                cognomeInput.style.borderColor = "red";
                valid = false;
            }

            // 3. Validazione Data
            const dataInput = document.getElementById('data');
            const errorData = document.getElementById('errorData');
            errorData.innerText = "";
            dataInput.style.borderColor = "";

            if (!isNotEmpty(dataInput.value)) {
                errorData.innerText = "La data di nascita è obbligatoria.";
                dataInput.style.borderColor = "red";
                valid = false;
            } else if (!isValidDate(dataInput.value)) {
                errorData.innerText = "Inserisci una data valida.";
                dataInput.style.borderColor = "red";
                valid = false;
            }

            // 4. Validazione Email
            const emailInput = document.getElementById('email');
            const errorEmail = document.getElementById('errorEmail');
            errorEmail.innerText = "";
            emailInput.style.borderColor = "";

            if (!isNotEmpty(emailInput.value)) {
                errorEmail.innerText = "L'email è obbligatoria.";
                emailInput.style.borderColor = "red";
                valid = false;
            } else if (!isValidEmail(emailInput.value)) {
                errorEmail.innerText = "Formato richiesto: nome.cognome@azienda.it";
                emailInput.style.borderColor = "red";
                valid = false;
            }

            // 5. NUOVO: Validazione Password Generata
            const passwordField = document.getElementById('passwordField');
            const errorPassword = document.getElementById('errorPassword');
            if (errorPassword) errorPassword.innerText = "";
            if (passwordField) passwordField.style.borderColor = "";

            if (!passwordField || !isNotEmpty(passwordField.value)) {
                if (errorPassword) errorPassword.innerText = "Devi generare una password.";
                else alert("Devi generare una password.");

                if (passwordField) passwordField.style.borderColor = "red";
                valid = false;
            }

            // 6. NUOVO: Validazione Supervisore (Solo se ruolo è dipendente)
            if (ruoloSelect && ruoloSelect.value === 'DIPENDENTE') {
                const errorSup = document.getElementById('errorSupervisore');
                if (errorSup) errorSup.innerText = "";
                if (supervisoreSelect) supervisoreSelect.style.borderColor = "";

                if (!supervisoreSelect || !isNotEmpty(supervisoreSelect.value)) {
                    if (errorSup) errorSup.innerText = "Seleziona un supervisore.";
                    if (supervisoreSelect) supervisoreSelect.style.borderColor = "red";
                    valid = false;
                }
            }

            // --- APERTURA MODALE ---
            // Se tutto è valido, apriamo il modale manualmente
            if (valid) {
                const myModal = new bootstrap.Modal(document.getElementById('staticBackdrop'));
                myModal.show();
            } else {
            }
        });
    }
});