import it.unisa.ewms.application.AccessManagement.SessionServiceImpl;
import it.unisa.ewms.application.AccessManagement.interfaces.SessionService;
import it.unisa.ewms.application.AccountManagement.ProfileManagementServiceImpl;
import it.unisa.ewms.application.AccountManagement.interfaces.ProfileManagementService;
import it.unisa.ewms.application.TaskManagement.TaskCommonServiceImpl;
import it.unisa.ewms.application.TaskManagement.TaskDipendenteServiceImpl;
import it.unisa.ewms.application.TaskManagement.TaskSupervisoreServiceImpl;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskCommonService;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskDipendenteService;
import it.unisa.ewms.application.TaskManagement.interfaces.TaskSupervisoreService;
import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.ClassiDAO.TaskDAO;
import it.unisa.ewms.persistance.ClassiDAO.UtenteDAO;
import it.unisa.ewms.persistance.DataSourceFactory;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;
import it.unisa.ewms.persistance.interfaces.ITaskDAO;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class IntegratioTestSuite {
    ProfileManagementService profileManagementService;

    @BeforeAll
    static void setupDatabase() {
        // Fondamentale: forza la Factory a usare la config di test con H2
        DataSourceFactory.setPropertiesFile("test.properties");
    }

    @Nested
    @DisplayName("Classe per integrazione AccountManagement con PersistenceManagement")
    class IntegrationTest_AccountManagement{
        private Utente baseUtente;
        private Dipendente baseDipendente;
        private Supervisore baseSupervisore;

        @BeforeEach
        void clearData() throws Exception {
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 1. IMPORTANTE: Sposta la connessione corrente sullo schema di default (PUBLIC).
                // Se rimani dentro 'ewmsDB', H2 non ti permetterà mai di cancellarlo.
                stmt.execute("SET SCHEMA PUBLIC");

                // 2. Cancella TUTTO (Tabelle, Viste, Vincoli)
                stmt.execute("DROP ALL OBJECTS");

                // 3. (Opzionale ma consigliato) Forza la cancellazione dello schema per evitare residui
                stmt.execute("DROP SCHEMA IF EXISTS ewmsDB CASCADE");

                // 4. Ora puoi ricreare tutto da zero in sicurezza
                stmt.execute("RUNSCRIPT FROM 'classpath:ewmsDB.sql'");
            }
        }

        @BeforeEach
        void setUp() {
            baseUtente = new Utente();
            baseUtente.setNome("Mario");
            baseUtente.setCognome("Rossi");
            baseUtente.setEmail("mario.rossi@azienda.it");
            baseUtente.setDataNasc(Date.valueOf("1985-05-20"));
            baseUtente.setRuolo(Tipi.ruolo.GESTORE);

            baseSupervisore = new Supervisore();
            baseSupervisore.setNome("Supervisore");
            baseSupervisore.setCognome("Rossi");
            baseSupervisore.setEmail("supervisore.rossi@azienda.it");
            baseSupervisore.setDataNasc(Date.valueOf("1985-05-22"));
            baseSupervisore.setRuolo(Tipi.ruolo.SUPERVISORE);


            baseDipendente = new Dipendente();
            baseDipendente.setNome("Luigi");
            baseDipendente.setCognome("Rossi");
            baseDipendente.setEmail("luigi.rossi@azienda.it");
            baseDipendente.setDataNasc(Date.valueOf("1985-05-23"));
            baseDipendente.setRuolo(Tipi.ruolo.DIPENDENTE);

            profileManagementService = new ProfileManagementServiceImpl();
        }

        @Test
        @DisplayName("addAccount: Test per la verifica del funzionamento metodo addAccount")
        void Test_addAccount() throws SQLException, ClassNotFoundException {
            // ACT

            try {
                profileManagementService.addAccount(baseUtente, "Goodpwd12!");
                profileManagementService.addAccount(baseSupervisore, "Goodpwd12!");
            } catch (EmailGiaPresenteException e) {
                Assertions.fail("Non ci dovrebbero essere email duplicate");
                throw new RuntimeException(e);
            }

            //DAO per la verifica del funzionamento e separazione dei test

            UtenteDAO udao = new  UtenteDAO();
            List<Utente> lista = udao.getAllUtente();

            boolean check= (lista.size() == 2);

            assertTrue(check, "La lista dovrebbe avere una size di 2");

        }

        @Test
        @DisplayName("getAccount: Test per la verifica del funzionamento metodo addAccount")
        void Test_getAccount() throws SQLException, ClassNotFoundException {

            // SETUP

            UtenteDAO udao = new  UtenteDAO();

            try {

                udao.createUtente(baseUtente, "Goodpwd.12");
            } catch (EmailGiaPresenteException e) {
                Assertions.fail("Non ci dovrebbero essere email duplicate");
            }

            // Recuperiamo l'utente appena inserito per avere la matricola generata e i dati attesi
            // Nota: findByEmail è più sicuro di getAll().get(0) in un DB non vuoto
            Utente utenteAtteso = udao.findByEmail(baseUtente.getEmail());

            // Verifica preliminare che il setup sia andato a buon fine
            assertNotNull(utenteAtteso, "Errore Setup: L'utente dovrebbe essere nel DB");
            int matricola = utenteAtteso.getMatricola();


            // 2. ACT & VERIFICA ECCEZIONI
            // Verifichiamo che non lanci IllegalArgumentException e catturiamo il risultato
            Utente utenteVerifica = Assertions.assertDoesNotThrow(() -> {
                return profileManagementService.getAccount(matricola);
            }, "Il metodo non deve lanciare eccezioni con una matricola valida");


            // 3. ASSERT (Verifica Campi)
            assertNotNull(utenteVerifica, "L'utente restituito non deve essere null");

            // Usiamo assertAll per verificare tutti i campi insieme
            Assertions.assertAll("Verifica corrispondenza campi Utente",
                    () -> assertEquals(utenteAtteso.getMatricola(), utenteVerifica.getMatricola(), "Matricola non corrisponde"),
                    () -> assertEquals(utenteAtteso.getEmail(), utenteVerifica.getEmail(), "Email non corrisponde"),
                    () -> assertEquals(utenteAtteso.getNome(), utenteVerifica.getNome(), "Nome non corrisponde"),
                    () -> assertEquals(utenteAtteso.getCognome(), utenteVerifica.getCognome(), "Cognome non corrisponde"),
                    // Nota: per le date assicurati che il formato (Date vs Timestamp) sia compatibile
                    () -> assertEquals(utenteAtteso.getDataNasc().toString(), utenteVerifica.getDataNasc().toString(), "Data di nascita non corrisponde")
            );
        }

        @Test
        @DisplayName("test getAllAccount: restituisce lista vuota se non ci sono utenti, restituisce lista popolata altrimenti")
        void testGetAllAccount() throws SQLException {
            // Test DB vuoto

            List<Utente> risultato = profileManagementService.getAllAccount();

            // 2. ASSERT
            assertNotNull(risultato, "La lista non deve essere null");
            assertTrue(risultato.isEmpty(), "La lista deve essere vuota se non ci sono utenti nel DB");


            // 1. ARRANGE (Popoliamo il DB di test)



            UtenteDAO dao = new UtenteDAO();

            // Inseriamo un Gestore
            try {
                dao.createUtente(baseUtente, "Pwd.1234");
                dao.createSupervisore(baseSupervisore, "Pwd.1234");

                Utente utenteAtteso = dao.findByEmail(baseSupervisore.getEmail());
                baseDipendente.setSupervisoreInfo(new Informazioni(utenteAtteso.getMatricola(), utenteAtteso.getNome(), utenteAtteso.getCognome()));

                dao.createDipendente(baseDipendente, "Pwd.123");
            } catch (EmailGiaPresenteException e) {
                throw new RuntimeException(e);
            }




              // 2. ACT
            risultato = profileManagementService.getAllAccount();

            // 3. ASSERT

            assertNotNull(risultato);
            assertEquals(3, risultato.size(), "Devono esserci esattamente 3 utenti nel sistema");

        }


        @Test
        @DisplayName("Violazione Vincolo DB: Inserimento Email Duplicata -> Lancia Exception Custom")
        void testAddAccount_EmailDuplicata_Reale() throws Exception {
            // 1. ARRANGE
            profileManagementService.addAccount(baseUtente, "Pwd.12345");

            // 2. ACT & ASSERT
            Assertions.assertThrows(EmailGiaPresenteException.class, () -> {
                profileManagementService.addAccount(baseUtente, "Pwd.12345");
            });
        }

        @Test
    @DisplayName("test getAllSupervisori: restituisce la lista di tutti i supervisori del sistema")
        void testGetAllSupervisori_Vuoto() throws SQLException, EmailGiaPresenteException {
        List<Informazioni> result = profileManagementService.getAllSupervisori();

        assertNotNull(result);
        assertTrue(result.isEmpty(), "La lista deve essere vuota se non ci sono supervisori");

        // 1. ARRANGE
        UtenteDAO dao = new UtenteDAO();

        // Inseriamo UN Gestore (non deve apparire)
        dao.createUtente(baseUtente, "Pwd.12345");
        dao.createSupervisore(baseSupervisore, "Pwd.12345");

        // 2. ACT
        List<Informazioni> risultato = profileManagementService.getAllSupervisori();

        // 3. ASSERT
        assertNotNull(risultato);
        assertEquals(1, risultato.size(),
                "La lista deve contenere SOLO il supervisore, ignorando Gestori e Dipendenti");


        Informazioni infoSupervisore = risultato.get(0);

        assertAll("Verifica mappatura DTO Informazioni",
                () -> assertEquals(baseSupervisore.getNome(), infoSupervisore.getNome(), "Il nome nel DTO errato"),
                () -> assertEquals(baseSupervisore.getCognome(), infoSupervisore.getCognome(), "Il cognome nel DTO errato"),
                () -> assertTrue(infoSupervisore.getMatricola() > 0, "La matricola deve essere valorizzata (>0)")
        );
    }

        @Test
    @DisplayName("Test per getAllSupervisori con lista non vuota")
        void testGetAllSupervisori_Pieno() throws SQLException, EmailGiaPresenteException {
            // 1. ARRANGE

        UtenteDAO dao = new UtenteDAO();

        // Inseriamo UN Gestore (non deve apparire)
        dao.createUtente(baseUtente, "Pwd.1234");
        dao.createSupervisore(baseSupervisore, "Pwd.1234");

        // 2. ACT
        List<Informazioni> risultato = profileManagementService.getAllSupervisori();

        // 3. ASSERT
        assertNotNull(risultato);
        assertEquals(1, risultato.size(),
                "La lista deve contenere SOLO il supervisore, ignorando Gestori e Dipendenti");


        Informazioni infoSupervisore = risultato.get(0);

        assertAll("Verifica mappatura DTO Informazioni",
                () -> assertEquals(baseSupervisore.getNome(), infoSupervisore.getNome(), "Il nome nel DTO errato"),
                () -> assertEquals(baseSupervisore.getCognome(), infoSupervisore.getCognome(), "Il cognome nel DTO errato"),
                () -> assertTrue(infoSupervisore.getMatricola() > 0, "La matricola deve essere valorizzata (>0)")
        );
        }

        @Test
        @DisplayName("Cancellazione Supervisore (Cascade/Multi-tabella) -> Rimosso dal DB")
        void testDeleteAccount_Supervisore() throws SQLException, EmailGiaPresenteException {
            // 1. ARRANGE
            UtenteDAO dao = new UtenteDAO();

            // Inseriamo un Supervisore nel DB reale.
            // Questo popola sia la tabella 'utente' che la tabella 'supervisore'.
            dao.createSupervisore(baseSupervisore, "Pwd.1234");

            Utente utenteDaCancellare = dao.findByEmail(baseSupervisore.getEmail());

            Assertions.assertNotNull(utenteDaCancellare, "Setup fallito: L'utente deve esistere prima del test");
            int matricola = utenteDaCancellare.getMatricola();

            // 2. ACT

            Assertions.assertDoesNotThrow(() -> {
                profileManagementService.deleteAccount(utenteDaCancellare);
            });

            // 3. ASSERT
            // Verifichiamo che non esista più tramite il DAO
            Utente risultato = dao.findByMatricola(matricola);
            Assertions.assertNull(risultato, "L'utente dovrebbe essere stato rimosso completamente dal DB");

            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM supervisore WHERE matricola = ?")) {
                ps.setInt(1, matricola);
                ResultSet rs = ps.executeQuery();
                rs.next();
                Assertions.assertEquals(0, rs.getInt(1), "Il record deve essere sparito anche dalla tabella supervisore");
            }
        }


    }


    @Nested
    @DisplayName("Classe per integrazione AccessManagement  --> PersistenceManagement")
    class IntegrationTest_AccessManagement{
        private Utente baseUtente;
        private Dipendente baseDipendente;
        private Supervisore baseSupervisore;
        private IUtenteDAO dao;
        private SessionService service;

        @BeforeEach
        void clearData() throws Exception {
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 1. IMPORTANTE: Sposta la connessione corrente sullo schema di default (PUBLIC).
                // Se rimani dentro 'ewmsDB', H2 non ti permetterà mai di cancellarlo.
                stmt.execute("SET SCHEMA PUBLIC");

                // 2. Cancella TUTTO (Tabelle, Viste, Vincoli)
                stmt.execute("DROP ALL OBJECTS");

                // 3. (Opzionale ma consigliato) Forza la cancellazione dello schema per evitare residui
                stmt.execute("DROP SCHEMA IF EXISTS ewmsDB CASCADE");

                // 4. Ora puoi ricreare tutto da zero in sicurezza
                stmt.execute("RUNSCRIPT FROM 'classpath:ewmsDB.sql'");
            }
        }

        @BeforeEach
        void setUp() throws SQLException, EmailGiaPresenteException {
            baseUtente = new Utente();
            baseUtente.setNome("Mario");
            baseUtente.setCognome("Rossi");
            baseUtente.setEmail("mario.rossi@azienda.it");
            baseUtente.setDataNasc(Date.valueOf("1985-05-20"));
            baseUtente.setRuolo(Tipi.ruolo.GESTORE);

            baseSupervisore = new Supervisore();
            baseSupervisore.setNome("Supervisore");
            baseSupervisore.setCognome("Rossi");
            baseSupervisore.setEmail("supervisore.rossi@azienda.it");
            baseSupervisore.setDataNasc(Date.valueOf("1985-05-22"));
            baseSupervisore.setRuolo(Tipi.ruolo.SUPERVISORE);


            baseDipendente = new Dipendente();
            baseDipendente.setNome("Luigi");
            baseDipendente.setCognome("Rossi");
            baseDipendente.setEmail("luigi.rossi@azienda.it");
            baseDipendente.setDataNasc(Date.valueOf("1985-05-23"));
            baseDipendente.setRuolo(Tipi.ruolo.DIPENDENTE);

            service = new SessionServiceImpl();
            dao = new UtenteDAO();

            dao.createUtente(baseUtente, BCrypt.hashpw("goodpwd.1", BCrypt.gensalt()));
            dao.createSupervisore(baseSupervisore, BCrypt.hashpw("goodpwd.1", BCrypt.gensalt()));

            Supervisore sup = (Supervisore) dao.findByEmail(baseSupervisore.getEmail());
            baseDipendente.setSupervisoreInfo(new Informazioni(sup.getMatricola(),sup.getNome(), sup.getCognome()));
            dao.createDipendente(baseDipendente, BCrypt.hashpw("goodpwd.1", BCrypt.gensalt()));

        }

        @Test
        @DisplayName("Login Successo: Credenziali valide (Gestore) -> Ritorna true")
        void testLogin_Successo_Gestore() {
            // 1. ACT

            boolean result = service.login(baseUtente.getEmail(), "goodpwd.1");

            // 2. ASSERT
            Assertions.assertTrue(result, "Il login deve riuscire con email corretta e password corretta");
        }

        @Test
        @DisplayName("Login Successo: Credenziali valide (Dipendente) -> Ritorna true")
        void testLogin_Successo_Dipendente() {
            // Verifica che funzioni anche per gli altri ruoli/tabelle
            boolean result = service.login(baseDipendente.getEmail(), "goodpwd.1");
            Assertions.assertTrue(result, "Il login deve riuscire anche per il dipendente");
        }

        @Test
        @DisplayName("Login Fallito: Password Errata -> Ritorna false")
        void testLogin_PasswordErrata() {
            // 1. ACT
            boolean result = service.login(baseUtente.getEmail(), "PASSWORD_SBAGLIATA_123");

            // 2. ASSERT
            Assertions.assertFalse(result, "Il login deve fallire se la password non corrisponde all'hash nel DB");
        }

        @Test
        @DisplayName("Login Fallito: Utente Non Esistente -> Ritorna false")
        void testLogin_UtenteNonTrovato() {
            // 1. ACT
            boolean result = service.login("email.inesistente@ghost.com", "qualasiasiPwd");

            // 2. ASSERT
            Assertions.assertFalse(result, "Il login deve fallire se l'email non esiste nel DB");
        }

        @Test
        @DisplayName("getUtente: Email esistente -> Ritorna oggetto Utente popolato")
        void testGetUtente_Successo() {
            // 1. ACT
            Utente risultato = service.getUtente(baseSupervisore.getEmail());

            // 2. ASSERT
            Assertions.assertNotNull(risultato, "Deve restituire un oggetto Utente");

            Assertions.assertAll("Verifica corrispondenza dati",
                    () -> Assertions.assertEquals(baseSupervisore.getEmail(), risultato.getEmail()),
                    () -> Assertions.assertEquals(baseSupervisore.getNome(), risultato.getNome()),
                    () -> Assertions.assertEquals(Tipi.ruolo.SUPERVISORE, risultato.getRuolo(), "Il ruolo deve essere mappato correttamente")
            );
        }

        @Test
        @DisplayName("getUtente: Email non esistente -> Ritorna null")
        void testGetUtente_NonTrovato() {
            // 1. ACT
            Utente risultato = service.getUtente("non.esisto@nulla.it");

            // 2. ASSERT
            Assertions.assertNull(risultato, "Deve restituire null se l'utente non viene trovato");
        }

    }

    @Nested
    @DisplayName("Classe per integrazione TaskManagement --> PersistanceManagement")
    class IntegrationTest_TaskManagement{
        private Dipendente baseDipendente;
        private Supervisore baseSupervisore;
        private IUtenteDAO dao;
        private ITaskDAO taskDAO;
        private Task task;




        @BeforeEach
        void clearData() throws Exception {
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 1. IMPORTANTE: Sposta la connessione corrente sullo schema di default (PUBLIC).
                // Se rimani dentro 'ewmsDB', H2 non ti permetterà mai di cancellarlo.
                stmt.execute("SET SCHEMA PUBLIC");

                // 2. Cancella TUTTO (Tabelle, Viste, Vincoli)
                stmt.execute("DROP ALL OBJECTS");

                // 3. (Opzionale ma consigliato) Forza la cancellazione dello schema per evitare residui
                stmt.execute("DROP SCHEMA IF EXISTS ewmsDB CASCADE");

                // 4. Ora puoi ricreare tutto da zero in sicurezza
                stmt.execute("RUNSCRIPT FROM 'classpath:ewmsDB.sql'");
            }
        }

        @BeforeEach
        void setUp() throws Exception {
            baseSupervisore = new Supervisore();
            baseSupervisore.setNome("Supervisore");
            baseSupervisore.setCognome("Rossi");
            baseSupervisore.setEmail("supervisore.rossi@azienda.it");
            baseSupervisore.setDataNasc(Date.valueOf("1985-05-22"));
            baseSupervisore.setRuolo(Tipi.ruolo.SUPERVISORE);


            baseDipendente = new Dipendente();
            baseDipendente.setNome("Luigi");
            baseDipendente.setCognome("Rossi");
            baseDipendente.setEmail("luigi.rossi@azienda.it");
            baseDipendente.setDataNasc(Date.valueOf("1985-05-23"));
            baseDipendente.setRuolo(Tipi.ruolo.DIPENDENTE);

            dao = new UtenteDAO();

            dao.createSupervisore(baseSupervisore, BCrypt.hashpw("goodpwd.1", BCrypt.gensalt()));

            // Supervisore sup = (Supervisore) dao.findByEmail(baseSupervisore.getEmail());
            //Non ho bisogno perché nei dao li imposto direttamente, inutile per l'app ma utile per i test
            // questo avviene quando faccio la query per inserirli nelle sottotabelle
            baseDipendente.setSupervisoreInfo(new Informazioni(baseSupervisore.getMatricola(),baseSupervisore.getNome(), baseSupervisore.getCognome()));
            dao.createDipendente(baseDipendente, BCrypt.hashpw("goodpwd.1", BCrypt.gensalt()));


            task = new Task("Task #1",Date.valueOf("2026-01-12"), Date.valueOf("2026-01-10"), "Provare che il sottosistema funzioni", Tipi.stato.DA_COMPLETARE,
                    baseSupervisore.getMatricola(), baseDipendente.getMatricola(), Tipi.priorita.BASSA);


            taskDAO = new TaskDAO();

            taskDAO.create(task);
        }

        @Nested
        @DisplayName("Metodi di TaskCommonService integrazione con PersistenceManagement")
        class TestInt_TackCommonService {

            private TaskCommonService taskCommonService = new TaskCommonServiceImpl();

            @Test
            @DisplayName("getTask: Task esistente -> Restituisce oggetto popolato correttamente")
            void testGetTask_Successo() throws SQLException {
                // 1. ACT
                Task result = taskCommonService.getTask(task.getId());

                // 2. ASSERT
                Assertions.assertNotNull(result, "Il task deve essere recuperato dal DB");

                Assertions.assertAll("Verifica Dati Task",
                        () -> Assertions.assertEquals(task.getId(), result.getId()),
                        () -> Assertions.assertEquals("Task #1", result.getTitolo()),
                        () -> Assertions.assertEquals(Tipi.stato.DA_COMPLETARE, result.getStato()),
                        // Verifica FK
                        () -> Assertions.assertEquals(baseDipendente.getMatricola(), result.getDipendente(), "Deve caricare il dipendente associato"),
                        () -> Assertions.assertEquals(baseSupervisore.getMatricola(), result.getSupervisore(), "Deve caricare il supervisore associato")
                );
            }

            @Test
            @DisplayName("holdTask: Task DA_COMPLETARE (Stato non valido) -> IllegalStateException")
            void testHoldTask_StatoErrato_DaCompletare() throws Exception {

                Assertions.assertThrows(IllegalStateException.class, () -> {
                    taskCommonService.holdTask(task.getId());
                });

            }

            @Test
            @DisplayName("holdTask: Task IN_ESECUZIONE -> Transizione a IN_SOSPENSIONE")
            void testHoldTask_Successo() throws Exception {
                // 1. ARRANGE SPECIFICO

                taskDAO.updateStatus(task.getId(), Tipi.stato.IN_ESECUZIONE);

                // 2. ACT
                boolean result = taskCommonService.holdTask(task.getId());

                // 3. ASSERT
                Assertions.assertTrue(result, "L'operazione deve avere successo");

                // Verifica Side-Effect sul DB

                Task taskAggiornato = taskDAO.findById(task.getId());
                Assertions.assertEquals(Tipi.stato.IN_SOSPENSIONE, taskAggiornato.getStato(),
                        "Lo stato nel DB deve essere aggiornato a IN_SOSPENSIONE");
            }

            @Test
            @DisplayName("holdTask: ID Inesistente -> Ritorna false")
            void testHoldTask_IdInesistente() throws Exception {

                boolean result = taskCommonService.holdTask(999999);
                Assertions.assertFalse(result);

            }
        }

        @Nested
        @DisplayName("Metodi di TaskSupervisoreService integrazione con persistencemanagement")
        class TestInt_TaskSupervisoreService{
            private TaskSupervisoreService taskSupervisoreService = new TaskSupervisoreServiceImpl();

            @Test
            @DisplayName("createTask: Dati validi -> Task salvato nel DB con FK corrette")
            void testCreateTask_Successo() throws Exception {
                // 1. ARRANGE
                // Definiamo i parametri per il nuovo task
                String titolo = "Nuovo Task Integration";
                String istruzioni = "Istruzioni molto lunghe per soddisfare la precondizione di lunghezza >= 10";
                Date dataCreazione = Date.valueOf(LocalDate.now());
                Date dataScadenza = Date.valueOf(LocalDate.now().plusDays(7)); // Scadenza > Creazione


                int matDip = baseDipendente.getMatricola();
                int matSup = baseSupervisore.getMatricola();

                // 2. ACT
                taskSupervisoreService.createTask(titolo, dataCreazione, dataScadenza, istruzioni,
                        Tipi.stato.DA_COMPLETARE, matSup,matDip,
                        Tipi.priorita.ALTA, null);

                // 3. ASSERT (Verifica persistenza)

                List<Task> tasks = taskDAO.findByUtente(baseSupervisore);

                // Cerchiamo il task appena creato nella lista
                Task taskCreato = tasks.stream()
                        .filter(t -> t.getTitolo().equals(titolo)) // Assumo titolo mappato su descrizione o campo simile
                        .findFirst()
                        .orElse(null);

                Assertions.assertNotNull(taskCreato, "Il task deve essere stato salvato nel DB");

                Assertions.assertAll("Verifica Campi Task",
                        () -> Assertions.assertEquals(istruzioni, taskCreato.getIstruzioni()),
                        () -> Assertions.assertEquals(Tipi.stato.DA_COMPLETARE, taskCreato.getStato()),
                        () -> Assertions.assertEquals(Tipi.priorita.ALTA, taskCreato.getPriorita()),
                        // Verifica FK
                        () -> Assertions.assertEquals(matDip, taskCreato.getDipendente()),
                        () -> Assertions.assertEquals(matSup, taskCreato.getSupervisore())
                );
            }

            @Test
            @DisplayName("createTask: Dipendente inesistente -> Errore Vincolo Integrità (DB)")
            void testCreateTask_DipendenteInesistente() {
                // 1. ARRANGE
                int matDipInesistente = 99999;

                // 2. ACT & ASSERT

                Assertions.assertThrows(IllegalArgumentException.class, () -> {
                    taskSupervisoreService.createTask("Titolo", Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(1)),
                            "Istruzioni valide > 10 chars", Tipi.stato.DA_COMPLETARE,
                            baseSupervisore.getMatricola(), matDipInesistente,
                            Tipi.priorita.BASSA, null);
                });
            }


            @Test
            @DisplayName("getAllTaskSup: Ritorna solo i task assegnati al supervisore specifico")
            void testGetAllTaskSup_Successo() throws Exception {
                // 1. ARRANGE
                // Creiamo un SECONDO supervisore per verificare che i suoi task NON vengano restituiti
                Supervisore sup2 = new Supervisore();
                sup2.setNome("Altro");
                sup2.setCognome("Supervisore");
                sup2.setEmail("altro@sup.it");
                sup2.setRuolo(Tipi.ruolo.SUPERVISORE);
                sup2.setDataNasc(Date.valueOf(LocalDate.now()));
                dao.createSupervisore(sup2, "pwd13456.");

                //sup2 = (Supervisore) dao.findByEmail("altro.sup@azienda.it"); // Refresh ID

                // Inseriamo manualmente 3 task nel DB:
                // Task 1 -> Base Supervisore
                Task t1 = new Task("Task Sup Base 1",Date.valueOf("2026-01-22"),Date.valueOf("2026-01-25"),"Istruzioni abbastanza lunghe", Tipi.stato.DA_COMPLETARE, baseSupervisore.getMatricola(),baseDipendente.getMatricola(), Tipi.priorita.MEDIA);

                taskDAO.create(t1);

                // Task 2 -> Base Supervisore
                Task t2 =  new Task("Task Sup Base 2",Date.valueOf("2026-01-22"),Date.valueOf("2026-01-25"),"Istruzioni abbastanza lunghe", Tipi.stato.DA_COMPLETARE, baseSupervisore.getMatricola(),baseDipendente.getMatricola(), Tipi.priorita.MEDIA);
                taskDAO.create(t2);

                // Task 3 -> Altro Supervisore (Disturbo)
                Task t3 = new Task("Task altro supervisore",Date.valueOf("2026-01-22"),Date.valueOf("2026-01-25"),"Istruzioni abbastanza lunghe", Tipi.stato.DA_COMPLETARE,sup2.getMatricola(),baseDipendente.getMatricola(), Tipi.priorita.MEDIA);
                taskDAO.create(t3);

                // 2. ACT
                List<Task> risultati = taskSupervisoreService.getAllTaskSup(baseSupervisore);

                // 3. ASSERT
                Assertions.assertNotNull(risultati);
                Assertions.assertEquals(3, risultati.size(), "Deve trovare solo i 2 task del supervisore base");

                // Verifica che i task siano quelli giusti

                boolean contieneT1 = risultati.stream().anyMatch(t -> t.getTitolo().equals("Task Sup Base 1"));
                boolean contieneT3 = risultati.stream().anyMatch(t -> t.getTitolo().equals("Task altro supervisore"));

                Assertions.assertTrue(contieneT1, "Deve contenere il task del supervisore");
                Assertions.assertFalse(contieneT3, "NON deve contenere il task dell'altro supervisore");
            }

            @Test
            @DisplayName("getAllDipendenteInfo: Recupera dipendenti assegnati al supervisore")
            void testGetAllDipendenteInfo() throws SQLException {
                // 1. ARRANGE


                // 2. ACT
                List<Informazioni> infoDipendenti = taskSupervisoreService.getAllDipendentiInfo(baseSupervisore.getMatricola());

                // 3. ASSERT
                Assertions.assertNotNull(infoDipendenti);
                Assertions.assertFalse(infoDipendenti.isEmpty(), "La lista non deve essere vuota");

                Informazioni info = infoDipendenti.get(0);
                Assertions.assertEquals(baseDipendente.getNome(), info.getNome());
                Assertions.assertEquals(baseDipendente.getCognome(), info.getCognome());
            }

            @Test
            @DisplayName("deleteTask: Task esistente -> Rimosso dal DB e ritorna true")
            void testDeleteTask_Successo() throws Exception {
                // 1. ARRANGE
                // Creiamo un task da cancellare

                List<Task> list = taskDAO.findByUtente(baseSupervisore);
                long idDaCancellare = -1;

                for (Task t : list) {
                    if  (task.getTitolo().equals(t.getTitolo())) {
                        idDaCancellare = t.getId();
                    }
                }

                // 2. ACT
                boolean risultato = taskSupervisoreService.deleteTask(idDaCancellare);

                // 3. ASSERT
                Assertions.assertTrue(risultato, "Delete deve restituire true");

                // 4. ASSERT
                Task check = taskDAO.findById(idDaCancellare);
                Assertions.assertNull(check, "Il task non deve più esistere nel DB");
            }

            @Test
            @DisplayName("deleteTask: ID inesistente -> Ritorna true (o false a seconda dell'impl) e non fa nulla")
            void testDeleteTask_Inesistente() throws SQLException {
                // La post-condizione OCL dice: if targetTask = null result = true (o false? la tua spec è un po' ambigua "result = true else result = false")
                // Solitamente delete su inesistente ritorna false. Adatto il test alla logica comune.

                boolean risultato = taskSupervisoreService.deleteTask(999999);

                Assertions.assertFalse(risultato, "Dovrebbe tornare false se non ha trovato nulla da cancellare");
            }
        }

        @Nested
        @DisplayName("Classe per integrazione TaskDipendenteService --> PersistenceManagement")
        class TestInt_TaskDipendenteService {
            private TaskDipendenteService  taskDipendenteService = new TaskDipendenteServiceImpl();

            @BeforeEach
            void clearData() throws Exception {
                try (Connection conn = DataSourceFactory.getConnection();
                     Statement stmt = conn.createStatement()) {

                    // 1. IMPORTANTE: Sposta la connessione corrente sullo schema di default (PUBLIC).
                    // Se rimani dentro 'ewmsDB', H2 non ti permetterà mai di cancellarlo.
                    stmt.execute("SET SCHEMA PUBLIC");

                    // 2. Cancella TUTTO (Tabelle, Viste, Vincoli)
                    stmt.execute("DROP ALL OBJECTS");

                    // 3. (Opzionale ma consigliato) Forza la cancellazione dello schema per evitare residui
                    stmt.execute("DROP SCHEMA IF EXISTS ewmsDB CASCADE");

                    // 4. Ora puoi ricreare tutto da zero in sicurezza
                    stmt.execute("RUNSCRIPT FROM 'classpath:ewmsDB.sql'");
                }
            }

            @BeforeEach
            void setUp() throws Exception {
                baseSupervisore = new Supervisore();
                baseSupervisore.setNome("Supervisore");
                baseSupervisore.setCognome("Rossi");
                baseSupervisore.setEmail("supervisore.rossi@azienda.it");
                baseSupervisore.setDataNasc(Date.valueOf("1985-05-22"));
                baseSupervisore.setRuolo(Tipi.ruolo.SUPERVISORE);


                baseDipendente = new Dipendente();
                baseDipendente.setNome("Luigi");
                baseDipendente.setCognome("Rossi");
                baseDipendente.setEmail("luigi.rossi@azienda.it");
                baseDipendente.setDataNasc(Date.valueOf("1985-05-23"));
                baseDipendente.setRuolo(Tipi.ruolo.DIPENDENTE);

                dao = new UtenteDAO();

                dao.createSupervisore(baseSupervisore, BCrypt.hashpw("goodpwd.1", BCrypt.gensalt()));

                // Supervisore sup = (Supervisore) dao.findByEmail(baseSupervisore.getEmail());
                //Non ho bisogno perché nei dao li imposto direttamente, inutile per l'app ma utile per i test
                // questo avviene quando faccio la query per inserirli nelle sottotabelle
                baseDipendente.setSupervisoreInfo(new Informazioni(baseSupervisore.getMatricola(),baseSupervisore.getNome(), baseSupervisore.getCognome()));
                dao.createDipendente(baseDipendente, BCrypt.hashpw("goodpwd.1", BCrypt.gensalt()));


                task = new Task("Task #1",Date.valueOf("2026-01-12"), Date.valueOf("2026-01-10"), "Provare che il sottosistema funzioni", Tipi.stato.DA_COMPLETARE,
                        baseSupervisore.getMatricola(), baseDipendente.getMatricola(), Tipi.priorita.BASSA);


                taskDAO = new TaskDAO();

                taskDAO.create(task);
            }

            @Test
            @DisplayName("getAllTaskDip: Recupero task assegnati al dipendente specifico")
            void testGetAllTaskDip_Successo() throws Exception {
                // 1. ARRANGE
                // Creiamo un secondo task per lo stesso dipendente
                Task task2 = new Task("Task #2", Date.valueOf("2026-02-12"), Date.valueOf("2026-02-10"),
                        "Secondo task", Tipi.stato.DA_COMPLETARE,
                        baseSupervisore.getMatricola(), baseDipendente.getMatricola(),
                        Tipi.priorita.MEDIA);

                //Non ho bisogno di ripescarlo perché nella logica per l'allegato (esclusivo db) mi piglio di l'id direttamente

                taskDAO.create(task2);

                // 2. ACT
                List<Task> listaRisultato = taskDipendenteService.getAllTaskDip(baseDipendente);

                // 3. ASSERT
                Assertions.assertNotNull(listaRisultato);
                // Verifichiamo che la lista contenga almeno i 2 task del dipendente
                boolean contieneTask1 = listaRisultato.stream().anyMatch(t -> t.getId() == task.getId());
                boolean contieneTask2 = listaRisultato.stream().anyMatch(t -> t.getId() == task2.getId());

                Assertions.assertTrue(contieneTask1 && contieneTask2, "La lista deve contenere tutti i task del dipendente");
            }

            @Test
            @DisplayName("inizializzaTask: Transizione da DA_COMPLETARE a IN_ESECUZIONE")
            void testInizializzaTask_Successo() throws Exception {
                // 1. ACT
                // Il task nel setup è DA_COMPLETARE, quindi la transizione è valida
                boolean esito = taskDipendenteService.inizializzaTask(task.getId());

                // 2. ASSERT
                Assertions.assertTrue(esito, "Il metodo deve restituire true per una transizione valida");

                Task taskAggiornato = taskDAO.findById(task.getId());
                Assertions.assertEquals(Tipi.stato.IN_ESECUZIONE, taskAggiornato.getStato(),
                        "Lo stato nel DB deve essere passato a IN_ESECUZIONE");
            }

            @Test
            @DisplayName("inizializzaTask: Task già COMPLETATO -> Lancia IllegalStateException")
            void testInizializzaTask_PrecondizioneViolata() throws Exception {
                // 1. ARRANGE

                taskDAO.updateStatus(task.getId(), Tipi.stato.COMPLETATO);

                // 2. ACT & ASSERT
                // Se è completato non posso inizializzarlo
                Assertions.assertThrows(IllegalStateException.class, () -> {
                    taskDipendenteService.inizializzaTask(task.getId());
                });
            }


            @Test
            @DisplayName("completeTask: Transizione da IN_ESECUZIONE a COMPLETATO")
            void testCompleteTask_Successo() throws Exception {
                // 1. ARRANGE

                taskDAO.updateStatus(task.getId(), Tipi.stato.IN_ESECUZIONE);

                // 2. ACT
                boolean esito = taskDipendenteService.completeTask(task.getId());

                // 3. ASSERT
                Assertions.assertTrue(esito, "Il completamento deve restituire true");

                // Verifica persistenza

                Task taskFinale = taskDAO.findById(task.getId());
                Assertions.assertEquals(Tipi.stato.COMPLETATO, taskFinale.getStato(),
                        "Lo stato finale nel DB deve essere COMPLETATO");
            }

            @Test
            @DisplayName("completeTask: Task in stato DA_COMPLETARE -> Lancia IllegalStateException")
            void testCompleteTask_StatoErrato() {
                // 1. ACT & ASSERT
                // Il task è DA_COMPLETARE (dal setup), ma il metodo richiede IN_ESECUZIONE
                Assertions.assertThrows(IllegalStateException.class, () -> {
                    taskDipendenteService.completeTask(task.getId());
                }, "Non si può completare un task che non è stato ancora iniziato");
            }

            @Test
            @DisplayName("Operazioni su ID inesistente -> Ritorna false")
            void testMetodi_IdInesistente() throws SQLException {
                int idFalso = 99999;
                Assertions.assertFalse(taskDipendenteService.inizializzaTask(idFalso));
                Assertions.assertFalse(taskDipendenteService.completeTask(idFalso));
            }

        }





    }

}










