package com.jparkie.aizoban.data.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.DownloadChapter;
import com.jparkie.aizoban.models.DownloadManga;
import com.jparkie.aizoban.models.DownloadPage;
import com.jparkie.aizoban.models.FavouriteManga;
import com.jparkie.aizoban.models.RecentChapter;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;

public class ApplicationSQLiteOpenHelper extends SQLiteOpenHelper {
    public static final String TAG = ApplicationSQLiteOpenHelper.class.getSimpleName();

    public ApplicationSQLiteOpenHelper(Context context) {
        super(context, ApplicationContract.DATABASE_NAME, null, ApplicationContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Cupboard applicationCupboard = constructCustomCupboard();
        applicationCupboard.withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Cupboard applicationCupboard = constructCustomCupboard();
        applicationCupboard.withDatabase(db).upgradeTables();
    }

    private Cupboard constructCustomCupboard() {
        Cupboard customCupboard = new CupboardBuilder().build();
        customCupboard.register(Chapter.class);
        customCupboard.register(FavouriteManga.class);
        customCupboard.register(RecentChapter.class);
        customCupboard.register(DownloadManga.class);
        customCupboard.register(DownloadChapter.class);
        customCupboard.register(DownloadPage.class);

        return customCupboard;
    }
}
