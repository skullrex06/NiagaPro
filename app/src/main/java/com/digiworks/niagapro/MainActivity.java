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
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    private BusinessDb helper;
    private long activeBusinessId=1;
    private String activeBusinessName="Perniagaan Saya";
    private android.content.SharedPreferences profilePrefs;
    private LinearLayout root, content;
    private TextView salesValue, expenseValue, profitValue, dateLabel;
    private final int navy = Color.rgb(8, 16, 48);
    private final int blue = Color.rgb(91, 115, 255);
    private final int ink = Color.rgb(245, 247, 255);
    private final int muted = Color.rgb(190, 202, 232);
    private final int page = Color.rgb(8, 16, 48);
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("ms", "MY"));

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        helper = new BusinessDb(this);
        profilePrefs=getSharedPreferences("niagapro_profile",MODE_PRIVATE);
        activeBusinessId=profilePrefs.getLong("active_id",1);
        loadActiveProfile();
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
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackground(gradient(Color.rgb(7,15,43),Color.rgb(48,24,105),0));
        LinearLayout header = new LinearLayout(this); header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(22),dp(18),dp(22),dp(22)); header.setBackground(gradient(navy,Color.rgb(40,35,130),24));
        LinearLayout brand = new LinearLayout(this); brand.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo = text("N",22,Color.WHITE,true); logo.setGravity(Gravity.CENTER); logo.setBackground(shape(0x334F8BFF,14));
        brand.addView(logo,new LinearLayout.LayoutParams(dp(42),dp(42)));
        LinearLayout brandText = new LinearLayout(this); brandText.setOrientation(LinearLayout.VERTICAL); brandText.setPadding(dp(11),0,0,0);
        brandText.addView(text("NiagaPro",21,Color.WHITE,true));
        brandText.addView(text("PENGURUS PERNIAGAAN",9,0xFFCFD9F0,true));
        brand.addView(brandText,new LinearLayout.LayoutParams(0,-2,1));
        TextView profileButton=text("▣ "+activeBusinessName+"  ▾",11,Color.WHITE,true);
        profileButton.setPadding(dp(10),dp(9),dp(10),dp(9));profileButton.setBackground(shape(0x334F8BFF,20));
        brand.addView(profileButton);profileButton.setOnClickListener(v->showProfiles());
        TextView plan = text("FREE",10,Color.WHITE,true); plan.setPadding(dp(12),dp(7),dp(12),dp(7)); plan.setBackground(gradient(0xFF315FEA,0xFF8A35E8,30)); brand.addView(plan);
        header.addView(brand);
        dateLabel=text(new SimpleDateFormat("EEEE, d MMMM yyyy",new Locale("ms","MY")).format(new Date()),13,0xFFD5DDF0,false); dateLabel.setPadding(0,dp(22),0,0); header.addView(dateLabel);
        TextView welcome=text("Urus bisnes dengan lebih mudah.",20,Color.WHITE,true); welcome.setPadding(0,dp(4),0,0); header.addView(welcome);
        root.addView(header);
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(18),dp(18),dp(18),dp(64)); content.setClipToPadding(false);
        scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
    private LinearLayout card() {
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(15),dp(16),dp(15)); c.setBackground(gradient(Color.rgb(25,43,91),Color.rgb(42,43,112),18));
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
        TextView badge=text(icon,19,color,true); badge.setGravity(Gravity.CENTER); badge.setBackground(shape(0x443D72FF,14));
        row.addView(badge,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout words=new LinearLayout(this); words.setOrientation(LinearLayout.VERTICAL); words.setPadding(dp(13),0,0,0);
        words.addView(text(title,14,ink,true)); words.addView(text(sub,11,muted,false));
        row.addView(words,new LinearLayout.LayoutParams(0,-2,1)); row.addView(text("›",25,muted,false));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(9); content.addView(row,p);
        row.setOnClickListener(v->run.run());
    }
    private void buildDashboard() {
        content.removeAllViews();

        // Ringkasan prestasi: KPI dipaparkan dahulu supaya mudah diimbas.
        section("Ringkasan perniagaan","Prestasi kewangan perniagaan anda.");
        LinearLayout profile=card();
        profile.setOrientation(LinearLayout.HORIZONTAL);
        profile.setGravity(Gravity.CENTER_VERTICAL);
        TextView profileIcon=text("▣",22,0xFFB8A5FF,true);
        profileIcon.setGravity(Gravity.CENTER);
        profileIcon.setBackground(shape(0x443D72FF,16));
        profile.addView(profileIcon,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout profileWords=new LinearLayout(this);
        profileWords.setOrientation(LinearLayout.VERTICAL);
        profileWords.setPadding(dp(13),0,0,0);
        profileWords.addView(text("PROFIL PERNIAGAAN",10,muted,true));
        profileWords.addView(text(activeBusinessName,15,ink,true));
        profileWords.addView(text("Tukar atau urus sehingga 5 perniagaan",10,muted,false));
        profile.addView(profileWords,new LinearLayout.LayoutParams(0,-2,1));
        profile.addView(text("›",26,muted,false));
        LinearLayout.LayoutParams profileParams=new LinearLayout.LayoutParams(-1,-2);
        profileParams.setMargins(0,dp(4),0,dp(12));
        content.addView(profile,profileParams);
        profile.setOnClickListener(v->showProfiles());

        LinearLayout metrics=new LinearLayout(this);
        metrics.setOrientation(LinearLayout.HORIZONTAL);
        content.addView(metrics);
        salesValue=metricIn(metrics,"JUALAN HARI INI",blue);
        expenseValue=metricIn(metrics,"BELANJA HARI INI",0xFFFFA64D);
        metrics.getChildAt(0).setOnClickListener(v->showHistory());
        metrics.getChildAt(1).setOnClickListener(v->showHistory());

        LinearLayout profit=card();
        profit.setBackground(gradient(0xFF245FEA,0xFF8A35E8,20));
        TextView pt=text("ANGGARAN UNTUNG BERSIH HARI INI",10,0xFFE3E9FF,true);
        profit.addView(pt);
        profitValue=text("RM 0.00",27,Color.WHITE,true);
        profitValue.setPadding(0,dp(7),0,0);
        profit.addView(profitValue);
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2);
        pp.setMargins(dp(4),dp(9),dp(4),dp(16));
        content.addView(profit,pp);

        section("Tindakan pantas","Rekod aktiviti harian dengan lebih cepat.");
        LinearLayout quick=new LinearLayout(this);
        quick.setOrientation(LinearLayout.HORIZONTAL);
        content.addView(quick);
        quickAction(quick,"＋","Rekod jualan",blue,()->addSale());
        quickAction(quick,"－","Rekod belanja",0xFFFFA64D,()->addRecord(false));

        section("Pengurusan","Semua alat perniagaan dalam satu tempat.");
        LinearLayout row1=new LinearLayout(this);row1.setOrientation(LinearLayout.HORIZONTAL);content.addView(row1);
        moduleTile(row1,"▦","Produk & inventori","Urus produk dan stok",0xFF8195FF,()->showProducts());
        moduleTile(row1,"↔","Sejarah transaksi","Jualan dan belanja",0xFFB18BFF,()->showHistory());
        LinearLayout row2=new LinearLayout(this);row2.setOrientation(LinearLayout.HORIZONTAL);content.addView(row2);
        moduleTile(row2,"▤","Laporan ringkas","Prestasi kewangan",0xFF55D6B0,()->showReport());
        moduleTile(row2,"RM","Hutang & bayaran","Pelanggan dan pembekal",0xFFE08BFF,()->showDebts());
        LinearLayout row3=new LinearLayout(this);row3.setOrientation(LinearLayout.HORIZONTAL);content.addView(row3);
        moduleTile(row3,"⇩","Backup & eksport","Simpan dan eksport data",0xFF69D6D0,()->showDataTools());

        TextView foot=text("NiagaPro  •  Data disimpan dengan selamat pada peranti ini",10,muted,false);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(0,dp(20),0,dp(12));
        content.addView(foot);
    }
    private void quickAction(LinearLayout parent,String icon,String title,int color,Runnable run) {
        LinearLayout tile=card();
        tile.setGravity(Gravity.CENTER_VERTICAL);
        tile.setOrientation(LinearLayout.HORIZONTAL);
        TextView badge=text(icon,22,color,true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(shape(0x443D72FF,14));
        tile.addView(badge,new LinearLayout.LayoutParams(dp(42),dp(42)));
        TextView label=text(title,13,ink,true);
        label.setPadding(dp(10),0,0,0);
        tile.addView(label,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(82),1);
        p.setMargins(dp(4),dp(3),dp(4),dp(7));
        parent.addView(tile,p);
        tile.setOnClickListener(v->run.run());
    }
    private void moduleTile(LinearLayout parent,String icon,String title,String subtitle,int color,Runnable run) {
        LinearLayout tile=card();
        tile.setGravity(Gravity.CENTER_VERTICAL);
        tile.setOrientation(LinearLayout.VERTICAL);
        TextView badge=text(icon,20,color,true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(shape(0x443D72FF,14));
        tile.addView(badge,new LinearLayout.LayoutParams(dp(42),dp(42)));
        TextView heading=text(title,13,ink,true);
        heading.setPadding(0,dp(10),0,dp(2));
        tile.addView(heading);
        tile.addView(text(subtitle,10,muted,false));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(126),1);
        p.setMargins(dp(4),dp(3),dp(4),dp(7));
        parent.addView(tile,p);
        tile.setOnClickListener(v->run.run());
    }
    private TextView metricIn(LinearLayout parent,String title,int color) {
        LinearLayout c=card(); c.addView(text(title,10,muted,true));
        TextView v=text("RM 0.00",17,color,true); v.setPadding(0,dp(8),0,0); c.addView(v);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1); p.setMargins(dp(4),dp(5),dp(4),dp(5)); parent.addView(c,p); return v;
    }
    private void refresh() {
        buildDashboard(); SQLiteDatabase db=helper.getReadableDatabase();
        double sales=sumToday(db,"sales"), expenses=sumToday(db,"expenses"), cost=sumTodayCost(db);
        salesValue.setText(money.format(sales)); expenseValue.setText(money.format(expenses)); profitValue.setText(money.format(sales-cost-expenses));
    }
    private double sumToday(SQLiteDatabase db,String table) {
        Cursor c=db.rawQuery("SELECT COALESCE(SUM(amount),0) FROM "+table+" WHERE business_id=? AND date(created_at)=date('now','localtime')",new String[]{String.valueOf(activeBusinessId)});
        double v=0; if(c.moveToFirst())v=c.getDouble(0); c.close(); return v;
    }
    private void addSale() {
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,price,stock FROM products WHERE business_id=? ORDER BY name",new String[]{String.valueOf(activeBusinessId)});
        java.util.ArrayList<Long> ids=new java.util.ArrayList<>();
        java.util.ArrayList<String> labels=new java.util.ArrayList<>();
        java.util.ArrayList<Double> prices=new java.util.ArrayList<>();
        java.util.ArrayList<Integer> stocks=new java.util.ArrayList<>();
        ids.add(-1L); labels.add("Perkhidmatan / item lain"); prices.add(0d); stocks.add(-1);
        while(c.moveToNext()){ids.add(c.getLong(0));labels.add(c.getString(1)+" (stok "+c.getInt(3)+")");prices.add(c.getDouble(2));stocks.add(c.getInt(3));}
        c.close();
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(20),dp(8),dp(20),0);
        Spinner item=new Spinner(this);ArrayAdapter<String> items=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,labels);items.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);item.setAdapter(items);
        EditText serviceName=input("Nama perkhidmatan / item lain",android.text.InputType.TYPE_CLASS_TEXT);
        EditText qty=input("Kuantiti",2);qty.setText("1");
        EditText price=input("Harga seunit (RM)",8194);
        EditText note=input("Catatan (pilihan)",android.text.InputType.TYPE_CLASS_TEXT);
        Spinner payment=new Spinner(this);String[] methods={"Tunai","DuitNow / QR","Kad","Pindahan bank","Kredit / Hutang"};
        ArrayAdapter<String> pays=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,methods);pays.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);payment.setAdapter(pays);
        TextView total=text("Jumlah: RM 0.00",16,blue,true);
        box.addView(text("Produk atau jenis jualan",12,muted,true));box.addView(item);box.addView(serviceName);
        box.addView(text("Kuantiti",12,muted,true));box.addView(qty);
        box.addView(text("Harga seunit (RM)",12,muted,true));box.addView(price);
        box.addView(text("Kaedah pembayaran",12,muted,true));box.addView(payment);box.addView(note);box.addView(total);
        item.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> p){}
            public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){
                boolean service=ids.get(pos)==-1;serviceName.setVisibility(service?View.VISIBLE:View.GONE);
                price.setText(prices.get(pos)>0?String.valueOf(prices.get(pos)):"");
                total.setText("Jumlah: "+money.format(0));
            }
        });
        TextWatcherCalc watcher=new TextWatcherCalc(qty,price,total);
        qty.addTextChangedListener(watcher);price.addTextChangedListener(watcher);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Rekod jualan").setView(box).setNegativeButton("Batal",null).setPositiveButton("Simpan jualan",null).create();
        dlg.setOnShowListener(x->dlg.getButton(-1).setOnClickListener(v->{
            try{
                int pos=item.getSelectedItemPosition();long productId=ids.get(pos);
                int q=Integer.parseInt(qty.getText().toString().trim());
                java.math.BigDecimal unit=new java.math.BigDecimal(price.getText().toString().trim().replace(",",".")); 
                if(q<=0||unit.signum()<=0||unit.scale()>2)throw new Exception();
                String itemName=productId==-1?serviceName.getText().toString().trim():labels.get(pos).replaceAll(" \\(stok.*","");
                if(itemName.isEmpty())throw new Exception();
                java.math.BigDecimal totalAmount=unit.multiply(new java.math.BigDecimal(q)).setScale(2,java.math.RoundingMode.HALF_UP);
                SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();
                try{
                    if(productId!=-1){
                        ContentValues stockUpdate=new ContentValues();stockUpdate.put("stock",stocks.get(pos)-q);
                        int changed=db.update("products",stockUpdate,"id=? AND business_id=? AND stock>=?",new String[]{String.valueOf(productId),String.valueOf(activeBusinessId),String.valueOf(q)});
                        if(changed!=1)throw new IllegalStateException("Stok tidak mencukupi.");
                    }
                    ContentValues sale=new ContentValues();sale.put("business_id",activeBusinessId);sale.put("amount",totalAmount.doubleValue());sale.put("method",payment.getSelectedItem().toString());
                    double itemCost=0;
                    if(productId!=-1){ Cursor pc=db.rawQuery("SELECT cost FROM products WHERE id=? AND business_id=?",new String[]{String.valueOf(productId),String.valueOf(activeBusinessId)}); if(pc.moveToFirst())itemCost=pc.getDouble(0)*q; pc.close(); }
                    sale.put("cost_amount",itemCost);
                    sale.put("note",itemName+" x"+q+(note.getText().length()>0?" • "+note.getText().toString().trim():""));
                    if(db.insert("sales",null,sale)==-1)throw new IllegalStateException("Gagal simpan jualan.");
                    db.setTransactionSuccessful();
                }finally{db.endTransaction();}
                dlg.dismiss();refresh();Toast.makeText(this,"Jualan berjaya direkodkan.",Toast.LENGTH_SHORT).show();
            }catch(IllegalStateException e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}
             catch(Exception e){Toast.makeText(this,"Semak item, kuantiti dan harga jual.",Toast.LENGTH_LONG).show();}
        }));
        dlg.show();
    }
    private class TextWatcherCalc implements android.text.TextWatcher {
        private EditText q,p; private TextView out;
        TextWatcherCalc(EditText q,EditText p,TextView out){this.q=q;this.p=p;this.out=out;}
        public void beforeTextChanged(CharSequence s,int st,int count,int after){}
        public void onTextChanged(CharSequence s,int st,int before,int count){
            try{java.math.BigDecimal a=new java.math.BigDecimal(q.getText().toString());java.math.BigDecimal b=new java.math.BigDecimal(p.getText().toString().replace(",","."));out.setText("Jumlah: "+money.format(a.multiply(b).doubleValue()));}
            catch(Exception e){out.setText("Jumlah: RM 0.00");}
        }
        public void afterTextChanged(android.text.Editable e){}
    }
    private void addRecord(boolean sale) {
        EditText amount=new EditText(this); amount.setSingleLine(true); amount.setHint("Contoh: 25.50"); amount.setInputType(8194);
        EditText note=new EditText(this); note.setHint(sale?"Catatan / produk (pilihan)":"Catatan tambahan (pilihan)");
        Spinner category=new Spinner(this);
        String[] categories={"Makanan & minuman","Sewa","Bil & utiliti","Pengangkutan","Gaji / upah","Stok / bahan","Pemasaran","Peralatan","Lain-lain"};
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); category.setAdapter(adapter);
        LinearLayout box=new LinearLayout(this); box.setPadding(dp(20),dp(8),dp(20),0); box.setOrientation(LinearLayout.VERTICAL);
        box.addView(text("Jumlah (RM)",12,muted,true)); box.addView(amount);
        if(!sale){box.addView(text("Kategori perbelanjaan",12,muted,true));box.addView(category);}
        box.addView(note);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(sale?"Rekod jualan":"Rekod perbelanjaan").setView(box)
          .setNegativeButton("Batal",null).setPositiveButton("Simpan",null).create();
        dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(v->{
            try {
                String raw=amount.getText().toString().trim().replace(",",".");
                java.math.BigDecimal parsed=new java.math.BigDecimal(raw);
                if(parsed.scale()>2 || parsed.compareTo(java.math.BigDecimal.ZERO)<=0) throw new Exception();
                ContentValues cv=new ContentValues(); cv.put("business_id",activeBusinessId); cv.put("amount",parsed.doubleValue());
                if(sale) cv.put("method","Tunai"); else cv.put("category",category.getSelectedItem().toString());
                cv.put("note",note.getText().toString().trim());
                long id=helper.getWritableDatabase().insert(sale?"sales":"expenses",null,cv);
                if(id==-1) throw new Exception();
                dialog.dismiss(); refresh(); Toast.makeText(this,"Rekod berjaya disimpan.",Toast.LENGTH_SHORT).show();
            } catch(Exception e){Toast.makeText(this,"Masukkan jumlah sah. Contoh: 12.50",Toast.LENGTH_LONG).show();}
        }));
        dialog.show();
    }
    private void showHistory() {
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT 'Jualan' AS jenis,amount,note,created_at FROM sales WHERE business_id=? UNION ALL SELECT 'Belanja',amount,note,created_at FROM expenses WHERE business_id=? ORDER BY created_at DESC LIMIT 50",new String[]{String.valueOf(activeBusinessId),String.valueOf(activeBusinessId)});
        StringBuilder out=new StringBuilder();
        while(c.moveToNext()) out.append(c.getString(0)).append(" • ").append(money.format(c.getDouble(1))).append("\n")
            .append(c.getString(2)==null?"":c.getString(2)).append("\n").append(c.getString(3)).append("\n\n");
        c.close(); if(out.length()==0)out.append("Belum ada transaksi direkodkan.");
        new AlertDialog.Builder(this).setTitle("Sejarah transaksi").setMessage(out.toString()).setPositiveButton("Tutup",null).show();
    }
    private EditText input(String hint, int type) {
        EditText e=new EditText(this); e.setSingleLine(true); e.setHint(hint); e.setInputType(type); return e;
    }
    private void showProducts() {
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,price,cost,stock FROM products WHERE business_id=? ORDER BY name",new String[]{String.valueOf(activeBusinessId)});
        LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(18),dp(8),dp(18),dp(8));
        while(c.moveToNext()) {
            final long id=c.getLong(0); final String name=c.getString(1);
            final double price=c.getDouble(2), cost=c.getDouble(3); final int stock=c.getInt(4);
            LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(dp(12),dp(10),dp(12),dp(10)); row.setBackground(shape(0xFFF3F5FA,12));
            row.addView(text(name,14,ink,true));
            row.addView(text("Harga "+money.format(price)+"  •  Kos "+money.format(cost)+"  •  Stok "+stock,11,muted,false));
            row.setOnClickListener(v->productForm(id,name,price,cost,stock));
            row.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle("Padam produk?").setMessage("Produk "+name+" akan dipadam.").setNegativeButton("Batal",null).setPositiveButton("Padam",(d,w)->{helper.getWritableDatabase().delete("products","id=? AND business_id=?",new String[]{String.valueOf(id),String.valueOf(activeBusinessId)});showProducts();}).show();return true;});
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2); rp.bottomMargin=dp(7); list.addView(row,rp);
        }
        c.close();
        if(list.getChildCount()==0) list.addView(text("Belum ada produk. Tekan + untuk tambah produk pertama.",13,muted,false));
        ScrollView sc=new ScrollView(this); sc.addView(list);
        new AlertDialog.Builder(this).setTitle("Produk & inventori").setView(sc).setPositiveButton("+ Tambah produk",(d,w)->productForm(-1,"",0,0,0)).setNegativeButton("Tutup",null).show();
    }
    private void productForm(long id,String oldName,double oldPrice,double oldCost,int oldStock) {
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(20),dp(5),dp(20),0);
        EditText name=input("Nama produk", android.text.InputType.TYPE_CLASS_TEXT);
        EditText price=input("Harga jual (RM)",8194), cost=input("Kos seunit (RM)",8194), stock=input("Kuantiti stok",2);
        name.setText(oldName); if(id!=-1){price.setText(String.valueOf(oldPrice));cost.setText(String.valueOf(oldCost));stock.setText(String.valueOf(oldStock));}
        box.addView(name);box.addView(price);box.addView(cost);box.addView(stock);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle(id==-1?"Tambah produk":"Kemaskini produk").setView(box).setNegativeButton("Batal",null).setPositiveButton("Simpan",null).create();
        dlg.setOnShowListener(x->dlg.getButton(-1).setOnClickListener(v->{
            try {
                String n=name.getText().toString().trim(); if(n.isEmpty())throw new Exception();
                java.math.BigDecimal p=new java.math.BigDecimal(price.getText().toString().trim().replace(",",".")), k=new java.math.BigDecimal(cost.getText().toString().trim().replace(",","."));
                int q=Integer.parseInt(stock.getText().toString().trim());
                if(p.signum()<0||k.signum()<0||p.scale()>2||k.scale()>2||q<0)throw new Exception();
                ContentValues cv=new ContentValues();cv.put("business_id",activeBusinessId);cv.put("name",n);cv.put("price",p.doubleValue());cv.put("cost",k.doubleValue());cv.put("stock",q);
                SQLiteDatabase writable=helper.getWritableDatabase();
                long result=id==-1?writable.insert("products",null,cv):writable.update("products",cv,"id=? AND business_id=?",new String[]{String.valueOf(id),String.valueOf(activeBusinessId)});
                if(result<0)throw new Exception();
                dlg.dismiss();Toast.makeText(this,"Produk berjaya disimpan.",Toast.LENGTH_SHORT).show();showProducts();
            }catch(Exception e){Toast.makeText(this,"Semak nama, harga, kos dan kuantiti.",Toast.LENGTH_LONG).show();}
        }));
        dlg.show();
    }
    private void showDebts() {
        SQLiteDatabase db=helper.getReadableDatabase();
        double receivable=querySum(db,"SELECT COALESCE(SUM(balance),0) FROM debts WHERE business_id="+activeBusinessId+" AND party_type='PELANGGAN'");
        double payable=querySum(db,"SELECT COALESCE(SUM(balance),0) FROM debts WHERE business_id="+activeBusinessId+" AND party_type='PEMBEKAL'");
        String summary="Pelanggan berhutang kepada anda: "+money.format(receivable)+"\nAnda berhutang kepada pembekal: "+money.format(payable);
        String[] options={"Lihat hutang tertunggak","Rekod hutang baharu","Sejarah bayaran & hutang selesai"};
        new AlertDialog.Builder(this).setTitle("Hutang & bayaran").setMessage(summary)
            .setItems(options,(d,w)->{if(w==0)listDebts();else if(w==1)addDebt();else showDebtHistory();})
            .setNegativeButton("Tutup",null).show();
    }
    private void addDebt() {
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(20),dp(6),dp(20),0);
        Spinner type=new Spinner(this);String[] types={"PELANGGAN","PEMBEKAL"};
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,types);adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);type.setAdapter(adapter);
        EditText name=input("Nama pelanggan / pembekal",android.text.InputType.TYPE_CLASS_TEXT);
        EditText desc=input("Butiran / invois (pilihan)",android.text.InputType.TYPE_CLASS_TEXT);
        EditText amount=input("Jumlah hutang (RM)",8194);
        box.addView(text("Jenis rekod",12,muted,true));box.addView(type);box.addView(name);box.addView(desc);box.addView(amount);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Rekod hutang baharu").setView(box).setNegativeButton("Batal",null).setPositiveButton("Simpan",null).create();
        dlg.setOnShowListener(v->dlg.getButton(-1).setOnClickListener(btn->{
            try{
                String n=name.getText().toString().trim();java.math.BigDecimal a=new java.math.BigDecimal(amount.getText().toString().trim().replace(",","." ));
                if(n.isEmpty()||a.signum()<=0||a.scale()>2)throw new Exception();
                ContentValues cv=new ContentValues();cv.put("business_id",activeBusinessId);cv.put("party_type",type.getSelectedItem().toString());cv.put("party_name",n);cv.put("description",desc.getText().toString().trim());cv.put("original_amount",a.doubleValue());cv.put("balance",a.doubleValue());
                if(helper.getWritableDatabase().insert("debts",null,cv)<0)throw new Exception();
                dlg.dismiss();listDebts();
            }catch(Exception e){Toast.makeText(this,"Semak nama dan jumlah hutang.",Toast.LENGTH_LONG).show();}
        }));dlg.show();
    }
    private void listDebts() {
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,party_type,party_name,description,original_amount,balance FROM debts WHERE business_id=? AND balance>0 ORDER BY created_at DESC",new String[]{String.valueOf(activeBusinessId)});
        LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(dp(16),dp(8),dp(16),dp(8));
        while(c.moveToNext()){
            long id=c.getLong(0);String type=c.getString(1),name=c.getString(2),desc=c.getString(3);double original=c.getDouble(4),balance=c.getDouble(5);
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(10),dp(12),dp(10));row.setBackground(shape(0xFFF2F5FB,12));
            row.addView(text(name+" • "+type,14,ink,true));row.addView(text("Asal "+money.format(original)+" | Baki "+money.format(balance),12,blue,true));
            if(desc!=null&&!desc.isEmpty())row.addView(text(desc,11,muted,false));
            row.setOnClickListener(v->recordDebtPayment(id,name,balance));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(8);list.addView(row,lp);
        }c.close();
        if(list.getChildCount()==0)list.addView(text("Tiada hutang tertunggak.",13,muted,false));
        ScrollView sc=new ScrollView(this);sc.addView(list);
        new AlertDialog.Builder(this).setTitle("Rekod tertunggak • Tekan untuk bayar").setView(sc)
            .setPositiveButton("+ Hutang baharu",(d,w)->addDebt()).setNegativeButton("Tutup",null).show();
    }
    private void showDebtHistory() {
        SQLiteDatabase db=helper.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT d.party_type,d.party_name,d.description,d.original_amount,d.balance,p.amount,p.note,p.created_at FROM debt_payments p INNER JOIN debts d ON d.id=p.debt_id WHERE p.business_id=? AND d.business_id=? ORDER BY p.created_at DESC,p.id DESC LIMIT 200",
            new String[]{String.valueOf(activeBusinessId),String.valueOf(activeBusinessId)});
        StringBuilder out=new StringBuilder();
        while(c.moveToNext()) {
            out.append(c.getString(1)).append(" • ").append(c.getString(0)).append("\n")
              .append("Bayaran: ").append(money.format(c.getDouble(5))).append(" | Baki semasa: ").append(money.format(c.getDouble(4))).append("\n");
            if(c.getString(2)!=null&&!c.getString(2).isEmpty())out.append("Butiran: ").append(c.getString(2)).append("\n");
            if(c.getString(6)!=null&&!c.getString(6).isEmpty())out.append("Catatan: ").append(c.getString(6)).append("\n");
            out.append(c.getString(7)).append("\n\n");
        }
        c.close();
        Cursor closed=db.rawQuery("SELECT party_type,party_name,original_amount,created_at FROM debts WHERE business_id=? AND balance<=0 ORDER BY created_at DESC",
            new String[]{String.valueOf(activeBusinessId)});
        if(closed.getCount()>0) {
            out.append("HUTANG SELESAI\n");
            while(closed.moveToNext())out.append(closed.getString(1)).append(" • ").append(closed.getString(0)).append(" • ").append(money.format(closed.getDouble(2))).append("\n").append(closed.getString(3)).append("\n\n");
        }
        closed.close();
        if(out.length()==0)out.append("Belum ada rekod bayaran atau hutang selesai.");
        ScrollView sc=new ScrollView(this);TextView body=text(out.toString(),13,ink,false);body.setPadding(dp(18),dp(12),dp(18),dp(12));sc.addView(body);
        new AlertDialog.Builder(this).setTitle("Sejarah bayaran (maksimum 200 rekod)").setView(sc).setPositiveButton("Tutup",null).show();
    }
    private void recordDebtPayment(long id,String name,double balance) {
        EditText amount=input("Jumlah bayaran (maksimum "+money.format(balance)+")",8194);
        EditText note=input("Catatan bayaran (pilihan)",android.text.InputType.TYPE_CLASS_TEXT);
        LinearLayout box=new LinearLayout(this);box.setPadding(dp(20),dp(5),dp(20),0);box.setOrientation(LinearLayout.VERTICAL);
        box.addView(text("Bayaran untuk "+name,13,ink,true));box.addView(amount);box.addView(note);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Rekod bayaran").setView(box).setNegativeButton("Batal",null).setPositiveButton("Simpan bayaran",null).create();
        dlg.setOnShowListener(v->dlg.getButton(-1).setOnClickListener(btn->{
            try{
                java.math.BigDecimal a=new java.math.BigDecimal(amount.getText().toString().trim().replace(",","." ));
                if(a.signum()<=0||a.scale()>2||a.doubleValue()>balance+0.000001)throw new Exception();
                SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();
                try{
                    ContentValues pay=new ContentValues();pay.put("business_id",activeBusinessId);pay.put("debt_id",id);pay.put("amount",a.doubleValue());pay.put("note",note.getText().toString().trim());
                    if(db.insert("debt_payments",null,pay)<0)throw new Exception();
                    ContentValues update=new ContentValues();update.put("balance",Math.max(0,balance-a.doubleValue()));
                    if(db.update("debts",update,"id=? AND business_id=?",new String[]{String.valueOf(id),String.valueOf(activeBusinessId)})!=1)throw new Exception();
                    db.setTransactionSuccessful();
                }finally{db.endTransaction();}
                dlg.dismiss();listDebts();Toast.makeText(this,"Bayaran berjaya direkodkan.",Toast.LENGTH_SHORT).show();
            }catch(Exception e){Toast.makeText(this,"Jumlah bayaran tidak sah atau melebihi baki.",Toast.LENGTH_LONG).show();}
        }));dlg.show();
    }
    private double sumTodayCost(SQLiteDatabase db) {
        Cursor c=db.rawQuery("SELECT COALESCE(SUM(cost_amount),0) FROM sales WHERE business_id=? AND date(created_at)=date('now','localtime')",new String[]{String.valueOf(activeBusinessId)});
        double v=0;if(c.moveToFirst())v=c.getDouble(0);c.close();return v;
    }
    private void showReport() {
        final String[] periods={"Hari ini","7 hari terakhir","Bulan ini","Keseluruhan"};
        new AlertDialog.Builder(this).setTitle("Pilih tempoh laporan").setItems(periods,(dialog,which)->renderReport(which)).setNegativeButton("Tutup",null).show();
    }
    private void renderReport(int period) {
        String where;
        if(period==0) where="business_id="+activeBusinessId+" AND date(created_at)=date('now','localtime')";
        else if(period==1) where="business_id="+activeBusinessId+" AND datetime(created_at)>=datetime('now','localtime','-6 days')";
        else if(period==2) where="business_id="+activeBusinessId+" AND strftime('%Y-%m',created_at)=strftime('%Y-%m','now','localtime')";
        else where="business_id="+activeBusinessId;
        SQLiteDatabase db=helper.getReadableDatabase();
        double sales=querySum(db,"SELECT COALESCE(SUM(amount),0) FROM sales WHERE "+where);
        double cost=querySum(db,"SELECT COALESCE(SUM(cost_amount),0) FROM sales WHERE "+where);
        double expenses=querySum(db,"SELECT COALESCE(SUM(amount),0) FROM expenses WHERE "+where);
        double gross=sales-cost, net=gross-expenses;
        String[] labels={"Hari ini","7 hari terakhir","Bulan ini","Keseluruhan"};
        String msg="Tempoh: "+labels[period]+"\n\nJualan: "+money.format(sales)+"\nKos produk terjual: "+money.format(cost)+"\nUntung kasar: "+money.format(gross)+"\nPerbelanjaan: "+money.format(expenses)+"\n--------------------\nUNTUNG BERSIH: "+money.format(net)+"\n\nNota: Kos produk dikira berdasarkan kos seunit yang direkod ketika jualan. Jualan lama sebelum kemas kini ini mungkin tiada rekod kos.";
        new AlertDialog.Builder(this).setTitle("Laporan untung rugi").setMessage(msg).setPositiveButton("Tukar tempoh",(d,w)->showReport()).setNegativeButton("Tutup",null).show();
    }
    private double querySum(SQLiteDatabase db,String sql) {
        Cursor c=db.rawQuery(sql,null);double v=0;if(c.moveToFirst())v=c.getDouble(0);c.close();return v;
    }


    private void loadActiveProfile() {
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT name FROM business_profiles WHERE id=?",new String[]{String.valueOf(activeBusinessId)});
        if(c.moveToFirst())activeBusinessName=c.getString(0);
        else {activeBusinessId=1;activeBusinessName="Perniagaan Saya";profilePrefs.edit().putLong("active_id",1).apply();}
        c.close();
    }
    private void showProfiles() {
        java.util.ArrayList<Long> ids=new java.util.ArrayList<>();
        java.util.ArrayList<String> names=new java.util.ArrayList<>();
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,business_type FROM business_profiles ORDER BY id",null);
        while(c.moveToNext()){ids.add(c.getLong(0));names.add(c.getString(1)+" • "+c.getString(2)+(c.getLong(0)==activeBusinessId?" ✓":""));}
        c.close();
        new AlertDialog.Builder(this).setTitle("Profil perniagaan")
          .setItems(names.toArray(new String[0]),(d,w)->{activeBusinessId=ids.get(w);profilePrefs.edit().putLong("active_id",activeBusinessId).apply();loadActiveProfile();refresh();})
          .setPositiveButton("+ Tambah profil",(d,w)->addBusinessProfile())
          .setNeutralButton("Urus profil",(d,w)->manageBusinessProfile()).setNegativeButton("Tutup",null).show();
    }
    private void addBusinessProfile() {
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM business_profiles",null);c.moveToFirst();int total=c.getInt(0);c.close();
        if(total>=5){Toast.makeText(this,"Maksimum 5 profil peranti ini.",Toast.LENGTH_LONG).show();return;}
        LinearLayout box=new LinearLayout(this);box.setPadding(dp(20),dp(8),dp(20),0);box.setOrientation(LinearLayout.VERTICAL);
        EditText name=input("Nama perniagaan",android.text.InputType.TYPE_CLASS_TEXT);
        Spinner type=new Spinner(this);String[] types={"Retail / Shop","Food & Beverage","Online Business","Service Business","Mixed Business"};
        ArrayAdapter<String> ad=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,types);ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);type.setAdapter(ad);
        box.addView(name);box.addView(type);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Tambah profil perniagaan").setView(box).setNegativeButton("Batal",null).setPositiveButton("Simpan",null).create();
        dlg.setOnShowListener(v->dlg.getButton(-1).setOnClickListener(btn->{
            String n=name.getText().toString().trim();if(n.isEmpty()){name.setError("Masukkan nama");return;}
            ContentValues cv=new ContentValues();cv.put("name",n);cv.put("business_type",type.getSelectedItem().toString());
            long id=helper.getWritableDatabase().insert("business_profiles",null,cv);
            if(id<0){Toast.makeText(this,"Gagal tambah profil.",Toast.LENGTH_SHORT).show();return;}
            dlg.dismiss();activeBusinessId=id;profilePrefs.edit().putLong("active_id",id).apply();loadActiveProfile();refresh();
        }));dlg.show();
    }
    private void manageBusinessProfile() {
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,business_type FROM business_profiles WHERE id=?",new String[]{String.valueOf(activeBusinessId)});
        if(!c.moveToFirst()){c.close();return;}long id=c.getLong(0);String oldName=c.getString(1),oldType=c.getString(2);c.close();
        LinearLayout box=new LinearLayout(this);box.setPadding(dp(20),dp(8),dp(20),0);box.setOrientation(LinearLayout.VERTICAL);
        EditText name=input("Nama perniagaan",android.text.InputType.TYPE_CLASS_TEXT);name.setText(oldName);
        Spinner type=new Spinner(this);String[] types={"Retail / Shop","Food & Beverage","Online Business","Service Business","Mixed Business"};
        ArrayAdapter<String> ad=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,types);ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);type.setAdapter(ad);
        for(int i=0;i<types.length;i++)if(types[i].equals(oldType))type.setSelection(i);
        box.addView(name);box.addView(type);
        new AlertDialog.Builder(this).setTitle("Kemaskini profil").setView(box).setNegativeButton("Batal",null)
          .setNeutralButton("Padam profil",(d,w)->confirmDeleteProfile(id,oldName))
          .setPositiveButton("Simpan",(d,w)->{String n=name.getText().toString().trim();if(n.isEmpty()){Toast.makeText(this,"Nama tidak boleh kosong.",Toast.LENGTH_SHORT).show();return;}ContentValues cv=new ContentValues();cv.put("name",n);cv.put("business_type",type.getSelectedItem().toString());helper.getWritableDatabase().update("business_profiles",cv,"id=?",new String[]{String.valueOf(id)});loadActiveProfile();refresh();}).show();
    }
    private void confirmDeleteProfile(long id,String name) {
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM business_profiles",null);c.moveToFirst();int total=c.getInt(0);c.close();
        if(total<=1){Toast.makeText(this,"Sekurang-kurangnya satu profil mesti dikekalkan.",Toast.LENGTH_LONG).show();return;}
        new AlertDialog.Builder(this).setTitle("Padam "+name+"?").setMessage("Semua jualan, belanja, produk dan hutang profil ini akan dipadam secara kekal.")
          .setNegativeButton("Batal",null).setPositiveButton("Padam",(d,w)->{
            SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();
            try{db.delete("debt_payments","business_id=?",new String[]{String.valueOf(id)});db.delete("debts","business_id=?",new String[]{String.valueOf(id)});db.delete("sales","business_id=?",new String[]{String.valueOf(id)});db.delete("expenses","business_id=?",new String[]{String.valueOf(id)});db.delete("products","business_id=?",new String[]{String.valueOf(id)});db.delete("business_profiles","id=?",new String[]{String.valueOf(id)});db.setTransactionSuccessful();}
            finally{db.endTransaction();}
            if(activeBusinessId==id){Cursor next=db.rawQuery("SELECT id FROM business_profiles ORDER BY id LIMIT 1",null);if(next.moveToFirst())activeBusinessId=next.getLong(0);next.close();profilePrefs.edit().putLong("active_id",activeBusinessId).apply();}
            loadActiveProfile();refresh();
          }).show();
    }

    private static final int REQ_BACKUP=7101, REQ_RESTORE=7102, REQ_CSV=7103, REQ_PDF=7104;
    private void showDataTools() {
        String[] options={"Backup semua data (.db)","Restore backup (.db)","Eksport Excel-compatible (.csv)","Eksport laporan PDF"};
        new AlertDialog.Builder(this).setTitle("Backup & eksport").setItems(options,(d,w)->{
            if(w==0) createDocument(REQ_BACKUP,"application/octet-stream","niagapro-backup.db");
            else if(w==1) openDocument(REQ_RESTORE);
            else if(w==2) createDocument(REQ_CSV,"text/csv","niagapro-data.csv");
            else createDocument(REQ_PDF,"application/pdf","niagapro-report.pdf");
        }).setNegativeButton("Tutup",null).show();
    }
    private void createDocument(int request,String mime,String name) {
        android.content.Intent i=new android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(android.content.Intent.CATEGORY_OPENABLE);i.setType(mime);
        i.putExtra(android.content.Intent.EXTRA_TITLE,name);startActivityForResult(i,request);
    }
    private void openDocument(int request) {
        android.content.Intent i=new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(android.content.Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,request);
    }
    @Override protected void onActivityResult(int request,int result,android.content.Intent data) {
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK||data==null||data.getData()==null)return;
        android.net.Uri uri=data.getData();
        try {
            if(request==REQ_BACKUP) {
                helper.getReadableDatabase().execSQL("PRAGMA wal_checkpoint(FULL)");
                helper.close();
                copyStream(new java.io.FileInputStream(getDatabasePath("niagapro.db")),getContentResolver().openOutputStream(uri));
                helper=new BusinessDb(this);
                Toast.makeText(this,"Backup berjaya disimpan.",Toast.LENGTH_LONG).show();
            } else if(request==REQ_RESTORE) {
                java.io.File temp=new java.io.File(getCacheDir(),"restore-niagapro.db");
                copyStream(getContentResolver().openInputStream(uri),new java.io.FileOutputStream(temp));
                android.database.sqlite.SQLiteDatabase check=android.database.sqlite.SQLiteDatabase.openDatabase(temp.getAbsolutePath(),null,android.database.sqlite.SQLiteDatabase.OPEN_READONLY);
                Cursor verify=check.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name IN ('sales','expenses','products','debts','debt_payments')",null);
                int found=verify.getCount();verify.close();check.close();
                if(found<5)throw new Exception("Fail backup tidak sah atau versi tidak serasi.");
                helper.close();
                java.io.File target=getDatabasePath("niagapro.db");
                new java.io.File(target.getAbsolutePath()+"-wal").delete();new java.io.File(target.getAbsolutePath()+"-shm").delete();
                java.io.File parent=target.getParentFile();if(parent!=null)parent.mkdirs();
                if(!temp.renameTo(target)){copyStream(new java.io.FileInputStream(temp),new java.io.FileOutputStream(target));temp.delete();}
                helper=new BusinessDb(this);refresh();
                Toast.makeText(this,"Data berjaya dipulihkan.",Toast.LENGTH_LONG).show();
            } else if(request==REQ_CSV) {
                writeCsv(uri);Toast.makeText(this,"Fail CSV berjaya dieksport.",Toast.LENGTH_LONG).show();
            } else if(request==REQ_PDF) {
                writePdf(uri);Toast.makeText(this,"Laporan PDF berjaya dieksport.",Toast.LENGTH_LONG).show();
            }
        } catch(Exception e) {
            try{helper=new BusinessDb(this);}catch(Exception ignored){}
            Toast.makeText(this,"Operasi gagal: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
    private void copyStream(java.io.InputStream in,java.io.OutputStream out)throws java.io.IOException {
        if(in==null||out==null)throw new java.io.IOException("Tidak dapat membuka fail.");
        try(java.io.InputStream src=in;java.io.OutputStream dst=out){byte[] b=new byte[8192];int n;while((n=src.read(b))!=-1)dst.write(b,0,n);dst.flush();}
    }
    private void writeCsv(android.net.Uri uri)throws Exception {
        java.io.OutputStream out=getContentResolver().openOutputStream(uri);
        if(out==null)throw new java.io.IOException("Tidak dapat membuka fail.");
        java.io.OutputStreamWriter w=new java.io.OutputStreamWriter(out,java.nio.charset.Charset.forName("UTF-8"));
        w.write("\uFEFFJENIS,ID,TARIKH,JUMLAH,KOS,KAEDAH/KATEGORI,CATATAN/NAMA,BAKI\r\n");
        SQLiteDatabase db=helper.getReadableDatabase();
        exportRows(w,db,"SELECT id,created_at,amount,cost_amount,method,note,'' FROM sales WHERE business_id="+activeBusinessId+" ORDER BY id","JUALAN");
        exportRows(w,db,"SELECT id,created_at,amount,0,category,note,'' FROM expenses WHERE business_id="+activeBusinessId+" ORDER BY id","BELANJA");
        exportRows(w,db,"SELECT id,created_at,original_amount,0,party_type,party_name,balance FROM debts WHERE business_id="+activeBusinessId+" ORDER BY id","HUTANG");
        w.flush();w.close();
    }
    private void exportRows(java.io.Writer w,SQLiteDatabase db,String sql,String type)throws Exception {
        Cursor c=db.rawQuery(sql,null);
        try{while(c.moveToNext()){
            StringBuilder line=new StringBuilder(type);
            for(int i=0;i<7;i++){String value=c.isNull(i)?"":c.getString(i);line.append(',').append('"').append(value.replace("\"","\"\"")).append('"');}
            w.write(line.append("\r\n").toString());
        }}finally{c.close();}
    }
    private void writePdf(android.net.Uri uri)throws Exception {
        SQLiteDatabase db=helper.getReadableDatabase();
        double sales=querySum(db,"SELECT COALESCE(SUM(amount),0) FROM sales WHERE business_id="+activeBusinessId);
        double cost=querySum(db,"SELECT COALESCE(SUM(cost_amount),0) FROM sales WHERE business_id="+activeBusinessId);
        double expenses=querySum(db,"SELECT COALESCE(SUM(amount),0) FROM expenses WHERE business_id="+activeBusinessId);
        double receivable=querySum(db,"SELECT COALESCE(SUM(balance),0) FROM debts WHERE business_id="+activeBusinessId+" AND party_type='PELANGGAN'");
        double payable=querySum(db,"SELECT COALESCE(SUM(balance),0) FROM debts WHERE business_id="+activeBusinessId+" AND party_type='PEMBEKAL'");
        android.graphics.pdf.PdfDocument pdf=new android.graphics.pdf.PdfDocument();
        android.graphics.pdf.PdfDocument.PageInfo info=new android.graphics.pdf.PdfDocument.PageInfo.Builder(595,842,1).create();
        android.graphics.pdf.PdfDocument.Page page=pdf.startPage(info);android.graphics.Canvas canvas=page.getCanvas();
        android.graphics.Paint paint=new android.graphics.Paint(3);paint.setColor(navy);paint.setTextSize(25);paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("NiagaPro",40,60,paint);paint.setTextSize(12);paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Laporan perniagaan • "+new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(new Date()),40,85,paint);
        String[] rows={"Jualan: "+money.format(sales),"Kos produk terjual: "+money.format(cost),"Untung kasar: "+money.format(sales-cost),"Perbelanjaan: "+money.format(expenses),"UNTUNG BERSIH: "+money.format(sales-cost-expenses),"","Hutang pelanggan: "+money.format(receivable),"Hutang pembekal: "+money.format(payable)};
        int y=140;for(String row:rows){paint.setTextSize(row.startsWith("UNTUNG")?17:14);paint.setTypeface(row.startsWith("UNTUNG")?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);canvas.drawText(row,40,y,paint);y+=row.isEmpty()?18:32;}
        paint.setTextSize(9);paint.setColor(Color.GRAY);canvas.drawText("Dijana oleh NiagaPro. Laporan berdasarkan rekod yang tersimpan pada peranti.",40,800,paint);
        pdf.finishPage(page);java.io.OutputStream out=getContentResolver().openOutputStream(uri);if(out==null)throw new java.io.IOException("Tidak dapat membuka fail.");
        try{pdf.writeTo(out);}finally{out.close();pdf.close();}
    }

    @Override protected void onDestroy(){if(helper!=null)helper.close();super.onDestroy();}
}
