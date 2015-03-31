package com.jparkie.aizoban.data.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jparkie.aizoban.models.Manga;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;

public class LibrarySQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String TAG = LibrarySQLiteOpenHelper.class.getSimpleName();

    private Context mContext;

    public LibrarySQLiteOpenHelper(Context context) {
        super(context, LibraryContract.DATABASE_NAME, null, LibraryContract.DATABASE_VERSION);

        mContext = context;

        initializeLibraryDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Cupboard libraryCupboard = constructCustomCupboard();
        libraryCupboard.withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Do Nothing.
    }

    private Cupboard constructCustomCupboard() {
        Cupboard customCupboard = new CupboardBuilder().build();
        customCupboard.register(Manga.class);

        return customCupboard;
    }

    private void initializeLibraryDatabase() {
        final File libraryDatabasePath = mContext.getApplicationContext().getDatabasePath(LibraryContract.DATABASE_NAME);
        if (shouldStopInitializingLibraryDatabase(libraryDatabasePath)){
            return;
        }

        final File parentPath = libraryDatabasePath.getParentFile();
        if (parentPath.exists()) {
            deleteOldLibraryDatabaseWhileRetainingApplicationDatabase();
        } else {
            parentPath.mkdirs();
        }

        if (parentPath.exists()) {
            copyNewLibraryDatabase(libraryDatabasePath);
        }
    }

    private boolean shouldStopInitializingLibraryDatabase(File libraryDatabasePath) {
        if (libraryDatabasePath.exists()){
            SQLiteDatabase libraryDatabase = SQLiteDatabase.openDatabase(libraryDatabasePath.getPath(), null, 0);

            int currentVersion = libraryDatabase.getVersion();

            libraryDatabase.close();
            libraryDatabase = null;

            if (currentVersion == LibraryContract.DATABASE_VERSION) {
                return true;
            }
        }

        return false;
    }

    private void deleteOldLibraryDatabaseWhileRetainingApplicationDatabase() {
        String[] databaseList = mContext.getApplicationContext().databaseList();

        for (String databaseName : databaseList) {
            if (!databaseName.contains(ApplicationContract.DATABASE_NAME)) {
                mContext.getApplicationContext().deleteDatabase(databaseName);
            }
        }
    }

    private void copyNewLibraryDatabase(File libraryDatabasePath) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = mContext.getApplicationContext().getAssets().open(LibraryContract.DATABASE_NAME);
            outputStream = new FileOutputStream(libraryDatabasePath);

            byte[] fileBuffer = new byte[8192];
            for (int counter = 0; counter != -1; counter = inputStream.read(fileBuffer, 0, 8192)) {
                outputStream.write(fileBuffer, 0, counter);
            }

            outputStream.flush();
        } catch (IOException e) {
            // Do Nothing.
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Do Nothing.
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // Do Nothing.
                }
            }
        }
    }
}
