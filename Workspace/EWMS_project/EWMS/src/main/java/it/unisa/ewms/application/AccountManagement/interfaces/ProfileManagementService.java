package it.unisa.ewms.application.AccountManagement.interfaces;

import it.unisa.ewms.model.beans.Informazioni;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;

import java.sql.SQLException;
import java.util.List;

public interface ProfileManagementService {
    void addAccount(Utente utente, String password) throws SQLException, EmailGiaPresenteException;
    Utente getAccount(int matricola) throws SQLException;
    List<Utente> getAllAccount() throws SQLException;
    List<Informazioni> getAllSupervisori() throws SQLException;

/*
    void modifyRole(Utente utente, Tipi.ruolo ruolo, String supMatricola) throws Exception;
    void modifySupervisor(Utente utente, String supMatricola) throws Exception;
    void replacePassword(int matricola, String newPassword) throws SQLException;
 */

    void deleteAccount(Utente utente) throws SQLException;

}
