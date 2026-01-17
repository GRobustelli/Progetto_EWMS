package it.unisa.ewms.persistance.beans;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Supervisore extends Utente{

    private List<String> dipendetiMat;

    public Supervisore() {
        dipendetiMat = new ArrayList<>();
    }

    public Supervisore(Tipi.ruolo ruolo, String email, String matricola, String nome, String cognome, Date dataNasc, boolean newUtente) {
        super(ruolo,email,matricola,nome,cognome,dataNasc,newUtente);
    }

    public void addDipendetiMat(List<String> dipendetiMat) {
        this.dipendetiMat = dipendetiMat;
    }

    public void addDipendenteMat(String dipendenteMat) {
        this.dipendetiMat.add(dipendenteMat);
    }

    public List<String> getDipendetiMat() {
        return dipendetiMat;
    }
}
