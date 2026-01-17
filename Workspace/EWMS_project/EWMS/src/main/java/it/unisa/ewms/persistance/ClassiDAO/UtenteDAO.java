package it.unisa.ewms.persistance.ClassiDAO;

import it.unisa.ewms.persistance.DataSourceFactory;
import it.unisa.ewms.persistance.beans.Tipi;
import it.unisa.ewms.persistance.beans.Utente;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteDAO implements IUtenteDAO {
    @Override
    public void createUtente(Utente utente, String password) throws SQLException {
        if (utente == null) {
            throw new IllegalArgumentException();
        }

        String sql = "INSERT INTO utente (matricola, email, nome, cognome, dataDiNascita, hashPassword, ruolo) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try(Connection con = DataSourceFactory.getConnection()){
            PreparedStatement ps = con.prepareStatement(sql);
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
        } catch (SQLException e) {
            throw new SQLException(e);
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



            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return utente;
    }

    @Override
    public void update(Utente utente) {

    }

    @Override
    public void delete(Utente utente) {

    }
}
