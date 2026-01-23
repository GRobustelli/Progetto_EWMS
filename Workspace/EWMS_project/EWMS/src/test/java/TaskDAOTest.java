import it.unisa.ewms.model.beans.Allegato;
import it.unisa.ewms.model.beans.Task;
import it.unisa.ewms.model.beans.Tipi;
import it.unisa.ewms.model.beans.Utente;
import it.unisa.ewms.persistance.ClassiDAO.TaskDAO;
import it.unisa.ewms.persistance.DataSourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskDAOTest {
    private TaskDAO taskDao;

    // Mocks per i componenti JDBC
    private Connection connection;
    private PreparedStatement psTask;
    private PreparedStatement psAllegato;
    private ResultSet generatedKeys;

    @BeforeEach
    public void setUp() throws SQLException, ClassNotFoundException {
        taskDao = new TaskDAO();
        // Creiamo i mock base
        connection = mock(Connection.class);
        psTask = mock(PreparedStatement.class);
        psAllegato = mock(PreparedStatement.class);
        generatedKeys = mock(ResultSet.class);
    }


    private Task creaTaskDiEsempio() {
        Task t = new Task();
        t.setTitolo("Implementare Unit Test");
        t.setDataCreazione(new Date(System.currentTimeMillis()));
        t.setDataDiScadenza(new Date(System.currentTimeMillis() + 86400000));
        t.setIstruzioni("Usare Mockito");
        t.setStato(Tipi.stato.IN_ESECUZIONE);
        t.setSupervisore("Rossi");
        t.setDipendente("Verdi");
        t.setPriorita(Tipi.priorita.ALTA);
        return t;
    }
    @Nested
    @DisplayName("Metodo: create")
    class create_Test {

        @Test
        public void Test_create_conAllegato_successo() throws Exception {
            Task task = creaTaskDiEsempio();
            // Aggiungiamo un allegato al task
            Allegato allegato = new Allegato("file.pdf", 0, "/tmp/file.pdf", "application/pdf");
            task.setAllegato(allegato);

            // Simuliamo il contesto statico. Il try-with-resources chiude il mock statico alla fine del blocco
            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {

                // Quando viene chiamato DataSourceFactory.getConnection(), restituisci il nostro mock connection
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Configurazione mock per la query del Task (nota l'uso di eq(Statement.RETURN_GENERATED_KEYS))
                when(connection.prepareStatement(contains("INSERT INTO Task"), eq(Statement.RETURN_GENERATED_KEYS)))
                        .thenReturn(psTask);

                // Configurazione mock per la query dell'Allegato
                when(connection.prepareStatement(contains("INSERT INTO Allegato")))
                        .thenReturn(psAllegato);

                // Simuliamo la generazione dell'ID autoincrement (es. ID = 10)
                when(psTask.getGeneratedKeys()).thenReturn(generatedKeys);
                when(generatedKeys.next()).thenReturn(true);
                when(generatedKeys.getInt(1)).thenReturn(10);

                // 2. ACT
                taskDao.create(task);

                // 3. ASSERT & VERIFY

                // Verifica gestione transazione
                verify(connection).setAutoCommit(false);
                verify(connection).commit();
                verify(connection, never()).rollback(); // Assicuriamoci che NON abbia fatto rollback

                // Verifica che i dati del Task siano stati passati al PreparedStatement corretto
                verify(psTask).setString(1, task.getTitolo());
                verify(psTask).setString(5, "IN_ESECUZIONE"); // Enum convertito in stringa
                verify(psTask).executeUpdate();

                // Verifica che l'ID generato (10) sia stato settato nel bean Task
                assertEquals(10, task.getId());

                // Verifica che i dati dell'Allegato siano stati passati usando l'ID del task appena creato
                verify(psAllegato).setString(1, "file.pdf");
                verify(psAllegato).setLong(2, 10); // Foreign Key corretta
                verify(psAllegato).executeUpdate();

                // Verifica chiusura risorse
                verify(psTask).close();
                verify(psAllegato).close();
                verify(connection).close();
            }


        }

        @Test
        void testCreate_SenzaAllegato_Successo() throws Exception {
            // 1. ARRANGE
            Task task = creaTaskDiEsempio();
            task.setAllegato(null); // Nessun allegato

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Mock solo per il Task
                when(connection.prepareStatement(contains("INSERT INTO Task"), eq(Statement.RETURN_GENERATED_KEYS)))
                        .thenReturn(psTask);

                when(psTask.getGeneratedKeys()).thenReturn(generatedKeys);
                when(generatedKeys.next()).thenReturn(true);
                when(generatedKeys.getInt(1)).thenReturn(5); // ID simulato = 5

                // 2. ACT
                taskDao.create(task);

                // 3. ASSERT
                verify(connection).commit();

                // VERIFICA CHIAVE: Non deve mai provare a salvare l'allegato
                verify(connection, never()).prepareStatement(contains("INSERT INTO Allegato"));

                // L'ID deve essere aggiornato
                assertEquals(5, task.getId());
            }
        }

        @Test
        void testCreate_TaskNull_LanciaEccezione() {

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                taskDao.create(null);
            });

            // Verifichiamo anche che il messaggio sia quello giusto

            assertEquals("Il task non può essere null", exception.getMessage());
        }

        @Test
        void testCreate_ErroreDatabase_EsegueRollback() throws Exception {
            // 1. ARRANGE
            Task task = creaTaskDiEsempio();

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                when(connection.prepareStatement(anyString(), anyInt())).thenReturn(psTask);

                // SIMULAZIONE ERRORE: Lanciamo SQLException quando prova a eseguire l'update del Task
                doThrow(new SQLException("Errore Connessione Persa")).when(psTask).executeUpdate();

                // 2. ACT
                Exception exception = assertThrows(Exception.class, () -> {
                    taskDao.create(task);
                });

                // 3. ASSERT
                // Verifichiamo che l'eccezione sia quella wrapper definita nel DAO
                assertTrue(exception.getMessage().contains("Errore durante l'inserimento"));

                // VERIFICA CHIAVE: Deve aver chiamato rollback()
                verify(connection).rollback();

                // Non deve aver fatto commit
                verify(connection, never()).commit();

                // Le risorse devono essere chiuse comunque (nel blocco finally)
                verify(connection).close();
            }
        }

        //non controllo se uno dei valori di Task è null perché
    }


    @Nested
    @DisplayName("Metodo: findById()")
    class FindById_Test {

        @Test
        void testFindById_TaskConAllegato_Trovato() throws Exception {

            long idDaCercare = 10L;

            Date dataCreazione = Date.valueOf("2025-01-01");
            Date dataScadenza = Date.valueOf("2025-02-01");

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {

                // Setup connessione e statement
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                PreparedStatement psSelect = mock(PreparedStatement.class);
                when(connection.prepareStatement(contains("SELECT t.*, a.filename"))).thenReturn(psSelect);

                // Mock del ResultSet
                ResultSet rs = mock(ResultSet.class);
                when(psSelect.executeQuery()).thenReturn(rs);

                // --- SIMULAZIONE RISULTATI DEL DATABASE ---

                // rs.next() deve restituire true la prima volta (trovato) e false le successive
                when(rs.next()).thenReturn(true).thenReturn(false);

                // Mappatura colonne Task
                when(rs.getInt("id")).thenReturn((int) idDaCercare);
                when(rs.getString("titolo")).thenReturn("Task di Prova");
                when(rs.getDate("data_creazione")).thenReturn(dataCreazione);
                when(rs.getDate("data_scadenza")).thenReturn(dataScadenza);
                when(rs.getString("istruzioni")).thenReturn("Istruzioni test");

                // Importante: Restituire stringhe valide per i tuoi ENUM
                when(rs.getString("stato")).thenReturn("IN_ESECUZIONE");
                when(rs.getString("priorita")).thenReturn("ALTA");

                when(rs.getString("supervisore")).thenReturn("SupRossi");
                when(rs.getString("dipendente")).thenReturn("DipVerdi");

                // Mappatura colonne Allegato (Simuliamo che ESISTA)
                when(rs.getString("filename")).thenReturn("documento.pdf"); // != null
                when(rs.getString("filepath")).thenReturn("/var/docs/documento.pdf");
                when(rs.getString("contentType")).thenReturn("application/pdf");


                Task risultato = taskDao.findById(idDaCercare);


                assertNotNull(risultato, "Il task non dovrebbe essere null");
                assertEquals(idDaCercare, risultato.getId());
                assertEquals("Task di Prova", risultato.getTitolo());
                assertEquals(Tipi.stato.IN_ESECUZIONE, risultato.getStato()); // Verifica conversione Enum

                // Verifica Allegato
                assertNotNull(risultato.getAllegato(), "L'allegato dovrebbe essere presente");
                assertEquals("documento.pdf", risultato.getAllegato().getFilename());
                assertEquals(idDaCercare, risultato.getAllegato().getTaskId()); // Verifica collegamento ID

                // Verifica che il parametro ID sia stato settato nello statement
                verify(psSelect).setLong(1, idDaCercare);
            }
        }

        @Test
        void testFindById_TaskSenzaAllegato_Trovato() throws Exception {
            // 1. ARRANGE
            long idDaCercare = 20L;

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                PreparedStatement psSelect = mock(PreparedStatement.class);
                when(connection.prepareStatement(anyString())).thenReturn(psSelect);

                ResultSet rs = mock(ResultSet.class);
                when(psSelect.executeQuery()).thenReturn(rs);

                // Troviamo il record
                when(rs.next()).thenReturn(true).thenReturn(false);

                // Dati minimi del Task
                when(rs.getInt("id")).thenReturn((int) idDaCercare);
                when(rs.getString("stato")).thenReturn("DA_COMPLETARE");
                when(rs.getString("priorita")).thenReturn("BASSA");

                // --- IL CUORE DEL TEST: Simuliamo NULL per l'allegato ---
                when(rs.getString("filename")).thenReturn(null);
                // Nota: se filename è null, il tuo codice non chiama filepath/contentType,
                // quindi non serve mockarli (Mockito restituisce null di default comunque)

                // 2. ACT
                Task risultato = taskDao.findById(idDaCercare);

                // 3. ASSERT
                assertNotNull(risultato);
                assertEquals(idDaCercare, risultato.getId());

                // L'allegato deve essere null
                assertNull(risultato.getAllegato(), "L'allegato dovrebbe essere null in caso di LEFT JOIN vuoto");
            }
        }

        @Test
        void testFindById_NonTrovato() throws Exception {
            // 1. ARRANGE
            long idInesistente = 999L;

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                PreparedStatement psSelect = mock(PreparedStatement.class);
                when(connection.prepareStatement(anyString())).thenReturn(psSelect);

                ResultSet rs = mock(ResultSet.class);
                when(psSelect.executeQuery()).thenReturn(rs);

                // --- SIMULAZIONE RECORD NON TROVATO ---
                when(rs.next()).thenReturn(false); // Il cursore è vuoto

                // 2. ACT
                Task risultato = taskDao.findById(idInesistente);

                // 3. ASSERT
                assertNull(risultato, "Se l'ID non esiste, il metodo deve restituire null");
            }
        }

        @Test
        void testFindById_ErroreSQL() throws Exception {
            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                PreparedStatement psSelect = mock(PreparedStatement.class);
                when(connection.prepareStatement(anyString())).thenReturn(psSelect);

                // Simuliamo il crash della query
                when(psSelect.executeQuery()).thenThrow(new SQLException("Errore di connessione"));

                // ACT & ASSERT
                Exception ex = assertThrows(Exception.class, () -> taskDao.findById(1L));
                assertTrue(ex.getMessage().contains("Errore durante il recupero"));
            }
        }


        @Test
        @DisplayName("Dovrebbe lanciare IllegalArgumentException se l'ID non è valido (<= 0)")
        void testFindById_IdInvalido() {
            // Non serve mockare nulla perché il controllo avviene prima del DB
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                taskDao.findById(0L); // O un numero negativo -1L
            });

            assertEquals("L'ID deve essere un numero positivo", ex.getMessage());
        }

    }

    @Nested
    @DisplayName("Metodo: findByUtente")
    class FindByUtenteTest {

        @Test
        void testFindByUtente_UtenteNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                taskDao.findByUtente(null);
            });
            assertEquals("Utente non valido", ex.getMessage());
        }

        @Test
        void testFindByUtente_RuoloGestore_NonPermesso() {
            // ARRANGE
            Utente gestore = new Utente();
            gestore.setRuolo(Tipi.ruolo.GESTORE); // Assumo che il setter esista

            // ACT & ASSERT
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                taskDao.findByUtente(gestore);
            });
            assertEquals("Privilegi utente non validi", ex.getMessage());
        }

        @Test
        void testFindByUtente_RuoloDipendente_QueryCorretta() throws Exception {
            // 1. ARRANGE
            Utente dipendente = new Utente();
            dipendente.setMatricola("DIP001");
            dipendente.setRuolo(Tipi.ruolo.DIPENDENTE);

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // --- VERIFICA QUERY DINAMICA ---
                // Qui è fondamentale controllare che la query contenga "dipendente ="
                PreparedStatement psSelect = mock(PreparedStatement.class);
                when(connection.prepareStatement(contains("WHERE dipendente = ?"))).thenReturn(psSelect);

                ResultSet rs = mock(ResultSet.class);
                when(psSelect.executeQuery()).thenReturn(rs);

                // Simuliamo 1 task trovato
                when(rs.next()).thenReturn(true).thenReturn(false);
                configuraMockResultSet(rs); // Helper method per pulizia codice

                // 2. ACT
                List<Task> tasks = taskDao.findByUtente(dipendente);

                // 3. ASSERT
                assertFalse(tasks.isEmpty());
                assertEquals(1, tasks.size());

                // Verifica che sia stata passata la matricola corretta
                verify(psSelect).setString(1, "DIP001");
                // Verifica che sia stata scelta la query giusta (implicitamente verificato dal 'when' sopra, ma rafforziamo)
                verify(connection).prepareStatement(contains("dipendente = ?"));
            }
        }

        @Test
        void testFindByUtente_RuoloSupervisore_QueryCorretta() throws Exception {
            // 1. ARRANGE
            Utente supervisore = new Utente();
            supervisore.setMatricola("SUP001");
            supervisore.setRuolo(Tipi.ruolo.SUPERVISORE); // O qualsiasi ruolo != DIPENDENTE/GESTORE

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // --- VERIFICA QUERY DINAMICA ---
                // Qui verifichiamo che la query cambi e cerchi "supervisore ="
                PreparedStatement psSelect = mock(PreparedStatement.class);
                when(connection.prepareStatement(contains("WHERE supervisore = ?"))).thenReturn(psSelect);

                ResultSet rs = mock(ResultSet.class);
                when(psSelect.executeQuery()).thenReturn(rs);

                // Simuliamo 2 task trovati
                when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
                configuraMockResultSet(rs);

                // 2. ACT
                List<Task> tasks = taskDao.findByUtente(supervisore);

                // 3. ASSERT
                assertEquals(2, tasks.size());

                verify(psSelect).setString(1, "SUP001");
                verify(connection).prepareStatement(contains("supervisore = ?"));
            }
        }

        @Test
        void testFindByUtente_NessunTaskTrovato() throws Exception {
            Utente dipendente = new Utente();
            dipendente.setMatricola("DIP001");
            dipendente.setRuolo(Tipi.ruolo.DIPENDENTE);

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                PreparedStatement psSelect = mock(PreparedStatement.class);
                when(connection.prepareStatement(anyString())).thenReturn(psSelect);
                ResultSet rs = mock(ResultSet.class);
                when(psSelect.executeQuery()).thenReturn(rs);

                // ResultSet vuoto
                when(rs.next()).thenReturn(false);

                // ACT
                List<Task> tasks = taskDao.findByUtente(dipendente);

                // ASSERT
                assertNotNull(tasks); // Mai restituire null per le liste!
                assertTrue(tasks.isEmpty());
            }
        }

        // Metodo helper locale per non ripetere i when(rs.get...)
        private void configuraMockResultSet(ResultSet rs) throws SQLException {
            when(rs.getInt("id")).thenReturn(1);
            when(rs.getString("titolo")).thenReturn("Task Test");
            when(rs.getDate("dataDiCreazione")).thenReturn(Date.valueOf("2025-01-01"));
            when(rs.getDate("dataDiScadenza")).thenReturn(Date.valueOf("2025-02-01"));
            when(rs.getString("istruzioni")).thenReturn("Info");
            when(rs.getString("stato")).thenReturn("IN_ESECUZIONE");
            when(rs.getString("priorita")).thenReturn("ALTA");
            when(rs.getString("supervisore")).thenReturn("SUP001");
            when(rs.getString("dipendente")).thenReturn("DIP001");
        }
    }


    @Nested
    @DisplayName("Metodo: updateStatus")
    class UpdateStatusTest {

        @Test
        @DisplayName("Dovrebbe aggiornare lo stato se i parametri sono validi")
        void testUpdateStatus_Successo() throws Exception {
            // 1. ARRANGE
            long idTask = 50L;
            Tipi.stato nuovoStato = Tipi.stato.COMPLETATO; // O un altro valore valido del tuo Enum

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Prepariamo lo statement di update
                // Nota: Non serve RETURN_GENERATED_KEYS per un UPDATE
                when(connection.prepareStatement(contains("UPDATE task SET"))).thenReturn(psTask);

                // 2. ACT
                taskDao.updateStatus(idTask, nuovoStato);

                // 3. ASSERT
                // Verifica che i parametri siano stati settati correttamente
                // Il codice usa nuovoStato.toString(), quindi ci aspettiamo la stringa dell'enum
                verify(psTask).setString(1, nuovoStato.toString());
                verify(psTask).setLong(2, idTask);

                // Verifica l'esecuzione
                verify(psTask).executeUpdate();

                // Verifica chiusura risorse
                verify(psTask).close();
                verify(connection).close();
            }
        }

        @Test
        @DisplayName("Dovrebbe lanciare IllegalArgumentException se l'ID è <= 0")
        void testUpdateStatus_IdInvalido() {
            // Testiamo ID = 0
            IllegalArgumentException exZero = assertThrows(IllegalArgumentException.class, () -> {
                taskDao.updateStatus(0, Tipi.stato.IN_ESECUZIONE);
            });
            assertEquals("Parametri non validi", exZero.getMessage());

            // Testiamo ID negativo
            assertThrows(IllegalArgumentException.class, () -> {
                taskDao.updateStatus(-5, Tipi.stato.IN_ESECUZIONE);
            });
        }

        @Test
        @DisplayName("Dovrebbe lanciare IllegalArgumentException se il nuovoStato è NULL")
        void testUpdateStatus_StatoNull() {
            // Anche con ID valido, se lo stato è null deve fallire
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                taskDao.updateStatus(10, null);
            });
            assertEquals("Parametri non validi", ex.getMessage());
        }

        @Test
        @DisplayName("Dovrebbe lanciare Exception in caso di errore SQL")
        void testUpdateStatus_ErroreSQL() throws Exception {
            // 1. ARRANGE
            long idTask = 50L;
            Tipi.stato nuovoStato = Tipi.stato.IN_ESECUZIONE;

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(psTask);

                // Simuliamo il fallimento dell'update
                doThrow(new SQLException("Errore DB")).when(psTask).executeUpdate();

                // 2. ACT & ASSERT
                Exception ex = assertThrows(Exception.class, () -> {
                    taskDao.updateStatus(idTask, nuovoStato);
                });

                // Verifica che il messaggio contenga l'ID problematico come da tuo codice
                assertTrue(ex.getMessage().contains("Errore durante il update per task: " + idTask));
            }
        }
        @Test
        void testUpdateStatus_IdNonEsistente_LanciaErrore() throws Exception {
            long idInesistente = 9999L;
            Tipi.stato stato = Tipi.stato.COMPLETATO;

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(psTask);

                // Simuliamo il caso "0 righe modificate"
                when(psTask.executeUpdate()).thenReturn(0);

                // ORA IL TEST È CORRETTO: Ci aspettiamo un errore!
                Exception ex = assertThrows(Exception.class, () -> {
                    taskDao.updateStatus(idInesistente, stato);
                });

                // Verifichiamo che il messaggio sia quello giusto (opzionale)
                // Nota: controlliamo il messaggio dell'eccezione wrapper o della causa
                assertTrue(ex.getMessage().contains("Errore durante l'update") ||
                        ex.getCause().getMessage().contains("Nessun task trovato"));
            }

    }
    }


    @Nested
    @DisplayName("Metodo: update")
    class UpdateTest {

        @Test
        void testUpdate_Successo() throws Exception {
            // 1. ARRANGE
            // Creiamo un task con dati completi per verificare il mapping
            Task task = creaTaskDiEsempio();
            task.setId(100L); // ID esistente
            task.setTitolo("Titolo Modificato");
            task.setIstruzioni("Nuove Istruzioni");
            task.setStato(Tipi.stato.COMPLETATO);
            task.setPriorita(Tipi.priorita.BASSA);

            // Nota: nel codice usi .name() per gli Enum, che restituisce la stringa esatta (es. "COMPLETATO")

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Mock del PreparedStatement
                when(connection.prepareStatement(contains("UPDATE task SET"))).thenReturn(psTask);

                // Simuliamo che l'update vada a buon fine (1 riga modificata)
                when(psTask.executeUpdate()).thenReturn(1);

                // 2. ACT
                taskDao.update(task);

                // 3. ASSERT & VERIFY
                // Verifichiamo che i setter dello statement siano chiamati nell'ordine giusto (1-9)
                verify(psTask).setString(1, task.getTitolo());
                verify(psTask).setDate(2, task.getDataCreazione());
                verify(psTask).setDate(3, task.getDataDiScadenza());
                verify(psTask).setString(4, task.getIstruzioni());

                // Verifica Enum convertito con .name()
                verify(psTask).setString(5, "COMPLETATO");

                verify(psTask).setString(6, task.getSupervisore());
                verify(psTask).setString(7, task.getDipendente());

                verify(psTask).setString(8, "BASSA");

                // Verifica ID nella clausola WHERE (parametro 9)
                verify(psTask).setLong(9, 100L);

                verify(psTask).executeUpdate();
                verify(psTask).close();
                verify(connection).close();
            }
        }

        @Test
        void testUpdate_ErroreSQL() throws Exception {
            Task task = creaTaskDiEsempio();
            task.setId(100L);

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(psTask);

                // Simuliamo fallimento
                doThrow(new SQLException("Errore update")).when(psTask).executeUpdate();

                // ACT & ASSERT
                Exception ex = assertThrows(Exception.class, () -> taskDao.update(task));
                assertTrue(ex.getMessage().contains("Errore durante l'aggiornamento"));
            }
        }

        @Test
        void testUpdate_IdInesistente_NessunErrore() throws Exception {
            // Questo test documenta il comportamento attuale: se l'ID non c'è, non succede nulla.
            Task task = creaTaskDiEsempio();
            task.setId(9999L); // ID inesistente

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(psTask);

                // Il DB dice: "Ho modificato 0 righe"
                when(psTask.executeUpdate()).thenReturn(0);

                // ACT
                // Attualmente questo NON lancia eccezione, anche se dovrebbe secondo la logica
                taskDao.update(task);

                // ASSERT
                verify(psTask).executeUpdate();
            }
        }

        @Test
        void testUpdate_TaskNull() {
            // 1. ARRANGE
            // Non serve configurare Mockito o DataSourceFactory perché il controllo
            // avviene PRIMA di tentare la connessione al database.

            // 2. ACT & ASSERT
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                taskDao.update(null);
            });

            // 3. VERIFY MESSAGE (Opzionale ma consigliato)
            assertEquals("Task non può essere null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Metodo: delete")
    class DeleteTest {

        @Test
        void testDelete_Successo() throws Exception {
            // 1. ARRANGE
            long idDaCancellare = 50L;

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Mock del PreparedStatement
                when(connection.prepareStatement(contains("DELETE FROM task"))).thenReturn(psTask);

                // Simuliamo che il DB cancelli 1 riga
                when(psTask.executeUpdate()).thenReturn(1);

                // 2. ACT
                taskDao.delete(idDaCancellare);

                // 3. ASSERT
                // Verifica che l'ID sia stato settato correttamente
                verify(psTask).setLong(1, idDaCancellare);

                // Verifica esecuzione
                verify(psTask).executeUpdate();

                // Verifica chiusura risorse (garantita dal try-with-resources nel codice)
                verify(psTask).close();
                verify(connection).close();
            }
        }

        @Test
        void testDelete_IdInvalido() {
            // Caso 1: ID uguale a 0
            IllegalArgumentException exZero = assertThrows(IllegalArgumentException.class, () -> {
                taskDao.delete(0L);
            });
            assertEquals("Id non può essere negativo o uguale a 0", exZero.getMessage());

            // Caso 2: ID negativo
            assertThrows(IllegalArgumentException.class, () -> {
                taskDao.delete(-1L);
            });
        }

        @Test
        void testDelete_ErroreSQL() throws Exception {
            long idDaCancellare = 50L;

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(psTask);

                // Simuliamo errore del DB (es. violazione foreign key o connessione persa)
                doThrow(new SQLException("Errore FK constraint")).when(psTask).executeUpdate();

                // ACT & ASSERT
                Exception ex = assertThrows(Exception.class, () -> {
                    taskDao.delete(idDaCancellare);
                });

                assertTrue(ex.getMessage().contains("Errore durante l'eliminazione"));
            }
        }

        @Test
        void testDelete_IdInesistente_NessunErrore() throws Exception {
            // Come per l'update, questo test passa con il tuo codice attuale,
            // ma evidenzia che il sistema non avvisa se non ha cancellato nulla.
            long idFantasma = 9999L;

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(psTask);

                // Il DB dice: "Ho cancellato 0 righe"
                when(psTask.executeUpdate()).thenReturn(0);

                // ACT
                taskDao.delete(idFantasma);

                // ASSERT
                verify(psTask).executeUpdate();
            }
        }
    }



}

