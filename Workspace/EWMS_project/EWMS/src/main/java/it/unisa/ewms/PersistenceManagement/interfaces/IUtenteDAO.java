package it.unisa.ewms.PersistenceManagement.interfaces;

import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.PersistenceManagement.eccezioni.EmailGiaPresenteException;

import java.sql.SQLException;
import java.util.List;

public interface IUtenteDAO {
    void createUtente(Utente utente, String password) throws SQLException, EmailGiaPresenteException;
    void createDipendente(Dipendente dipendente, String password) throws SQLException, EmailGiaPresenteException;
    void createSupervisore(Supervisore supervisore, String password) throws SQLException, EmailGiaPresenteException;

    Utente findByMatricola(int matricola)  throws SQLException;
    Utente findByEmail(String email)  throws SQLException;
    List<Informazioni> getAllDipendentiInfo(int matricola) throws SQLException; // oppure con utente direttamente
    Informazioni getSupervisoreInfo(int matricola)  throws SQLException;
    List<Informazioni> getAllSupervisori() throws SQLException;
    List<Utente> getAllUtente() throws SQLException;
/*
    void updateAnagrafica(String nome, String cognome, Date dataDiNascita, int matricola)  throws SQLException;
    void updateRuolo(int matricola, Tipi.ruolo nuovoRuolo, int matricolaNuovoSupervisore) throws Exception;
    void updateSupervisore(int matricola, int matricolaSup) throws SQLException, Exception;
    void updatePassword(int matricola, String hashPassword) throws SQLException;
*/
    void delete(Utente utente)  throws SQLException;

    String recuperaPassword(String email) throws SQLException;


}
