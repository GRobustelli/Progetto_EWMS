package it.unisa.ewms.model.beans;

import java.sql.Date;

public class Dipendente extends  Utente {
    private Informazioni supervisoreInfo;

    public Dipendente() {}

    public Dipendente(Tipi.ruolo ruolo, String email, int matricola, String nome, String cognome, Date dataNasc, boolean newUtente){
        super(ruolo,email,matricola,nome,cognome,dataNasc,newUtente);
        supervisoreInfo = null;
    }

    public void setSupervisoreInfo(Informazioni supervisoreInfo) {
        this.supervisoreInfo = supervisoreInfo;
    }

    public Informazioni getSupervisoreInfo() {
        return supervisoreInfo;
    }
}
