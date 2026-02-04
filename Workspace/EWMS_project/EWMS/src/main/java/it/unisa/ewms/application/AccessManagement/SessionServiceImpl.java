package it.unisa.ewms.application.AccessManagement;

import it.unisa.ewms.application.AccessManagement.interfaces.SessionService;
import it.unisa.ewms.model.beans.Dipendente;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;
import it.unisa.ewms.persistance.PersistenceServiceImpl;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;
import it.unisa.ewms.model.beans.Utente;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class SessionServiceImpl implements SessionService {
    private final PersistenceService serv;

    public SessionServiceImpl(){
        this.serv = PersistenceServiceImpl.getInstance();
    }


    //utilizzato per unit testing
    public SessionServiceImpl(PersistenceService serv) {
        if (serv == null) { throw new IllegalArgumentException();}
        this.serv = serv;
    }


    @Override
    public boolean login(String email, String password) {
        if (password == null || email == null){
            throw new IllegalArgumentException("Password or Email address is null");
        }


        //Recupero un istanza di PersistanceService e mi faccio restituire un UtenteDAO
        IUtenteDAO utenteDAO = serv.getUtenteDAO();

        //Controllo se la password corrisponde a quella salvata nel database (già fatto l'hash: metodo hash in AccountManagement)
        try {

            String actualPassword = utenteDAO.recuperaPassword(email);
            if (actualPassword != null) {
                return (BCrypt.checkpw(password, actualPassword));
            }
            else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }


    }

    @Override
    public Utente getUtente(String email) {

        if (email == null) {
            throw new IllegalArgumentException("Email non può essere null");
        }
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
