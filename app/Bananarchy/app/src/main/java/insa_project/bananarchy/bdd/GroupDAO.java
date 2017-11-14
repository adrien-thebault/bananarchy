package insa_project.bananarchy.bdd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.sql.Timestamp;
import java.util.ArrayList;
import insa_project.bananarchy.model.Group;

/**
 * Created by Pierre on 27/09/2016.
 */

public class GroupDAO extends DAOBase{

    public static final String TABLE_NAME = "groups";
    public static final String KEY = "id";
    public static final String LIB = "libelle";
    public static final String DATE = "date_record";
    public static final String CHECKED = "checked";
    public static final String IDNIVEAU = "level_id";

    public static final String TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("+ KEY +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    LIB+" TEXT , "+
                    DATE+" INTEGER, " +
                    CHECKED+" INTEGER, "+
                    IDNIVEAU+" INTEGER, "+
                    "FOREIGN KEY("+IDNIVEAU+") REFERENCES "+ LevelDAO.TABLE_NAME+"("+ LevelDAO.KEY+"));";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS "+TABLE_NAME+";";

    public GroupDAO(Context pContext) {
        super(pContext);
        if(mDb==null) {
            this.open();
        }
        mDb.execSQL(TABLE_CREATE);
    }


    /**
     *
     * @param g Groupe qui existe ou non en bdd
     * @return true si le groupe n'existe pas en BDD.
     */
    public long canAdd(Group g, long levelID)
    {
        if(mDb==null) {
            this.open();
        }
        mDb.execSQL(TABLE_CREATE);
        Cursor c = mDb.rawQuery("SELECT "+ GroupDAO.KEY+" as _id, "+ GroupDAO.LIB+" from "+ GroupDAO.TABLE_NAME+" WHERE "+ GroupDAO.LIB+" = ? AND "+ GroupDAO.IDNIVEAU+" = ? LIMIT 1",new String[]{g.getLibelle(),String.valueOf(levelID)});
        if(c.moveToFirst())
        {
            return c.getLong(0);
        }
        return -1;
    }

    /**
     * @param g Groupe à ajouter à la base
     */
    public long ajouter(Group g, long idNiveau) {
        if(mDb==null) {
            this.open();
        }
        long idGr = canAdd(g,idNiveau);
        if(idGr == -1)
        {
            mDb.execSQL(TABLE_CREATE);
            ContentValues value = new ContentValues();
            value.put(GroupDAO.LIB,g.getLibelle());
            java.util.Date date= new java.util.Date();
            Timestamp time = new Timestamp(date.getTime());
            value.put(GroupDAO.DATE,time.toString());
            value.put(GroupDAO.CHECKED,0);
            value.put(GroupDAO.IDNIVEAU,idNiveau);

            return mDb.insert(GroupDAO.TABLE_NAME,null,value);
        }

        return idGr;



    }

    public void updateGroup(Group g)
    {
        if(mDb==null) {
            this.open();
        }
        ContentValues value = new ContentValues();
        value.put(GroupDAO.KEY,g.getId());
        value.put(GroupDAO.LIB,g.getLibelle());
        java.util.Date date= new java.util.Date();
        Timestamp time = new Timestamp(date.getTime());
        value.put(GroupDAO.DATE,time.toString());
        value.put(GroupDAO.CHECKED,g.isChecked());

        mDb.update(TABLE_NAME, value, KEY + " = ?", new String[]{String.valueOf(g.getId())});

    }

    /**
     * @param id l'identifiant du groupe à supprimer
     */
    public void supprimer(long id) {
        if(mDb==null) {
            this.open();
        }
        mDb.delete(TABLE_NAME, KEY + " = ?", new String[] {String.valueOf(id)});
    }

    /**
     * Vide la table
     */
    public void vider() {
        if(mDb==null) {
            this.open();
        }
        mDb.execSQL(TABLE_CREATE);
        mDb.delete(TABLE_NAME, "", null);
    }

    /**
     * Supprime la table Groupe.
     */
    public void supprimerTable() {
        if(mDb==null) {
            this.open();
        }
        mDb.execSQL(GroupDAO.TABLE_DROP);
    }

    /**
     * Ramène tout les groupes.
     */
    public ArrayList<Group> getAllGroups() {
        if(mDb==null) {
            this.open();
        }
        Cursor c = mDb.rawQuery("SELECT "+ GroupDAO.KEY+" as _id, "+ GroupDAO.LIB+", "+ GroupDAO.CHECKED+" from "+ GroupDAO.TABLE_NAME+" ;",null);
        if(c.moveToFirst())
        {
            ArrayList<Group> listGr = new ArrayList<>();
            boolean b = false;
            if(c.getInt(2)==1)
                b = true;
            listGr.add(new Group(c.getString(1),null,b,c.getLong(0)));
            while (c.moveToNext()) {
                b = false;
                if(c.getInt(2)==1)
                    b = true;
                listGr.add(new Group(c.getString(1),null,b,c.getLong(0)));

            }
            return listGr;
        }
        return null;
    }

    /**
     * @param idNiveau id du Niveau
     * @return ArrayList<Groupe> liste de groupe correspondant au niveau passé en paramètre
     */
    public ArrayList<Group> listGroupeFromLevel(long idNiveau)
    {
        if(mDb==null) {
            this.open();
        }
        Cursor c = mDb.rawQuery("SELECT "+ GroupDAO.KEY+" as _id, "+ GroupDAO.LIB+", "+ GroupDAO.CHECKED+" from "+ GroupDAO.TABLE_NAME+" WHERE "+ GroupDAO.IDNIVEAU+" = ?;",new String[] {String.valueOf(idNiveau)});
        if(c.moveToFirst())
        {
            ArrayList<Group> listGr = new ArrayList<>();
            boolean b = false;
            if(c.getInt(2)==1)
                b = true;
            listGr.add(new Group(c.getString(1),null,b,c.getLong(0)));
            while (c.moveToNext()) {
                b = false;
                if(c.getInt(2)==1)
                    b = true;
                listGr.add(new Group(c.getString(1),null,b,c.getLong(0)));

            }
            return listGr;
        }
        return null;
    }


    public ArrayList<Group> listRessourceUser()
    {
        if(mDb==null) {
            this.open();
        }
        Cursor c = mDb.rawQuery("SELECT "+ GroupDAO.KEY+" as _id, "+ GroupDAO.LIB+", "+ GroupDAO.CHECKED+" from "+ GroupDAO.TABLE_NAME+" WHERE "+ GroupDAO.CHECKED+"=1;",null);
        if(c.moveToFirst())
        {

            RessourcesDAO rdao = new RessourcesDAO(context);
            ArrayList<Group> listGr = new ArrayList<>();
            ArrayList<String>listRessource;
            boolean b = false;
            if(c.getInt(2)==1)
                b = true;
            listRessource = rdao.getRessources(c.getLong(0));

            listGr.add(new Group(c.getString(1),listRessource,b,c.getLong(0)));
            while (c.moveToNext()) {
                b = false;
                if(c.getInt(2)==1)
                    b = true;
                listRessource = rdao.getRessources(c.getLong(0));
                listGr.add(new Group(c.getString(1),listRessource,b,c.getLong(0)));
            }
            return listGr;
        }
        return null;
    }



}
