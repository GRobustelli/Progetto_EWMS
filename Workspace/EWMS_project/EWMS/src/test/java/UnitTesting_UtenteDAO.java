import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistence.PersistenceManagement.UtenteDAO;
import it.unisa.ewms.persistence.DataSourceFactory;
import it.unisa.ewms.persistence.eccezioni.EmailGiaPresenteException;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UnitTesting_UtenteDAO {
    private UtenteDAO utenteDAO = new UtenteDAO();

    @BeforeAll
    static void setupDatabase() {
        // Fondamentale: forza la Factory a usare la config di test con H2
        DataSourceFactory.setPropertiesFile("test.properties");
    }

    //Utilizzeremo createUtente per accedere al metodo private insertUtenteGenerico
    @Nested
    @DisplayName("UT_1: Test insertUtenteGenerico (via createUtente)")
    class Test_insertUtenteGenerico {

        private Utente baseUtente;

        @BeforeEach
        void clearData() throws Exception {
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                //Statements per ottenere uno schema pulito

                stmt.execute("SET SCHEMA PUBLIC");

                stmt.execute("DROP ALL OBJECTS");

                stmt.execute("DROP SCHEMA IF EXISTS ewmsDB CASCADE");

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
        }

        @Test
        @DisplayName("TF_1: Inserimento Valido - Utente non esistente")
        void testInserimentoUtenteGenerico_successo() throws Exception {
            utenteDAO.createUtente(baseUtente, "goodPWd12!");


            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM utente WHERE email = ?")) {

                ps.setString(1, baseUtente.getEmail());

                try (ResultSet rs = ps.executeQuery()) {

                    // Verifica che il record esista
                    assertTrue(rs.next(), "L'utente dovrebbe essere stato salvato nel database");

                    // Verifica che i dati siano corretti
                    assertEquals(baseUtente.getNome(), rs.getString("nome"));
                    assertEquals(baseUtente.getCognome(), rs.getString("cognome"));
                    assertEquals(baseUtente.getRuolo().toString(), rs.getString("ruolo"));
                    assertEquals("goodPWd12!", rs.getString("hashPassword"));
                    assertEquals(baseUtente.getDataNasc(), rs.getDate("dataDiNascita"));
                }
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                    rs.next();
                    assertEquals(1, rs.getInt(1), "Il database dovrebbe essere vuoto");
                } catch (Exception e) {
                    fail("Errore durante la verifica del DB: " + e.getMessage());
                }
            }
        }


        @Test
        @DisplayName("TF_2: Inserimento non valido email = null")
        void testCreateUtente_EmailNull_ShouldThrowException() {
            baseUtente.setEmail(null);

            // --- ACT & ASSERT ---
            // Verifichiamo che venga lanciata l'eccezione specifica
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createUtente(baseUtente, "goodPWd12!");
            }, "Dovrebbe lanciare eccezione perché l'email è null");

            //Query abortita

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                rs.next();
                assertEquals(0, rs.getInt(1), "Il database dovrebbe essere vuoto");
            } catch (Exception e) {
                fail("Errore durante la verifica del DB: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TF_3: Email troppo lunga (> 255 caratteri)")
        void testCreateUtente_EmailTooLong_ShouldThrowException() {
            // --- ARRANGE ---

            String emailTroppoLunga = "a".repeat(250) + "@email.com";

            baseUtente.setEmail(emailTroppoLunga);

            // --- ACT & ASSERT ---
            // Verifichiamo che il metodo rifiuti l'input violando la pre-condizione
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createUtente(baseUtente, "goodPWd12!");
            }, "Dovrebbe lanciare IllegalArgumentException se l'email supera i 250 caratteri");


            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                rs.next();
                assertEquals(0, rs.getInt(1), "Il database dovrebbe essere vuoto");
            } catch (Exception e) {
                fail("Errore durante la verifica del DB: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TF_4: Email già presente (Violazione Unique)")
        void testCreateUtente_EmailDuplicata_ShouldThrowEmailGiaPresenteException() throws Exception {
            // --- ARRANGE ---

            utenteDAO.createUtente(baseUtente, "goodPWd12!");

            Utente utenteDuplicato = new Utente();
            utenteDuplicato.setNome("Luigi");      // Nome diverso
            utenteDuplicato.setCognome("Verdi");   // Cognome diverso
            utenteDuplicato.setEmail(baseUtente.getEmail());
            utenteDuplicato.setDataNasc(Date.valueOf("1990-01-01"));
            utenteDuplicato.setRuolo(Tipi.ruolo.GESTORE);

            // --- ACT & ASSERT ---
            assertThrows(EmailGiaPresenteException.class, () -> {
                utenteDAO.createUtente(utenteDuplicato, "passwordValid1!");
            }, "Dovrebbe lanciare EmailGiaPresenteException se l'email esiste già nel DB");

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                rs.next();
                assertEquals(1, rs.getInt(1), "Il database dovrebbe avere solo 1 entry");
            } catch (Exception e) {
                fail("Errore durante la verifica del DB: " + e.getMessage());
            }

        }

        @Test
        @DisplayName("TF_5: Nome Null")
        void testCreateUtente_NomeNull_ShouldThrowException() {
            // --- ARRANGE ---

            baseUtente.setNome(null);

            // --- ACT & ASSERT ---
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createUtente(baseUtente, "goodPWd12!");
            }, "Dovrebbe lanciare IllegalArgumentException se il nome è null");

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                rs.next();
                assertEquals(0, rs.getInt(1), "Il database dovrebbe essere vuoto");
            } catch (Exception e) {
                fail("Errore durante la verifica del DB: " + e.getMessage());
            }
        }


        @Test
        @DisplayName("TF_6: Nome troppo lungo (> 50 caratteri)")
        void testCreateUtente_NomeTooLong_ShouldThrowException() {
            // --- ARRANGE ---

            String nomeTroppoLungo = "a".repeat(51);

            baseUtente.setNome(nomeTroppoLungo);
            // --- ACT & ASSERT ---
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createUtente(baseUtente, "goodPWd12!");
            }, "Dovrebbe lanciare IllegalArgumentException se il nome supera i 50 caratteri");
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                rs.next();
                assertEquals(0, rs.getInt(1), "Il database dovrebbe essere vuoto");
            } catch (Exception e) {
                fail("Errore durante la verifica del DB: " + e.getMessage());
            }

        }

        @Test
        @DisplayName("TF_7: Cognome Null")
        void testCreateUtente_CognomeNull_ShouldThrowException() {
            // --- ARRANGE ---
            baseUtente.setCognome(null);

            // --- ACT & ASSERT ---
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createUtente(baseUtente, "goodPWd12!");
            }, "Dovrebbe lanciare IllegalArgumentException se il cognome è null");
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                rs.next();
                assertEquals(0, rs.getInt(1), "Il database dovrebbe essere vuoto");
            } catch (Exception e) {
                fail("Errore durante la verifica del DB: " + e.getMessage());
            }

        }

        @Test
        @DisplayName("TF_8: Cognome troppo lungo (> 50 caratteri)")
        void testCreateUtente_CognomeTooLong_ShouldThrowException() {
            // --- ARRANGE ---
            String cognomeTroppoLungo = "a".repeat(51);

            baseUtente.setCognome(cognomeTroppoLungo);

            // --- ACT & ASSERT ---
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createUtente(baseUtente, "goodPWd12!");
            }, "Dovrebbe lanciare IllegalArgumentException se il cognome supera i 50 caratteri");
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                rs.next();
                assertEquals(0, rs.getInt(1), "Il database dovrebbe essere vuoto");
            } catch (Exception e) {
                fail("Errore durante la verifica del DB: " + e.getMessage());
            }

        }

        @Test
        @DisplayName("TF_9: Data di Nascita Null")
        void testCreateUtente_DataNascitaNull_ShouldThrowException() {
            // --- ARRANGE ---
            baseUtente.setDataNasc(null);

            String passwordValida = "goodPWd12!";

            // --- ACT & ASSERT ---
            assertThrows(IllegalArgumentException.class, () -> {
                // Passiamo la password come argomento, come richiesto
                utenteDAO.createUtente(baseUtente, passwordValida);
            }, "Dovrebbe lanciare IllegalArgumentException se la data di nascita è null");

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                rs.next();
                assertEquals(0, rs.getInt(1), "Il database dovrebbe essere vuoto");
            } catch (Exception e) {
                fail("Errore durante la verifica del DB: " + e.getMessage());
            }
        }


        @Test
        @DisplayName("TF_10: Password troppo lunga (> 255 caratteri)")
        void testCreateUtente_PasswordTooLong_ShouldThrowException() {
            // --- ARRANGE ---

            String passwordTroppoLunga = "P".repeat(256);

            // --- ACT & ASSERT ---
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createUtente(baseUtente, passwordTroppoLunga);
            }, "Dovrebbe lanciare IllegalArgumentException se la password supera i 255 caratteri");

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                rs.next();
                assertEquals(0, rs.getInt(1), "Il database dovrebbe essere vuoto");
            } catch (Exception e) {
                fail("Errore durante la verifica del DB: " + e.getMessage());
            }
        }


    }

    @Nested
    @DisplayName("UT_2: Test per il metodo createUtente")
    class CreateUtente {
        private Utente baseUtente;


        @BeforeEach
        void clearData() throws Exception {
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {


                stmt.execute("SET SCHEMA PUBLIC");


                stmt.execute("DROP ALL OBJECTS");

                stmt.execute("DROP SCHEMA IF EXISTS ewmsDB CASCADE");

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
        }

        @Test
        @DisplayName("TF_1: Utente Gestore con password valida -> Successo")
        void testCreateUtente_Successo() throws Exception {
            // --- ARRANGE ---
            String password = "GoodPwd12!";

            // --- ACT ---
            utenteDAO.createUtente(baseUtente, password);

            // --- ASSERT ---

            String checkQuery = "SELECT COUNT(*) FROM ewmsDB.Utente WHERE email = ? AND ruolo = 'GESTORE'";

            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(checkQuery)) {

                stmt.setString(1, baseUtente.getEmail());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        Assertions.assertEquals(1, count,
                                "Fallimento Post-condizione: L'utente Gestore dovrebbe essere presente nel database");
                    } else {
                        Assertions.fail("Errore nella verifica del database: Nessun risultato restituito.");
                    }
                }
            }
        }

        @Test
        @DisplayName("TF_2: Oggetto utente null -> IllegalArgumentException")
        void testCreateUtente_UtenteNull() {
            // --- ARRANGE ---
            Utente utenteNull = null;
            String password = "GoodPwd12!";

            // --- ACT & ASSERT ---

            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createUtente(utenteNull, password);
            }, "Dovrebbe lanciare IllegalArgumentException se l'oggetto utente è null");

            // --- ASSERT EXTRA (Database Integrity) ---

            String countQuery = "SELECT COUNT(*) FROM ewmsDB.Utente";

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countQuery)) {

                if (rs.next()) {
                    int count = rs.getInt(1);
                    Assertions.assertEquals(0, count,
                            "Il database non dovrebbe contenere record dopo un tentativo fallito di inserimento");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore SQL durante la verifica del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TF_3: Ruolo Dipendente (Non Gestore) -> IllegalArgumentException")
        void testCreateUtente_RuoloDipendente() {
            // --- ARRANGE ---

            baseUtente.setRuolo(Tipi.ruolo.DIPENDENTE);
            String password = "GoodPwd12!";

            // --- ACT & ASSERT ---

            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createUtente(baseUtente, password);
            }, "Dovrebbe lanciare IllegalArgumentException se il ruolo è diverso da GESTORE");


            String countQuery = "SELECT COUNT(*) FROM ewmsDB.Utente";

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countQuery)) {

                if (rs.next()) {
                    int count = rs.getInt(1);
                    Assertions.assertEquals(0, count,
                            "Il database deve rimanere vuoto se il ruolo non è valido (DIPENDENTE)");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore SQL durante la verifica del database: " + e.getMessage());
            }
        }


        @Test
        @DisplayName("TF_4: Ruolo null -> IllegalArgumentException")
        void testCreateUtente_RuoloNull() {
            // --- ARRANGE ---

            baseUtente.setRuolo(null);
            String password = "GoodPwd12!";

            // --- ACT & ASSERT ---

            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createUtente(baseUtente, password);
            }, "Dovrebbe lanciare IllegalArgumentException se il ruolo dell'utente è null");


            String countQuery = "SELECT COUNT(*) FROM ewmsDB.Utente";

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countQuery)) {

                if (rs.next()) {
                    int count = rs.getInt(1);
                    Assertions.assertEquals(0, count,
                            "Il database non dovrebbe contenere record dopo un tentativo fallito di inserimento");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore SQL durante la verifica del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TF_5: Password null -> IllegalArgumentException")
        void testCreateUtente_PasswordNull() {
            // --- ARRANGE ---

            String password = null;

            // --- ACT & ASSERT ---

            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createUtente(baseUtente, password);
            }, "Dovrebbe lanciare IllegalArgumentException se la password è null");


            String countQuery = "SELECT COUNT(*) FROM ewmsDB.Utente";

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countQuery)) {

                if (rs.next()) {
                    int count = rs.getInt(1);
                    Assertions.assertEquals(0, count,
                            "Il database non dovrebbe contenere record dopo un tentativo con password null");
                }
            } catch (SQLException e) {
                Assertions.fail("Errore SQL durante la verifica del database: " + e.getMessage());
            }
        }

    }

    @Nested
    @DisplayName("UT_4: Test per il metodo createSupervisore")
    class CreateSupervisore {
        private Supervisore baseSupervisore;

        @BeforeEach
        void clearData() throws Exception {
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {


                stmt.execute("SET SCHEMA PUBLIC");

                stmt.execute("DROP ALL OBJECTS");

                stmt.execute("DROP SCHEMA IF EXISTS ewmsDB CASCADE");

                stmt.execute("RUNSCRIPT FROM 'classpath:ewmsDB.sql'");
            }
        }

        @BeforeEach
        void setUp() {
            baseSupervisore = new Supervisore();
            baseSupervisore.setNome("Mario");
            baseSupervisore.setCognome("Rossi");
            baseSupervisore.setEmail("mario.rossi@azienda.it");
            baseSupervisore.setDataNasc(Date.valueOf("1985-05-20"));
            baseSupervisore.setRuolo(Tipi.ruolo.SUPERVISORE);
        }

        @Test
        @DisplayName("TF_1: Creazione Supervisore Valido -> Successo")
        void testCreateSupervisore_Successo() throws Exception {
            // --- ARRANGE ---
            // baseSupervisore è già inizializzato nel @BeforeEach con ruolo SUPERVISORE
            String password = "GoodPwd12!";

            // --- ACT ---
            // Invocazione del metodo da testare
            utenteDAO.createSupervisore(baseSupervisore, password);

            // --- ASSERT ---
            // Verifica Post-condizione: Utente.allIstances()->includes(supervisore)
            // AND Supervisore.allInstances()->includes(supervisore)

            try (Connection conn = DataSourceFactory.getConnection()) {

                // 1. Verifica inserimento nella tabella padre (UTENTE)
                String queryUtente = "SELECT COUNT(*) FROM ewmsDB.Utente WHERE email = ? AND ruolo = 'SUPERVISORE'";
                try (PreparedStatement stmt = conn.prepareStatement(queryUtente)) {
                    stmt.setString(1, baseSupervisore.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Assertions.assertEquals(1, rs.getInt(1),
                                    "Il supervisore deve essere presente nella tabella Utente");
                        } else {
                            Assertions.fail("Nessun record trovato in Utente");
                        }
                    }
                }

                String querySupervisore = "SELECT COUNT(*) FROM ewmsDB.Supervisore";
                try (PreparedStatement stmt = conn.prepareStatement(querySupervisore)) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Assertions.assertEquals(1, rs.getInt(1),
                                    "Il supervisore deve essere presente nella tabella Supervisore");
                        } else {
                            Assertions.fail("Nessun record trovato in Supervisore");
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("TF_2: Oggetto Supervisore null -> IllegalArgumentException")
        void testCreateSupervisore_OggettoNull() {
            // --- ARRANGE ---
            Supervisore supervisoreNull = null;
            String password = "GoodPwd12!";

            // --- ACT & ASSERT ---
            // Verifica che il metodo rifiuti l'oggetto null
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createSupervisore(supervisoreNull, password);
            }, "Dovrebbe lanciare IllegalArgumentException se l'oggetto supervisore è null");

            // --- ASSERT EXTRA (Database Integrity) ---
            // Verifica che il database sia rimasto pulito (nessun inserimento parziale o errato)
            String countUtente = "SELECT COUNT(*) FROM ewmsDB.Utente";
            String countSupervisore = "SELECT COUNT(*) FROM ewmsDB.Supervisore";

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                // Controllo tabella Utente
                try (ResultSet rs = stmt.executeQuery(countUtente)) {
                    if (rs.next()) {
                        Assertions.assertEquals(0, rs.getInt(1),
                                "La tabella Utente deve rimanere vuota");
                    }
                }

                // Controllo tabella Supervisore
                try (ResultSet rs = stmt.executeQuery(countSupervisore)) {
                    if (rs.next()) {
                        Assertions.assertEquals(0, rs.getInt(1),
                                "La tabella Supervisore deve rimanere vuota");
                    }
                }

            } catch (SQLException e) {
                Assertions.fail("Errore SQL durante la verifica del database: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("TF_3: Ruolo Gestore (Non Supervisore) -> IllegalArgumentException")
        void testCreateSupervisore_RuoloErrato() {
            // --- ARRANGE ---
            // Modifichiamo il ruolo dell'oggetto base in GESTORE.
            // La pre-condizione richiede specificamente Tipi.ruolo.SUPERVISORE.
            baseSupervisore.setRuolo(Tipi.ruolo.GESTORE);
            String password = "GoodPwd12!";

            // --- ACT & ASSERT ---
            // Verifica che venga lanciata l'eccezione a causa del ruolo errato
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createSupervisore(baseSupervisore, password);
            }, "Dovrebbe lanciare IllegalArgumentException se il ruolo non è SUPERVISORE");

            // --- ASSERT EXTRA (Database Integrity) ---
            // Verifica che nessuna tabella sia stata modificata
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 1. Verifica Tabella Utente
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Utente")) {
                    if (rs.next()) {
                        Assertions.assertEquals(0, rs.getInt(1),
                                "La tabella Utente deve rimanere vuota");
                    }
                }

                // 2. Verifica Tabella Supervisore
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Supervisore")) {
                    if (rs.next()) {
                        Assertions.assertEquals(0, rs.getInt(1),
                                "La tabella Supervisore deve rimanere vuota");
                    }
                }

            } catch (SQLException e) {
                Assertions.fail("Errore SQL durante la verifica del database: " + e.getMessage());
            }
        }


        @Test
        @DisplayName("TF_4: Password null -> IllegalArgumentException")
        void testCreateSupervisore_PasswordNull() {
            // --- ARRANGE ---
            // Assicuriamoci che l'oggetto base sia valido (Ruolo SUPERVISORE)
            // per isolare il test sulla password.
            baseSupervisore.setRuolo(Tipi.ruolo.SUPERVISORE);
            String password = null;

            // --- ACT & ASSERT ---
            // Verifica che venga lanciata IllegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createSupervisore(baseSupervisore, password);
            }, "Dovrebbe lanciare IllegalArgumentException se la password è null");

            // --- ASSERT EXTRA (Database Integrity) ---
            // Verifica che il database sia rimasto intatto
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                // Verifica che non ci siano record in Utente
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Utente")) {
                    if (rs.next()) {
                        Assertions.assertEquals(0, rs.getInt(1),
                                "La tabella Utente deve rimanere vuota");
                    }
                }

                // Verifica che non ci siano record in Supervisore
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Supervisore")) {
                    if (rs.next()) {
                        Assertions.assertEquals(0, rs.getInt(1),
                                "La tabella Supervisore deve rimanere vuota");
                    }
                }

            } catch (SQLException e) {
                Assertions.fail("Errore SQL durante la verifica del database: " + e.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("UT_3: Test per il metodo createDipendente")
    class CreateDipendente {

        //Serve per testare la FK di dipendente
        private Supervisore baseSupervisore;
        private Dipendente baseDipendente;

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
            baseSupervisore = new Supervisore();
            baseSupervisore.setNome("Mario");
            baseSupervisore.setCognome("Rossi");
            baseSupervisore.setEmail("mario.rossi@azienda.it");
            baseSupervisore.setDataNasc(Date.valueOf("1985-05-20"));
            baseSupervisore.setRuolo(Tipi.ruolo.SUPERVISORE);

            baseDipendente = new Dipendente();
            baseDipendente.setNome("Luigi");
            baseDipendente.setCognome("Rossi");
            baseDipendente.setEmail("luigi.rossi@azienda.it");
            baseDipendente.setDataNasc(Date.valueOf("1985-05-20"));
            baseDipendente.setRuolo(Tipi.ruolo.DIPENDENTE);

        }

        @Test
        @DisplayName("TF_1: Creazione Dipendente Valido -> Successo")
        void testCreateDipendente_Successo() throws Exception {
            // --- ARRANGE ---
            String passwordDipendente = "GoodPwd12!";
            String passwordSupervisore = "SupPass1!";

            // 1. Inseriamo PRIMA il supervisore nel DB (Prerequisito fondamentale)
            utenteDAO.createSupervisore(baseSupervisore, passwordSupervisore);

            // 2. Recuperiamo la matricola generata/assegnata al supervisore
            // (Necessario perché non sappiamo che ID abbia assegnato il DB/DAO)
            int matricolaSupervisore = -1;
            String sqlGetId = "SELECT matricola FROM ewmsDB.Utente WHERE email = ?";

            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sqlGetId)) {

                stmt.setString(1, baseSupervisore.getEmail());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        matricolaSupervisore = rs.getInt("matricola");
                    } else {
                        Assertions.fail("Impossibile recuperare la matricola del supervisore appena inserito.");
                    }
                }
            }

            // 3. Configuriamo il Dipendente collegandolo al Supervisore esistente
            Informazioni infoSup = new Informazioni(
                    matricolaSupervisore,
                    baseSupervisore.getNome(),
                    baseSupervisore.getCognome()
            );
            baseDipendente.setSupervisoreInfo(infoSup);

            // --- ACT ---
            // Ora possiamo chiamare il metodo da testare
            utenteDAO.createDipendente(baseDipendente, passwordDipendente);

            // --- ASSERT ---
            // Verifica Post-condizione: Presenza in entrambe le tabelle
            try (Connection conn = DataSourceFactory.getConnection()) {

                // Verifica Tabella Utente
                String checkUtente = "SELECT COUNT(*) FROM ewmsDB.Utente WHERE email = ? AND ruolo = 'DIPENDENTE'";
                try (PreparedStatement stmt = conn.prepareStatement(checkUtente)) {
                    stmt.setString(1, baseDipendente.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Assertions.assertEquals(1, rs.getInt(1), "Il dipendente deve esistere nella tabella Utente");
                        }
                    }
                }
                String checkDipendente = "SELECT COUNT(*) FROM ewmsDB.Dipendente";
                try (PreparedStatement stmt = conn.prepareStatement(checkUtente)) {
                    stmt.setString(1, baseDipendente.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Assertions.assertEquals(1, rs.getInt(1), "Il dipendente deve esistere nella tabella Dipendente");
                        }
                    }
                }

            }
        }

        @Test
        @DisplayName("TF_2: Oggetto Dipendente null -> IllegalArgumentException")
        void testCreateDipendente_OggettoNull() throws Exception {
            // --- ARRANGE ---
            String passwordDipendente = "GoodPwd12!";

            // 1. Soddisfiamo la condizione "Supervisore(stato DB) = esiste"
            // Anche se passiamo null, prepariamo l'ambiente come se fosse un inserimento valido
            utenteDAO.createSupervisore(baseSupervisore, "SupPass1!");

            Dipendente dipendenteNull = null;

            // --- ACT & ASSERT ---
            // Verifica che il metodo rifiuti l'oggetto null
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createDipendente(dipendenteNull, passwordDipendente);
            }, "Dovrebbe lanciare IllegalArgumentException se l'oggetto dipendente è null");

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 1. Controlliamo la tabella Dipendente: deve essere vuota
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Dipendente")) {
                    if (rs.next()) {
                        Assertions.assertEquals(0, rs.getInt(1),
                                "La tabella Dipendente deve rimanere vuota");
                    }
                }

                // 2. Controlliamo la tabella Utente: deve esserci SOLO il supervisore (count = 1)
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Utente")) {
                    if (rs.next()) {
                        Assertions.assertEquals(1, rs.getInt(1),
                                "La tabella Utente deve contenere solo il supervisore precedentemente inserito");
                    }
                }

            }
        }

        @Test
        @DisplayName("TF_3: Ruolo Gestore (invece di Dipendente) -> IllegalArgumentException")
        void testCreateDipendente_RuoloErrato() throws Exception {
            String passwordDipendente = "GoodPwd12!";

            String passwordSupervisore = "SupPass1!";

            // 1. Soddisfiamo la condizione "Supervisore(stato DB) = esiste"
            // Inseriamo un supervisore valido per isolare l'errore sulla password
            utenteDAO.createSupervisore(baseSupervisore, passwordSupervisore);

            // 2. Recuperiamo la matricola generata dal DB
            int matricolaSupervisore = -1;
            String sqlGetId = "SELECT matricola FROM ewmsDB.utente WHERE email = ?";
            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sqlGetId)) {
                stmt.setString(1, baseSupervisore.getEmail());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) matricolaSupervisore = rs.getInt("matricola");
                }
            }

            // 3. Configuriamo il dipendente affinché sia valido (tranne la password che passeremo nel metodo)
            // "dipendente.SupInfo = istanziato" e "Matricola > 0"
            Informazioni infoSup = new Informazioni(
                    matricolaSupervisore,
                    baseSupervisore.getNome(),
                    baseSupervisore.getCognome()
            );
            baseDipendente.setSupervisoreInfo(infoSup);


            baseDipendente.setRuolo(Tipi.ruolo.GESTORE);
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createDipendente(baseDipendente, passwordDipendente);
            }, "Dovrebbe lanciare IllegalArgumentException se l'oggetto dipendente ha ruolo diverso da dipendente");
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 1. Controlliamo la tabella Dipendente: deve essere vuota
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Dipendente")) {
                    if (rs.next()) {
                        Assertions.assertEquals(0, rs.getInt(1),
                                "La tabella Dipendente deve rimanere vuota");
                    }
                }

                // 2. Controlliamo la tabella Utente: deve esserci SOLO il supervisore (count = 1)
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Utente")) {
                    if (rs.next()) {
                        Assertions.assertEquals(1, rs.getInt(1),
                                "La tabella ha solo supervisore come entry");
                    }
                }

            }

        }

        @Test
        @DisplayName("TF_4: Password null -> IllegalArgumentException")
        void testCreateDipendente_PasswordNull() throws SQLException, EmailGiaPresenteException {
            // --- ARRANGE ---
            String passwordSupervisore = "SupPass1!";

            // 1. Soddisfiamo la condizione "Supervisore(stato DB) = esiste"
            // Inseriamo un supervisore valido per isolare l'errore sulla password
            utenteDAO.createSupervisore(baseSupervisore, passwordSupervisore);

            // 2. Recuperiamo la matricola generata dal DB
            int matricolaSupervisore = -1;
            String sqlGetId = "SELECT matricola FROM ewmsDB.utente WHERE email = ?";
            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sqlGetId)) {
                stmt.setString(1, baseSupervisore.getEmail());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) matricolaSupervisore = rs.getInt("matricola");
                }
            }

            // 3. Configuriamo il dipendente affinché sia valido (tranne la password che passeremo nel metodo)
            // "dipendente.SupInfo = istanziato" e "Matricola > 0"
            Informazioni infoSup = new Informazioni(
                    matricolaSupervisore,
                    baseSupervisore.getNome(),
                    baseSupervisore.getCognome()
            );
            baseDipendente.setSupervisoreInfo(infoSup);

            // "Ruolo inserito = dipendente" (già settato nel @BeforeEach, ma confermiamo)
            baseDipendente.setRuolo(Tipi.ruolo.DIPENDENTE);

            // "Password = null"
            String passwordDipendente = null;

            // --- ACT & ASSERT ---
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createDipendente(baseDipendente, passwordDipendente);
            }, "Dovrebbe lanciare IllegalArgumentException se la password è null");

            // --- ASSERT EXTRA (Database Integrity) ---
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 1. La tabella Dipendente deve rimanere VUOTA
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Dipendente")) {
                    if (rs.next()) {
                        Assertions.assertEquals(0, rs.getInt(1),
                                "Non deve essere creato nessun record in Dipendente se la password è null");
                    }
                }

                // 2. La tabella Utente deve contenere SOLO il supervisore (Count = 1)
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Utente")) {
                    if (rs.next()) {
                        Assertions.assertEquals(1, rs.getInt(1),
                                "La tabella Utente deve contenere solo il supervisore esistente");
                    }
                }
            }
        }

        @Test
        @DisplayName("TF_5: SupervisoreInfoNull -> IllegalArgumentException")
        void testCreateDipendente_SupervisoreInfoNull() throws SQLException, EmailGiaPresenteException {
            // --- ARRANGE ---
            String passwordDipendente = "GoodPwd12!";

            baseDipendente.setSupervisoreInfo(null);

            // --- ACT & ASSERT ---
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createDipendente(baseDipendente, passwordDipendente);
            }, "Dovrebbe lanciare IllegalArgumentException se supervisore info è null");

        }

        @Test
        @DisplayName("TF_6: Matricola Supervisore <= 0 -> IllegalArgumentException")
        void testCreateDipendente_MatricolaSupervisoreInvalida() throws SQLException, EmailGiaPresenteException {
            // --- ARRANGE ---
            String passwordDipendente = "GoodPwd12!";
            String passwordSupervisore = "SupPass1!";

            // 1. Soddisfiamo la condizione "Supervisore(stato DB) = esiste"
            // Inseriamo un supervisore valido nel DB.
            utenteDAO.createSupervisore(baseSupervisore, passwordSupervisore);

            // 2. Configuriamo il dipendente con SupInfo istanziato, MA con Matricola <= 0.
            // Non ci interessa recuperare la matricola vera dal DB, perché vogliamo testare
            // che il metodo rifiuti il valore "0" a prescindere.
            Informazioni infoSupInvalida = new Informazioni(
                    0, // MATRICOLA INVALIDA (Violazione pre-condizione > 0)
                    baseSupervisore.getNome(),
                    baseSupervisore.getCognome()
            );
            baseDipendente.setSupervisoreInfo(infoSupInvalida);

            // --- ACT & ASSERT ---
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.createDipendente(baseDipendente, passwordDipendente);
            }, "Dovrebbe lanciare IllegalArgumentException se la matricola del supervisore è <= 0");

            // --- ASSERT EXTRA (Database Integrity) ---
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 1. La tabella Dipendente deve rimanere VUOTA
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Dipendente")) {
                    if (rs.next()) {
                        Assertions.assertEquals(0, rs.getInt(1),
                                "Nessun dipendente deve essere creato se la matricola supervisore è invalida");
                    }
                }

                // 2. La tabella Utente deve contenere SOLO il supervisore (Count = 1)
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Utente")) {
                    if (rs.next()) {
                        Assertions.assertEquals(1, rs.getInt(1),
                                "La tabella Utente deve contenere solo il supervisore esistente");
                    }
                }
            }
        }

        @Test
        @DisplayName("TF_7: Supervisore Non Esistente nel DB -> SQLException")
        void testCreateDipendente_SupervisoreNonEsistente() {
            // --- ARRANGE ---
            String passwordDipendente = "GoodPwd12!";

            // 1. NON inseriamo nessun supervisore nel DB.
            // Il DB è vuoto o comunque non contiene la matricola che stiamo per usare.

            // 2. Configuriamo il dipendente con una matricola sicuramente inesistente
            int matricolaInesistente = 99999;
            Informazioni infoSupInesistente = new Informazioni(
                    matricolaInesistente,
                    "Fantasma",
                    "Inesistente"
            );
            baseDipendente.setSupervisoreInfo(infoSupInesistente);

            // --- ACT & ASSERT ---
            // Ci aspettiamo che il Database (o il driver JDBC) sollevi un'eccezione
            // per violazione del vincolo di chiave esterna (Foreign Key Constraint Violation).
            Assertions.assertThrows(SQLException.class, () -> {
                utenteDAO.createDipendente(baseDipendente, passwordDipendente);
            }, "Dovrebbe lanciare SQLException per violazione della Foreign Key (Supervisore non trovato)");

            // --- ASSERT EXTRA (Database Integrity & Transaction Atomicity) ---
            // È CRUCIALE verificare che l'operazione sia atomica.
            // Poiché l'inserimento avviene in due tabelle (Utente -> Dipendente),
            // se fallisce la seconda (Dipendente) per la FK, deve essere fatto il ROLLBACK anche della prima (Utente).

            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {

                // 1. La tabella Dipendente deve essere ovviamente vuota
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Dipendente")) {
                    if (rs.next()) {
                        Assertions.assertEquals(0, rs.getInt(1),
                                "Nessun dipendente deve essere creato se il supervisore non esiste");
                    }
                }

                // 2. IMPORTANTE: Anche la tabella Utente deve essere vuota (Rollback avvenuto)
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ewmsDB.Utente")) {
                    if (rs.next()) {
                        Assertions.assertEquals(0, rs.getInt(1),
                                "La tabella Utente deve essere vuota: l'inserimento parziale doveva essere annullato (Rollback)");
                    }
                }
            } catch (SQLException e) {
                Assertions.fail("Errore SQL durante la verifica del database: " + e.getMessage());
            }
        }


    }

    @Nested
    @DisplayName("UT_5: Classe per il test metodo findByMatricola")
    class FindByMatricola {
        private int matricola;
        private Utente baseUtente;

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
            utenteDAO.createUtente(baseUtente, "goodPsw12!");


            matricola = -1;
            String sqlGetId = "SELECT matricola FROM ewmsDB.Utente WHERE email = ?";

            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sqlGetId)) {

                stmt.setString(1, baseUtente.getEmail());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        matricola = rs.getInt("matricola");
                    } else {
                        Assertions.fail("Impossibile recuperare la matricola del supervisore appena inserito.");
                    }

                }

            }

        }

        @Test
        @DisplayName("TestFrame1: Matricola esistente (> 0) -> Restituisce l'oggetto Utente")
        void testFindByMatricola_UtenteEsistente() throws SQLException {

            // Il setup è già stato effettuato nel @BeforeEach.



            Utente risultato = utenteDAO.findByMatricola(this.matricola);

            Assertions.assertNotNull(risultato,
                    "Il metodo deve restituire un'istanza di Utente quando la matricola esiste nel DB");

            Assertions.assertEquals(this.matricola, risultato.getMatricola(),
                    "La matricola dell'oggetto restituito deve corrispondere a quella cercata");

            Assertions.assertEquals(baseUtente.getEmail(), risultato.getEmail(),
                    "L'email dell'utente restituito non corrisponde");

            Assertions.assertEquals(baseUtente.getNome(), risultato.getNome(),
                    "Il nome dell'utente restituito non corrisponde");

            Assertions.assertEquals(baseUtente.getCognome(), risultato.getCognome(),
                    "Il cognome dell'utente restituito non corrisponde");

            Assertions.assertEquals(baseUtente.getRuolo(), risultato.getRuolo(),
                    "Il ruolo dell'utente restituito non corrisponde");

            Assertions.assertEquals(baseUtente.getDataNasc(), risultato.getDataNasc(),
                    "Il ruolo dell'utente restituito non corrisponde");
        }

        @Test
        @DisplayName("TestFrame2: Matricola valida (> 0) ma non presente nel database -> Restituisce null")
        void testFindByMatricola_UtenteNonPresente() throws SQLException {
            // --- ARRANGE ---
            // Scegliamo una matricola che non esiste nel database.
            // Poiché il setup inserisce un solo utente, basterà usare un ID incrementato o un valore arbitrario alto.
            int matricolaInesistente = this.matricola + 100;

            // --- ACT ---
            // Invocazione del metodo per una matricola non censita
            Utente risultato = utenteDAO.findByMatricola(matricolaInesistente);

            // --- ASSERT ---
            // Post-condizione: result = null
            Assertions.assertNull(risultato,
                    "Il metodo deve restituire null se la matricola non corrisponde a nessun utente nel database");
        }

        @Test
        @DisplayName("TestFrame3: Matricola non valida (<= 0) -> IllegalArgumentException")
        void testFindByMatricola_MatricolaNonValida() {
            // --- ARRANGE ---
            // Testiamo il limite inferiore della pre-condizione (matricola > 0)
            int matricolaInvalida = 0;

            // --- ACT & ASSERT ---
            // Il metodo deve sollevare un'eccezione prima di interrogare il database
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.findByMatricola(matricolaInvalida);
            }, "Dovrebbe lanciare IllegalArgumentException per matricola minore o uguale a zero");
        }


    }

    @Nested
    @DisplayName("UT_6: Classe di test per metodo findByEmail")
    class FindByEmail{

        private Utente baseUtente;

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

            // Persistenza dell'utente nel database per il test
            utenteDAO.createUtente(baseUtente, "goodPsw12!");
        }


        @Test
        @DisplayName("TF_1: Email valida e presente nel database -> Successo")
        void testFindByEmail_Presente() {
            // --- ARRANGE ---
            // Email: size() > 0 e presente nel db
            String emailRicerca = "mario.rossi@azienda.it";

            // --- ACT ---
            Utente risultato = utenteDAO.findByEmail(emailRicerca);

            // --- ASSERT ---
            // Oracolo: restituisce l'utente
            Assertions.assertNotNull(risultato, "L'utente dovrebbe essere recuperato correttamente");

            Assertions.assertEquals(baseUtente.getEmail(), risultato.getEmail(),
                    "L'email dell'utente restituito deve coincidere con quella cercata");

            Assertions.assertEquals(baseUtente.getNome(), risultato.getNome(),
                    "Il nome dell'utente restituito deve corrispondere a quello nel database");

            Assertions.assertEquals(baseUtente.getCognome(), risultato.getCognome(),
                    "Il cognome dell'utente restituito deve corrispondere a quello nel database");
        }

        @Test
        @DisplayName("TestFrame2: Email valida ma NON presente nel database -> Restituisce null")
        void testFindByEmail_Assente() {
            // --- ARRANGE ---
            // Email: size() > 0 ma assente nel db (usiamo un'email diversa da quella del setup)
            String emailNonPresente = "fantasma@azienda.it";

            // --- ACT ---
            Utente risultato = utenteDAO.findByEmail(emailNonPresente);

            // --- ASSERT ---
            // Oracolo: restituisce null
            Assertions.assertNull(risultato,
                    "Il metodo deve restituire null se l'email cercata non esiste nel database");
        }

        @Test
        @DisplayName("TestFrame3: Email null -> IllegalArgumentException")
        void testFindByEmail_EmailNull() {
            // --- ARRANGE ---
            String emailNull = null;

            // --- ACT & ASSERT ---
            // Oracolo: illegalArgumentException
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.findByEmail(emailNull);
            }, "Dovrebbe lanciare IllegalArgumentException se l'email passata è null");
        }
    }

    @Nested
    @DisplayName("UT_7: Test per il metodo getAllDipendentiInfo")
    class GetAllDipendentiInfo {

        private int matricolaSupervisore;
        private Dipendente dipendente1;
        private Dipendente dipendente2;

        @BeforeEach
        void clearData() throws Exception {
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SET SCHEMA PUBLIC");
                stmt.execute("DROP ALL OBJECTS");
                stmt.execute("DROP SCHEMA IF EXISTS ewmsDB CASCADE");
                stmt.execute("RUNSCRIPT FROM 'classpath:ewmsDB.sql'");
            }
        }

        @BeforeEach
        void setUp() throws SQLException, EmailGiaPresenteException {
            // 1. Creiamo il SUPERVISORE
            Supervisore sup = new Supervisore();
            sup.setNome("Capo");
            sup.setCognome("Supremo");
            sup.setEmail("capo@azienda.it"); // Email univoca
            sup.setDataNasc(Date.valueOf("1980-01-01"));
            sup.setRuolo(Tipi.ruolo.SUPERVISORE);
            utenteDAO.createSupervisore(sup, "PassSup1!");

            // 2. Recuperiamo la sua MATRICOLA (generata dal DB)
            matricolaSupervisore = -1;
            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                stmt.setString(1, sup.getEmail());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) matricolaSupervisore = rs.getInt("matricola");
                }
            }

            // Prepariamo l'oggetto Informazioni per il collegamento
            Informazioni infoSup = new Informazioni(matricolaSupervisore, sup.getNome(), sup.getCognome());

            // 3. Creiamo il DIPENDENTE 1 collegato al supervisore
            dipendente1 = new Dipendente();
            dipendente1.setNome("Luigi");
            dipendente1.setCognome("Verdi");
            dipendente1.setEmail("luigi.verdi@azienda.it"); // Email univoca
            dipendente1.setDataNasc(Date.valueOf("1990-01-01"));
            dipendente1.setRuolo(Tipi.ruolo.DIPENDENTE);
            dipendente1.setSupervisoreInfo(infoSup);
            utenteDAO.createDipendente(dipendente1, "PassDip1!");

            // 4. Creiamo il DIPENDENTE 2 collegato allo STESSO supervisore
            dipendente2 = new Dipendente();
            dipendente2.setNome("Anna");
            dipendente2.setCognome("Bianchi");
            dipendente2.setEmail("anna.bianchi@azienda.it"); // Email univoca
            dipendente2.setDataNasc(Date.valueOf("1992-05-05"));
            dipendente2.setRuolo(Tipi.ruolo.DIPENDENTE);
            dipendente2.setSupervisoreInfo(infoSup);
            utenteDAO.createDipendente(dipendente2, "PassDip2!");
        }

        @Test
        @DisplayName("TF_1: Matricola valida e dipendenti presenti -> Restituisce lista popolata")
        void testGetAllDipendentiInfo_Successo() throws SQLException {
            // --- ARRANGE ---
            // Il setup ha già creato 2 dipendenti sotto 'matricolaSupervisore'

            // --- ACT ---
            List<Informazioni> risultati = utenteDAO.getAllDipendentiInfo(matricolaSupervisore);

            // --- ASSERT ---
            Assertions.assertNotNull(risultati, "La lista restituita non deve essere null");
            Assertions.assertEquals(2, risultati.size(), "Dovrebbero esserci esattamente 2 dipendenti associati");

            boolean trovatoLuigi = risultati.stream()
                    .anyMatch(info -> info.getNome().equals("Luigi") && info.getCognome().equals("Verdi"));

            boolean trovatoAnna = risultati.stream()
                    .anyMatch(info -> info.getNome().equals("Anna") && info.getCognome().equals("Bianchi"));

            Assertions.assertTrue(trovatoLuigi, "La lista deve contenere Luigi Verdi");
            Assertions.assertTrue(trovatoAnna, "La lista deve contenere Anna Bianchi");

        }

        @Test
        @DisplayName("TF_2: Matricola valida e dipendenti assenti -> Restituisce lista vuota")
        void testGetAllDipendentiInfo_Successo_MatricolaInesistenteoNessunDipAssociato() throws SQLException {
           // --- ARRANGE ---
            matricolaSupervisore = 999;
           //  --- ACT ---
            List<Informazioni> risultati = utenteDAO.getAllDipendentiInfo(matricolaSupervisore);

            // --- ASSERT ---
            Assertions.assertNotNull(risultati, "La lista restituita non deve essere null");
            Assertions.assertEquals(0, risultati.size(), "Dovrebbero esserci esattamente 0 dipendenti associati");
        }

        @Test
        @DisplayName("TF_3: Matricola non valida -> Restituisce IllegalArgumentException")
        void testGetAllDipendentiInfo_Successo_MatricolanonValida() throws SQLException {
            // --- ARRANGE ---

            matricolaSupervisore = -1;

            // --- ACT, ASSERT ---

            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.getAllDipendentiInfo(matricolaSupervisore);
            }, "Dovrebbe lanciare IllegalArgumentException se l'email passata è null");
        }
    }

    @Nested
    @DisplayName("UT_8: Test per metodo getSupervisoreInfo")
    class GetSupervisoreInfo{
        private Informazioni SupervisoreInfo;
        private Dipendente dipendente1;
        private int matricolaDipendente;

        @BeforeEach
        void clearData() throws Exception {
            try (Connection conn = DataSourceFactory.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SET SCHEMA PUBLIC");
                stmt.execute("DROP ALL OBJECTS");
                stmt.execute("DROP SCHEMA IF EXISTS ewmsDB CASCADE");
                stmt.execute("RUNSCRIPT FROM 'classpath:ewmsDB.sql'");
            }
        }

        @BeforeEach
        void setUp() throws SQLException, EmailGiaPresenteException {
            // 1. Creiamo il SUPERVISORE
            Supervisore sup = new Supervisore();
            sup.setNome("Capo");
            sup.setCognome("Supremo");
            sup.setEmail("capo@azienda.it"); // Email univoca
            sup.setDataNasc(Date.valueOf("1980-01-01"));
            sup.setRuolo(Tipi.ruolo.SUPERVISORE);
            utenteDAO.createSupervisore(sup, "PassSup1!");

            // 2. Recuperiamo la sua MATRICOLA (generata dal DB)
            int matricolaSupervisore = -1;
            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                stmt.setString(1, sup.getEmail());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) matricolaSupervisore = rs.getInt("matricola");
                }
            }

            // Prepariamo l'oggetto Informazioni per il collegamento
            SupervisoreInfo = new Informazioni(matricolaSupervisore, sup.getNome(), sup.getCognome());

            // 3. Creiamo il DIPENDENTE 1 collegato al supervisore
            dipendente1 = new Dipendente();
            dipendente1.setNome("Luigi");
            dipendente1.setCognome("Verdi");
            dipendente1.setEmail("luigi.verdi@azienda.it"); // Email univoca
            dipendente1.setDataNasc(Date.valueOf("1990-01-01"));
            dipendente1.setRuolo(Tipi.ruolo.DIPENDENTE);
            dipendente1.setSupervisoreInfo(SupervisoreInfo);
            utenteDAO.createDipendente(dipendente1, "PassDip1!");

            matricolaDipendente = -1;
            try (Connection conn = DataSourceFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                stmt.setString(1, dipendente1.getEmail());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) matricolaDipendente = rs.getInt("matricola");
                }
            }
        }

        @Test
        @DisplayName("TF_1: Matricola valida e dipendente presente -> Restituisce Informazioni supervisore")
        void testSupervisoreInfo_Successo() throws SQLException {
            // --- ARRANGE --- già fatto nel setup

            Informazioni infoFromDB = utenteDAO.getSupervisoreInfo(matricolaDipendente);

            assertNotNull(infoFromDB, "Informazioni non dovrebbe essere null");
            assertEquals(infoFromDB.getNome(), SupervisoreInfo.getNome(), "Il nome dovrebbe coincidere");
            assertEquals(infoFromDB.getCognome(), SupervisoreInfo.getCognome(), "Il cognome dovrebbe coincidere");
            assertEquals(infoFromDB.getMatricola(), SupervisoreInfo.getMatricola(), "La matricola coincidere");

        }

        @Test
        @DisplayName("TF_2: Matricola valida e dipendente non presente -> Restituisce null")
        void testSupervisoreInfo_Successo_DipedentenonPresente() throws SQLException {
            // --- ARRANGE ---
            matricolaDipendente = 999;

            // // --- ACT, ASSERT ---
            assertNull(utenteDAO.getSupervisoreInfo(matricolaDipendente), "Il metodo dovrebbe restituire valore null");

        }

        @Test
        @DisplayName("TF_3: Matricola non valida -> IllegalArgumentException")
        void testSupervisoreInfo(){
            // --- ARRANGE ---
            matricolaDipendente = -1;

            // --- ACT, ASSERT ---

            assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.getSupervisoreInfo(matricolaDipendente);
            }, "Il metodo dovrebbe bloccare l'esecuzione e lanciare l'eccezione");

        }


        }

    @Nested
    @DisplayName("UT_9: Test per metodo delete")
    class Delete{
            private Supervisore sup;
            private Dipendente dipendente1;
            private Dipendente dipendente2;
            private int matricolaDip1;
            private int matricolaDip2;
            private int matricolaSup;

            @BeforeEach
            void clearData() throws Exception {
                try (Connection conn = DataSourceFactory.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("SET SCHEMA PUBLIC");
                    stmt.execute("DROP ALL OBJECTS");
                    stmt.execute("DROP SCHEMA IF EXISTS ewmsDB CASCADE");
                    stmt.execute("RUNSCRIPT FROM 'classpath:ewmsDB.sql'");
                }
            }

            @BeforeEach
            void setUp() throws SQLException, EmailGiaPresenteException {
                // 1. Creiamo il SUPERVISORE
                sup = new Supervisore();
                sup.setNome("Capo");
                sup.setCognome("Supremo");
                sup.setEmail("capo.supremo@azienda.it"); // Email univoca
                sup.setDataNasc(Date.valueOf("1980-01-01"));
                sup.setRuolo(Tipi.ruolo.SUPERVISORE);
                utenteDAO.createSupervisore(sup, "PassSup1!");

                // 2. Recuperiamo la sua MATRICOLA (generata dal DB)
                matricolaSup = -1;
                try (Connection conn = DataSourceFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                    stmt.setString(1, sup.getEmail());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) matricolaSup = rs.getInt("matricola");
                    }
                }
                sup.setMatricola(matricolaSup);

                // Prepariamo l'oggetto Informazioni per il collegamento
                Informazioni infoSup = new Informazioni(matricolaSup, sup.getNome(), sup.getCognome());

                // 3. Creiamo il DIPENDENTE 1 collegato al supervisore
                dipendente1 = new Dipendente();
                dipendente1.setNome("Luigi");
                dipendente1.setCognome("Verdi");
                dipendente1.setEmail("luigi.verdi@azienda.it"); // Email univoca
                dipendente1.setDataNasc(Date.valueOf("1990-01-01"));
                dipendente1.setRuolo(Tipi.ruolo.DIPENDENTE);
                dipendente1.setSupervisoreInfo(infoSup);
                utenteDAO.createDipendente(dipendente1, "PassDip1!");

                matricolaDip1 = -1;
                try (Connection conn = DataSourceFactory.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT matricola FROM ewmsDB.utente WHERE email = ?")) {
                    stmt.setString(1, dipendente1.getEmail());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) matricolaDip1 = rs.getInt("matricola");
                    }
                }

                dipendente1.setMatricola(matricolaDip1);

            }

            @Test
            @DisplayName("TF_1: eliminazione utenti senza dipedenza")
            void test_delete_successo() throws SQLException {
                // --- ARRANGE ---

                // --- ACT ---

                utenteDAO.delete(dipendente1);

                //--- ASSERT ---
                try (Connection conn = DataSourceFactory.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                     rs.next();
                     assertEquals(1, rs.getInt(1), "Il database dovrebbe avere solo 1 entry in Utente");



                } catch (Exception e) {
                    fail("Errore durante la verifica del DB: " + e.getMessage());
                }

                try (Connection conn = DataSourceFactory.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM dipendente")) {
                    rs.next();
                    assertEquals(0, rs.getInt(1), "Il database dovrebbe avere 0 entry nella tabella dipendente");

                } catch (Exception e) {
                    fail("Errore durante la verifica del DB: " + e.getMessage());
                }
            }

            @Test
            @DisplayName("TF_2: eliminazione utenti fallimento --> IllegalArgumentException")
            void test_delete_Fallimento_throwsIllegalArgumentException() throws SQLException {
                // --- ARRANGE ---

                //--- ACT ---

                // --- ASSERT ---
                assertThrows(IllegalArgumentException.class, () -> {
                    utenteDAO.delete(null);
                }, "Il metodo dovrebbe bloccare tutto e lanciare IllegalArgumentException");

            }

            @Test
            @DisplayName("TF_3: matricola non valida -->IllegalArgumentException")
            void test_delete_MatricolaNonValida_throwsIllegalArgumentException() throws SQLException {
                dipendente1.setMatricola(-1);

                assertThrows(IllegalArgumentException.class, () -> {
                    utenteDAO.delete(dipendente1);
                }, "Il metodo dovrebbe bloccare tutto e lanciare IllegalArgumentException");

                try (Connection conn = DataSourceFactory.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                    rs.next();
                    assertEquals(2, rs.getInt(1), "Il database dovrebbe avere solo 2 entry in Utente");



                } catch (Exception e) {
                    fail("Errore durante la verifica del DB: " + e.getMessage());
                }

            }

            @Test
            @DisplayName("TF_4: supervisore con vincolo di dipendenti --> SQLIntegrityConstraintViolationException")
            void test_delete_SupervisoreConVincolo_throwsSQLIntegrityConstraintViolationException() throws SQLException {
                // --- ARRANGE ---
                //--- ACT, ASSERT ---
                assertThrows(SQLIntegrityConstraintViolationException.class, () -> {
                    utenteDAO.delete(sup);
                }, "Il metodo dovrebbe lanciare l'eccezione SQLIntegrityConstraintViolationException");

                try (Connection conn = DataSourceFactory.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utente")) {
                    rs.next();
                    assertEquals(2, rs.getInt(1), "Il database dovrebbe avere solo 2 entry in Utente");



                } catch (Exception e) {
                    fail("Errore durante la verifica del DB: " + e.getMessage());
                }



            }



        }

    @Nested
    @DisplayName("UT_10: Test per metodo recuperaPassword")
    class RecuperaPassword{

        private Utente baseUtente;

        // Eseguito prima di OGNI test dentro questa Nested Class
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

            try {
                utenteDAO.createUtente(baseUtente, "goodPwd12!");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (EmailGiaPresenteException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("TF_1: recupera password --> successo")
        void test_recuperPassword_successo() throws SQLException {
            //--- ARRANGE ---

            // --- ACT ---
            String passwordHash = utenteDAO.recuperaPassword(baseUtente.getEmail());


            // --- ASSERT ----
            assertNotNull(passwordHash, "La password recuperata non dovrebbe essere null");
            assertTrue(passwordHash.equals("goodPwd12!"), "Il metodo dovrebbe restituire true");
        }

        @Test
        @DisplayName("TF_1: recuper password --> successo - nessuna corrispondenza email")
        void test_recuperPassword_successo_returnNull() throws SQLException{
            //--- ARRANGE ---
                baseUtente.setEmail("different.email@azienda.it");

            // --- ACT ---

            String passwordHash = utenteDAO.recuperaPassword(baseUtente.getEmail());

            // --- ASSERT

            assertNull(passwordHash, "Il metodo dovrebbe restituire null");
        }

        @Test
        @DisplayName("TF_1: recuper password --> successo - nessuna corrispondenza email")
        void test_recuperPassword_fallimento_throwIllegalArgumentException() throws SQLException{
            //--- ARRANGE ---

            // --- ACT ---

            // --- ASSERT

            assertThrows(IllegalArgumentException.class, () -> {
                utenteDAO.recuperaPassword(null);
            }, "Il metodo dovrebbe lanciare una IllegalArgumentException");

        }
    }


}








