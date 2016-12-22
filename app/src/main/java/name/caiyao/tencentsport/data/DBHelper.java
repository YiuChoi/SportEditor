package name.caiyao.tencentsport.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 蔡小木 on 2016/12/22 0022.
 */

public class DBHelper extends SQLiteOpenHelper {
    private final static String DB_NAME = "applist.db";
    public final static String APP_TABLE_NAME = "app";
    public final static String APP_KEY_CHECK = "checked";
    private final static int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_APP_TABLE = "CREATE TABLE IF NOT EXISTS " + APP_TABLE_NAME + "(package_name TEXT PRIMARY KEY," + APP_KEY_CHECK + " INTEGER)";
        db.execSQL(CREATE_APP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
