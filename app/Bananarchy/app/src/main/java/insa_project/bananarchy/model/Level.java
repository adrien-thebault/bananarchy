package insa_project.bananarchy.model;

/**
 * Created by pierre on 13/11/17.
 */

public class Level {

    private String libelle;
    private long id;

    public Level(String l)
    {
        this.libelle = l;
    }

    public Level(String l, long id)
    {
        this.libelle = l;
        this.id = id;
    }


    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
