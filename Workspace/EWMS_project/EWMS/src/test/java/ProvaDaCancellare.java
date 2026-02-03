import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.PersistenceServiceImpl;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;
import net.bytebuddy.implementation.bind.annotation.Super;

import java.sql.Date;
import java.sql.SQLException;

public class ProvaDaCancellare {

    public static void main(String[] args) {
        PersistenceService service = PersistenceServiceImpl.getInstance();

        IUtenteDAO utenteDAO = service.getUtenteDAO();

        Supervisore utente = new Supervisore();
        utente.setNome("Juanito");
        utente.setCognome("Bros");
        utente.setDataNasc(Date.valueOf("1990-11-12"));
        utente.setEmail("juoanitobros@azienda.it");
        utente.setRuolo(Tipi.ruolo.SUPERVISORE);
        utente.setNewUtente(true);

        try {

            utenteDAO.createSupervisore(utente, "CicciaAlCulo");

        } catch (SQLException e) {
            System.out.println(e.getMessage() + "Si è proprio verificato SQLException");
            throw new RuntimeException(e);
        } catch (EmailGiaPresenteException e) {
            System.out.println("Email già presente");
        }
/*
        try {
            Utente dipendente = utenteDAO.findByMatricola(3);
            if (dipendente == null) {
                System.out.println("Nessuna utente non trovata");
            }else {
                System.out.println(dipendente.getNome() + " " + dipendente.getCognome() + " " + dipendente.getMatricola());
            }} catch (SQLException e) {
            System.out.println("Cazzooooooooo");
            throw new RuntimeException(e);
        }

*/
        System.out.println("Non entro nel catch");
    }
}
