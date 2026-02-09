import it.unisa.ewms.application.AccessManagement.SessionServiceImpl;
import it.unisa.ewms.application.AccessManagement.interfaces.SessionService;
import it.unisa.ewms.application.AccountManagement.ProfileManagementServiceImpl;
import it.unisa.ewms.application.AccountManagement.interfaces.ProfileManagementService;
import it.unisa.ewms.application.TaskManagement.TaskSupervisoreServiceImpl;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskSupervisoreService;
import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;
import net.bytebuddy.implementation.bind.annotation.Super;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class ProvaDaCancellate {
    public static void main(String[] args) throws SQLException, EmailGiaPresenteException {
        ProfileManagementService prof = new ProfileManagementServiceImpl();

/*
        Utente user = new Utente();
        user.setNome("Gestore");
        user.setCognome("Rossi");
        user.setEmail("gestore.rossi@azienda.it");
        user.setDataNasc(Date.valueOf("1985-05-22"));
        user.setRuolo(Tipi.ruolo.GESTORE);
        prof.addAccount(user, "Goodpwd.12");
/*
        Utente baseSupervisore = new Supervisore();
        baseSupervisore.setNome("Supervisore");
        baseSupervisore.setCognome("Rossi");
        baseSupervisore.setEmail("gestore.rossi@azienda.it");
        baseSupervisore.setDataNasc(Date.valueOf("1985-05-22"));
        baseSupervisore.setRuolo(Tipi.ruolo.SUPERVISORE);
        baseSupervisore.setMatricola(4);
//        prof.addAccount(baseSupervisore, "Goodpwd.12");


        TaskSupervisoreService service = new TaskSupervisoreServiceImpl();

        List<Task> tasks = service.getAllTaskSup((Supervisore)baseSupervisore);

        System.out.println(tasks.size());

/*
        Dipendente baseDipendente = new Dipendente();
        baseDipendente.setNome("Luigi");
        baseDipendente.setCognome("Rossi");
        baseDipendente.setEmail("luigi.rossi@azienda.it");
        baseDipendente.setDataNasc(Date.valueOf("1985-05-23"));
        baseDipendente.setRuolo(Tipi.ruolo.DIPENDENTE);
        baseDipendente.setSupervisoreInfo(new Informazioni(baseSupervisore.getMatricola(), baseSupervisore.getNome(), baseSupervisore.getCognome()));

        prof.addAccount(baseDipendente, "Goodpwd.12");
    */}
}
