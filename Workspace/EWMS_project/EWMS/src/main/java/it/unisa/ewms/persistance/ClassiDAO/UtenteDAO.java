package it.unisa.ewms.persistance.ClassiDAO;

import it.unisa.ewms.persistance.DataSourceFactory;
import it.unisa.ewms.persistance.beans.*;
import it.unisa.ewms.persistance.eccezioni.EmailGiaPresenteException;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtenteDAO implements IUtenteDAO {

    private void insertUtenteGenerico(Connection conn, Utente utente, String password) throws SQLException, EmailGiaPresenteException {
        if (utente == null) {
            throw new IllegalArgumentException();
        }

        String sql = "INSERT INTO utente (matricola, email, nome, cognome, dataDiNascita, hashPassword, ruolo) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try(PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setString(1,utente.getMatricola());
            ps.setString(2,utente.getEmail());
            ps.setString(3,utente.getNome());
            ps.setString(4,utente.getCognome());
            ps.setDate(5,utente.getDataNasc());
            ps.setString(6, password);
            ps.setString(7,utente.getRuolo().toString());

            if (ps.execute()){
                System.out.println("Inserimento nuovo utente riuscito");
            }
        } catch(SQLIntegrityConstraintViolationException icv){
            if (icv.getMessage().contains(utente.getEmail())){
                throw new EmailGiaPresenteException("L'email inserita è già presente nel db");
            }

        } catch (SQLException e) {
            throw new SQLException(e);
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

            throw new SQLException("Inserimento utente Generico non riuscito",e);
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
                ps.setString(1, dipendente.getMatricola());
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
                ps.setString(1, supervisore.getMatricola());
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
    public Utente findByMatricola(String matricola) {
        if (matricola == null) {
            throw new IllegalArgumentException();
        }

        String sql = "SELECT * FROM utente WHERE matricola = ?";

        Utente utente = new Utente();

        try(Connection conn = DataSourceFactory.getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,matricola);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                utente.setMatricola(rs.getString("matricola"));
                utente.setEmail(rs.getString("email"));
                utente.setNome(rs.getString("nome"));
                utente.setCognome(rs.getString("cognome"));
                utente.setDataNasc(rs.getDate("dataDiNascita"));
                utente.setRuolo(Tipi.ruolo.valueOf(rs.getString("ruolo")));
                utente.setNewUtente(rs.getBoolean("newUtente"));



            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return utente;
    }

    @Override
    public List<Informazioni> getAllDipendentiInfo(String matricola) throws SQLException {
        List<Informazioni> dipendentiInfo = new ArrayList<>();
        if (matricola == null) {
            throw new IllegalArgumentException();
        }
        String sql = "SELECT u.matricola,u.nome,u.cognome FROM utente u JOIN dipendente d ON u.matricola = d.matricola WHERE d.supMatricola = ?";

        try(Connection conn = DataSourceFactory.getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, matricola);
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
    public Informazioni getSupervisoreInfo(String matricola) throws SQLException {
        String sqlSupInfo = "SELECT u.matricola,u.nome,u.cognome FROM utente u JOIN dipendente d ON u.matricola = d.matricola WHERE d.matricola = ?";

        Informazioni informazioni = null;
        try(Connection conn = DataSourceFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlSupInfo)){
            ps.setString(1, matricola);
            try(ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                     informazioni = new Informazioni(rs.getString("matricola"),rs.getString("nome"),rs.getString("cognome"));
                }
            }
        }catch (SQLException ex){
            ex.printStackTrace();
        }

        return informazioni;
    }

    @Override
    public void update(Utente utente) {


    }

    @Override
    public void delete(Utente utente) {

    }
}
