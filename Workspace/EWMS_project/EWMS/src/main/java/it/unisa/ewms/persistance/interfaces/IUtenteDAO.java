package it.unisa.ewms.persistance.interfaces;

import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface IUtenteDAO {
    void createUtente(Utente utente, String password) throws SQLException, EmailGiaPresenteException;
    void createDipendente(Dipendente dipendente, String password) throws SQLException, EmailGiaPresenteException;
    void createSupervisore(Supervisore supervisore, String password) throws SQLException, EmailGiaPresenteException;

    Utente findByMatricola(String matricola)  throws SQLException;
    Utente findByEmail(String email)  throws SQLException;
    List<Informazioni> getAllDipendentiInfo(String matricola) throws SQLException; // oppure con utente direttamente
    Informazioni getSupervisoreInfo(String matricola)  throws SQLException;



    void updateAnagrafica(String nome, String cognome, Date dataDiNascita, String matricola)  throws SQLException;
    void updateRuolo(String matricola, Tipi.ruolo nuovoRuolo, String matricolaNuovoSupervisore) throws Exception;
    void updateSupervisore(String matricola, String matricolaSup) throws SQLException;
    void updatePassword(String matricola, String hashPassword) throws SQLException;


    void delete(Utente utente)  throws SQLException;

    String recuperaPassword(String email) throws SQLException;

}
