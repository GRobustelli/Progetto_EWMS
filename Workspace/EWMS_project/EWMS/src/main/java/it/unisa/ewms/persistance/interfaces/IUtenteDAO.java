package it.unisa.ewms.persistance.interfaces;

import it.unisa.ewms.persistance.beans.Utente;

import java.sql.SQLException;

public interface IUtenteDAO {
    void createUtente(Utente utente, String password) throws SQLException;
    Utente findByMatricola(String matricola);
    void  update(Utente utente);
    void delete(Utente utente);
}
