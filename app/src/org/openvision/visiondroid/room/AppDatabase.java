package org.openvision.visiondroid.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import org.openvision.visiondroid.Profile;

@Database(entities = {Profile.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
	public abstract Profile.ProfileDao profileDao();

	private static AppDatabase db = null;


	public static AppDatabase database(Context context) {
		if (db != null)
			return db;
		db = Room.databaseBuilder(
						context.getApplicationContext(),
						AppDatabase.class, "stb"
				)
				.allowMainThreadQueries()
				.build();
		return db;
	}

	public static Profile.ProfileDao profiles(Context context) {
		return database(context).profileDao();
	}


}
