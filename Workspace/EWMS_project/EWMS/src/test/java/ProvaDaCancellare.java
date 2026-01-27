import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;
import it.unisa.ewms.persistance.PersistenceServiceImpl;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;
import it.unisa.ewms.persistance.interfaces.PersistenceService;

import java.sql.Date;
import java.sql.SQLException;

public class ProvaDaCancellare {

    public static void main(String[] args) {
        PersistenceService service = PersistenceServiceImpl.getInstance();

        IUtenteDAO utenteDAO = service.getUtenteDAO();

        Utente utente = new Utente();
        utente.setNome("Juanito");
        utente.setCognome("Bros");
        utente.setDataNasc(Date.valueOf("1990-11-12"));
        utente.setEmail("juoanitobros@azienda.it");
        utente.setMatricola("2501000000002");
        utente.setRuolo(Tipi.ruolo.DIPENDENTE);
        utente.setNewUtente(true);

        try {
            utenteDAO.createUtente(utente, "CicciaAlCulo");
        } catch (SQLException e) {
            System.out.println(e.getMessage() + "Si è proprio verificato SQLException");
            throw new RuntimeException(e);
        } catch (EmailGiaPresenteException e) {
            System.out.println("Email già presente");
            throw new RuntimeException(e);

        }


        System.out.println("Non entro nel catch");
    }
}
