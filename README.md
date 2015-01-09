# Android-SQLiteMergerHelper

__Class instrumentation for merging SQLite database.__

# Add in project

 - Add __SQLiteMergerHelper.java__ class in your project
 - Create dbscript.txt in a project __assets__
 
# Start using

First of all go to your SQLiteOpenHelper and update onUpgrade method

```sh
	@Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        SQLiteMergerHelper.getInstance().readScript(context,db,DATABASE_VERSION);
    }
```

Where is __DATABASE_VERSION__ - current version of the database.

Other work to go to a script.

# Script commands


 - __VERSION:__

  set version script for identical version db
```sh
	VERSION:
	18
```



 - __ADD_COLUMNS:__
 
  add column in table. First parameter - table, then split symbol ":" and new column with attributes
```sh
	ADD_COLUMNS:  
	messages:id INTEGER DEFAULT 0
```  


 - __DROP_COLUMNS:__
 
 Drop column in table. First parameter - new query table without unnecessary fields, second - main table name,third - fields for removing separated by commas.
```sh
	DROP_COLUMNS:
	CREATE TABLE message(_id...ets.):message:test_id,is_deleted 
```


 - __RENAME_TABLE:__
 
 Rename table. First parameter - table name, second - new table name.
```sh
	RENAME_TABLE:
	message:message2
```


 - __OTHER_QUERY:__
 
Other queries. Implementation of any other queries.
```sh
	OTHER_QUERY:
	CREATE TABLE messages (_id INTEGER PRIMARY KEY,name TEXT,message TEXT,moderate TEXT)
```
 
All together one entry in the script looks like this(Example)
 
```sh
	VERSION:
	18
	ADD_COLUMNS:  
	messages:id INTEGER DEFAULT 0
	DROP_COLUMNS:
	CREATE TABLE messages(_id...ets.):messages:test_id,is_deleted 
	RENAME_TABLE:
	messages:message
	OTHER_QUERY:
	CREATE TABLE messages (_id INTEGER PRIMARY KEY,name TEXT,message TEXT,moderate TEXT)
 
	VERSION:
	19
	ADD_COLUMNS:  
	messages:sort INTEGER DEFAULT 0
	...
```
 
The first command always goes __VERSION:__ further order of of commands is not important until the next command __VERSION:__
 
"This projected is licensed under the terms of the MIT license."