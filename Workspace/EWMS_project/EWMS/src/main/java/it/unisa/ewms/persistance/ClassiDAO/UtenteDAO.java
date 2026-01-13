package it.unisa.ewms.persistance.ClassiDAO;

import it.unisa.ewms.persistance.DataSourceFactory;
import it.unisa.ewms.persistance.beans.Utente;
import it.unisa.ewms.persistance.interfaces.IUtenteDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UtenteDAO implements IUtenteDAO {
    @Override
    public void createUtente(Utente utente) {
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
            ps.setString(6,utente.getPassword());
            ps.setString(7,utente.getRuolo().toString());

            if (ps.execute()){
                System.out.println("Inserimento nuovo utente riuscito");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Utente findByMatricola(String matricola) {
        return null;
    }

    @Override
    public void update(Utente utente) {

    }

    @Override
    public void delete(Utente utente) {

    }
}
