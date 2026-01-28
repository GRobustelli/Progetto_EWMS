package it.unisa.ewms.model.beans;

import java.sql.Date;

public class Utente {
    private String email;
    private String matricola;
    private String nome;
    private String cognome;
    private Date dataNasc;
    private boolean newUtente;
    private Tipi.ruolo ruolo;

    public Utente() {
    }

    public Utente(Tipi.ruolo ruolo, String email, String matricola, String nome, String cognome, Date dataNasc, boolean newUtente) {
        this.ruolo = ruolo;
        this.email = email;
        this.matricola = matricola;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNasc = dataNasc;
        this.newUtente = newUtente;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatricola() {
        return matricola;
    }

    public void setMatricola(String matricola) {
        this.matricola = matricola;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public Date getDataNasc() {
        return dataNasc;
    }

    public void setDataNasc(Date dataNasc) {
        this.dataNasc = dataNasc;
    }

    public boolean isNewUtente() {
        return newUtente;
    }

    public void setNewUtente(boolean newUtente) {
        this.newUtente = newUtente;
    }

    public Tipi.ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Tipi.ruolo ruolo) {
        this.ruolo = ruolo;
    }

}
