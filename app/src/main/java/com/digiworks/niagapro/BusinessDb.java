package com.digiworks.niagapro;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BusinessDb extends SQLiteOpenHelper {
    public BusinessDb(Context context) { super(context, "niagapro.db", null, 3); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE sales(id INTEGER PRIMARY KEY AUTOINCREMENT, amount REAL NOT NULL, cost_amount REAL NOT NULL DEFAULT 0, method TEXT, note TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE expenses(id INTEGER PRIMARY KEY AUTOINCREMENT, amount REAL NOT NULL, category TEXT, note TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, price REAL NOT NULL DEFAULT 0, cost REAL NOT NULL DEFAULT 0, stock INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE debts(id INTEGER PRIMARY KEY AUTOINCREMENT, party_type TEXT NOT NULL, party_name TEXT NOT NULL, description TEXT, original_amount REAL NOT NULL, balance REAL NOT NULL, created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE debt_payments(id INTEGER PRIMARY KEY AUTOINCREMENT, debt_id INTEGER NOT NULL, amount REAL NOT NULL, note TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(debt_id) REFERENCES debts(id))");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE sales ADD COLUMN cost_amount REAL NOT NULL DEFAULT 0");
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE debts(id INTEGER PRIMARY KEY AUTOINCREMENT, party_type TEXT NOT NULL, party_name TEXT NOT NULL, description TEXT, original_amount REAL NOT NULL, balance REAL NOT NULL, created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
            db.execSQL("CREATE TABLE debt_payments(id INTEGER PRIMARY KEY AUTOINCREMENT, debt_id INTEGER NOT NULL, amount REAL NOT NULL, note TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(debt_id) REFERENCES debts(id))");
        }
    }
}
