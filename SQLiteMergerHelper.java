
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.crashlytics.android.Crashlytics;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class SQLiteMergerHelper {
	
	/**
	 * SQLiteMergerHelper version 1.0.0
	 * 
	 * Example with comments:
	 * 
	 * Set version script for identical version db
	 * VERSION:
	 * 18
	 *
	 * Add column in table. First parameter - table, then split symbol ":" and new column with attributes 
	 * ADD_COLUMNS:  
	 * messages:test_id integer default 0
	 *
	 * Drop column in table. First parameter - new query table without unnecessary fields, second - main table name,
	 * third - fields for removing separated by commas.
	 * DROP_COLUMNS:
	 * CREATE TABLE message(_id...ets.):message:test_id,is_deleted 
	 * 
	 * Rename table. First parameter - table name, second - new table name.
	 * RENAME_TABLE:
	 * message:message2
	 * 
	 * Other queries. Implementation of any other queries. 
	 * OTHER_QUERY:
	 *
	 * main script locate in assets - dbscript.txt (create)
	 */
	
	private static final String FILE_NAME = "dbscript.txt";
	private static final String VERSION = "VERSION:";
	private static final String ADD_COLUMN = "ADD_COLUMNS:";
	private static final String DROP_COLUMN = "DROP_COLUMNS:";
	private static final String RENAME_TABLE = "RENAME_TABLE:";
	private static final String OTHER_QUERY = "OTHER_QUERY:";
	
	private FILE_CONSTS type_operation;
	
	private static SQLiteMergerHelper instance = null;
	private SQLiteDatabase db;
	private int version_update;
	private int current_version;
	
	private static enum FILE_CONSTS{
		VERSION,ADD_COLUMN,DROP_COLUMN,
		RENAME_TABLE,OTHER_QUERY
	}
	
	public static SQLiteMergerHelper getInstance() {
		if (instance == null) {
			instance = new SQLiteMergerHelper(); 
		}
		return instance;
	}
	
	// read/parse file script in assets
	public void readScript(Context context,SQLiteDatabase db, int currentVersion) {
		
		this.current_version = currentVersion;
		this.db = db;
		
		BufferedReader reader = null;
		try {
			InputStreamReader is = new InputStreamReader(context.getAssets().open(FILE_NAME),"UTF-8");
	        reader = new BufferedReader(is);
	        String line = reader.readLine();
	        while (line != null) {
	        	parseScript(line);
	        	line = reader.readLine();
	        }
		} catch (IOException e) {
			e.printStackTrace();
		} if (reader!=null) {
			try {
				reader.close();
			} catch (IOException e) {
                e.printStackTrace();
			}
		}
	}
	
	private void parseScript(String parseLine) {
		if (parseLine!=null && !parseLine.trim().equals("")) {
			Log.e(SQLiteMergerHelper.class.getName(), parseLine);
			
			if (parseLine.equals(VERSION)) {
				type_operation = FILE_CONSTS.VERSION;
				return;
			} else if (parseLine.equals(DROP_COLUMN)) {
				type_operation = FILE_CONSTS.DROP_COLUMN;
				return;
			} else if (parseLine.equals(ADD_COLUMN)) {
				type_operation = FILE_CONSTS.ADD_COLUMN;
				return;
			} else if (parseLine.equals(RENAME_TABLE)) {
				type_operation = FILE_CONSTS.RENAME_TABLE;
				return;
			} else if (parseLine.equals(OTHER_QUERY)){
				type_operation = FILE_CONSTS.OTHER_QUERY;
				return;
			}
			
			String[] variables = null;
			switch (type_operation) {
				
				case VERSION:{
					version_update = Integer.parseInt(parseLine);
				}break;
							
				case ADD_COLUMN:{
					try {
						if (current_version >= version_update) {
							variables = parseSprlitBy(parseLine,":");
							addColumn(db, variables[0], variables[1]);
						}
					} catch (Exception e) {
                        e.printStackTrace();
					}
					
				}break;
				
				case DROP_COLUMN:{
					try {
						if (current_version >= version_update) {
							variables = parseSprlitBy(parseLine,":");
							try {
								dropColumn(db, variables[0], variables[1], parseSprlitBy(variables[2],","));
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
                        e.printStackTrace();
					}
					
				}break;
	
				case RENAME_TABLE:{
					try {
						if (current_version >= version_update) {
							variables = parseSprlitBy(parseLine,":");
							renameTable(db, variables[0], variables[1]);
						}
					} catch (Exception e) {
                        e.printStackTrace();
					}
				}break;
				
				case OTHER_QUERY:{
					try {
						if (current_version >= version_update) {
							db.execSQL(parseLine);
						}
					} catch (Exception e) {
                        e.printStackTrace();
					}
				}break;

			default:
				break;
			}
		}
	}
	
	// split string by
	private String[] parseSprlitBy(String line,String splitBy){
		String delims = "["+ splitBy +"]";
		String[] tokens = line.split(delims);
		return tokens;
	}
	
	// rename table
	public void renameTable(SQLiteDatabase db,
	        String tableName, String newTableName) {
		if (db!=null) {
			db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + newTableName);
		} else if (this.db!=null) {
			this.db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + newTableName);
		} else {
			Log.e(SQLiteMergerHelper.class.getName(),"Data base is not exist");
		}
	}
	
	// add column in table
	public void addColumn(SQLiteDatabase db,
	        String tableName, String columnString){
		if (db!=null) {
			db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnString);
		} else if (this.db!=null) {
			this.db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnString);
		} else {
			Log.e(SQLiteMergerHelper.class.getName(),"Data base is not exist");
		}
		
	}
	
	// remove column in table
	public static void dropColumn(SQLiteDatabase db,
	        String createTableCmd,
	        String tableName,
	        String[] colsToRemove) throws java.sql.SQLException {

	    List<String> updatedTableColumns = getTableColumns(db,tableName);
	    updatedTableColumns.removeAll(Arrays.asList(colsToRemove));
	    String columnsSeperated = TextUtils.join(",", updatedTableColumns);
	    db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tableName + "_old;");
	    db.execSQL(createTableCmd);
	    db.execSQL("INSERT INTO " + tableName + "(" + columnsSeperated + ") SELECT " + columnsSeperated + " FROM " + tableName + "_old;");
	    db.execSQL("DROP TABLE " + tableName + "_old;");
	}
	
	private static List<String> getTableColumns(SQLiteDatabase db, String tableName) {
	    ArrayList<String> columns = new ArrayList<String>();
	    String cmd = "pragma table_info(" + tableName + ");";
	    Cursor cur = db.rawQuery(cmd, null);

	    while (cur.moveToNext()) {
	        columns.add(cur.getString(cur.getColumnIndex("name")));
	    }
	    cur.close();

	    return columns;
	}
	
	// check exist column in table
	public boolean existsColumnInTable(SQLiteDatabase db, String table, String columnToCheck) {
	    try{
	    	if (db == null) {
				db = this.db;
			} 
	    	//query 1 row
	        Cursor mCursor  = db.rawQuery( "SELECT * FROM " + table + " LIMIT 0", null );
	        //getColumnIndex gives us the index (0 to ...) of the column - otherwise we get a -1
	        if(mCursor.getColumnIndex(columnToCheck) != -1)
	            return true;
	        else
	            return false;
	    }catch (Exception e){
	        //something went wrong. Missing the database? The table?
            e.printStackTrace();
	        return false;
	    }
	}

}
