package it.unisa.ewms.model.beans;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Supervisore extends Utente{

    private List<Informazioni> dipendetiInfo;

    public Supervisore() {
        dipendetiInfo = new ArrayList<>();
    }

    public Supervisore(Tipi.ruolo ruolo, String email, int matricola, String nome, String cognome, Date dataNasc, boolean newUtente) {
        super(ruolo,email,matricola,nome,cognome,dataNasc,newUtente);
        dipendetiInfo = new ArrayList<>();

    }

    public Supervisore(Tipi.ruolo ruolo, String email, String nome, String cognome, Date dataNasc, boolean newUtente) {
        super(ruolo,email,nome,cognome,dataNasc,newUtente);
        dipendetiInfo = new ArrayList<>();
    }

    public void addDipendetiInfo(List<Informazioni> dipendetiInfo) {
        this.dipendetiInfo = dipendetiInfo;
    }

    public void addDipendenteInfo(Informazioni dipendenteInfo) {
        this.dipendetiInfo.add(dipendenteInfo);
    }

    public List<Informazioni> getDipendetiInfo() {
        return dipendetiInfo;
    }
}
