package it.unisa.ewms.application.AccessManagement;

import it.unisa.ewms.application.AccessManagement.interfaces.SessionService;
import it.unisa.ewms.model.beans.Dipendente;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;
import it.unisa.ewms.persistance.PersistenceServiceImpl;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;
import it.unisa.ewms.model.beans.Utente;

import java.sql.SQLException;

public class SessionServiceImpl implements SessionService {

    @Override
    public boolean login(String email, String password) {

        //Recupero un istanza di PersistanceService e mi faccio restituire un UtenteDAO

        PersistenceService serv = PersistenceServiceImpl.getInstance();

        IUtenteDAO utenteDAO = serv.getUtenteDAO();

        //Controllo se la password corrisponde a quella salvata nel database (già fatto l'hash: metodo hash in AccountManagement)
        try {

            String actualPassword = utenteDAO.recuperaPassword(email);
            return  (actualPassword.equals(password));

        } catch (SQLException e) {
            return false;
        }


    }

    @Override
    public Utente getUtente(String email) {

        //Recupero un istanza di PersistanceService e mi faccio restituire un UtenteDAO
        PersistenceService serv = PersistenceServiceImpl.getInstance();
        IUtenteDAO utenteDAO = serv.getUtenteDAO();

        //Inizializzato come null, nella servlet controllo e se ci sono errori reindirizzo sulla login page
        Utente utente = null;
        try {

            utente = utenteDAO.findByEmail(email);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return utente;

    }

}
