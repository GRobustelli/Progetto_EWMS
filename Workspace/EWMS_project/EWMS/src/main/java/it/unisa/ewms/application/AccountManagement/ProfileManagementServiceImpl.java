package it.unisa.ewms.application.AccountManagement;

import it.unisa.ewms.application.AccountManagement.interfaces.ProfileManagementService;
import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.PersistenceServiceImpl;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;

import java.sql.SQLException;
import java.util.List;

public class ProfileManagementServiceImpl implements ProfileManagementService {

    private final PersistenceService persistenceService;
    private final String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$";


    public ProfileManagementServiceImpl() {
        persistenceService = PersistenceServiceImpl.getInstance();
    }

    @Override
    public void addAccount(Utente utente, String password) throws SQLException, EmailGiaPresenteException {

        //Controllo se mi sono stati passati valori nulli
        if (utente == null || password == null) {
            throw new IllegalArgumentException("utente o password non possono essere null");
        }
        //Controllo se i campi di utente siano stati correttamente inseriti
        if (utente.getEmail() == null || utente.getEmail().isEmpty()) {
            throw new  IllegalArgumentException("Email non valido");
        }
        if (utente.getNome()== null || utente.getNome().isEmpty()) {
            throw new  IllegalArgumentException("Nome non valido");
        }
        if (utente.getCognome()== null || utente.getCognome().isEmpty()) {
            throw new  IllegalArgumentException("Cognome non valido");
        }
        if (utente.getRuolo() == null || utente.getRuolo().toString().isEmpty()){
            throw new  IllegalArgumentException("Ruolo non valido");
        }
        if (utente.getDataNasc() == null || utente.getDataNasc().toString().isEmpty()){
            throw new  IllegalArgumentException("Data di nascita non valido");
        }
        if (password.isEmpty() || !password.matches(regex)) {
            throw new  IllegalArgumentException("Password non valida");
        }

        //Prima di questo devo fare l'hash della password


        IUtenteDAO udao  = persistenceService.getUtenteDAO();

        if (udao == null) {
            throw new RuntimeException("Impossibile aggiungere utente");
        }

        if (utente instanceof Dipendente) {
            udao.createDipendente((Dipendente)utente,password);
        }
        else if (utente instanceof Supervisore) {
            udao.createSupervisore((Supervisore)utente,password);
        }
        else if (utente.getRuolo() == Tipi.ruolo.GESTORE) {
            udao.createUtente(utente,password);
        }
        else{
            throw new IllegalArgumentException("Ruolo specificato non valido");
        }
        //Il controllo del tipo di eccezioni lanciate lo faccio nella servlet, questo metodo le lancia
    }

    @Override
    public Utente getAccount(int matricola) throws SQLException {
        if (matricola <= 0) {
            throw new IllegalArgumentException("Matricola non valida");
        }

        IUtenteDAO udao  = persistenceService.getUtenteDAO();
        if (udao == null) {
            throw new RuntimeException("Impossibile recuperare l'utente");
        }

        Utente utente  = udao.findByMatricola(matricola);

        if (utente != null) {
            if(utente.getRuolo() == Tipi.ruolo.GESTORE){
                return utente;
            }
            else if (utente.getRuolo() == Tipi.ruolo.DIPENDENTE){
                Dipendente dipendente = (Dipendente)utente;
                dipendente.setSupervisoreInfo(udao.getSupervisoreInfo(matricola));
                return dipendente;

            } else if (utente.getRuolo() == Tipi.ruolo.SUPERVISORE) {
                Supervisore supervisore = (Supervisore)utente;
                List<Informazioni> lista = udao.getAllDipendentiInfo(matricola);
                if (lista != null) {
                    supervisore.addDipendetiInfo(lista);
                }
            return supervisore;
            }
        }

        return null;
    }

    @Override
    public List<Utente> getAllAccount() throws SQLException {
        IUtenteDAO udao =  persistenceService.getUtenteDAO();
        if (udao == null) {
            throw new RuntimeException("Impossibile recuperare utenti");
        }

        return udao.getAllUtente();
    }

    @Override
    public List<Informazioni> getAllSupervisori() throws SQLException {
        IUtenteDAO udao =  persistenceService.getUtenteDAO();
        if (udao == null) {
            throw new RuntimeException("Impossibile recuperare informazioni dal db");
        }
        return udao.getAllSupervisori();
    }

/*
    //Conviene implementarla client side con javascript probabilmente
    public String generateNewPwd() throws SQLException {

        return "";
    }


    @Override
    public void modifyRole(Utente utente, Tipi.ruolo ruolo, int supMatricola) throws Exception {
        if (utente == null || ruolo == null) {
            throw new IllegalArgumentException("Utente e ruolo non validi");
        }
        if (ruolo == Tipi.ruolo.DIPENDENTE && supMatricola <= 0 ) {
            throw new IllegalArgumentException("Impossibile cambiare ruolo in dipendente senza matricola supervisore");
        }
        if (utente.getMatricola()== supMatricola) {
            throw new IllegalArgumentException("Un utente non può supervisionarsi da solo");
        }

        IUtenteDAO udao  = persistenceService.getUtenteDAO();
        if (udao == null) {
            throw new RuntimeException("Impossibile modficare ruolo");
        }

        //Questo metodo può lanciare Exception, SQLException, IllegalArgumentException
        udao.updateRuolo(utente.getMatricola(),ruolo,supMatricola);

    }

    @Override
    public void modifySupervisor(Utente utente, int supMatricola) throws Exception {
        if (utente == null || supMatricola <= 0) {
            throw new IllegalArgumentException("Dati inseriti non validi");
        }
        if (utente.getRuolo() != Tipi.ruolo.DIPENDENTE) {
            throw new IllegalArgumentException("Solo i dipendenti possono avere supervisori");
        }
        if (utente.getMatricola() == supMatricola) {
            throw new IllegalArgumentException("Un utente non può supervisionarsi da solo");
        }

        IUtenteDAO udao  = persistenceService.getUtenteDAO();
        if (udao == null) {
            throw new RuntimeException("Impossibile modificare ruolo");
        }
        //Questo metodo lancia IllegalArgumentException, SQLException (non trova l'utente), Runtime (errore con la connessione db)
        udao.updateSupervisore(utente.getMatricola(),supMatricola);
    }

    @Override
    public void replacePassword(int matricola, String newPassword) throws SQLException {
        if (newPassword == null || matricola <= 0 || !newPassword.matches(regex)) {
            throw new IllegalArgumentException("Matricola e Password non validi");
        }
        IUtenteDAO udao  = persistenceService.getUtenteDAO();

        //Questo metodo lancia SQLException(se non trova la matricola) Runtime (problemi connessione db)
        udao.updatePassword(matricola,newPassword);

    }
*/

    @Override
    public void deleteAccount(Utente utente) throws SQLException, RuntimeException, IllegalArgumentException {
        if (utente == null || utente.getMatricola() <= 0) {
            throw new IllegalArgumentException("Dati inseriti non validi");
        }

        IUtenteDAO udao  = persistenceService.getUtenteDAO();

        if  (udao == null) {
            throw new RuntimeException("Impossibile modificare ruolo");
        }

        udao.delete(utente);
    }
}
