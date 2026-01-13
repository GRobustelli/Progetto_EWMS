import it.unisa.ewms.persistance.ClassiDAO.UtenteDAO;
import it.unisa.ewms.persistance.DataSourceFactory;
import it.unisa.ewms.persistance.beans.Tipi;
import it.unisa.ewms.persistance.beans.Utente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UtenteDaoTest {

    private UtenteDAO utenteDao;

    // Oggetti Mock
    private Connection connectionMock;
    private PreparedStatement preparedStatementMock;
    private Utente utenteMock;

    @BeforeEach
    void setUp() {
        utenteDao = new UtenteDAO();
        connectionMock = mock(Connection.class);
        preparedStatementMock = mock(PreparedStatement.class);

        // Setup di un utente dummy per il test
        utenteMock = new Utente();
        utenteMock.setMatricola("1234511111111");
        utenteMock.setEmail("test@email.com");
        utenteMock.setNome("Mario");
        utenteMock.setCognome("Rossi");
        utenteMock.setDataNasc(Date.valueOf(LocalDate.of(1990, 1, 1)));
        utenteMock.setPassword("hashedPwd");
        utenteMock.setRuolo(Tipi.ruolo.DIPENDENTE);
    }

    @Test
    void testCreateUtente_Success() throws SQLException {
        // Utilizziamo try-with-resources per il MockedStatic perché deve essere chiuso dopo l'uso
        try (MockedStatic<DataSourceFactory> mockedFactory = mockStatic(DataSourceFactory.class)) {

            // 1. Definire il comportamento del mock statico e della connessione
            mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connectionMock);
            when(connectionMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);
            when(preparedStatementMock.execute()).thenReturn(true);

            // 2. Eseguire il metodo da testare
            utenteDao.createUtente(utenteMock);

            // 3. Verificare (Verify) che i metodi SQL siano stati chiamati con i parametri corretti
            verify(connectionMock).prepareStatement(contains("INSERT INTO utente"));

            verify(preparedStatementMock).setString(1, utenteMock.getMatricola());
            verify(preparedStatementMock).setString(2, utenteMock.getEmail());
            verify(preparedStatementMock).setString(3, utenteMock.getNome());
            verify(preparedStatementMock).setString(4, utenteMock.getCognome());
            verify(preparedStatementMock).setDate(5, utenteMock.getDataNasc());
            verify(preparedStatementMock).setString(6, utenteMock.getPassword());
            verify(preparedStatementMock).setString(7, utenteMock.getRuolo().toString());

            verify(preparedStatementMock).execute();
            // Verifica che la connessione venga chiusa (grazie al try-with-resources nel codice originale)
            verify(connectionMock).close();
        }
    }

    @Test
    void testCreateUtente_NullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            utenteDao.createUtente(null);
        });
    }

    @Test
    void testCreateUtente_SqlExceptionThrowsRuntimeException() throws SQLException {
        try (MockedStatic<DataSourceFactory> mockedFactory = mockStatic(DataSourceFactory.class)) {

            mockedFactory.when(DataSourceFactory::getConnection).thenReturn(connectionMock);
            when(connectionMock.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

            assertThrows(RuntimeException.class, () -> {
                utenteDao.createUtente(utenteMock);
            });

            // Verifica che la connessione venga chiusa anche in caso di errore
            verify(connectionMock).close();
        }
    }
}
