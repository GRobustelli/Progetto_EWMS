package it.unisa.ewms.application.AccountManagement.interfaces;

import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;

import java.sql.SQLException;

public interface ProfileManagementService {
    boolean addAccount(Utente utente) throws SQLException;
    Utente getAccount(String matricola) throws SQLException;
    String generateNewPwd () throws SQLException; // rimpiazza la password all'utente selezionato
    boolean modifyRole(Utente utente, Tipi.ruolo ruolo) throws SQLException;
    boolean modifySupervisor(Utente utente, String supMatricola) throws SQLException;
    boolean replacePassword(String matricola, String newPassword) throws SQLException;
    boolean deleteAccount(String matricola) throws SQLException;

}
