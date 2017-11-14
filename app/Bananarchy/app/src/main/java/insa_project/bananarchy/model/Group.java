package insa_project.bananarchy.model;

import java.util.ArrayList;

/**
 * Created by pierre on 13/11/17.
 */

public class Group {
    private String libelle;
    private ArrayList<String> ressource;
    private boolean checked;
    private long id;

    public Group(String l, ArrayList<String> r)
    {
        this.libelle = l;
        this.ressource = r;
        this.checked = false;
    }

    public Group(String l, ArrayList<String> r, boolean checked)
    {
        this.libelle = l;
        this.ressource = r;
        this.checked = checked;
    }

    public Group(String l, ArrayList<String> r, boolean checked, long id)
    {
        this.libelle = l;
        this.ressource = r;
        this.checked = checked;
        this.id = id;
    }


    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public ArrayList<String> getRessource() {
        return ressource;
    }

    public void setRessource(ArrayList<String> ressource) {
        this.ressource = ressource;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
