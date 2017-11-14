package insa_project.bananarchy.bdd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by Pierre on 27/09/2016.
 */

public class RessourcesDAO extends DAOBase{

    public static final String TABLE_NAME = "ressources";
    public static final String KEY = "id";
    public static final String FOREIGNKEY = "idGroupe";
    public static final String LIB = "libelle";

    public static final String TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("+ KEY +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    LIB+" TEXT, "+
                    FOREIGNKEY+" INTEGER, "+
                    "FOREIGN KEY("+FOREIGNKEY+") REFERENCES "+GroupDAO.TABLE_NAME+"("+GroupDAO.KEY+"));";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS "+TABLE_NAME+";";

    public RessourcesDAO(Context pContext) {
        super(pContext);
        if(mDb==null) {
            this.open();
        }
        mDb.execSQL(TABLE_CREATE);
    }

    public long canAdd(String r, long idGr)
    {
        if(mDb==null) {
            this.open();
        }
        mDb.execSQL(TABLE_CREATE);
        Cursor c = mDb.rawQuery("SELECT "+RessourcesDAO.KEY+" as _id, "+RessourcesDAO.LIB+" from "+RessourcesDAO.TABLE_NAME+" WHERE "+RessourcesDAO.LIB+" = ? AND "+RessourcesDAO.FOREIGNKEY+" = ? LIMIT 1",new String[]{r, String.valueOf(idGr)});
        if(c.moveToFirst())
        {
            return c.getLong(0);
        }
        return -1;
    }

    /**
     * @param r Ressource à ajouter à la base
     */
    public void ajouter(String r, long id) {
        if(mDb==null) {
            this.open();
        }
        mDb.execSQL(TABLE_CREATE);

        long idRes = canAdd(r,id);
        if(idRes == -1)
        {
            ContentValues value = new ContentValues();
            value.put(RessourcesDAO.FOREIGNKEY,id);
            value.put(RessourcesDAO.LIB,r);
            mDb.insert(RessourcesDAO.TABLE_NAME,null,value);
        }

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
     * @param id l'identifiant de la ressource à supprimer
     */
    public void supprimer(long id) {
        if(mDb==null) {
            this.open();
        }
        mDb.delete(TABLE_NAME, KEY + " = ?", new String[] {String.valueOf(id)});
    }

    /**
     * Supprime la table Ressource.
     */
    public void supprimerTable() {
        if(mDb==null) {
            this.open();
        }
        mDb.execSQL(RessourcesDAO.TABLE_DROP);
    }


    public ArrayList<String> getRessources(long id) {
        if(mDb==null) {
            this.open();
        }
        Cursor c = mDb.rawQuery("SELECT "+ RessourcesDAO.KEY+" as _id, "+ RessourcesDAO.LIB+ " from "+ RessourcesDAO.TABLE_NAME+" where "+RessourcesDAO.FOREIGNKEY+" = ? ",new String[]{String.valueOf(id)});
        if(c.moveToFirst()) {

            ArrayList<String> retour = new ArrayList<>();
            retour.add(c.getString(1));
            while (c.moveToNext()) {
                retour.add(c.getString(1));
            }
            return retour;
        }
        return null;
    }


}
