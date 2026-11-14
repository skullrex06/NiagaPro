package com.digiworks.niagapro;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BusinessDb extends SQLiteOpenHelper {
    public BusinessDb(Context context) { super(context, "niagapro.db", null, 4); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE business_profiles(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, business_type TEXT NOT NULL DEFAULT 'Mixed Business', created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("INSERT INTO business_profiles(name,business_type) VALUES('Perniagaan Saya','Mixed Business')");
        db.execSQL("CREATE TABLE sales(id INTEGER PRIMARY KEY AUTOINCREMENT, business_id INTEGER NOT NULL DEFAULT 1, amount REAL NOT NULL, cost_amount REAL NOT NULL DEFAULT 0, method TEXT, note TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE expenses(id INTEGER PRIMARY KEY AUTOINCREMENT, business_id INTEGER NOT NULL DEFAULT 1, amount REAL NOT NULL, category TEXT, note TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT, business_id INTEGER NOT NULL DEFAULT 1, name TEXT NOT NULL, price REAL NOT NULL DEFAULT 0, cost REAL NOT NULL DEFAULT 0, stock INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE debts(id INTEGER PRIMARY KEY AUTOINCREMENT, business_id INTEGER NOT NULL DEFAULT 1, party_type TEXT NOT NULL, party_name TEXT NOT NULL, description TEXT, original_amount REAL NOT NULL, balance REAL NOT NULL, created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
        db.execSQL("CREATE TABLE debt_payments(id INTEGER PRIMARY KEY AUTOINCREMENT, business_id INTEGER NOT NULL DEFAULT 1, debt_id INTEGER NOT NULL, amount REAL NOT NULL, note TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(debt_id) REFERENCES debts(id))");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE sales ADD COLUMN cost_amount REAL NOT NULL DEFAULT 0");
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE debts(id INTEGER PRIMARY KEY AUTOINCREMENT, party_type TEXT NOT NULL, party_name TEXT NOT NULL, description TEXT, original_amount REAL NOT NULL, balance REAL NOT NULL, created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
            db.execSQL("CREATE TABLE debt_payments(id INTEGER PRIMARY KEY AUTOINCREMENT, debt_id INTEGER NOT NULL, amount REAL NOT NULL, note TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(debt_id) REFERENCES debts(id))");
        }
        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE business_profiles(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, business_type TEXT NOT NULL DEFAULT 'Mixed Business', created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
            db.execSQL("INSERT INTO business_profiles(name,business_type) VALUES('Perniagaan Saya','Mixed Business')");
            db.execSQL("ALTER TABLE sales ADD COLUMN business_id INTEGER NOT NULL DEFAULT 1");
            db.execSQL("ALTER TABLE expenses ADD COLUMN business_id INTEGER NOT NULL DEFAULT 1");
            db.execSQL("ALTER TABLE products ADD COLUMN business_id INTEGER NOT NULL DEFAULT 1");
            db.execSQL("ALTER TABLE debts ADD COLUMN business_id INTEGER NOT NULL DEFAULT 1");
            db.execSQL("ALTER TABLE debt_payments ADD COLUMN business_id INTEGER NOT NULL DEFAULT 1");
            db.execSQL("CREATE INDEX idx_sales_business_date ON sales(business_id,created_at)");
            db.execSQL("CREATE INDEX idx_expenses_business_date ON expenses(business_id,created_at)");
            db.execSQL("CREATE INDEX idx_products_business ON products(business_id)");
            db.execSQL("CREATE INDEX idx_debts_business ON debts(business_id,balance)");
        }
    }
}