package it.unisa.ewms.application.AccessManagement.interfaces;

import it.unisa.ewms.model.beans.Utente;

public interface SessionService {
    boolean login(String email, String password);
    Utente getUtente(String email);
}
