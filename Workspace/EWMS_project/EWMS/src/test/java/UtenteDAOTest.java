import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.ClassiDAO.UtenteDAO;
import it.unisa.ewms.persistance.DataSourceFactory;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests per UtenteDAO")
class UtenteDAOTest {

    private UtenteDAO utenteDao;
    private Connection connection;
    private PreparedStatement ps;


    @BeforeEach
    void setUp() {
        utenteDao = new UtenteDAO();
        connection = mock(Connection.class);
        ps = mock(PreparedStatement.class);
    }

    @Nested
    @DisplayName("Metodo: createUtente")
    class CreateUtenteTest {

        @Test
        void testCreateUtente_Successo() throws Exception {
            // 1. ARRANGE
            Utente utente = new Utente();
            utente.setMatricola("0512103361001");
            utente.setEmail("mario.rossi@test.it");
            utente.setNome("Mario");
            utente.setCognome("Rossi");
            utente.setDataNasc(Date.valueOf("1990-01-01"));
            utente.setRuolo(Tipi.ruolo.DIPENDENTE);

            String password = "passwordSegreta";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Intercettiamo la query SQL
                when(connection.prepareStatement(contains("INSERT INTO utente"))).thenReturn(ps);

                // Il tuo codice usa ps.execute() e stampa un messaggio se true
                when(ps.execute()).thenReturn(true);

                // 2. ACT
                utenteDao.createUtente(utente, password);

                // 3. ASSERT & VERIFY
                // Verifichiamo l'ordine esatto dei parametri definito nel metodo insertUtenteGenerico

                verify(ps).setString(1, "0512103361001"); // Matricola
                verify(ps).setString(2, "mario.rossi@test.it"); // Email
                verify(ps).setString(3, "Mario"); // Nome
                verify(ps).setString(4, "Rossi"); // Cognome
                verify(ps).setDate(5, utente.getDataNasc()); // Data Nascita
                verify(ps).setString(6, "passwordSegreta"); // Password
                verify(ps).setString(7, "DIPENDENTE"); // Ruolo (toString)

                verify(ps).execute();
                verify(ps).close();
                verify(connection).close();
            }
        }

        @Test
        void testCreateUtente_InputNull() {
            // Caso 1: Utente Null
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.createUtente(null, "pass");
            });

            // Caso 2: Password Null
            Utente u = new Utente();
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.createUtente(u, null);
            });

            // Caso 3: Password Vuota
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.createUtente(u, "");
            });
        }

        @Test
        void testCreateUtente_EmailDuplicata() throws Exception {
            // 1. ARRANGE
            Utente utente = new Utente();
            utente.setEmail("esistente@test.it");
            utente.setMatricola("12345");
            utente.setRuolo(Tipi.ruolo.DIPENDENTE);

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // SIMULAZIONE DEL COMPORTAMENTO SPECIFICO

                SQLIntegrityConstraintViolationException sqlException =
                        new SQLIntegrityConstraintViolationException("Duplicate entry 'esistente@test.it' for key 'email'");

                when(ps.execute()).thenThrow(sqlException);

                // 2. ACT & ASSERT
                EmailGiaPresenteException ex = assertThrows(EmailGiaPresenteException.class, () -> {
                    utenteDao.createUtente(utente, "pass");
                });

                assertEquals("L'email inserita è già presente nel db", ex.getMessage());

                verify(ps).close();
                verify(connection).close();
            }
        }

        @Test
        @DisplayName("Dovrebbe lanciare SQLException wrappata per altri errori SQL")
        void testCreateUtente_ErroreGenericoSQL() throws Exception {
            // 1. ARRANGE
            Utente utente = new Utente();
            utente.setEmail("test@test.it");
            utente.setMatricola("123");
            utente.setRuolo(Tipi.ruolo.DIPENDENTE);

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // Simuliamo un errore generico (es. connessione caduta, tabella non trovata)
                doThrow(new SQLException("Errore di connessione")).when(ps).execute();

                // 2. ACT & ASSERT
                SQLException ex = assertThrows(SQLException.class, () -> {
                    utenteDao.createUtente(utente, "pass");
                });

                // Verifica che il messaggio sia quello del catch esterno wrapper
                assertEquals("Inserimento utente Generico non riuscito", ex.getMessage());
                // Verifica che la causa originale sia preservata
                assertNotNull(ex.getCause());
                assertTrue(ex.getCause().getMessage().contains("Errore di connessione"));
            }
        }


    @Test
    void testCreateUtente_PasswordNull() {
        // 1. ARRANGE
        Utente utente = new Utente();
        // Non serve settare i campi dell'utente perché il controllo fallisce prima

        // 2. ACT & ASSERT
        // Non serve mockare il DB perché l'eccezione viene lanciata prima del try-with-resources
        assertThrows(IllegalArgumentException.class, () -> {
            utenteDao.createUtente(utente, null);
        });
    }

    @Test
    @DisplayName("Dovrebbe lanciare IllegalArgumentException se la password è VUOTA")
    void testCreateUtente_PasswordVuota() {
        // 1. ARRANGE
        Utente utente = new Utente();

        // 2. ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> {
            utenteDao.createUtente(utente, "");
        });
    }


    }

    @Nested
    @DisplayName("Metodo: createDipendente")
    class CreateDipendenteTest {

        private PreparedStatement psUtente;
        private PreparedStatement psDipendente;

        @BeforeEach
        void initMocks() {
            // Inizializziamo i mock specifici per questo gruppo di test
            psUtente = mock(PreparedStatement.class);
            psDipendente = mock(PreparedStatement.class);
        }

        @Test
        void testCreateDipendente_Successo() throws Exception {
            // 1. ARRANGE
            Dipendente dipendente = new Dipendente();
            dipendente.setMatricola("DIP0000000001");
            dipendente.setEmail("dipendente@test.it");
            dipendente.setNome("Luigi");
            dipendente.setCognome("Verdi");
            dipendente.setDataNasc(Date.valueOf("1995-01-01"));
            dipendente.setRuolo(Tipi.ruolo.DIPENDENTE);

            // Creiamo l'oggetto reale Informazioni (come farebbe il livello applicativo)
            Informazioni infoSup = new Informazioni("SUP0000000001", "Mario", "Rossi");
            dipendente.setSupervisoreInfo(infoSup);

            String password = "passwordSegreta";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                // Setup Connessione
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // MOCK 1: Inserimento Tabella UTENTE (Generico)
                when(connection.prepareStatement(contains("INSERT INTO utente"))).thenReturn(psUtente);
                when(psUtente.execute()).thenReturn(true);

                // MOCK 2: Inserimento Tabella DIPENDENTE (Specifico)
                when(connection.prepareStatement(contains("INSERT INTO dipendente"))).thenReturn(psDipendente);
                when(psDipendente.executeUpdate()).thenReturn(1);

                // 2. ACT
                utenteDao.createDipendente(dipendente, password);

                // 3. ASSERT & VERIFY

                // Verifica Transazionale
                verify(connection).setAutoCommit(false); // Inizio transazione
                verify(connection).commit();             // Fine transazione con successo
                verify(connection, never()).rollback();  // Nessun errore

                // Verifica inserimento dati Utente (controllo a campione)
                verify(psUtente).setString(1, "DIP0000000001");
                verify(psUtente).execute();

                // Verifica inserimento dati Dipendente
                // Qui verifichiamo che il DAO stia estraendo correttamente la matricola dall'oggetto Informazioni
                verify(psDipendente).setString(1, "DIP0000000001"); // Matricola Dipendente
                verify(psDipendente).setString(2, "SUP0000000001"); // Matricola Supervisore (FK)
                verify(psDipendente).executeUpdate();

                // Chiusura risorse
                verify(connection).close();
            }
        }

        @Test
        @DisplayName("Dovrebbe eseguire ROLLBACK se l'inserimento nella tabella DIPENDENTE fallisce")
        void testCreateDipendente_ErroreSpecifico_Rollback() throws Exception {
            // 1. ARRANGE
            Dipendente dipendente = new Dipendente();
            dipendente.setMatricola("DIP_ERROR");
            dipendente.setEmail("error@test.it");

            Informazioni infoSup = new Informazioni("SUP_ERRATO", "N", "C");
            dipendente.setSupervisoreInfo(infoSup);

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Query 1 (Utente): Successo
                when(connection.prepareStatement(contains("INSERT INTO utente"))).thenReturn(psUtente);
                when(psUtente.execute()).thenReturn(true);

                // Query 2 (Dipendente): Fallimento (es. FK supervisore non valida)
                when(connection.prepareStatement(contains("INSERT INTO dipendente"))).thenReturn(psDipendente);
                doThrow(new SQLException("Violazione FK Supervisore")).when(psDipendente).executeUpdate();

                // 2. ACT & ASSERT
                SQLException ex = assertThrows(SQLException.class, () -> {
                    utenteDao.createDipendente(dipendente, "pass");
                });

                assertEquals("Errore creazione dipendente", ex.getMessage());

                // VERIFICA CRUCIALE: Rollback obbligatorio
                verify(connection).rollback();
                verify(connection, never()).commit();

                verify(connection).close();
            }
        }

        @Test
        void testCreateDipendente_EmailDuplicata() throws Exception {
            // 1. ARRANGE
            Dipendente dipendente = new Dipendente();
            dipendente.setEmail("esistente@test.it");
            dipendente.setMatricola("DIP123");
            dipendente.setSupervisoreInfo(new Informazioni("SUP1", "A", "B")); // Setup minimo
            dipendente.setNome("Luigi");
            dipendente.setCognome("Verdi");
            dipendente.setDataNasc(Date.valueOf("1995-01-01"));
            dipendente.setRuolo(Tipi.ruolo.DIPENDENTE);

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Query 1 (Utente): Fallimento immediato
                when(connection.prepareStatement(contains("INSERT INTO utente"))).thenReturn(psUtente);

                SQLIntegrityConstraintViolationException sqlEx =
                        new SQLIntegrityConstraintViolationException("Duplicate entry 'esistente@test.it' for key 'email'");
                when(psUtente.execute()).thenThrow(sqlEx);

                // 2. ACT & ASSERT
                EmailGiaPresenteException ex = assertThrows(EmailGiaPresenteException.class, () -> {
                    utenteDao.createDipendente(dipendente, "pass");
                });

                assertEquals("L'email inserita è già presente nel db", ex.getMessage());

                verify(connection).rollback();
                // Assicuriamoci che non abbia nemmeno provato a preparare la seconda query
                verify(connection, never()).prepareStatement(contains("INSERT INTO dipendente"));
            }
        }
    }


    @Nested
    @DisplayName("Metodo: createSupervisore")
    class CreateSupervisoreTest {

        private PreparedStatement psUtente;
        private PreparedStatement psSupervisore;

        @BeforeEach
        void initMocks() {
            psUtente = mock(PreparedStatement.class);
            psSupervisore = mock(PreparedStatement.class);
        }

        @Test
        void testCreateSupervisore_Successo() throws Exception {
            // 1. ARRANGE
            Supervisore supervisore = new Supervisore();
            supervisore.setMatricola("SUP0000000001");
            supervisore.setEmail("super@test.it");
            supervisore.setNome("Super");
            supervisore.setCognome("Visore");
            supervisore.setDataNasc(Date.valueOf("1980-01-01"));
            supervisore.setRuolo(Tipi.ruolo.SUPERVISORE);

            String password = "passwordAdmin";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // MOCK 1: Tabella Utente (Generico)
                when(connection.prepareStatement(contains("INSERT INTO utente"))).thenReturn(psUtente);
                when(psUtente.execute()).thenReturn(true);

                // MOCK 2: Tabella Supervisore (Specifico)
                // La query è: INSERT INTO supervisore (matricola) values (?)
                when(connection.prepareStatement(contains("INSERT INTO supervisore"))).thenReturn(psSupervisore);
                when(psSupervisore.executeUpdate()).thenReturn(1);

                // 2. ACT
                utenteDao.createSupervisore(supervisore, password);

                // 3. ASSERT & VERIFY

                // Verifica Transazione
                verify(connection).setAutoCommit(false);
                verify(connection).commit();
                verify(connection, never()).rollback();

                // Verifica inserimento Utente
                verify(psUtente).setString(1, "SUP0000000001");
                verify(psUtente).execute();

                // Verifica inserimento Supervisore
                verify(psSupervisore).setString(1, "SUP0000000001"); // Matricola
                verify(psSupervisore).executeUpdate();

                verify(connection).close();
            }
        }

        @Test
        void testCreateSupervisore_ErroreSpecifico_Rollback() throws Exception {
            // 1. ARRANGE
            Supervisore supervisore = new Supervisore();
            supervisore.setMatricola("SUP_FAIL");
            supervisore.setEmail("fail@test.it");


            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Query 1 (Utente): Successo
                when(connection.prepareStatement(contains("INSERT INTO utente"))).thenReturn(psUtente);
                when(psUtente.execute()).thenReturn(true);

                // Query 2 (Supervisore): Fallimento
                when(connection.prepareStatement(contains("INSERT INTO supervisore"))).thenReturn(psSupervisore);
                doThrow(new SQLException("Errore Connessione DB")).when(psSupervisore).executeUpdate();

                // 2. ACT & ASSERT
                SQLException ex = assertThrows(SQLException.class, () -> {
                    utenteDao.createSupervisore(supervisore, "pass");
                });

                assertEquals("Errore creazione supervisore", ex.getMessage());

                // VERIFICA CRUCIALE: Rollback
                verify(connection).rollback();
                verify(connection, never()).commit();

                verify(connection).close();
            }
        }

        @Test
        void testCreateSupervisore_EmailDuplicata() throws Exception {
            // 1. ARRANGE
            Supervisore supervisore = new Supervisore();
            supervisore.setEmail("esistente@test.it");
            supervisore.setMatricola("SUP123");
            supervisore.setNome("Super");
            supervisore.setCognome("Visore");
            supervisore.setDataNasc(Date.valueOf("1980-01-01"));
            supervisore.setRuolo(Tipi.ruolo.SUPERVISORE);


            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Query 1 (Utente): Fallisce subito per duplicato
                when(connection.prepareStatement(contains("INSERT INTO utente"))).thenReturn(psUtente);

                SQLIntegrityConstraintViolationException sqlEx =
                        new SQLIntegrityConstraintViolationException("Duplicate entry 'esistente@test.it' for key 'email'");
                when(psUtente.execute()).thenThrow(sqlEx);

                // 2. ACT & ASSERT
                EmailGiaPresenteException ex = assertThrows(EmailGiaPresenteException.class, () -> {
                    utenteDao.createSupervisore(supervisore, "pass");
                });

                assertEquals("L'email inserita è già presente nel db", ex.getMessage());

                verify(connection).rollback();

                // Verifica che non provi nemmeno a inserire nella tabella supervisore
                verify(connection, never()).prepareStatement(contains("INSERT INTO supervisore"));
            }
        }
    }


    @Nested
    @DisplayName("Metodo: findByMatricola")
    class FindByMatricolaTest {

        @Test
        void testFindByMatricola_Successo() throws SQLException {
            // 1. ARRANGE
            String matricolaCercata = "0512103361001";
            Date dataNascita = Date.valueOf("1990-01-01");

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Mock della query
                when(connection.prepareStatement(contains("SELECT * FROM utente"))).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);

                // Simuliamo che il ResultSet contenga 1 riga
                when(rs.next()).thenReturn(true, false); // Prima true (trovato), poi false (fine)

                // Mappiamo le colonne ai valori di ritorno
                when(rs.getString("matricola")).thenReturn(matricolaCercata);
                when(rs.getString("email")).thenReturn("mario@test.it");
                when(rs.getString("nome")).thenReturn("Mario");
                when(rs.getString("cognome")).thenReturn("Rossi");
                when(rs.getDate("dataDiNascita")).thenReturn(dataNascita);
                when(rs.getString("ruolo")).thenReturn("DIPENDENTE"); // Deve essere una stringa valida per l'Enum
                when(rs.getBoolean("newUtente")).thenReturn(true);

                // 2. ACT
                Utente risultato = utenteDao.findByMatricola(matricolaCercata);

                // 3. ASSERT
                assertNotNull(risultato);
                assertEquals(matricolaCercata, risultato.getMatricola());
                assertEquals("Mario", risultato.getNome());
                assertEquals(Tipi.ruolo.DIPENDENTE, risultato.getRuolo());
                assertTrue(risultato.isNewUtente());

                // Verifica che il parametro sia stato settato nella query
                verify(ps).setString(1, matricolaCercata);
            }
        }

        @Test
        void testFindByMatricola_NonTrovato() throws SQLException {
            // 1. ARRANGE
            String matricolaInesistente = "0000000000000";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);

                // Simuliamo ResultSet vuoto
                when(rs.next()).thenReturn(false);

                // 2. ACT
                Utente risultato = utenteDao.findByMatricola(matricolaInesistente);

                // 3. ASSERT
                assertNull(risultato);
            }
        }

        @Test
        void testFindByMatricola_InputInvalido() {
            // Caso 1: Null
            IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.findByMatricola(null);
            });
            assertEquals("La matricola non può essere nulla", exNull.getMessage());

            // Caso 2: Vuota
            IllegalArgumentException exVuota = assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.findByMatricola("");
            });
            assertEquals("La matricola non può essere nulla", exVuota.getMessage());
        }

        @Test
        void testFindByMatricola_ErroreSQL() throws SQLException {
            // 1. ARRANGE
            String matricola = "M123";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // Simuliamo crash del DB
                when(ps.executeQuery()).thenThrow(new SQLException("Errore connessione"));

                // 2. ACT & ASSERT
                RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                    utenteDao.findByMatricola(matricola);
                });

                assertNotNull(ex.getCause());
                assertTrue(ex.getCause() instanceof SQLException);
            }
        }
    }

    @Nested
    @DisplayName("Metodo: findByEmail")
    class FindByEmailTest {

        @Test
        void testFindByEmail_PolimorfismoDipendente() throws SQLException {
            // 1. ARRANGE
            String email = "dip@test.it";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Mock Query
                when(connection.prepareStatement(contains("SELECT * FROM utente"))).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);

                // Trovato!
                when(rs.next()).thenReturn(true);

                // MOCK CRUCIALE: Il ruolo determina la classe
                when(rs.getString("ruolo")).thenReturn("DIPENDENTE");

                // Altri dati
                when(rs.getString("matricola")).thenReturn("DIP001");
                when(rs.getString("email")).thenReturn(email);
                when(rs.getString("nome")).thenReturn("Luigi");

                // 2. ACT
                Utente risultato = utenteDao.findByEmail(email);

                // 3. ASSERT
                assertNotNull(risultato);
                // VERIFICA POLIMORFISMO: Deve essere un'istanza di Dipendente
                assertTrue(risultato instanceof Dipendente, "L'oggetto restituito deve essere di tipo Dipendente");
                assertEquals(Tipi.ruolo.DIPENDENTE, risultato.getRuolo());
                assertEquals("Luigi", risultato.getNome());
            }
        }

        @Test
        void testFindByEmail_PolimorfismoSupervisore() throws SQLException {
            // 1. ARRANGE
            String email = "sup@test.it";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(contains("SELECT * FROM utente"))).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);
                when(rs.next()).thenReturn(true);

                // MOCK RUOLO
                when(rs.getString("ruolo")).thenReturn("SUPERVISORE");

                when(rs.getString("email")).thenReturn(email);

                // 2. ACT
                Utente risultato = utenteDao.findByEmail(email);

                // 3. ASSERT
                assertNotNull(risultato);
                // VERIFICA POLIMORFISMO
                assertTrue(risultato instanceof Supervisore, "L'oggetto restituito deve essere di tipo Supervisore");
                assertEquals(Tipi.ruolo.SUPERVISORE, risultato.getRuolo());
            }
        }

        @Test
        void testFindByEmail_PolimorfismoGenerico() throws SQLException {
            // 1. ARRANGE
            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);
                when(rs.next()).thenReturn(true);

                // Ruolo diverso da Dipendente/Supervisore
                when(rs.getString("ruolo")).thenReturn("GESTORE");
                when(rs.getString("email")).thenReturn("admin@test.it");

                // 2. ACT
                Utente risultato = utenteDao.findByEmail("admin@test.it");

                // 3. ASSERT
                assertNotNull(risultato);
                // Non deve essere né Dipendente né Supervisore
                assertFalse(risultato instanceof Dipendente);
                assertFalse(risultato instanceof Supervisore);
                // Deve essere proprio Utente
                assertEquals(Utente.class, risultato.getClass());
                assertEquals(Tipi.ruolo.GESTORE, risultato.getRuolo());
            }
        }

        @Test
        void testFindByEmail_NonTrovato() throws SQLException {
            // 1. ARRANGE
            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);

                // Nessun risultato
                when(rs.next()).thenReturn(false);

                // 2. ACT
                Utente risultato = utenteDao.findByEmail("inesistente@test.it");

                // 3. ASSERT
                assertNull(risultato, "Se l'utente non c'è, deve restituire null");
            }
        }

        @Test
        void testFindByEmail_InputNonValido() {
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.findByEmail(null);
            });

            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.findByEmail("");
            });
        }
    }

    @Nested
    @DisplayName("Metodo: getAllDipendentiInfo")
    class GetAllDipendentiInfoTest {

        @Test
        void testGetAllDipendentiInfo_Successo() throws SQLException {
            // 1. ARRANGE
            String matricolaSupervisore = "SUP0001";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Mockiamo la query complessa (JOIN)
                when(connection.prepareStatement(contains("SELECT u.matricola,u.nome,u.cognome"))).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);

                // Simuliamo il ritrovamento di 2 dipendenti associati al supervisore
                when(rs.next()).thenReturn(true, true, false);

                // Dati Dipendente 1
                when(rs.getString("matricola")).thenReturn("DIP1", "DIP2");
                when(rs.getString("nome")).thenReturn("Mario", "Luigi");
                when(rs.getString("cognome")).thenReturn("Rossi", "Verdi");

                // 2. ACT
                List<Informazioni> risultato = utenteDao.getAllDipendentiInfo(matricolaSupervisore);

                // 3. ASSERT
                assertNotNull(risultato);
                assertEquals(2, risultato.size(), "Dovrebbe trovare 2 dipendenti");

                // Verifica contenuto primo elemento
                Informazioni info1 = risultato.get(0);
                assertEquals("DIP1", info1.getMatricola());
                assertEquals("Mario", info1.getNome());
                assertEquals("Rossi", info1.getCognome());

                // Verifica contenuto secondo elemento
                Informazioni info2 = risultato.get(1);
                assertEquals("DIP2", info2.getMatricola());

                // Verifica che il parametro passato alla query sia la matricola del supervisore
                verify(ps).setString(1, matricolaSupervisore);
            }
        }

        @Test
        void testGetAllDipendentiInfo_NessunRisultato() throws SQLException {
            // 1. ARRANGE
            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);

                // Nessun risultato
                when(rs.next()).thenReturn(false);

                // 2. ACT
                List<Informazioni> risultato = utenteDao.getAllDipendentiInfo("SUP_SOLO");

                // 3. ASSERT
                assertNotNull(risultato);
                assertTrue(risultato.isEmpty());
            }
        }

        @Test
        void testGetAllDipendentiInfo_InputInvalido() {
            // Caso 1: Null
            IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.getAllDipendentiInfo(null);
            });
            assertEquals("La matricola non può essere nulla", exNull.getMessage());

            // Caso 2: Vuota
            IllegalArgumentException exVuota = assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.getAllDipendentiInfo("");
            });
            assertEquals("La matricola non può essere nulla", exVuota.getMessage());
        }

        @Test
        void testGetAllDipendentiInfo_ErroreSQL() throws SQLException {
            // 1. ARRANGE
            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // Simuliamo errore durante l'esecuzione della query
                when(ps.executeQuery()).thenThrow(new SQLException("Errore di sintassi SQL o connessione"));

                // 2. ACT & ASSERT
                SQLException ex = assertThrows(SQLException.class, () -> {
                    utenteDao.getAllDipendentiInfo("SUP1");
                });

                assertEquals("Errore di sintassi SQL o connessione", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Metodo: getSupervisoreInfo")
    class GetSupervisoreInfoTest {

        @Test
        void testGetSupervisoreInfo_Successo() throws SQLException {
            // 1. ARRANGE
            String matricolaDipendente = "DIP001";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Mock della query con JOIN
                when(connection.prepareStatement(contains("SELECT u.matricola"))).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);

                // Simuliamo che venga trovata 1 riga
                when(rs.next()).thenReturn(true);

                // Dati attesi del Supervisore
                when(rs.getString("matricola")).thenReturn("SUP_MARIO");
                when(rs.getString("nome")).thenReturn("Mario");
                when(rs.getString("cognome")).thenReturn("Rossi");

                // 2. ACT
                Informazioni risultato = utenteDao.getSupervisoreInfo(matricolaDipendente);

                // 3. ASSERT
                assertNotNull(risultato, "Dovrebbe restituire un oggetto Informazioni");
                assertEquals("SUP_MARIO", risultato.getMatricola());
                assertEquals("Mario", risultato.getNome());
                assertEquals("Rossi", risultato.getCognome());

                // Verifica che la query sia stata eseguita con la matricola del dipendente
                verify(ps).setString(1, matricolaDipendente);
            }
        }

        @Test
        void testGetSupervisoreInfo_NessunRisultato() throws SQLException {
            // 1. ARRANGE
            String matricola = "DIP_ORFANO";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);

                // Nessun risultato dalla query
                when(rs.next()).thenReturn(false);

                // 2. ACT
                Informazioni risultato = utenteDao.getSupervisoreInfo(matricola);

                // 3. ASSERT
                assertNull(risultato, "Se non trova risultati, deve restituire null");
            }
        }

        @Test
        void testGetSupervisoreInfo_InputInvalido() {
            // Caso 1: Null
            IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.getSupervisoreInfo(null);
            });
            assertEquals("La matricola non può essere nulla", exNull.getMessage());

            // Caso 2: Vuota
            IllegalArgumentException exVuota = assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.getSupervisoreInfo("");
            });
            assertEquals("La matricola non può essere nulla", exVuota.getMessage());
        }

        @Test
        void testGetSupervisoreInfo_ErroreSQL_RestituisceNull() throws SQLException {
            // 1. ARRANGE
            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // Simuliamo il fallimento della query
                when(ps.executeQuery()).thenThrow(new SQLException("Errore Connessione DB"));

                // 2. ACT
                Informazioni risultato = utenteDao.getSupervisoreInfo("DIP123");

                // 3. ASSERT
                assertNull(risultato, "In caso di eccezione SQL catturata, il metodo deve restituire null");
            }
        }
    }

    @Nested
    @DisplayName("Metodo: updateRuolo")
    class UpdateRuoloTest {

        // Dichiariamo mock distinti per ogni tipo di query che il metodo potrebbe eseguire
        private PreparedStatement psSelect; // Per leggere il ruolo attuale
        private PreparedStatement psUpdate; // Per aggiornare la tabella Utente
        private PreparedStatement psDelete; // Per cancellare dalla vecchia tabella specifica
        private PreparedStatement psInsert; // Per inserire nella nuova tabella specifica
        private ResultSet rs;

        @BeforeEach
        void initMocks() {
            psSelect = mock(PreparedStatement.class);
            psUpdate = mock(PreparedStatement.class);
            psDelete = mock(PreparedStatement.class);
            psInsert = mock(PreparedStatement.class);
            rs = mock(ResultSet.class);
        }

        @Test
        void testUpdateRuolo_DaDipendenteASupervisore() throws Exception {
            // 1. ARRANGE
            String matricola = "DIP001";
            Tipi.ruolo nuovoRuolo = Tipi.ruolo.SUPERVISORE;
            // Supervisor non serve se divento Supervisore, ma passiamo null
            String nuovoSupervisore = null;

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // --- MOCK QUERY 1: SELECT (Legge Ruolo Attuale) ---
                when(connection.prepareStatement(contains("SELECT ruolo"))).thenReturn(psSelect);
                when(psSelect.executeQuery()).thenReturn(rs);
                when(rs.next()).thenReturn(true);
                // Simuliamo che attualmente sia un DIPENDENTE
                when(rs.getString("ruolo")).thenReturn("DIPENDENTE");

                // --- MOCK QUERY 2: DELETE (Pulizia vecchio ruolo) ---
                // Il metodo chiama deleteFromTable("dipendente"...)
                when(connection.prepareStatement(contains("DELETE FROM dipendente"))).thenReturn(psDelete);

                // --- MOCK QUERY 3: UPDATE (Tabella Padre) ---
                when(connection.prepareStatement(contains("UPDATE utente"))).thenReturn(psUpdate);

                // --- MOCK QUERY 4: INSERT (Nuovo ruolo) ---
                // Il metodo chiama insertIntoSupervisore(...)
                when(connection.prepareStatement(contains("INSERT INTO supervisore"))).thenReturn(psInsert);

                // 2. ACT
                utenteDao.updateRuolo(matricola, nuovoRuolo, nuovoSupervisore);

                // 3. ASSERT & VERIFY

                // Verifica Transazione
                verify(connection).setAutoCommit(false);
                verify(connection).commit();

                // Verifica flusso logico:
                // 1. Ha letto il ruolo
                verify(psSelect).setString(1, matricola);

                // 2. Ha cancellato dalla tabella DIPENDENTE (vecchio ruolo)
                verify(psDelete).executeUpdate();

                // 3. Ha aggiornato la tabella UTENTE
                verify(psUpdate).setString(1, "SUPERVISORE");
                verify(psUpdate).executeUpdate();

                // 4. Ha inserito nella tabella SUPERVISORE (nuovo ruolo)
                verify(psInsert).executeUpdate();
            }
        }

        @Test
        void testUpdateRuolo_DaSupervisoreADipendente() throws Exception {
            // 1. ARRANGE
            String matricola = "SUP001";
            Tipi.ruolo nuovoRuolo = Tipi.ruolo.DIPENDENTE;
            String matricolaCapo = "SUP_CAPO"; // Obbligatorio per diventare dipendente

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // 1. Select: Attualmente è SUPERVISORE
                when(connection.prepareStatement(contains("SELECT ruolo"))).thenReturn(psSelect);
                when(psSelect.executeQuery()).thenReturn(rs);
                when(rs.next()).thenReturn(true);
                when(rs.getString("ruolo")).thenReturn("SUPERVISORE");

                // 2. Delete: Deve cancellare da SUPERVISORE
                when(connection.prepareStatement(contains("DELETE FROM supervisore"))).thenReturn(psDelete);

                // 3. Update: Utente
                when(connection.prepareStatement(contains("UPDATE utente"))).thenReturn(psUpdate);

                // 4. Insert: Deve inserire in DIPENDENTE
                when(connection.prepareStatement(contains("INSERT INTO dipendente"))).thenReturn(psInsert);

                // 2. ACT
                utenteDao.updateRuolo(matricola, nuovoRuolo, matricolaCapo);

                // 3. ASSERT
                verify(connection).commit();

                // Verifica cancellazione dal vecchio
                verify(psDelete).executeUpdate(); // delete from supervisore...

                // Verifica inserimento nel nuovo
                verify(psInsert).executeUpdate(); // insert into dipendente...

                // Verifica che l'update sia corretto
                verify(psUpdate).setString(1, "DIPENDENTE");
            }
        }

        @Test
        void testUpdateRuolo_StessoRuolo_NessunaAzione() throws Exception {
            // 1. ARRANGE
            String matricola = "DIP001";
            Tipi.ruolo nuovoRuolo = Tipi.ruolo.DIPENDENTE; // Stesso ruolo
            String supervisor = "SUP_QUALSIASI";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Select: è DIPENDENTE
                when(connection.prepareStatement(contains("SELECT ruolo"))).thenReturn(psSelect);
                when(psSelect.executeQuery()).thenReturn(rs);
                when(rs.next()).thenReturn(true);
                when(rs.getString("ruolo")).thenReturn("DIPENDENTE");

                // 2. ACT
                utenteDao.updateRuolo(matricola, nuovoRuolo, supervisor);

                // 3. ASSERT
                // Verifica che NON abbia chiamato delete, update o insert
                verify(connection, never()).prepareStatement(contains("DELETE FROM"));
                verify(connection, never()).prepareStatement(contains("UPDATE utente"));
                verify(connection, never()).prepareStatement(contains("INSERT INTO"));
            }
        }

        @Test
        void testUpdateRuolo_UtenteNonTrovato() throws Exception {
            // 1. ARRANGE
            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                when(connection.prepareStatement(contains("SELECT ruolo"))).thenReturn(psSelect);
                when(psSelect.executeQuery()).thenReturn(rs);

                // ResultSet vuoto -> Utente non esiste
                when(rs.next()).thenReturn(false);

                // 2. ACT & ASSERT
                Exception ex = assertThrows(Exception.class, () -> {
                    utenteDao.updateRuolo("INESISTENTE", Tipi.ruolo.DIPENDENTE, "SUP");
                });

                assertNotNull(ex.getCause());
                assertTrue(ex.getCause().getMessage().contains("Utente non trovato"));

                // Verifica Rollback
                verify(connection).rollback();
                verify(connection, never()).commit();
            }
        }

        @Test
        void testUpdateRuolo_ValidazioneDipendenteSenzaSupervisore() {
            // Caso 1: Supervisore NULL
            IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.updateRuolo("M1", Tipi.ruolo.DIPENDENTE, null);
            });
            assertTrue(exNull.getMessage().contains("senza specificare un Supervisore"));

            // Caso 2: Supervisore VUOTO
            IllegalArgumentException exVuoto = assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.updateRuolo("M1", Tipi.ruolo.DIPENDENTE, "");
            });
            assertTrue(exVuoto.getMessage().contains("senza specificare un Supervisore"));
        }

        @Test
        void testUpdateRuolo_InputBaseInvalidi() {
            // Matricola Null
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.updateRuolo(null, Tipi.ruolo.GESTORE, null);
            });

            // Ruolo Null
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.updateRuolo("M1", null, null);
            });
        }

        @Test
        void testUpdateRuolo_ErroreSQL_Rollback() throws Exception {
            // 1. ARRANGE
            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Select OK (è Gestore)
                when(connection.prepareStatement(contains("SELECT ruolo"))).thenReturn(psSelect);
                when(psSelect.executeQuery()).thenReturn(rs);
                when(rs.next()).thenReturn(true);
                when(rs.getString("ruolo")).thenReturn("GESTORE");

                // Simuliamo errore durante l'UPDATE della tabella padre
                when(connection.prepareStatement(contains("UPDATE utente"))).thenReturn(psUpdate);
                doThrow(new SQLException("Lock wait timeout exceeded")).when(psUpdate).executeUpdate();

                // 2. ACT & ASSERT
                Exception ex = assertThrows(Exception.class, () -> {
                    // Provo a cambiare da Gestore a Supervisore
                    utenteDao.updateRuolo("G1", Tipi.ruolo.SUPERVISORE, null);
                });

                assertTrue(ex.getMessage().contains("Errore durante il cambio ruolo"));

                // Verifica Rollback
                verify(connection).rollback();
            }
        }
    }

    @Nested
    @DisplayName("Metodo: updateSupervisore")
    class UpdateSupervisoreTest {

        @Test
        void testUpdateSupervisore_Successo() throws Exception {
            // 1. ARRANGE
            String matricolaDipendente = "DIP0001";
            String nuovoSupervisore = "SUP0001";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Mock Query
                when(connection.prepareStatement(contains("UPDATE dipendente"))).thenReturn(ps);

                // Simuliamo che 1 riga venga aggiornata
                when(ps.executeUpdate()).thenReturn(1);

                // 2. ACT
                utenteDao.updateSupervisore(matricolaDipendente, nuovoSupervisore);

                // 3. ASSERT
                // Verifica ordine parametri: 1=Supervisore, 2=Dipendente
                verify(ps).setString(1, nuovoSupervisore);
                verify(ps).setString(2, matricolaDipendente);

                verify(ps).executeUpdate();
                verify(ps).close();
                verify(connection).close();
            }
        }

        @Test
        void testUpdateSupervisore_InputInvalidi() {
            // Caso 1: Dipendente Null
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.updateSupervisore(null, "SUP1");
            });

            // Caso 2: Dipendente Vuoto
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.updateSupervisore("", "SUP1");
            });

            // Caso 3: Supervisore Null
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.updateSupervisore("DIP1", null);
            });

            // Caso 4: Supervisore Vuoto
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.updateSupervisore("DIP1", "");
            });
        }

        @Test
        void testUpdateSupervisore_ErroreSQL() throws SQLException {
            // 1. ARRANGE
            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // Simuliamo errore (es. Foreign Key non valida se il supervisore non esiste)
                doThrow(new SQLException("FK Constraint fail")).when(ps).executeUpdate();

                // 2. ACT & ASSERT
                RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                    utenteDao.updateSupervisore("DIP1", "SUP_INESISTENTE");
                });

                assertNotNull(ex.getCause());
                assertTrue(ex.getCause() instanceof SQLException);
            }
        }

        @Test
        void testUpdateSupervisore_DipendenteNonTrovato() throws Exception {
            // Come per gli altri metodi update/delete, il codice non controlla il valore di ritorno
            // Questo test documenta che se aggiorno un dipendente inesistente, non succede nulla.

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // Il DB dice: "Ho aggiornato 0 righe"
                when(ps.executeUpdate()).thenReturn(0);

                // ACT
                // Non deve lanciare eccezioni
                utenteDao.updateSupervisore("DIP_FANTASMA", "SUP1");

                // ASSERT
                verify(ps).executeUpdate();
            }
        }
    }

    @Nested
    @DisplayName("Metodo: updatePassword")
    class UpdatePasswordTest {

        @Test
        void testUpdatePassword_Successo() throws SQLException {
            // 1. ARRANGE
            String matricola = "DIP001";
            String nuovaPasswordHash = "newHash12345";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Mock Query
                when(connection.prepareStatement(contains("UPDATE utente SET hashPassword"))).thenReturn(ps);

                // Simuliamo che 1 riga venga aggiornata
                when(ps.executeUpdate()).thenReturn(1);

                // 2. ACT
                utenteDao.updatePassword(matricola, nuovaPasswordHash);

                // 3. ASSERT
                // Verifica ordine parametri:
                // 1 = hashPassword (SET)
                // 2 = matricola (WHERE)
                verify(ps).setString(1, nuovaPasswordHash);
                verify(ps).setString(2, matricola);

                verify(ps).executeUpdate();
                verify(ps).close();
                verify(connection).close();
            }
        }

        @Test
        void testUpdatePassword_InputInvalidi() {
            // Caso 1: Matricola Null
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.updatePassword(null, "hash");
            });

            // Caso 2: Matricola Vuota
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.updatePassword("", "hash");
            });

            // Caso 3: Password Null
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.updatePassword("M123", null);
            });

            // Caso 4: Password Vuota
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.updatePassword("M123", "");
            });
        }

        @Test
        void testUpdatePassword_ErroreSQL() throws SQLException {
            // 1. ARRANGE
            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // Simuliamo errore (es. connessione caduta)
                doThrow(new SQLException("Errore DB")).when(ps).executeUpdate();

                // 2. ACT & ASSERT
                RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                    utenteDao.updatePassword("M123", "hash");
                });

                assertNotNull(ex.getCause());
                assertTrue(ex.getCause() instanceof SQLException);
            }
        }

        @Test
        void testUpdatePassword_UtenteInesistente() throws SQLException {
            // Anche qui, verifichiamo che il metodo non lanci eccezioni se l'utente non viene trovato
            // (dato che executeUpdate restituisce 0 e il codice lo ignora)

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // Il DB restituisce 0 righe modificate
                when(ps.executeUpdate()).thenReturn(0);

                // ACT -> Non deve lanciare eccezioni
                utenteDao.updatePassword("FANTASMA", "hash");

                // ASSERT
                verify(ps).executeUpdate();
            }
        }
    }

    @Nested
    @DisplayName("Metodo: delete")
    class DeleteTest {

        @Test
        void testDelete_Successo() throws SQLException {
            // 1. ARRANGE
            Utente utente = new Utente();
            utente.setMatricola("0512103361001");

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Mock Query
                when(connection.prepareStatement(contains("DELETE FROM utente"))).thenReturn(ps);

                // Simuliamo che il DB cancelli 1 riga (Successo)
                when(ps.executeUpdate()).thenReturn(1);

                // 2. ACT
                utenteDao.delete(utente);

                // 3. ASSERT
                verify(ps).setString(1, "0512103361001");
                verify(ps).executeUpdate();
                verify(ps).close();
                verify(connection).close();
            }
        }

        @Test
        void testDelete_UtenteNonTrovato() throws SQLException {
            // 1. ARRANGE
            Utente utente = new Utente();
            utente.setMatricola("INESISTENTE");

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // Simuliamo che il DB cancelli 0 righe (Fallimento logico)
                when(ps.executeUpdate()).thenReturn(0);

                // 2. ACT & ASSERT
                RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                    utenteDao.delete(utente);
                });

                // Analisi dell'eccezione:
                // Il tuo codice lancia new SQLException("Impossibile cancellare...")
                // Poi il catch la incapsula in RuntimeException(e)

                assertNotNull(ex.getCause(), "Deve esserci una causa scatenante");
                assertTrue(ex.getCause() instanceof SQLException);
                assertTrue(ex.getCause().getMessage().contains("Impossibile cancellare"));

                verify(ps).executeUpdate();
            }
        }

        @Test
        void testDelete_UtenteNull() {
            assertThrows(IllegalArgumentException.class, () -> {
                utenteDao.delete(null);
            });
        }

        @Test
        void testDelete_ErroreSQL() throws SQLException {
            // 1. ARRANGE
            Utente utente = new Utente();
            utente.setMatricola("M123");

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // Simuliamo errore tecnico (es. connessione persa)
                when(ps.executeUpdate()).thenThrow(new SQLException("Errore Connessione"));

                // 2. ACT & ASSERT
                RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                    utenteDao.delete(utente);
                });

                assertNotNull(ex.getCause());
                assertEquals("Errore Connessione", ex.getCause().getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Metodo: recuperaPassword")
    class RecuperaPasswordTest {

        @Test
        void testRecuperaPassword_Successo() throws SQLException {
            // 1. ARRANGE
            String email = "mario.rossi@test.it";
            String hashAtteso = "hashSegreto123";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Mock della query
                // Usiamo contains("SELECT hashPassword") per intercettare la query
                when(connection.prepareStatement(contains("SELECT hashPassword"))).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);

                // Simuliamo che venga trovata una riga (rs.next() = true)
                when(rs.next()).thenReturn(true);
                // Simuliamo il valore ritornato dalla colonna "hashPassword"
                when(rs.getString("hashPassword")).thenReturn(hashAtteso);

                // 2. ACT
                String risultato = utenteDao.recuperaPassword(email);

                // 3. ASSERT
                assertNotNull(risultato);
                assertEquals(hashAtteso, risultato);

                // Verifica che l'email sia stata passata correttamente al PreparedStatement
                verify(ps).setString(1, email);
                verify(ps).executeQuery();
                verify(ps).close();
                verify(connection).close();
            }
        }

        @Test
        void testRecuperaPassword_EmailNonTrovata() throws SQLException {
            // 1. ARRANGE
            String emailInesistente = "non_esisto@test.it";

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);

                // Simuliamo ResultSet vuoto (nessun utente trovato -> rs.next() = false)
                when(rs.next()).thenReturn(false);

                // 2. ACT & ASSERT
                SQLException ex = assertThrows(SQLException.class, () -> {
                    utenteDao.recuperaPassword(emailInesistente);
                });

                // Verifichiamo che il messaggio dell'eccezione sia quello definito nel tuo "else"
                assertTrue(ex.getMessage().contains("Email inesistente"));
                assertTrue(ex.getMessage().contains(emailInesistente));
            }
        }

        @Test
        void testRecuperaPassword_ErroreConnessione() throws SQLException {
            // 1. ARRANGE
            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // Simuliamo errore SQL al momento dell'esecuzione
                when(ps.executeQuery()).thenThrow(new SQLException("Errore di connessione al DB"));

                // 2. ACT & ASSERT
                SQLException ex = assertThrows(SQLException.class, () -> {
                    utenteDao.recuperaPassword("test@test.it");
                });

                assertEquals("Errore di connessione al DB", ex.getMessage());
            }
        }
    }

}