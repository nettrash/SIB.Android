package ru.nettrash.sibcoin.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by nettrash on 21.01.2018.
 */

// User and Book are classes annotated with @Entity.
@Database(version = 1, entities = {Address.class})
public abstract class SIBDatabase extends RoomDatabase {

    private static SIBDatabase INSTANCE;

    public abstract AddressDao addressDao();

    public static SIBDatabase getSIBDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context, SIBDatabase.class, "sib-database")
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}