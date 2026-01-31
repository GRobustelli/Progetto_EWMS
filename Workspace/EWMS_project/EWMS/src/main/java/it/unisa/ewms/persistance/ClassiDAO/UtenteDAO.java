package it.unisa.ewms.persistance.ClassiDAO;

import it.unisa.ewms.model.beans.*;
import it.unisa.ewms.persistance.DataSourceFactory;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtenteDAO implements IUtenteDAO {

    private void insertUtenteGenerico(Connection conn, Utente utente, String password) throws SQLException, EmailGiaPresenteException {
        if (utente == null || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Dati inseriti non possono essere null");
        }


        String sql = "INSERT INTO utente (email, nome, cognome, dataDiNascita, hashPassword, ruolo) VALUES (?, ?, ?, ?, ?, ?) ";

        if (conn != null) {
        try(PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1,utente.getEmail());
            ps.setString(2,utente.getNome());
            ps.setString(3,utente.getCognome());
            ps.setDate(4,utente.getDataNasc());
            ps.setString(5, password);
            ps.setString(6,utente.getRuolo().toString());

            if (ps.execute()){
                System.out.println("Inserimento nuovo utente riuscito");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Prendo il primo campo generato (l'ID)
                    int idGenerato = generatedKeys.getInt(1);

                    // AGGIORNO L'OGGETTO JAVA
                    // Fondamentale: ora l'oggetto 'utente' ha la matricola vera
                    utente.setMatricola(idGenerato);
                } else {
                    throw new SQLException("Creazione utente fallita, nessun ID ottenuto.");
                }
            }

        } catch(SQLIntegrityConstraintViolationException icv){
            if (icv.getMessage().contains(utente.getEmail())){
                throw new EmailGiaPresenteException("L'email inserita è già presente nel db");
            }
            else {
                throw new SQLException(icv.getMessage());
            }

        } catch (SQLException e) {
            throw new SQLException(e);
        }
    } else  {
        throw new SQLException("Connessione non riuscita");
    }

    }

    @Override
    public void createUtente(Utente utente, String password) throws SQLException, EmailGiaPresenteException {
        try(Connection conn = DataSourceFactory.getConnection()){
            insertUtenteGenerico(conn, utente, password);
        } catch (Exception e) {
            if (e instanceof EmailGiaPresenteException) {
                throw (EmailGiaPresenteException) e;
            }

            if (e instanceof IllegalArgumentException ) {
                throw (IllegalArgumentException) e;
            }

            throw new SQLException("Inserimento utente Generico non riuscito\n",e);
        }
    }

    @Override
    public void createDipendente(Dipendente dipendente, String password) throws SQLException, EmailGiaPresenteException {
        String sqlDipendente = "INSERT INTO dipendente (matricola, supMatricola) VALUES (?,?)";

        Connection con = null;
        try {
            con = DataSourceFactory.getConnection();
            con.setAutoCommit(false); // INIZIO TRANSAZIONE

            // A. Inserisco nella tabella padre (Utente) usando la stessa connessione
            insertUtenteGenerico(con, dipendente, password);

            // B. Inserisco nella tabella specifica (Dipendente)
            try (PreparedStatement ps = con.prepareStatement(sqlDipendente)) {
                ps.setInt(1, dipendente.getMatricola());
                ps.setString(2, dipendente.getSupervisoreInfo().getMatricola());
                ps.executeUpdate();
            }

            con.commit(); // COMMIT: Tutto salvato

        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                    System.out.println("Errore creazione dipendente per errore:" + e.getMessage());// ROLLBACK: Annulla anche l'insert in Utente se fallisce Dipendente
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            if (e instanceof EmailGiaPresenteException) {
                throw (EmailGiaPresenteException) e;
            }
            throw new SQLException("Errore creazione dipendente", e);
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    @Override
    public void createSupervisore(Supervisore supervisore, String password) throws SQLException, EmailGiaPresenteException {
        String sqlSupervisore = "INSERT INTO supervisore (matricola) values (?) ";
        Connection con = null;
        try {
            con = DataSourceFactory.getConnection();
            con.setAutoCommit(false); // INIZIO TRANSAZIONE

            // A. Tabella Padre
            insertUtenteGenerico(con, supervisore, password);

            // B. Tabella Figlia
            try (PreparedStatement ps = con.prepareStatement(sqlSupervisore)) {
                ps.setInt(1, supervisore.getMatricola());
                ps.executeUpdate();
            }

            con.commit();

        } catch (Exception e) {
            if (con != null){
                try {
                    con.rollback();
                    System.out.println("Errore creazione supervisore per errore: " + e.getMessage());

                }catch (SQLException ex){
                    ex.printStackTrace();
                }
            }
            if (e instanceof EmailGiaPresenteException) {
                throw (EmailGiaPresenteException) e;
            }

            throw new SQLException("Errore creazione supervisore", e);
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    @Override
    public Utente findByMatricola(int matricola) throws SQLException {
        if (matricola <= 0) {
            throw new IllegalArgumentException("Matricola non esistente");
        }

        String sql = "SELECT * FROM utente WHERE matricola = ?";

        Utente utente = null;

        try(Connection conn = DataSourceFactory.getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,matricola);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                utente = new Utente();
                utente.setMatricola(rs.getInt("matricola"));
                utente.setEmail(rs.getString("email"));
                utente.setNome(rs.getString("nome"));
                utente.setCognome(rs.getString("cognome"));
                utente.setDataNasc(rs.getDate("dataDiNascita"));
                utente.setRuolo(Tipi.ruolo.valueOf(rs.getString("ruolo")));
                utente.setNewUtente(rs.getBoolean("newUtente"));



            }
        } catch (SQLException e) {
            throw (SQLException) e;
        }

        return utente;
    }

    //Questo metodo viene chiamato dopo aver fatto il check con recuperaPassword, quindi siamo sicuri della presenza
    //dell'email
    public Utente findByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("L'email non può essere nulla o vuota");
        }

        String sql = "SELECT * FROM utente WHERE email = ?";

        Utente utente = null;

        try (Connection conn = DataSourceFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1. Leggo il ruolo per decidere quale sottoclasse istanziare
                    // (Fondamentale per far funzionare instanceof e casting successivi)
                    String ruoloStr = rs.getString("ruolo");
                    Tipi.ruolo ruolo = Tipi.ruolo.valueOf(ruoloStr);

                    if (ruolo == Tipi.ruolo.DIPENDENTE) {
                        utente = new Dipendente();
                    } else if (ruolo == Tipi.ruolo.SUPERVISORE) {
                        utente = new Supervisore();
                    } else {
                        utente = new Utente();
                    }

                    // 2. Popolo i dati comuni presenti nella tabella Utente
                    utente.setMatricola(rs.getInt("matricola"));
                    utente.setEmail(rs.getString("email"));
                    utente.setNome(rs.getString("nome"));
                    utente.setCognome(rs.getString("cognome"));
                    utente.setDataNasc(rs.getDate("dataDiNascita"));
                    utente.setRuolo(ruolo);
                    utente.setNewUtente(rs.getBoolean("newUtente"));

                    // Nota: hashPassword non viene settato per sicurezza
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore ricerca utente per email", e);
        }

        return utente;
    }

    //Questo metodo se non trova supervisore con la matricola specificata ritorna una lista vuota
    @Override
    public List<Informazioni> getAllDipendentiInfo(int matricola) throws SQLException {
        List<Informazioni> dipendentiInfo = new ArrayList<>();
        if (matricola < 0) {
            throw new IllegalArgumentException("La matricola non esistente");
        }
        String sql = "SELECT u.matricola,u.nome,u.cognome FROM utente u JOIN dipendente d ON u.matricola = d.matricola WHERE d.supMatricola = ?";

        try(Connection conn = DataSourceFactory.getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, matricola);
            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()) {
                    Informazioni informazioni = new Informazioni(rs.getString("matricola"),rs.getString("nome"),rs.getString("cognome"));
                    dipendentiInfo.add(informazioni);
                }
            }
        }



        return dipendentiInfo;
    }

    @Override
    public Informazioni getSupervisoreInfo(int matricola) throws SQLException {
        if (matricola <= 0) {
            throw new IllegalArgumentException("La matricola non può essere nulla");
        }

        String sqlSupInfo = "SELECT u.matricola,u.nome,u.cognome FROM utente u JOIN dipendente d ON u.matricola = d.supMatricola WHERE d.matricola = ?";

        Informazioni informazioni = null;
        try(Connection conn = DataSourceFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlSupInfo)){
            ps.setInt(1, matricola);
            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                     informazioni = new Informazioni(rs.getString("matricola"),rs.getString("nome"),rs.getString("cognome"));
                }
            }
        }catch (SQLException ex){
            throw ex;
        }

        return informazioni;
    }

    @Override
    public List<Informazioni> getAllSupervisori() throws SQLException {
        // Uso una JOIN per assicurarmi di prendere solo gli utenti
        // che sono effettivamente registrati nella tabella 'supervisore'.
        String sql = "SELECT u.matricola, u.nome, u.cognome " +
                "FROM utente u " +
                "JOIN supervisore s ON u.matricola = s.matricola";

        List<Informazioni> lista = new ArrayList<>();

        try (Connection con = DataSourceFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // NOTA: Se hai cambiato la matricola in INT sul DB,
                // qui fai la conversione se il tuo DTO 'Informazioni' si aspetta Stringhe.
                String matricolaStr = String.valueOf(rs.getInt("matricola"));

                Informazioni info = new Informazioni(
                        matricolaStr,
                        rs.getString("nome"),
                        rs.getString("cognome")
                );
                lista.add(info);
            }
        }
        return lista;
    }

    @Override
    public List<Utente> getAllUtente() throws SQLException {

        // Interrogo solo la tabella padre. Molto più veloce e leggero.
        String sql = "SELECT * FROM utente";

        List<Utente> listaUtenti = new ArrayList<>();

        try (Connection con = DataSourceFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Utente utente = null;

                // 1. Leggo il ruolo per decidere quale "guscio" vuoto creare
                String ruoloStr = rs.getString("ruolo");
                Tipi.ruolo ruolo = Tipi.ruolo.valueOf(ruoloStr);

                if (ruolo == Tipi.ruolo.DIPENDENTE) {
                    utente = new Dipendente();
                    // Il campo supervisoreInfo rimane null per ora (Lazy Loading)
                } else if (ruolo == Tipi.ruolo.SUPERVISORE) {
                    utente = new Supervisore();
                } else {
                    utente = new Utente();
                }

                // 2. Popolo i dati comuni
                // Nota: Converto l'int del DB in String se il tuo Bean usa ancora String
                utente.setMatricola(rs.getInt("matricola"));
                utente.setNome(rs.getString("nome"));
                utente.setCognome(rs.getString("cognome"));
                utente.setEmail(rs.getString("email"));
                utente.setDataNasc(rs.getDate("dataDiNascita"));
                utente.setRuolo(ruolo);
                utente.setNewUtente(rs.getBoolean("newUtente"));

                // Non settiamo la password per sicurezza nelle liste pubbliche

                listaUtenti.add(utente);
            }
        }
        return listaUtenti;
    }
/*
    @Override
    public void updateAnagrafica(String nome, String cognome, Date dataDiNascita, int matricola) throws SQLException {

    }


    @Override
    public void updateRuolo(int matricola, Tipi.ruolo nuovoRuolo, int matricolaNuovoSupervisore) throws Exception {

        //Controlliamo le precondizioni
        if (matricola <= 0) {
            throw new IllegalArgumentException("La matricola non può essere nulla");
        }

        if (nuovoRuolo == null) {
            throw new IllegalArgumentException("Il ruolo non può essere nullo");
        }

        // Validazione preventiva: Se diventi Dipendente, DEVI avere un supervisore

        if (nuovoRuolo == Tipi.ruolo.DIPENDENTE && (matricolaNuovoSupervisore <= 0)) {
            throw new IllegalArgumentException("Impossibile passare al ruolo DIPENDENTE senza specificare un Supervisore.");
        }



        Connection con = null;

        // Query per leggere il ruolo attuale
        String selectRuoloSql = "SELECT ruolo FROM utente WHERE matricola = ?";
        // Query per aggiornare la tabella padre
        String updateUtenteSql = "UPDATE utente SET ruolo = ? WHERE matricola = ?";

        try {
            con = DataSourceFactory.getConnection();
            con.setAutoCommit(false); // 1. INIZIO TRANSAZIONE

            // 2. RECUPERO IL RUOLO ATTUALE
            Tipi.ruolo ruoloAttuale = null;
            try (PreparedStatement ps = con.prepareStatement(selectRuoloSql)) {
                ps.setString(1, matricola);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ruoloAttuale = Tipi.ruolo.valueOf(rs.getString("ruolo"));
                    } else {
                        throw new SQLException("Utente non trovato: " + matricola);
                    }
                }
            }

            // Se il ruolo è lo stesso, non faccio nulla ed esco
            if (ruoloAttuale == nuovoRuolo) {
                con.rollback(); // O commit, ininfluente qui
                return;
            }

            // 3. CANCELLO DALLA VECCHIA TABELLA SPECIFICA (Pulizia)
            // Se era Gestore, non faccio nulla perché non ha tabella specifica
            if (ruoloAttuale == Tipi.ruolo.DIPENDENTE) {
                deleteFromTable(con, "dipendente", matricola);
            } else if (ruoloAttuale == Tipi.ruolo.SUPERVISORE) {
                deleteFromTable(con, "supervisore", matricola);
            }

            // 4. AGGIORNO LA TABELLA PADRE (UTENTE)
            try (PreparedStatement ps = con.prepareStatement(updateUtenteSql)) {
                ps.setString(1, nuovoRuolo.name());
                ps.setInt(2, matricola);
                ps.executeUpdate();
            }

            // 5. INSERISCO NELLA NUOVA TABELLA SPECIFICA (Migrazione)
            // Se diventa Gestore, non faccio nulla
            if (nuovoRuolo == Tipi.ruolo.DIPENDENTE) {
                insertIntoDipendente(con, matricola, matricolaNuovoSupervisore);
            } else if (nuovoRuolo == Tipi.ruolo.SUPERVISORE) {
                insertIntoSupervisore(con, matricola);
            }

            con.commit(); // 6. CONFERMO TUTTO

        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback(); // ROLLBACK IN CASO DI ERRORE
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }
            throw new Exception("Errore durante il cambio ruolo per " + matricola, e);
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    //Non so se inserirli nei documenti
    private void deleteFromTable(Connection con, String tabella, int matricola){
        String sql  = "DELETE FROM "+ tabella +  "WHERE matricola = ?";

        try(PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, matricola);
            ps.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    //Non so se inserirli nei documenti
    private void insertIntoSupervisore(Connection con, int matricola) throws SQLException {
        String sql = "INSERT INTO supervisore (matricola) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, matricola);
            ps.executeUpdate();
        }
    }

    //Non so se inserirli nei documenti
    private void insertIntoDipendente(Connection con, int matricola, int supMatricola) throws SQLException {
        String sql = "INSERT INTO dipendente (matricola, supMatricola) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, matricola);
            ps.setInt(2, supMatricola);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateSupervisore(int matricola, int matricolaSup) throws Exception, SQLException {
        if (matricola <= 0 || matricolaSup <= 0) {
            throw new IllegalArgumentException("Dati inviati non validi");
        }

        String sql = "UPDATE dipendente SET supMatricola  = ? WHERE matricola = ? ";

        try(Connection con = DataSourceFactory.getConnection(); PreparedStatement ps = con.prepareStatement(sql)){

            ps.setInt(1, matricolaSup);
            ps.setInt(2, matricola);


            int result = ps.executeUpdate();

            if (result == 0){
                throw new SQLException("Dipendente non trovato: " + matricola);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il cambio di ruolo"+e);
        }

    }

    @Override
    public void updatePassword(int matricola, String hashPassword) throws SQLException {
        if (matricola <= 0 || hashPassword == null || hashPassword.isEmpty() ) {
            throw new IllegalArgumentException("Dati non validi");
        }

        String sql = "UPDATE utente SET hashPassword = ? WHERE matricola = ? ";
        try(Connection con = DataSourceFactory.getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, hashPassword);
            ps.setInt(2, matricola);
            int righe = ps.executeUpdate();

            if (righe == 0){
                throw new SQLException("Utente non trovato: " + matricola);
            }
        }catch (SQLException ex){
            throw new RuntimeException(ex);
        }
    }

*/


    @Override
    public void delete(Utente utente) throws SQLException {
        // Grazie al CASCADE sul DB, basta cancellare dalla tabella padre.
        if (utente == null) {
            throw new IllegalArgumentException();
        }
        String sql = "DELETE FROM utente WHERE matricola = ?";

        try (Connection con = DataSourceFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, utente.getMatricola());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                // Opzionale: lanciare eccezione se l'utente non esisteva
                throw new SQLException("Impossibile cancellare: nessun utente trovato con matricola " + utente.getMatricola());
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLIntegrityConstraintViolationException("Impossibile cancellare l'utente: è un Supervisore con dipendenti assegnati. Riassegna prima i dipendenti." + e);
        }

    }

    @Override
    public String recuperaPassword(String email) throws SQLException {

        String sql = "SELECT hashPassword FROM utente WHERE email = ?";

        try (Connection con = DataSourceFactory.getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("hashPassword");
            }
            else {
                return null;
            }
        }

    }


}
