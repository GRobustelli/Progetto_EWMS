function validateForm() {
    // 1. Recupera i valori
    const user = document.getElementById("username").value;
    const pass = document.getElementById("password").value;

    // 2. Recupera i contenitori degli errori
    const errUser = document.getElementById("error-username");
    const errPass = document.getElementById("error-password");

    // 3. Reset: Nasconde gli errori precedenti
    errUser.style.display = "none";
    errPass.style.display = "none";

    let isValid = true;

    // 4. Validazione Username
    if (user == null || user.trim() === "") {
        errUser.innerText = "Il campo Username è obbligatorio";
        errUser.style.display = "inline"; // Mostra il messaggio
        isValid = false;
    }

    // 5. Validazione Password
    if (pass == null || pass.trim() === "") {
        errPass.innerText = "Il campo Password è obbligatorio";
        errPass.style.display = "inline"; // Mostra il messaggio
        isValid = false;
    }

    // Se isValid è false, il form non viene inviato
    return isValid;
}