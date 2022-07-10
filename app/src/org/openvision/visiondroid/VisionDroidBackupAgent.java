/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * @author sre
 *
 */
public class VisionDroidBackupAgent extends BackupAgentHelper {
	public static final String PREFS = "org.openvision.visiondroid_preferences";
	
	public static final String DATABASE_BACKUP_KEY = "database";
	public static final String PREFS_BACKUP_KEY ="preferences";
	public void onCreate(){
		SharedPreferencesBackupHelper spbh = new SharedPreferencesBackupHelper(this, PREFS);
		addHelper(PREFS_BACKUP_KEY, spbh);
		FileBackupHelper dbfbh = new FileBackupHelper(this, "../databases/" + DatabaseHelper.DATABASE_NAME);
		addHelper(DATABASE_BACKUP_KEY, dbfbh);
	}
}
