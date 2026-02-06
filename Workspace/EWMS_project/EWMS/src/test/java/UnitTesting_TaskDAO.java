import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.ClassiDAO.TaskDAO;
import it.unisa.ewms.persistance.ClassiDAO.UtenteDAO;
import it.unisa.ewms.persistance.DataSourceFactory;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;
import it.unisa.ewms.persistance.interfaces.ITaskDAO;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class UnitTesting_TaskDAO {
    private TaskDAO taskDAO= new TaskDAO();
    private UtenteDAO  utenteDAO= new UtenteDAO();
    private Supervisore sup;
    private Dipendente dipendente1;
    private int matricolaDip1;
    private int matricolaSup;
    private Task task;

    @BeforeAll
    static void setupDatabase() {
        // Fondamentale: forza la Factory a usare la config di test con H2
        DataSourceFactory.setPropertiesFile("test.properties");

    }

    @Nested
    @DisplayName("Classe per il test del metodo create")
    class Test_create{

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
        void setup(){
            sup = new Supervisore();
            sup.setNome("Mario");
            sup.setCognome("Rossi");
            sup.setEmail("mario.rossi@azienda.it");
            sup.setDataNasc(Date.valueOf("1985-05-20"));
            sup.setRuolo(Tipi.ruolo.SUPERVISORE);

            dipendente1 = new Dipendente();
            dipendente1.setNome("Mario");
            dipendente1.setCognome("Rossi");
            dipendente1.setEmail("luigi.rossi@azienda.it");
            dipendente1.setDataNasc(Date.valueOf("1985-05-20"));
            dipendente1.setRuolo(Tipi.ruolo.DIPENDENTE);

            try {
                utenteDAO.createSupervisore(sup, "goodpwd12!");
                matricolaSup = -1;
                try (Connection conn = DataSourceFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                    stmt.setString(1, sup.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) matricolaSup = rs.getInt("matricola");
                    }
                }
                sup.setMatricola(matricolaSup);
                dipendente1.setSupervisoreInfo(new Informazioni(matricolaSup, sup.getNome(), sup.getCognome()));

                utenteDAO.createDipendente(dipendente1, "goodpwd12.2");

                try (Connection conn = DataSourceFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                    stmt.setString(1, dipendente1.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) matricolaDip1 = rs.getInt("matricola");
                    }
                }

                dipendente1.setMatricola(matricolaDip1);

            } catch (SQLException | EmailGiaPresenteException e) {
                throw new RuntimeException(e);
            }

            task = new Task();
            task.setTitolo("Task #1");
            task.setIstruzioni("niente di che");
            // Conversione delle date dalla specifica (formato YYYY-MM-DD per Date.valueOf)
            task.setDataCreazione(Date.valueOf("2026-02-02"));
            task.setDataDiScadenza(Date.valueOf("2026-02-03"));

            // Assumiamo l'utilizzo della classe Tipi per Enums come visto nel setup dei ruoli
            task.setPriorita(Tipi.priorita.BASSA);
            task.setStato(Tipi.stato.IN_ESECUZIONE);

            // Utilizziamo le matricole create nel setup per soddisfare i vincoli di integrità
            task.setSupervisore(matricolaSup);
            task.setDipendente(matricolaDip1);
            task.setAllegato(null);
        }


        @Test
        @DisplayName("TestFrame1: Creazione Task valida senza allegato -> Successo")
        void testCreate_Successo() throws Exception {
            // --- ACT ---
            taskDAO.create(task);

            // --- ASSERT ---
            // Verifica Post-condizione: Task.allInstances()->includes(task)
            String sql = "SELECT COUNT(*) FROM ewmsDB.Task WHERE titolo = ? AND supervisore = ? AND dipendente = ?";

            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, task.getTitolo());
                stmt.setInt(2, matricolaSup);
                stmt.setInt(3, matricolaDip1);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Assertions.assertEquals(1, rs.getInt(1),
                                "La task deve essere stata correttamente inserita nel database");
                    } else {
                        Assertions.fail("Nessun record trovato nella tabella Task");
                    }
                }
            } catch (SQLException e) {
                Assertions.fail("Errore durante la verifica del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame2: Creazione Task con Allegato -> Successo")
        void testCreate_ConAllegato_Successo() throws Exception {
            // --- ARRANGE ---

            // Creazione dell'oggetto Allegato

            Allegato allegato = new Allegato();
            allegato.setFilename("documento.pdf");
            allegato.setDirectoryFilename("nomenellaDirectory.pdf");
            allegato.setFilePath("/server/storage/uploads/2026/documento.pdf");
            allegato.setContentType("application/pdf");

            task.setAllegato(allegato);

            // --- ACT ---
            taskDAO.create(task);

            // --- ASSERT ---
            try (Connection conn = DataSourceFactory.getConnection()) {

                // 1. Verifica presenza della Task
                String sqlTask = "SELECT id FROM ewmsDB.Task WHERE titolo = ? AND dipendente = ?";
                int idTaskGenerato = -1;

                try (PreparedStatement stmt = conn.prepareStatement(sqlTask)) {
                    stmt.setString(1, task.getTitolo());
                    stmt.setInt(2, matricolaDip1);
                    try (ResultSet rs = stmt.executeQuery()) {
                        Assertions.assertTrue(rs.next(), "La task deve essere presente nel database");
                        idTaskGenerato = rs.getInt("id");
                    }
                }

                // 2. Verifica presenza dell'Allegato (Post-condizione)
                // Assumiamo che l'allegato sia collegato alla task tramite FK o tabella dedicata
                String sqlAllegato = "SELECT COUNT(*) FROM ewmsDB.Allegato WHERE filename = ? AND task_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlAllegato)) {
                    stmt.setString(1, "documento.pdf");
                    stmt.setInt(2, idTaskGenerato);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Assertions.assertEquals(1, rs.getInt(1),
                                    "L'allegato deve essere presente e collegato alla task corretta");
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("TestFrame3: Task null -> IllegalArgumentException")
        void testCreate_TaskNull() {
            // --- ARRANGE ---
            // Task volutamente null per testare la pre-condizione
            Task taskNull = null;

            // --- ACT & ASSERT ---
            // Verifica che venga lanciata l'eccezione IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.create(taskNull);
            }, "Dovrebbe lanciare IllegalArgumentException se l'oggetto task è null");

            // --- ASSERT EXTRA (Database Integrity) ---
            // Verifichiamo che la tabella Task sia rimasta vuota
            String sqlCheck = "SELECT COUNT(*) FROM ewmsDB.Task";

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCheck)) {

                if (rs.next()) {
                    Assertions.assertEquals(0, rs.getInt(1),
                            "Il database non deve contenere record dopo un tentativo di creazione con task null");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore SQL durante la verifica dell'integrità: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame4: Titolo null -> IllegalArgumentException")
        void testCreate_TitoloNull() {
            // --- ARRANGE ---
            // Modifichiamo il baseTask (inizializzato nel setup) per violare la pre-condizione
            task.setTitolo(null);


            // --- ACT & ASSERT ---
            // Verifica che venga lanciata IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.create(task);
            }, "Dovrebbe lanciare IllegalArgumentException se il titolo della task è null");

            // --- ASSERT EXTRA (Database Integrity) ---
            // Verifichiamo che l'integrità del database sia preservata
            String sqlCheck = "SELECT COUNT(*) FROM ewmsDB.Task";

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCheck)) {

                if (rs.next()) {
                    Assertions.assertEquals(0, rs.getInt(1),
                            "La tabella Task deve rimanere vuota dopo un tentativo di creazione non valido");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore SQL durante la verifica del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame5: Titolo troppo lungo (> 50 caratteri) -> IllegalArgumentException")
        void testCreate_TitoloTroppoLungo() {
            // --- ARRANGE ---

            String titoloLungo = "QuestoTitoloEAssolutamenteTroppoLungoPerIlDatabaseX";
            task.setTitolo(titoloLungo);
            task.setStato(Tipi.stato.IN_ESECUZIONE);

            // --- ACT & ASSERT ---
            // Oracolo: IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.create(task);
            }, "Dovrebbe lanciare IllegalArgumentException se il titolo supera i 50 caratteri");


            String sqlCheck = "SELECT COUNT(*) FROM ewmsDB.Task";

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCheck)) {

                if (rs.next()) {
                    Assertions.assertEquals(0, rs.getInt(1),
                            "La tabella Task deve rimanere vuota dopo un tentativo con titolo non valido");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore SQL durante la verifica dell'integrità del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame6: Istruzioni null -> IllegalArgumentException")
        void testCreate_IstruzioniNull() {
            // --- ARRANGE ---

            task.setIstruzioni(null);

            // --- ACT & ASSERT ---
            // L'oracolo prevede il lancio di IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.create(task);
            }, "Dovrebbe lanciare IllegalArgumentException se le istruzioni sono null");


            String sqlCheck = "SELECT COUNT(*) FROM ewmsDB.Task";

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCheck)) {

                if (rs.next()) {
                    Assertions.assertEquals(0, rs.getInt(1),
                            "La tabella Task deve rimanere vuota a causa della violazione della pre-condizione");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore durante la verifica dell'integrità del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame7: Istruzioni troppo lunghe (> 2000 caratteri) -> IllegalArgumentException")
        void testCreate_IstruzioniTroppoLunghe() {
            // --- ARRANGE ---
            // Generiamo una string di 2001 caratteri (Limite della specifica: 2000)
            String istruzioniEccessive = "A".repeat(2001);

            task.setTitolo("Titolo Valido");
            task.setIstruzioni(istruzioniEccessive);
            task.setStato(Tipi.stato.IN_ESECUZIONE);

            // --- ACT & ASSERT ---
            // Oracolo: IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.create(task);
            }, "Dovrebbe lanciare IllegalArgumentException se le istruzioni superano i 2000 caratteri");

            // --- ASSERT EXTRA (Database Integrity) ---
            String sqlCheck = "SELECT COUNT(*) FROM ewmsDB.Task";

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCheck)) {

                if (rs.next()) {
                    Assertions.assertEquals(0, rs.getInt(1),
                            "La tabella Task deve rimanere vuota a causa del superamento del limite caratteri");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore durante la verifica dell'integrità del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame8: Date null -> IllegalArgumentException")
        void testCreate_DateNull() {
            // --- ARRANGE ---

            task.setDataCreazione(null); // Violazione pre-condizione: task.dataDiCreazione <> null

            // --- ACT & ASSERT ---
            // L'oracolo prevede il lancio di IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.create(task);
            }, "Dovrebbe lanciare IllegalArgumentException se la data di creazione è null");

            task.setDataCreazione(Date.valueOf(LocalDate.now()));
            task.setDataDiScadenza(null);

            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.create(task);
            }, "Dovrebbe lanciare IllegalArgumentException se la data di creazione è null");

            String sqlCheck = "SELECT COUNT(*) FROM ewmsDB.Task";

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCheck)) {

                if (rs.next()) {
                    Assertions.assertEquals(0, rs.getInt(1),
                            "La tabella Task deve rimanere vuota a causa della violazione della pre-condizione sulla data");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore durante la verifica dell'integrità del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame9: Priorità e stato null -> IllegalArgumentException")
        void testCreate_EnumNull() {
            // --- ARRANGE ---
            task.setPriorita(null);

            // --- ACT & ASSERT ---

            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.create(task);
            }, "Dovrebbe lanciare IllegalArgumentException se la priorità è null");

            task.setPriorita(Tipi.priorita.ALTA);
            task.setStato(null);

            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.create(task);
            }, "Dovrebbe lanciare IllegalArgumentException se la priorità è null");

            String sqlCheck = "SELECT COUNT(*) FROM ewmsDB.Task";

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCheck)) {

                if (rs.next()) {
                    Assertions.assertEquals(0, rs.getInt(1),
                            "Nessun record deve essere presente nella tabella Task se la priorità è null");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore durante la verifica dell'integrità del database: " + e.getMessage());
            }
        }


        @Test
        @DisplayName("TestFrame10: Matricola Supervisore non valida (<= 0) -> IllegalArgumentException")
        void testCreate_SupervisoreMatricolaInvalida() {
            // --- ARRANGE ---
            // Utilizziamo il baseTask inizializzato nel setup
            task.setSupervisore(0);

            // --- ACT & ASSERT ---
            // L'oracolo prevede il lancio di IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.create(task);
            }, "Dovrebbe lanciare IllegalArgumentException se la matricola del supervisore è <= 0");

            // --- ASSERT EXTRA (Database Integrity) ---
            // Verifica che il DAO non abbia interagito con il Database a causa della pre-condizione fallita
            String sqlCheck = "SELECT COUNT(*) FROM ewmsDB.Task";
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCheck)) {

                if (rs.next()) {
                    Assertions.assertEquals(0, rs.getInt(1),
                            "La tabella Task deve rimanere vuota; l'inserimento non doveva procedere");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore durante la verifica dell'integrità del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame11: Matricola Dipendente non valida (<= 0) -> IllegalArgumentException")
        void testCreate_DipendenteMatricolaInvalida() {
            // --- ARRANGE ---

            task.setDipendente(0);

            // --- ACT & ASSERT ---
            // Oracolo: IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.create(task);
            }, "Dovrebbe lanciare IllegalArgumentException se la matricola del dipendente è <= 0");


            String sqlCheck = "SELECT COUNT(*) FROM ewmsDB.Task";
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCheck)) {

                if (rs.next()) {
                    Assertions.assertEquals(0, rs.getInt(1),
                            "La tabella Task deve rimanere vuota a causa della pre-condizione fallita");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore durante la verifica dell'integrità del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame12: Supervisore Inesistente nel DB (999) -> SQLException")
        void testCreate_SupervisoreInesistente() {
            // --- ARRANGE ---

            task.setSupervisore(999);


            Assertions.assertThrows(SQLException.class, () -> {
                taskDAO.create(task);
            }, "Dovrebbe lanciare SQLException perché il supervisore 999 non esiste nel database");


            String sqlCheck = "SELECT COUNT(*) FROM ewmsDB.Task";
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCheck)) {

                if (rs.next()) {
                    Assertions.assertEquals(0, rs.getInt(1),
                            "La tabella Task deve essere vuota: l'operazione deve fallire per mancanza di integrità referenziale");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore durante la verifica dell'integrità del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame13: Dipendente Inesistente nel DB (999) -> SQLException")
        void testCreate_DipendenteInesistente() {
            // --- ARRANGE ---

            task.setDipendente(999);

            Assertions.assertThrows(SQLException.class, () -> {
                taskDAO.create(task);
            }, "Dovrebbe lanciare SQLException perché la matricola dipendente 999 non è censita");

            String sqlCheck = "SELECT COUNT(*) FROM ewmsDB.Task";
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCheck)) {

                if (rs.next()) {
                    Assertions.assertEquals(0, rs.getInt(1),
                            "La tabella Task deve essere vuota: violazione dell'integrità referenziale sul dipendente");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore durante la verifica dell'integrità del database: " + e.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Classe per il test del metodo findById")
    class Test_findById {

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
        void setup() throws Exception {
            sup = new Supervisore();
            sup.setNome("Mario");
            sup.setCognome("Rossi");
            sup.setEmail("mario.rossi@azienda.it");
            sup.setDataNasc(Date.valueOf("1985-05-20"));
            sup.setRuolo(Tipi.ruolo.SUPERVISORE);

            dipendente1 = new Dipendente();
            dipendente1.setNome("Mario");
            dipendente1.setCognome("Rossi");
            dipendente1.setEmail("luigi.rossi@azienda.it");
            dipendente1.setDataNasc(Date.valueOf("1985-05-20"));
            dipendente1.setRuolo(Tipi.ruolo.DIPENDENTE);

            try {
                utenteDAO.createSupervisore(sup, "goodpwd12!");
                matricolaSup = -1;
                try (Connection conn = DataSourceFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                    stmt.setString(1, sup.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) matricolaSup = rs.getInt("matricola");
                    }
                }
                sup.setMatricola(matricolaSup);
                dipendente1.setSupervisoreInfo(new Informazioni(matricolaSup, sup.getNome(), sup.getCognome()));

                utenteDAO.createDipendente(dipendente1, "goodpwd12.2");

                try (Connection conn = DataSourceFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                    stmt.setString(1, dipendente1.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) matricolaDip1 = rs.getInt("matricola");
                    }
                }

                dipendente1.setMatricola(matricolaDip1);

            } catch (SQLException | EmailGiaPresenteException e) {
                throw new RuntimeException(e);
            }

            task = new Task();
            task.setTitolo("Task #1");
            task.setIstruzioni("niente di che");
            // Conversione delle date dalla specifica (formato YYYY-MM-DD per Date.valueOf)
            task.setDataCreazione(Date.valueOf("2026-02-02"));
            task.setDataDiScadenza(Date.valueOf("2026-02-03"));

            // Assumiamo l'utilizzo della classe Tipi per Enums come visto nel setup dei ruoli
            task.setPriorita(Tipi.priorita.BASSA);
            task.setStato(Tipi.stato.IN_ESECUZIONE);

            // Utilizziamo le matricole create nel setup per soddisfare i vincoli di integrità
            task.setSupervisore(matricolaSup);
            task.setDipendente(matricolaDip1);
            task.setAllegato(null);


            taskDAO.create(task);
            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT id FROM ewmsDB.task WHERE dipendente = ?")) {
                 stmt.setInt(1, dipendente1.getMatricola());
                 try (ResultSet rs = stmt.executeQuery()) {
                     if (rs.next()) task.setId(rs.getInt("id"));


                 }
            } catch (Exception e) {throw new RuntimeException(e);}

            System.out.println("Setup: " + task.getId());
        }

        @Test
        @DisplayName("TestFrame1: ID valido e Task presente -> Restituisce l'oggetto Task")
        void testFindById_Presente() throws SQLException {
            // --- ARRANGE ---
            // L'id della task è stato recuperato e settato nell'oggetto 'task' durante il setup
            long idRicerca = task.getId();
            System.out.println(idRicerca);
            // --- ACT ---
            Task risultato = taskDAO.findById(idRicerca);

            // --- ASSERT ---
            // 1. Verifichiamo che il risultato non sia null
            Assertions.assertNotNull(risultato, "Il metodo dovrebbe restituire una Task, non null");

            // 2. Verifichiamo la corrispondenza dei dati (Post-condizione)
            Assertions.assertEquals(idRicerca, risultato.getId(),
                    "L'ID della task restituita deve corrispondere a quello cercato");

            Assertions.assertEquals(task.getTitolo(), risultato.getTitolo(),
                    "Il titolo della task non corrisponde");

            Assertions.assertEquals(task.getIstruzioni(), risultato.getIstruzioni(),
                    "Le istruzioni della task non corrispondono");

            Assertions.assertEquals(task.getPriorita(), risultato.getPriorita(),
                    "La priorità della task non corrisponde");

            Assertions.assertEquals(task.getStato(), risultato.getStato(),
                    "Lo stato della task non corrisponde");

            Assertions.assertEquals(task.getSupervisore(), risultato.getSupervisore(),
                    "Il riferimento al supervisore non corrisponde");

            Assertions.assertEquals(task.getDipendente(), risultato.getDipendente(),
                    "Il riferimento al dipendente non corrisponde");
        }

        @Test
        @DisplayName("TestFrame2: ID valido ma Task non presente -> Restituisce null")
        void testFindById_NonPresente() throws SQLException {
            // --- ARRANGE ---
            // Utilizziamo un ID che non esiste nel DB (es. 9999)
            // Sappiamo che nel setup è stata creata solo una task con un ID differente
            long idInesistente = 9999;

            // --- ACT ---
            Task risultato = taskDAO.findById(idInesistente);

            // --- ASSERT ---
            // Verifica Post-condizione: result = null
            Assertions.assertNull(risultato,
                    "Il metodo deve restituire null se non esiste una task con l'ID specificato");
        }

        @Test
        @DisplayName("TestFrame3: ID negativo (< 0) -> IllegalArgumentException")
        void testFindById_IdNegativo() {
            // --- ARRANGE ---
            // Violazione della pre-condizione Id > 0
            int idInvalido = -1;

            // --- ACT & ASSERT ---
            // Oracolo: IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.findById(idInvalido);
            }, "Dovrebbe lanciare IllegalArgumentException se l'ID passato è minore di 1");
        }

    }

    @Nested
    @DisplayName("Classe per il test del metodo findByUtente")
    class Test_findByUtente{
        private Task task2;

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
        void setup() throws Exception {
            sup = new Supervisore();
            sup.setNome("Mario");
            sup.setCognome("Rossi");
            sup.setEmail("mario.rossi@azienda.it");
            sup.setDataNasc(Date.valueOf("1985-05-20"));
            sup.setRuolo(Tipi.ruolo.SUPERVISORE);

            dipendente1 = new Dipendente();
            dipendente1.setNome("Mario");
            dipendente1.setCognome("Rossi");
            dipendente1.setEmail("luigi.rossi@azienda.it");
            dipendente1.setDataNasc(Date.valueOf("1985-05-20"));
            dipendente1.setRuolo(Tipi.ruolo.DIPENDENTE);

            try {
                utenteDAO.createSupervisore(sup, "goodpwd12!");
                matricolaSup = -1;
                try (Connection conn = DataSourceFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                    stmt.setString(1, sup.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) matricolaSup = rs.getInt("matricola");
                    }
                }
                sup.setMatricola(matricolaSup);
                dipendente1.setSupervisoreInfo(new Informazioni(matricolaSup, sup.getNome(), sup.getCognome()));

                utenteDAO.createDipendente(dipendente1, "goodpwd12.2");

                try (Connection conn = DataSourceFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                    stmt.setString(1, dipendente1.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) matricolaDip1 = rs.getInt("matricola");
                    }
                }

                dipendente1.setMatricola(matricolaDip1);

            } catch (SQLException | EmailGiaPresenteException e) {
                throw new RuntimeException(e);
            }

            task = new Task();
            task.setTitolo("Task #1");
            task.setIstruzioni("niente di che");
            // Conversione delle date dalla specifica (formato YYYY-MM-DD per Date.valueOf)
            task.setDataCreazione(Date.valueOf("2026-02-02"));
            task.setDataDiScadenza(Date.valueOf("2026-02-03"));

            // Assumiamo l'utilizzo della classe Tipi per Enums come visto nel setup dei ruoli
            task.setPriorita(Tipi.priorita.BASSA);
            task.setStato(Tipi.stato.IN_ESECUZIONE);

            // Utilizziamo le matricole create nel setup per soddisfare i vincoli di integrità
            task.setSupervisore(matricolaSup);
            task.setDipendente(matricolaDip1);
            task.setAllegato(null);

            task2 = new Task();
            task2.setTitolo("Task #2");
            task2.setIstruzioni("niente di che");
            task2.setDataCreazione(Date.valueOf("2026-02-02"));
            task2.setDataDiScadenza(Date.valueOf("2026-02-03"));
            task2.setPriorita(Tipi.priorita.BASSA);
            task2.setStato(Tipi.stato.IN_ESECUZIONE);


            task2.setSupervisore(matricolaSup);
            task2.setDipendente(matricolaDip1);


            taskDAO.create(task);
            taskDAO.create(task2);

    }

        @Test
        @DisplayName("TestFrame1: Ricerca per Dipendente -> Restituisce lista non vuota")
        void testFindByUtente_DipendentePresente() throws Exception {
            // --- ARRANGE ---
            // L'oggetto dipendente1 è già stato persistito e ha matricola > 0 e ruolo DIPENDENTE

            // --- ACT ---
            List<Task> risultati = taskDAO.findByUtente(dipendente1);

            // --- ASSERT ---
            // 1. La lista non deve essere null
            Assertions.assertNotNull(risultati, "La lista restituita non deve essere null");

            // 2. La lista deve contenere almeno la task creata nel setup
            Assertions.assertFalse(risultati.isEmpty(), "La lista non deve essere vuota");

            // 3. Verifica della post-condizione: ogni task nella lista deve avere il dipendente cercato
            for (Task t : risultati) {
                Assertions.assertEquals(dipendente1.getMatricola(), t.getDipendente(),
                        "La task restituita non è assegnata al dipendente corretto");
            }

            // Verifica specifica sul contenuto (opzionale)
            Assertions.assertTrue(risultati.stream().anyMatch(t -> t.getTitolo().equals("Task #1")),
                    "La task creata nel setup dovrebbe essere presente nei risultati");

            Assertions.assertEquals(2, risultati.size(), "Dovrebbe trovare 2 task");
        }

        @Test
        @DisplayName("TestFrame2: Ricerca per Supervisore -> Restituisce lista non vuota")
        void testFindByUtente_SupervisorePresente() throws Exception {


            List<Task> risultati = taskDAO.findByUtente(sup);


            Assertions.assertNotNull(risultati, "La lista restituita non deve essere null");


            Assertions.assertFalse(risultati.isEmpty(), "La lista non deve essere vuota per un supervisore con task create");


            for (Task t : risultati) {
                Assertions.assertEquals(sup.getMatricola(), t.getSupervisore(),
                        "La task restituita non appartiene al supervisore specificato");
            }

            boolean taskTrovata = risultati.stream()
                    .anyMatch(t -> t.getTitolo().equals("Task #1"));

            Assertions.assertTrue(taskTrovata, "La task creata nel setup deve essere presente nei risultati");
            Assertions.assertEquals(2, risultati.size(), "Dovrebbe trovare 2 task");
        }

        @Test
        @DisplayName("TestFrame3: Ricerca per Supervisore con matricola inesistente -> Lista vuota")
        void testFindByUtente_SupervisoreMatricolaInesistente() throws Exception {
            // --- ARRANGE ---

            Supervisore supervisoreInesistente = new Supervisore();
            supervisoreInesistente.setMatricola(999);
            supervisoreInesistente.setRuolo(Tipi.ruolo.SUPERVISORE);

            // --- ACT ---
            List<Task> risultati = taskDAO.findByUtente(supervisoreInesistente);

            // --- ASSERT ---
            Assertions.assertNotNull(risultati, "La lista restituita non deve essere null");
            Assertions.assertTrue(risultati.isEmpty(), "La lista deve essere vuota se non ci sono task associate alla matricola 999");
        }

        @Test
        @DisplayName("TestFrame4: Ricerca per Dipendente con matricola inesistente -> Lista vuota")
        void testFindByUtente_DipendenteMatricolaInesistente() throws Exception {
            // --- ARRANGE ---

            Dipendente dipendenteInesistente = new Dipendente();
            dipendenteInesistente.setMatricola(999);
            dipendenteInesistente.setRuolo(Tipi.ruolo.DIPENDENTE);

            // --- ACT ---
            List<Task> risultati = taskDAO.findByUtente(dipendenteInesistente);

            // --- ASSERT ---
            Assertions.assertNotNull(risultati, "Il metodo non deve restituire null");
            Assertions.assertTrue(risultati.isEmpty(),
                    "La lista deve essere vuota se non ci sono task assegnate alla matricola 999");
        }

        @Test
        @DisplayName("TestFrame5: Utente con ruolo GESTORE -> IllegalArgumentException")
        void testFindByUtente_RuoloGestoreInvalido() {
            // --- ARRANGE ---
            // Creiamo un utente con ruolo GESTORE per testare la pre-condizione.
            // La specifica ammette solo DIPENDENTE o SUPERVISORE.
            Utente gestore = new Utente();
            gestore.setMatricola(matricolaDip1);
            gestore.setRuolo(Tipi.ruolo.GESTORE);

            // --- ACT & ASSERT ---
            // L'oracolo prevede il lancio di IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.findByUtente(gestore);
            }, "Dovrebbe lanciare IllegalArgumentException poiché il GESTORE non può avere task assegnate o supervisionate");

            // --- ASSERT EXTRA (Database Integrity) ---

            String sqlCheck = "SELECT COUNT(*) FROM ewmsDB.Task";
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCheck)) {

                if (rs.next()) {
                    Assertions.assertEquals(2, rs.getInt(1),
                            "Il numero di task nel database non deve cambiare");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore durante la verifica del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame6: Matricola dipendente non valida (0) -> IllegalArgumentException")
        void testFindByUtente_MatricolaInvalida() {
            // --- ARRANGE ---
            // Utilizziamo l'oggetto dipendente1 creato nel setup
            // Violiamo la pre-condizione utente.matricola > 0 impostandola a 0
            dipendente1.setMatricola(0);

            // --- ACT & ASSERT ---
            // L'oracolo prevede il lancio di IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.findByUtente(dipendente1);
            }, "Dovrebbe lanciare IllegalArgumentException se la matricola dell'utente è <= 0");


        }

        @Test
        @DisplayName("TestFrame7: Utente null -> IllegalArgumentException")
        void testFindByUtente_UtenteNull() {
            // --- ARRANGE ---
            // --- ACT & ASSERT ---
            // L'oracolo prevede il lancio di IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.findByUtente(null);
            }, "Dovrebbe lanciare IllegalArgumentException se l'oggetto utente è null");


        }


        }

    @Nested
    @DisplayName("Classe per il test del metodo updateStatus")
    class Test_updateStatus{

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
        void setup() throws Exception {
            sup = new Supervisore();
            sup.setNome("Mario");
            sup.setCognome("Rossi");
            sup.setEmail("mario.rossi@azienda.it");
            sup.setDataNasc(Date.valueOf("1985-05-20"));
            sup.setRuolo(Tipi.ruolo.SUPERVISORE);

            dipendente1 = new Dipendente();
            dipendente1.setNome("Mario");
            dipendente1.setCognome("Rossi");
            dipendente1.setEmail("luigi.rossi@azienda.it");
            dipendente1.setDataNasc(Date.valueOf("1985-05-20"));
            dipendente1.setRuolo(Tipi.ruolo.DIPENDENTE);

            try {
                utenteDAO.createSupervisore(sup, "goodpwd12!");
                matricolaSup = -1;
                try (Connection conn = DataSourceFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                    stmt.setString(1, sup.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) matricolaSup = rs.getInt("matricola");
                    }
                }
                sup.setMatricola(matricolaSup);
                dipendente1.setSupervisoreInfo(new Informazioni(matricolaSup, sup.getNome(), sup.getCognome()));

                utenteDAO.createDipendente(dipendente1, "goodpwd12.2");

                try (Connection conn = DataSourceFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                    stmt.setString(1, dipendente1.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) matricolaDip1 = rs.getInt("matricola");
                    }
                }

                dipendente1.setMatricola(matricolaDip1);

            } catch (SQLException | EmailGiaPresenteException e) {
                throw new RuntimeException(e);
            }

            task = new Task();
            task.setTitolo("Task #1");
            task.setIstruzioni("niente di che");
            // Conversione delle date dalla specifica (formato YYYY-MM-DD per Date.valueOf)
            task.setDataCreazione(Date.valueOf("2026-02-02"));
            task.setDataDiScadenza(Date.valueOf("2026-02-03"));

            // Assumiamo l'utilizzo della classe Tipi per Enums come visto nel setup dei ruoli
            task.setPriorita(Tipi.priorita.BASSA);
            task.setStato(Tipi.stato.IN_ESECUZIONE);

            // Utilizziamo le matricole create nel setup per soddisfare i vincoli di integrità
            task.setSupervisore(matricolaSup);
            task.setDipendente(matricolaDip1);
            task.setAllegato(null);


            taskDAO.create(task);
            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT id FROM ewmsDB.task WHERE dipendente = ?")) {
                stmt.setInt(1, dipendente1.getMatricola());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) task.setId(rs.getInt("id"));


                }
            } catch (Exception e) {throw new RuntimeException(e);}

            System.out.println("Setup: " + task.getId());
        }

        @Test
        @DisplayName("TestFrame1: Aggiornamento stato -> Stato modificato correttamente")
        void testUpdateStatus_Successo() throws Exception {
            // --- ARRANGE ---
            // L'id è valido (> 0) e la task è presente nel database grazie al setup
            long idTask = task.getId();
            Tipi.stato nuovoStato = Tipi.stato.COMPLETATO;

            // --- ACT ---
            taskDAO.updateStatus(idTask, nuovoStato);

            // --- ASSERT ---
            // Verifichiamo la post-condizione: t.stato = nuovoStato nel database
            String sqlCheck = "SELECT stato FROM ewmsDB.task WHERE id = ?";

            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {

                stmt.setLong(1, idTask);
                try (ResultSet rs = stmt.executeQuery()) {
                    Assertions.assertTrue(rs.next(), "La task con ID " + idTask + " deve esistere nel database");

                    String statoDb = rs.getString("stato");
                    Assertions.assertEquals(nuovoStato.toString(), statoDb,
                            "Lo stato nel database dovrebbe essere " + nuovoStato + " ma è stato trovato " + statoDb);
                }
            } catch (SQLException e) {
                Assertions.fail("Errore SQL durante la verifica della post-condizione: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame2: ID non valido (<= 0) -> IllegalArgumentException")
        void testUpdateStatus_IdInvalido() {
            // --- ARRANGE ---
            // Violazione della pre-condizione id > 0
            long idInvalido = 0;
            Tipi.stato nuovoStato = Tipi.stato.COMPLETATO;

            // --- ACT & ASSERT ---
            // Oracolo: IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.updateStatus(idInvalido, nuovoStato);
            }, "Dovrebbe lanciare IllegalArgumentException se l'ID è <= 0");

            // --- ASSERT EXTRA (Database Integrity) ---
            // Verifichiamo che la task creata nel setup non sia stata alterata
            String sqlCheck = "SELECT stato FROM ewmsDB.task WHERE id = ?";
            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {

                stmt.setLong(1, task.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Assertions.assertEquals(Tipi.stato.IN_ESECUZIONE.toString(), rs.getString("stato"),
                                "Lo stato della task originale non deve essere cambiato");
                    }
                }
            } catch (SQLException e) {
                Assertions.fail("Errore durante la verifica dell'integrità del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame3: Nuovo Stato null -> IllegalArgumentException")
        void testUpdateStatus_StatoNull() {
            // --- ARRANGE ---
            // Id valido e presente (recuperato dal setup)
            long idValido = task.getId();

            // VIOLAZIONE: nuovoStato <> null
            Tipi.stato nuovoStatoInvalido = null;

            // --- ACT & ASSERT ---
            // Oracolo: IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.updateStatus(idValido, nuovoStatoInvalido);
            }, "Dovrebbe lanciare IllegalArgumentException se il nuovo stato è null");

            // --- ASSERT EXTRA (Database Integrity) ---
            // Verifichiamo che lo stato sul database sia rimasto IN_ESECUZIONE (come da setup)
            String sqlCheck = "SELECT stato FROM ewmsDB.task WHERE id = ?";
            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {

                stmt.setLong(1, idValido);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Assertions.assertEquals(Tipi.stato.IN_ESECUZIONE.toString(), rs.getString("stato"),
                                "Lo stato nel database non deve essere stato modificato");
                    }
                }
            } catch (SQLException e) {
                Assertions.fail("Errore durante la verifica dell'integrità del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TestFrame4: Task assente dal database -> SQLException")
        void testUpdateStatus_TaskNonPresente() {
            // --- ARRANGE ---
            // Id valido e presente (recuperato dal setup)
            long idNonValido = 999;

            // VIOLAZIONE: nuovoStato <> null
            Tipi.stato nuovoStatovalido = Tipi.stato.COMPLETATO;

            // --- ACT & ASSERT ---
            // Oracolo: IllegalArgumentException
            Assertions.assertThrows(SQLException.class, () -> {
                taskDAO.updateStatus(idNonValido, nuovoStatovalido);
            }, "Dovrebbe lanciare SQLException se l'id non è esistente");

        }

    }

    @Nested
    @DisplayName("Classe per il test del metodo delete")
    class Test_delete{

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
        void setup() throws Exception {
            sup = new Supervisore();
            sup.setNome("Mario");
            sup.setCognome("Rossi");
            sup.setEmail("mario.rossi@azienda.it");
            sup.setDataNasc(Date.valueOf("1985-05-20"));
            sup.setRuolo(Tipi.ruolo.SUPERVISORE);

            dipendente1 = new Dipendente();
            dipendente1.setNome("Mario");
            dipendente1.setCognome("Rossi");
            dipendente1.setEmail("luigi.rossi@azienda.it");
            dipendente1.setDataNasc(Date.valueOf("1985-05-20"));
            dipendente1.setRuolo(Tipi.ruolo.DIPENDENTE);

            try {
                utenteDAO.createSupervisore(sup, "goodpwd12!");
                matricolaSup = -1;
                try (Connection conn = DataSourceFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                    stmt.setString(1, sup.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) matricolaSup = rs.getInt("matricola");
                    }
                }
                sup.setMatricola(matricolaSup);
                dipendente1.setSupervisoreInfo(new Informazioni(matricolaSup, sup.getNome(), sup.getCognome()));

                utenteDAO.createDipendente(dipendente1, "goodpwd12.2");

                try (Connection conn = DataSourceFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                    stmt.setString(1, dipendente1.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) matricolaDip1 = rs.getInt("matricola");
                    }
                }

                dipendente1.setMatricola(matricolaDip1);

            } catch (SQLException | EmailGiaPresenteException e) {
                throw new RuntimeException(e);
            }

            task = new Task();
            task.setTitolo("Task #1");
            task.setIstruzioni("niente di che");
            // Conversione delle date dalla specifica (formato YYYY-MM-DD per Date.valueOf)
            task.setDataCreazione(Date.valueOf("2026-02-02"));
            task.setDataDiScadenza(Date.valueOf("2026-02-03"));

            // Assumiamo l'utilizzo della classe Tipi per Enums come visto nel setup dei ruoli
            task.setPriorita(Tipi.priorita.BASSA);
            task.setStato(Tipi.stato.IN_ESECUZIONE);

            // Utilizziamo le matricole create nel setup per soddisfare i vincoli di integrità
            task.setSupervisore(matricolaSup);
            task.setDipendente(matricolaDip1);
            Allegato allegato = new Allegato();
            allegato.setFilename("documento.pdf");
            allegato.setDirectoryFilename("nomenellaDirectory.pdf");
            allegato.setFilePath("/server/storage/uploads/2026/documento.pdf");
            allegato.setContentType("application/pdf");

            task.setAllegato(allegato);


            taskDAO.create(task);
            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT id FROM ewmsDB.task WHERE dipendente = ?")) {
                stmt.setInt(1, dipendente1.getMatricola());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) task.setId(rs.getInt("id"));


                }
            } catch (Exception e) {throw new RuntimeException(e);}

            System.out.println("Setup: " + task.getId());
        }


        @Test
        @DisplayName("TF: taskId > 0, Task presente con Allegato -> Successo (Delete Cascade)")
        void testDelete_TaskWithAttachment() throws Exception {

            long inputId = task.getId();


            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT count(*) FROM ewmsDB.allegato WHERE allegato.task_id = ?")) {
                stmt.setLong(1, inputId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Assertions.assertTrue(rs.getInt(1) > 0, "ERRORE SETUP: L'allegato dovrebbe essere presente nel DB prima dell'esecuzione.");
                    }
                }
            }

            // 2. ESECUZIONE
            taskDAO.delete(inputId);

            // 3. ORACOLO (Verifica Post-Condizione)
            try (Connection conn = DataSourceFactory.getConnection()) {

                // Check 1: Il Task deve essere stato eliminato
                try (PreparedStatement stmt = conn.prepareStatement("SELECT count(*) FROM ewmsDB.task WHERE id = ?")) {
                    stmt.setLong(1, inputId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Assertions.assertEquals(0, rs.getInt(1), "Fail: Il task è ancora presente nel DB.");
                        }
                    }
                }

                // Check 2: L'Allegato deve essere stato eliminato
                // NOTA: Verifica che il nome della colonna Foreign Key nella tabella 'allegato' sia 'task' (o 'task_id')
                try (PreparedStatement stmt = conn.prepareStatement("SELECT count(*) FROM ewmsDB.allegato WHERE allegato.task_id = ?")) {
                    stmt.setLong(1, inputId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Assertions.assertEquals(0, rs.getInt(1), "Fail: L'allegato è ancora presente nel DB (Delete Cascade fallita).");
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("TF: taskId = 999 (Inesistente) -> Lancia SQLException")
        void testDelete_NonExistentId() {
            // 1. SETUP
            // Definiamo un ID che siamo sicuri non esista nel DB (basandoci sul setup pulito)
            long nonExistentId = 999L;

            // 2. ESECUZIONE & ORACOLO
            // Verifichiamo che il metodo delete lanci una SQLException quando l'ID non viene trovato.
            // Utilizziamo la classe Assertions esplicitamente come richiesto.
            Assertions.assertThrows(SQLException.class, () -> {
                taskDAO.delete(nonExistentId);
            });
        }

        @Test
        @DisplayName("TF: taskId <= 0 (Invalido) -> Lancia IllegalArgumentException")
        void testDelete_InvalidId() {
            // 1. SETUP DATI DI INPUT
            // Definiamo un ID negativo che viola la pre-condizione
            long invalidId = 0L;

            // 2. ESECUZIONE & ORACOLO
            // Ci aspettiamo che il metodo controlli l'input e lanci l'eccezione specifica
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                taskDAO.delete(invalidId);
            });
        }

    }
}


