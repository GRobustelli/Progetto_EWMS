package it.unisa.ewms.model.beans;

import java.sql.Date;

public class Task {
    private long id;
    private String titolo;
    private Date dataCreazione;
    private Date dataDiScadenza;
    private String istruzioni;
    private Tipi.stato stato;
    private String supervisore;
    private String dipendente;
    private Allegato allegato;
    private Tipi.priorita priorita;

    public Task() {
    }

    public Task(long id, String titolo, Date dataDiScadenza, Date dataCreazione, String istruzioni, Tipi.stato stato, String supervisore, String dipendente, Tipi.priorita priorita) {
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

    public String getSupervisore() {
        return supervisore;
    }

    public void setSupervisore(String supervisore) {
        this.supervisore = supervisore;
    }

    public String getDipendente() {
        return dipendente;
    }

    public void setDipendente(String dipendente) {
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
}
