package insa_project.bananarchy.bdd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import insa_project.bananarchy.model.Level;

/**
 * Created by Pierre on 27/09/2016.
 */

public class LevelDAO extends DAOBase{

    public static final String TABLE_NAME = "levels";
    public static final String KEY = "id";
    public static final String LIB = "libelle";

    public static final String TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("+ KEY +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    LIB+" TEXT );";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS "+TABLE_NAME+";";

    public LevelDAO(Context pContext) {
        super(pContext);
        if(mDb==null) {
            this.open();
        }
        mDb.execSQL(TABLE_CREATE);
    }

    public long canAdd(Level n)
    {
        if(mDb==null) {
            this.open();
        }
        mDb.execSQL(TABLE_CREATE);
        Cursor c = mDb.rawQuery("SELECT "+ LevelDAO.KEY+" as _id, "+ LevelDAO.LIB+" from "+ LevelDAO.TABLE_NAME+" WHERE "+ LevelDAO.LIB+" = ? LIMIT 1",new String[]{n.getLibelle()});
        if(c.moveToFirst())
        {
            return c.getLong(0);
        }
        return -1;
    }


    /**
     * @param n Niveau à ajouter à la base
     */
    public long ajouter(Level n) {
        if(mDb==null) {
            this.open();
        }
        long idN = canAdd(n);
        if(idN==-1)
        {
            mDb.execSQL(TABLE_CREATE);
            ContentValues value = new ContentValues();
            value.put(LevelDAO.LIB,n.getLibelle());
            return mDb.insert(LevelDAO.TABLE_NAME,null,value);
        }
        return idN;

    }


    /**
     * @param id l'identifiant du niveau à supprimer
     */
    public void supprimer(long id) {
        if(mDb==null) {
            this.open();
        }
        mDb.execSQL(TABLE_CREATE);
        mDb.delete(TABLE_NAME, KEY + " = ?", new String[] {String.valueOf(id)});
    }

    /**
     * Vide la table Niveau sans la supprimer.
     */
    public void viderTable() {
        if(mDb==null) {
            this.open();
        }
        mDb.delete(TABLE_NAME, "", null);
    }


    /**
     * Supprime la table Groupe.
     */
    public void supprimerTable() {
        if(mDb==null) {
            this.open();
        }
        mDb.execSQL(LevelDAO.TABLE_DROP);
    }

    /**
     * Ramène tout les niveaux.
     */
    public ArrayList<Level> getAllLevels() {
        if(mDb==null) {
            this.open();
        }
        Cursor c = mDb.rawQuery("SELECT "+ LevelDAO.KEY+" as _id, "+ LevelDAO.LIB+" from "+ LevelDAO.TABLE_NAME+" ;",null);
        if(c.moveToFirst())
        {
            ArrayList<Level> listGr = new ArrayList<>();
            listGr.add(new Level(c.getString(1),c.getLong(0)));
            while (c.moveToNext()) {
                listGr.add(new Level(c.getString(1),c.getLong(0)));

            }
            return listGr;
        }
        return null;
    }



}
