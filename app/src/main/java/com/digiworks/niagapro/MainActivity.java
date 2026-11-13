package com.digiworks.niagapro;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends Activity {
    private BusinessDb helper;
    private LinearLayout root, content;
    private TextView salesValue, expenseValue, profitValue;
    private final int navy = Color.rgb(22, 39, 75);
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("ms", "MY"));

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        helper = new BusinessDb(this);
        buildUi();
        refresh();
    }

    private void buildUi() {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.rgb(246,248,252));
        TextView header = new TextView(this); header.setText("NiagaPro"); header.setTextColor(Color.WHITE); header.setTextSize(25); header.setTypeface(null, Typeface.BOLD); header.setPadding(20,22,20,22); header.setBackgroundColor(navy);
        root.addView(header);
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this); content.setPadding(16,16,16,24); content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content); root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }

    private TextView label(String text, int size, int color) {
        TextView v = new TextView(this); v.setText(text); v.setTextSize(size); v.setTextColor(color); return v;
    }
    private TextView metric(String title) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(16,14,16,14); card.setBackgroundColor(Color.WHITE);
        TextView t = label(title,14,Color.DKGRAY); TextView v = label("RM 0.00",23,navy); v.setTypeface(null,Typeface.BOLD);
        card.addView(t); card.addView(v); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=10; content.addView(card,p); return v;
    }
    private void button(String text, Runnable action) {
        Button b = new Button(this); b.setText(text); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(navy));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=8; content.addView(b,p); b.setOnClickListener(v -> action.run());
    }
    private void buildDashboard() {
        content.removeAllViews();
        TextView title = label("Ringkasan Perniagaan",20,navy); title.setTypeface(null,Typeface.BOLD); content.addView(title);
        TextView today = label("Jualan dan perbelanjaan direkodkan secara offline.",13,Color.GRAY); today.setPadding(0,4,0,14); content.addView(today);
        salesValue = metric("JUALAN HARI INI"); expenseValue = metric("PERBELANJAAN HARI INI"); profitValue = metric("BAKI HARI INI");
        button("+ Rekod Jualan", () -> addRecord(true));
        button("+ Rekod Perbelanjaan", () -> addRecord(false));
        button("Sejarah Transaksi", this::showHistory);
        button("Produk & Stok (akan datang)", () -> new AlertDialog.Builder(this).setMessage("Modul produk dan inventori sedang dibangunkan.").setPositiveButton("OK",null).show());
        TextView foot = label("NiagaPro • Free plan • Data disimpan dalam telefon ini",12,Color.GRAY); foot.setPadding(4,12,4,4); content.addView(foot);
    }
    private void refresh() {
        buildDashboard();
        SQLiteDatabase db = helper.getReadableDatabase();
        double sales = sumToday(db,"sales"), expenses = sumToday(db,"expenses");
        salesValue.setText(money.format(sales)); expenseValue.setText(money.format(expenses)); profitValue.setText(money.format(sales-expenses));
    }
    private double sumToday(SQLiteDatabase db, String table) {
        Cursor c = db.rawQuery("SELECT COALESCE(SUM(amount),0) FROM "+table+" WHERE date(created_at)=date('now','localtime')",null);
        double value=0; if(c.moveToFirst()) value=c.getDouble(0); c.close(); return value;
    }
    private void addRecord(boolean sale) {
        EditText amount = new EditText(this); amount.setHint("Jumlah RM (contoh 12.50)"); amount.setInputType(8194);
        EditText note = new EditText(this); note.setHint(sale ? "Catatan / produk (pilihan)" : "Kategori / catatan");
        LinearLayout box = new LinearLayout(this); box.setPadding(20,8,20,0); box.setOrientation(LinearLayout.VERTICAL); box.addView(amount); box.addView(note);
        new AlertDialog.Builder(this).setTitle(sale?"Rekod Jualan":"Rekod Perbelanjaan").setView(box)
          .setNegativeButton("Batal",null).setPositiveButton("Simpan",(d,w)->{
              try {
                  double value=Double.parseDouble(amount.getText().toString().trim());
                  if(value<=0) throw new NumberFormatException();
                  ContentValues cv=new ContentValues(); cv.put("amount",value); cv.put(sale?"method":"category",sale?"Tunai":"Lain-lain"); cv.put("note",note.getText().toString());
                  helper.getWritableDatabase().insert(sale?"sales":"expenses",null,cv); refresh();
              } catch(Exception e) { Toast.makeText(this,"Masukkan jumlah yang sah.",Toast.LENGTH_LONG).show(); }
          }).show();
    }
    private void showHistory() {
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT 'Jualan' AS jenis, amount, note, created_at FROM sales UNION ALL SELECT 'Belanja', amount, note, created_at FROM expenses ORDER BY created_at DESC LIMIT 50",null);
        StringBuilder out=new StringBuilder();
        while(c.moveToNext()) out.append(c.getString(0)).append(" • ").append(money.format(c.getDouble(1))).append("\\n").append(c.getString(2)==null?"":c.getString(2)).append("\\n").append(c.getString(3)).append("\\n\\n");
        c.close(); if(out.length()==0) out.append("Belum ada transaksi.");
        new AlertDialog.Builder(this).setTitle("Sejarah Transaksi").setMessage(out.toString()).setPositiveButton("Tutup",null).show();
    }
}
