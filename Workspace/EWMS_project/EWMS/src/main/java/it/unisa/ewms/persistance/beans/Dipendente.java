package it.unisa.ewms.persistance.beans;

import java.sql.Date;

public class Dipendente extends  Utente {
    private String supervisoreMat;

    public Dipendente() {}

    public Dipendente(Tipi.ruolo ruolo, String email, String matricola, String nome, String cognome, Date dataNasc, boolean newUtente){
        super(ruolo,email,matricola,nome,cognome,dataNasc,newUtente);
    }

    public void setSupervisoreMat(String supervisoreMat) {
        this.supervisoreMat = supervisoreMat;
    }

    public String getSupervisoreMat() {
        return supervisoreMat;
    }
}
