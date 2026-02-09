package it.unisa.ewms.model.beans;

import java.sql.Date;

public class Task {
    private long id;
    private String titolo;
    private Date dataCreazione;
    private Date dataDiScadenza;
    private String istruzioni;
    private Tipi.stato stato;
    private int supervisore;
    private int dipendente;
    private Allegato allegato;
    private Tipi.priorita priorita;

    public Task() {
    }

    public Task(long id, String titolo, Date dataDiScadenza, Date dataCreazione, String istruzioni, Tipi.stato stato, int supervisore, int dipendente, Tipi.priorita priorita) {
        this.id = id;
        this.titolo = titolo;
        this.dataDiScadenza = dataDiScadenza;
        this.dataCreazione = dataCreazione;
        this.istruzioni = istruzioni;
        this.stato = stato;
        this.supervisore = supervisore;
        this.dipendente = dipendente;
        this.priorita = priorita;
        allegato = null;
    }

    public Task(String titolo, Date dataCreazione, Date dataDiScadenza, String istruzioni, Tipi.stato stato, int supervisore, int dipendente, Tipi.priorita priorita) {
        this.titolo = titolo;
        this.dataCreazione = dataCreazione;
        this.dataDiScadenza = dataDiScadenza;
        this.istruzioni = istruzioni;
        this.stato = stato;
        this.supervisore = supervisore;
        this.dipendente = dipendente;
        this.priorita = priorita;
        allegato = null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDataCreazione() {
        return dataCreazione;
    }

    public void setDataCreazione(Date dataCreazione) {
        this.dataCreazione = dataCreazione;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public Date getDataDiScadenza() {
        return dataDiScadenza;
    }

    public void setDataDiScadenza(Date dataDiScadenza) {
        this.dataDiScadenza = dataDiScadenza;
    }

    public String getIstruzioni() {
        return istruzioni;
    }

    public void setIstruzioni(String istruzioni) {
        this.istruzioni = istruzioni;
    }

    public Tipi.stato getStato() {
        return stato;
    }

    public void setStato(Tipi.stato stato) {
        this.stato = stato;
    }

    public int getSupervisore() {
        return supervisore;
    }

    public void setSupervisore(int supervisore) {
        this.supervisore = supervisore;
    }

    public int getDipendente() {
        return dipendente;
    }

    public void setDipendente(int dipendente) {
        this.dipendente = dipendente;
    }

    public Allegato getAllegato() {
        return allegato;
    }

    public void setAllegato(Allegato allegato) {
        this.allegato = allegato;
    }

    public Tipi.priorita getPriorita() {
        return priorita;
    }

    public void setPriorita(Tipi.priorita priorita) {
        this.priorita = priorita;
    }

    public String getSupervisoreFormattato(){

        // %08d assicura che la parte variabile sia sempre di 8 cifre
        return "2002-" + String.format("%08d", this.supervisore);

    }

    public String getDipendenteFormattato(){

        // %08d assicura che la parte variabile sia sempre di 8 cifre
        return "2002-" + String.format("%08d", this.dipendente);

    }
}
