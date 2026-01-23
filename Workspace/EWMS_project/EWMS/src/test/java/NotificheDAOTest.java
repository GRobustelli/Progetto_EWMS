import it.unisa.ewms.model.beans.Notifica;
import it.unisa.ewms.model.beans.Utente;
import it.unisa.ewms.persistance.ClassiDAO.NotificaDAO;
import it.unisa.ewms.persistance.DataSourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@DisplayName("Unit Tests per NotificaDAO")
public class NotificheDAOTest {

        private NotificaDAO notificaDao;

        // Mocks JDBC
        private Connection connection;
        private PreparedStatement ps;

        @BeforeEach
        void setUp() {
            notificaDao = new NotificaDAO();
            connection = mock(Connection.class);
            ps = mock(PreparedStatement.class);
        }

        @Nested
        @DisplayName("Metodo: create")
        class CreateTest {

            @Test
            void testCreate_Successo() {
                // 1. ARRANGE
                Notifica notifica = new Notifica();
                notifica.setTaskId(10);
                notifica.setMessaggio("Nuovo task assegnato");
                notifica.setSender("capo_progetto");
                notifica.setReceiver("sviluppatore_jr");
                // Nota: 'vista' e 'id' non vengono usati nell'insert

                try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                    mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                    // Configura il mock per restituire il PreparedStatement
                    // Nota: Il tuo codice NON usa RETURN_GENERATED_KEYS qui, quindi basta prepareStatement(sql)
                    when(connection.prepareStatement(contains("INSERT INTO notifica"))).thenReturn(ps);

                    // 2. ACT
                    notificaDao.create(notifica);

                    // 3. ASSERT & VERIFY
                    // Verifica parametri (attenzione all'ordine definito nel tuo codice)
                    // 1: task_id, 2: messaggio, 3: sender, 4: receiver
                    verify(ps).setInt(1, 10);
                    verify(ps).setString(2, "Nuovo task assegnato");
                    verify(ps).setString(3, "capo_progetto");
                    verify(ps).setString(4, "sviluppatore_jr");

                    verify(ps).executeUpdate();

                    // Verifica chiusura risorse
                    verify(ps).close();
                    verify(connection).close();
                } catch (SQLException e) {
                    fail("Non doveva lanciare SQLException: " + e.getMessage());
                }
            }

            @Test
            void testCreate_NotificaNull() {
                // Non serve mockare il DB perché il controllo è all'inizio
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                    notificaDao.create(null);
                });
                assertEquals("L'oggetto notifica non può essere null.", ex.getMessage());
            }

            @Test
            @DisplayName("Dovrebbe lanciare RuntimeException se c'è un errore SQL")
            void testCreate_ErroreSQL() throws SQLException {
                // 1. ARRANGE
                Notifica notifica = new Notifica();
                notifica.setTaskId(1);
                notifica.setMessaggio("Test");
                notifica.setSender("A");
                notifica.setReceiver("B");

                try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                    mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                    when(connection.prepareStatement(anyString())).thenReturn(ps);

                    // Simuliamo errore SQL
                    doThrow(new SQLException("Errore Insert")).when(ps).executeUpdate();

                    // 2. ACT & ASSERT
                    // Nota: Il tuo codice cattura SQLException e lancia RuntimeException
                    RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                        notificaDao.create(notifica);
                    });

                    // Verifica che l'eccezione originale sia incapsulata (opzionale ma utile)
                    assertNotNull(ex.getCause());
                    assertTrue(ex.getCause() instanceof SQLException);
                }
            }
        }

    @Nested
    @DisplayName("Metodo: getByUtente")
    class GetByUtenteTest {

        @Test
        @DisplayName("Dovrebbe restituire una lista di notifiche se presenti")
        void testGetByUtente_Successo_NotificheTrovate() throws SQLException {
            // 1. ARRANGE
            Utente utente = new Utente();
            utente.setMatricola("M12345");
            utente.setEmail("mario@test.it"); // Utile per il messaggio di errore, anche se la query usa la matricola

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);

                // Mock query
                when(connection.prepareStatement(contains("SELECT * FROM notifica WHERE receiver = ?")))
                        .thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);

                // --- SIMULAZIONE ResultSet CON 2 RIGHE ---
                // rs.next() restituirà true (riga 1), true (riga 2), false (fine)
                when(rs.next()).thenReturn(true, true, false);

                // Configurazione Riga 1
                when(rs.getInt("id")).thenReturn(1, 2); // Prima chiamata -> 1, Seconda -> 2
                when(rs.getInt("task_id")).thenReturn(100, 101);
                when(rs.getString("sender")).thenReturn("Capo", "Collega");
                when(rs.getString("receiver")).thenReturn("M12345", "M12345");
                when(rs.getBoolean("vista")).thenReturn(false, true);

                // 2. ACT
                List<Notifica> risultati = notificaDao.getByUtente(utente);

                // 3. ASSERT
                assertNotNull(risultati);
                assertEquals(2, risultati.size(), "Dovrebbe trovare 2 notifiche");

                // Verifiche sul primo oggetto
                Notifica n1 = risultati.get(0);
                assertEquals(1, n1.getId());
                assertEquals("Capo", n1.getSender());
                assertFalse(n1.isVista()); // Assuming getter is isVista() for boolean

                // Verifiche sul secondo oggetto
                Notifica n2 = risultati.get(1);
                assertEquals(2, n2.getId());
                assertTrue(n2.isVista());

                // VERIFICA FONDAMENTALE:
                // Controlliamo che abbia usato la MATRICOLA nel parametro della query, come da codice
                verify(ps).setString(1, "M12345");
            }
        }

        @Test
        @DisplayName("Dovrebbe restituire lista vuota se non ci sono notifiche")
        void testGetByUtente_NessunaNotifica() throws SQLException {
            // 1. ARRANGE
            Utente utente = new Utente();
            utente.setMatricola("M_VUOTO");

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                ResultSet rs = mock(ResultSet.class);
                when(ps.executeQuery()).thenReturn(rs);

                // ResultSet vuoto
                when(rs.next()).thenReturn(false);

                // 2. ACT
                List<Notifica> risultati = notificaDao.getByUtente(utente);

                // 3. ASSERT
                assertNotNull(risultati, "La lista non deve mai essere null");
                assertTrue(risultati.isEmpty(), "La lista dovrebbe essere vuota");
            }
        }

        @Test
        @DisplayName("Dovrebbe lanciare IllegalArgumentException se l'utente è null")
        void testGetByUtente_UtenteNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                notificaDao.getByUtente(null);
            });
            assertEquals("L'utente non può essere null.", ex.getMessage());
        }

        @Test
        @DisplayName("Dovrebbe lanciare SQLException in caso di errore DB")
        void testGetByUtente_ErroreSQL() throws SQLException {
            // 1. ARRANGE
            Utente utente = new Utente();
            utente.setMatricola("M123");
            utente.setEmail("test@email.com");

            try (MockedStatic<DataSourceFactory> mockedFactory = Mockito.mockStatic(DataSourceFactory.class)) {
                mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(ps);

                // Simuliamo crash della query
                doThrow(new SQLException("Errore Connessione")).when(ps).executeQuery();

                // 2. ACT & ASSERT
                SQLException ex = assertThrows(SQLException.class, () -> {
                    notificaDao.getByUtente(utente);
                });

                // Verifica che il messaggio contenga l'email (come da tuo blocco catch)
                assertTrue(ex.getMessage().contains("test@email.com"));
                assertTrue(ex.getMessage().contains("Errore durante il recupero"));
            }
        }
    }


}
