package it.unisa.ewms.persistance.interfaces;

import it.unisa.ewms.persistance.beans.Utente;

public interface IUtenteDAO {
    public void createUtente(Utente utente);
    public Utente findByMatricola(String matricola);
    public void  update(Utente utente);
    public void delete(Utente utente);
}
