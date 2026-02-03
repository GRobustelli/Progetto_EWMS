package it.unisa.ewms.model.beans;

public class Informazioni {
    private int matricola;
    private String nome;
    private String cognome;

    public Informazioni() {
    }

    public Informazioni(int matricola, String nome, String cognome) {
        this.matricola = matricola;
        this.nome = nome;
        this.cognome = cognome;
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

    public int getMatricola() {
        return matricola;
    }

    public void setMatricola(int matricola) {
        this.matricola = matricola;
    }

    public String getMatricolaFormattata(){
        return "2002-" + String.format("%08d", this.matricola);
    }


}
