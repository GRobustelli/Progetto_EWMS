package it.unisa.ewms.persistance.interfaces;

import it.unisa.ewms.persistance.beans.Dipendente;
import it.unisa.ewms.persistance.beans.Informazioni;
import it.unisa.ewms.persistance.beans.Supervisore;
import it.unisa.ewms.persistance.beans.Utente;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;

import java.sql.SQLException;
import java.util.List;

public interface IUtenteDAO {
    void createUtente(Utente utente, String password) throws SQLException, EmailGiaPresenteException;
    void createDipendente(Dipendente dipendente, String password) throws SQLException, EmailGiaPresenteException;
    void createSupervisore(Supervisore supervisore, String password) throws SQLException, EmailGiaPresenteException;

    Utente findByMatricola(String matricola)  throws SQLException;
    List<Informazioni> getAllDipendentiInfo(String matricola) throws SQLException; // oppure con utente direttamente
    Informazioni getSupervisoreInfo(String matricola)  throws SQLException;

    void  update(Utente utente)  throws SQLException;
    void delete(Utente utente)  throws SQLException;
}
