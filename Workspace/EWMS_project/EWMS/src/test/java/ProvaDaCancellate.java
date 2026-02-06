import it.unisa.ewms.application.AccountManagement.ProfileManagementServiceImpl;
import it.unisa.ewms.application.AccountManagement.interfaces.ProfileManagementService;
import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;

import java.sql.Date;
import java.sql.SQLException;

public class ProvaDaCancellate {
    public static void main(String[] args) throws SQLException, EmailGiaPresenteException {
        ProfileManagementService prof = new ProfileManagementServiceImpl();


        Supervisore baseSupervisore = new Supervisore();
        baseSupervisore.setNome("Supervisore");
        baseSupervisore.setCognome("Rossi");
        baseSupervisore.setEmail("supervisore.rossi@azienda.it");
        baseSupervisore.setDataNasc(Date.valueOf("1985-05-22"));
        baseSupervisore.setRuolo(Tipi.ruolo.SUPERVISORE);
        baseSupervisore.setMatricola(3);
        prof.deleteAccount(baseSupervisore);

        prof.addAccount(baseSupervisore, "Goodpwd.12");


        Dipendente baseDipendente = new Dipendente();
        baseDipendente.setNome("Luigi");
        baseDipendente.setCognome("Rossi");
        baseDipendente.setEmail("luigi.rossi@azienda.it");
        baseDipendente.setDataNasc(Date.valueOf("1985-05-23"));
        baseDipendente.setRuolo(Tipi.ruolo.DIPENDENTE);
        baseDipendente.setSupervisoreInfo(new Informazioni(baseSupervisore.getMatricola(), baseSupervisore.getNome(), baseSupervisore.getCognome()));

        prof.addAccount(baseDipendente, "Goodpwd.12");
    }
}
