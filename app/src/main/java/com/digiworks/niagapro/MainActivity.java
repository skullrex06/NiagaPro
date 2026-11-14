package com.digiworks.niagapro;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends Activity {
    private BusinessDb helper;
    private LinearLayout root, content;
    private TextView salesValue, expenseValue, profitValue, dateLabel;
    private final int navy = Color.rgb(15, 27, 52);
    private final int blue = Color.rgb(55, 105, 245);
    private final int ink = Color.rgb(31, 43, 68);
    private final int muted = Color.rgb(117, 129, 151);
    private final int page = Color.rgb(245, 247, 252);
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("ms", "MY"));

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        helper = new BusinessDb(this);
        getWindow().setStatusBarColor(navy);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        buildUi();
        refresh();
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius * getResources().getDisplayMetrics().density);
        return d;
    }
    private GradientDrawable gradient(int a, int b, int radius) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{a,b});
        d.setCornerRadius(radius * getResources().getDisplayMetrics().density); return d;
    }
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private void buildUi() {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(page);
        LinearLayout header = new LinearLayout(this); header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(22),dp(18),dp(22),dp(22)); header.setBackground(gradient(navy,Color.rgb(37,65,119),24));
        LinearLayout brand = new LinearLayout(this); brand.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo = text("N",22,Color.WHITE,true); logo.setGravity(Gravity.CENTER); logo.setBackground(shape(0x334F8BFF,14));
        brand.addView(logo,new LinearLayout.LayoutParams(dp(42),dp(42)));
        LinearLayout brandText = new LinearLayout(this); brandText.setOrientation(LinearLayout.VERTICAL); brandText.setPadding(dp(11),0,0,0);
        brandText.addView(text("NiagaPro",21,Color.WHITE,true));
        brandText.addView(text("PENGURUS PERNIAGAAN",9,0xFFCFD9F0,true));
        brand.addView(brandText,new LinearLayout.LayoutParams(0,-2,1));
        TextView plan = text("FREE",10,Color.WHITE,true); plan.setPadding(dp(12),dp(7),dp(12),dp(7)); plan.setBackground(shape(0x334F8BFF,30)); brand.addView(plan);
        header.addView(brand);
        dateLabel=text("Ringkasan perniagaan",13,0xFFD5DDF0,false); dateLabel.setPadding(0,dp(22),0,0); header.addView(dateLabel);
        TextView welcome=text("Urus bisnes dengan lebih mudah.",20,Color.WHITE,true); welcome.setPadding(0,dp(4),0,0); header.addView(welcome);
        root.addView(header);
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(18),dp(18),dp(18),dp(26));
        scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
    private LinearLayout card() {
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(15),dp(16),dp(15)); c.setBackground(shape(Color.WHITE,18));
        if(android.os.Build.VERSION.SDK_INT>=21) c.setElevation(dp(2));
        return c;
    }
    private TextView metric(String title, int color) {
        LinearLayout c=card(); TextView t=text(title,11,muted,true); c.addView(t);
        TextView v=text("RM 0.00",21,color,true); v.setPadding(0,dp(8),0,0); c.addView(v);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1); p.setMargins(dp(4),dp(6),dp(4),dp(6)); content.addView(c,p); return v;
    }
    private void section(String title, String subtitle) {
        TextView h=text(title,17,ink,true); h.setPadding(dp(2),dp(16),0,dp(3)); content.addView(h);
        if(subtitle!=null){TextView s=text(subtitle,12,muted,false); s.setPadding(dp(2),0,0,dp(9)); content.addView(s);}
    }
    private void action(String icon, String title, String sub, int color, Runnable run) {
        LinearLayout row=card(); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        TextView badge=text(icon,19,color,true); badge.setGravity(Gravity.CENTER); badge.setBackground(shape(0xFFEFF3FF,14));
        row.addView(badge,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout words=new LinearLayout(this); words.setOrientation(LinearLayout.VERTICAL); words.setPadding(dp(13),0,0,0);
        words.addView(text(title,14,ink,true)); words.addView(text(sub,11,muted,false));
        row.addView(words,new LinearLayout.LayoutParams(0,-2,1)); row.addView(text("›",25,muted,false));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(9); content.addView(row,p);
        row.setOnClickListener(v->run.run());
    }
    private void buildDashboard() {
        content.removeAllViews();
        section("Prestasi hari ini","Pantau aliran wang perniagaan anda.");
        LinearLayout metrics=new LinearLayout(this); metrics.setOrientation(LinearLayout.HORIZONTAL);
        content.addView(metrics);
        salesValue=metricIn(metrics,"JUALAN",blue);
        expenseValue=metricIn(metrics,"BELANJA",0xFFE28A36);
        LinearLayout profit=card(); profit.setBackground(gradient(0xFF315FEA,0xFF684CE6,18));
        TextView pt=text("UNTUNG BERSIH HARI INI",11,0xFFE3E9FF,true); profit.addView(pt);
        profitValue=text("RM 0.00",25,Color.WHITE,true); profitValue.setPadding(0,dp(8),0,0); profit.addView(profitValue);
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2); pp.setMargins(dp(4),dp(5),dp(4),dp(8)); content.addView(profit,pp);
        section("Urus niaga","Rekod aktiviti harian dengan pantas.");
        action("+","Rekod jualan","Produk atau perkhidmatan",blue,()->addRecord(true));
        action("−","Rekod perbelanjaan","Catat kos operasi perniagaan",0xFFE28A36,()->addRecord(false));
        section("Pengurusan","Modul perniagaan.");
        action("▦","Produk & inventori","Senarai produk dan baki stok",blue,()->showProducts());
        action("↔","Sejarah transaksi","Semak rekod jualan dan belanja",0xFF7958D8,()->showHistory());
        action("▤","Laporan ringkas","Ringkasan jualan dan perbelanjaan",0xFF269A83,()->showReport());
        TextView foot=text("NiagaPro • Data disimpan dalam peranti ini",11,muted,false); foot.setGravity(Gravity.CENTER); foot.setPadding(0,dp(18),0,dp(8)); content.addView(foot);
    }
    private TextView metricIn(LinearLayout parent,String title,int color) {
        LinearLayout c=card(); c.addView(text(title,10,muted,true));
        TextView v=text("RM 0.00",17,color,true); v.setPadding(0,dp(8),0,0); c.addView(v);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1); p.setMargins(dp(4),dp(5),dp(4),dp(5)); parent.addView(c,p); return v;
    }
    private void refresh() {
        buildDashboard(); SQLiteDatabase db=helper.getReadableDatabase();
        double sales=sumToday(db,"sales"), expenses=sumToday(db,"expenses");
        salesValue.setText(money.format(sales)); expenseValue.setText(money.format(expenses)); profitValue.setText(money.format(sales-expenses));
    }
    private double sumToday(SQLiteDatabase db,String table) {
        Cursor c=db.rawQuery("SELECT COALESCE(SUM(amount),0) FROM "+table+" WHERE date(created_at)=date('now','localtime')",null);
        double v=0; if(c.moveToFirst())v=c.getDouble(0); c.close(); return v;
    }
    private void addRecord(boolean sale) {
        EditText amount=new EditText(this); amount.setSingleLine(true); amount.setHint("Contoh: 25.50"); amount.setInputType(8194);
        EditText note=new EditText(this); note.setHint(sale?"Catatan / produk (pilihan)":"Kategori / catatan");
        LinearLayout box=new LinearLayout(this); box.setPadding(dp(20),dp(8),dp(20),0); box.setOrientation(LinearLayout.VERTICAL);
        box.addView(text("Jumlah (RM)",12,muted,true)); box.addView(amount); box.addView(note);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(sale?"Rekod jualan":"Rekod perbelanjaan").setView(box)
          .setNegativeButton("Batal",null).setPositiveButton("Simpan",null).create();
        dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(v->{
            try {
                String raw=amount.getText().toString().trim().replace(",",".");
                java.math.BigDecimal parsed=new java.math.BigDecimal(raw);
                if(parsed.scale()>2 || parsed.compareTo(java.math.BigDecimal.ZERO)<=0) throw new Exception();
                ContentValues cv=new ContentValues(); cv.put("amount",parsed.doubleValue());
                if(sale) cv.put("method","Tunai"); else cv.put("category","Lain-lain");
                cv.put("note",note.getText().toString().trim());
                long id=helper.getWritableDatabase().insert(sale?"sales":"expenses",null,cv);
                if(id==-1) throw new Exception();
                dialog.dismiss(); refresh(); Toast.makeText(this,"Rekod berjaya disimpan.",Toast.LENGTH_SHORT).show();
            } catch(Exception e){Toast.makeText(this,"Masukkan jumlah sah. Contoh: 12.50",Toast.LENGTH_LONG).show();}
        }));
        dialog.show();
    }
    private void showHistory() {
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT 'Jualan' AS jenis,amount,note,created_at FROM sales UNION ALL SELECT 'Belanja',amount,note,created_at FROM expenses ORDER BY created_at DESC LIMIT 50",null);
        StringBuilder out=new StringBuilder();
        while(c.moveToNext()) out.append(c.getString(0)).append(" • ").append(money.format(c.getDouble(1))).append("\n")
            .append(c.getString(2)==null?"":c.getString(2)).append("\n").append(c.getString(3)).append("\n\n");
        c.close(); if(out.length()==0)out.append("Belum ada transaksi direkodkan.");
        new AlertDialog.Builder(this).setTitle("Sejarah transaksi").setMessage(out.toString()).setPositiveButton("Tutup",null).show();
    }
    private void showProducts() {
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT name,price,stock FROM products ORDER BY name",null);
        StringBuilder b=new StringBuilder();
        while(c.moveToNext()) b.append(c.getString(0)).append("\nHarga: ").append(money.format(c.getDouble(1))).append(" • Stok: ").append(c.getInt(2)).append("\n\n");
        c.close(); if(b.length()==0)b.append("Belum ada produk. Pengurusan produk penuh akan ditambah dalam fasa seterusnya.");
        new AlertDialog.Builder(this).setTitle("Produk & inventori").setMessage(b.toString()).setPositiveButton("Tutup",null).show();
    }
    private void showReport() {
        SQLiteDatabase db=helper.getReadableDatabase(); double s=sumToday(db,"sales"), e=sumToday(db,"expenses");
        String msg="Jualan hari ini: "+money.format(s)+"\nPerbelanjaan hari ini: "+money.format(e)+"\nUntung bersih: "+money.format(s-e)+"\n\nLaporan ini berdasarkan rekod hari ini sahaja.";
        new AlertDialog.Builder(this).setTitle("Laporan ringkas").setMessage(msg).setPositiveButton("Tutup",null).show();
    }
    @Override protected void onDestroy(){if(helper!=null)helper.close();super.onDestroy();}
}
