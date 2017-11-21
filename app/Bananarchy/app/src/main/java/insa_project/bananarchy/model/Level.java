package insa_project.bananarchy.model;

import java.util.ArrayList;

/**
 * Created by pierre on 13/11/17.
 */

public class Level {

    private String libelle;
    private long id;
    private ArrayList<Group> groups;

    public Level(String l)
    {
        this.libelle = l;
    }

    public Level(String l, long id)
    {
        this.libelle = l;
        this.id = id;
        this.groups = new ArrayList<>();
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

    public void addGroupToLevel(Group g){
        groups.add(g);
    }

    public ArrayList<Group> getGroups(){
        return groups;
    }

    public void addAllGroupsToLevel(ArrayList<Group> listGr){
        groups = listGr;
    }

}
