package it.unisa.ewms.application.AccountManagement;

import it.unisa.ewms.application.AccountManagement.interfaces.PersonalProfileService;
import it.unisa.ewms.model.beans.Utente;
import it.unisa.ewms.persistance.PersistenceServiceImpl;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;

import java.sql.SQLException;

public class PersonalProfileServiceImpl implements PersonalProfileService {

/*
    @Override
    public boolean changePwd(Utente utente, String oldPwd, String newPwd) throws SQLException {

        if (utente == null || oldPwd == null || newPwd == null || oldPwd.isEmpty() || newPwd.isEmpty()) {
            throw new IllegalArgumentException("I campi non possono essere null o vuoti");
        }

        PersistenceService service = PersistenceServiceImpl.getInstance();

        IUtenteDAO utenteDAO = service.getUtenteDAO();

        try {
            String pwdFromDb = utenteDAO.recuperaPassword(utente.getEmail());
            if (pwdFromDb.equals(oldPwd)) {
                utenteDAO.updatePassword(utente.getMatricola(), newPwd);
                return true;
            }

        } catch (SQLException e) {
            throw new SQLException(e);
        }

        return false;
    }

 */
}
