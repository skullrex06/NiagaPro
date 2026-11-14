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
        dateLabel=text(new SimpleDateFormat("EEEE, d MMMM yyyy",new Locale("ms","MY")).format(new Date()),13,0xFFD5DDF0,false); dateLabel.setPadding(0,dp(22),0,0); header.addView(dateLabel);
        TextView welcome=text("Urus bisnes dengan lebih mudah.",20,Color.WHITE,true); welcome.setPadding(0,dp(4),0,0); header.addView(welcome);
        root.addView(header);
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(18),dp(18),dp(18),dp(64)); content.setClipToPadding(false);
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
        metrics.getChildAt(0).setOnClickListener(v->showHistory());
        metrics.getChildAt(1).setOnClickListener(v->showHistory());
        LinearLayout profit=card(); profit.setBackground(gradient(0xFF315FEA,0xFF684CE6,18));
        TextView pt=text("UNTUNG BERSIH HARI INI",11,0xFFE3E9FF,true); profit.addView(pt);
        profitValue=text("RM 0.00",25,Color.WHITE,true); profitValue.setPadding(0,dp(8),0,0); profit.addView(profitValue);
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2); pp.setMargins(dp(4),dp(5),dp(4),dp(8)); content.addView(profit,pp);
        section("Urus niaga","Rekod aktiviti harian dengan pantas.");
        action("+","Rekod jualan","Jual produk atau rekod perkhidmatan",blue,()->addSale());
        action("−","Rekod perbelanjaan","Catat kos operasi perniagaan",0xFFE28A36,()->addRecord(false));
        section("Pengurusan","Modul perniagaan.");
        action("▦","Produk & inventori","Tambah, edit, padam dan kemas kini stok",blue,()->showProducts());
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
    private void addSale() {
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,price,stock FROM products ORDER BY name",null);
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
                        int changed=db.update("products",stockUpdate,"id=? AND stock>=?",new String[]{String.valueOf(productId),String.valueOf(q)});
                        if(changed!=1)throw new IllegalStateException("Stok tidak mencukupi.");
                    }
                    ContentValues sale=new ContentValues();sale.put("amount",totalAmount.doubleValue());sale.put("method",payment.getSelectedItem().toString());
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
                ContentValues cv=new ContentValues(); cv.put("amount",parsed.doubleValue());
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
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT 'Jualan' AS jenis,amount,note,created_at FROM sales UNION ALL SELECT 'Belanja',amount,note,created_at FROM expenses ORDER BY created_at DESC LIMIT 50",null);
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
        Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,price,cost,stock FROM products ORDER BY name",null);
        LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(18),dp(8),dp(18),dp(8));
        while(c.moveToNext()) {
            final long id=c.getLong(0); final String name=c.getString(1);
            final double price=c.getDouble(2), cost=c.getDouble(3); final int stock=c.getInt(4);
            LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(dp(12),dp(10),dp(12),dp(10)); row.setBackground(shape(0xFFF3F5FA,12));
            row.addView(text(name,14,ink,true));
            row.addView(text("Harga "+money.format(price)+"  •  Kos "+money.format(cost)+"  •  Stok "+stock,11,muted,false));
            row.setOnClickListener(v->productForm(id,name,price,cost,stock));
            row.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle("Padam produk?").setMessage("Produk "+name+" akan dipadam.").setNegativeButton("Batal",null).setPositiveButton("Padam",(d,w)->{helper.getWritableDatabase().delete("products","id=?",new String[]{String.valueOf(id)});showProducts();}).show();return true;});
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
                ContentValues cv=new ContentValues();cv.put("name",n);cv.put("price",p.doubleValue());cv.put("cost",k.doubleValue());cv.put("stock",q);
                SQLiteDatabase writable=helper.getWritableDatabase();
                long result=id==-1?writable.insert("products",null,cv):writable.update("products",cv,"id=?",new String[]{String.valueOf(id)});
                if(result<0)throw new Exception();
                dlg.dismiss();Toast.makeText(this,"Produk berjaya disimpan.",Toast.LENGTH_SHORT).show();showProducts();
            }catch(Exception e){Toast.makeText(this,"Semak nama, harga, kos dan kuantiti.",Toast.LENGTH_LONG).show();}
        }));
        dlg.show();
    }
    private void showReport() {
        SQLiteDatabase db=helper.getReadableDatabase(); double s=sumToday(db,"sales"), e=sumToday(db,"expenses");
        String msg="Jualan hari ini: "+money.format(s)+"\nPerbelanjaan hari ini: "+money.format(e)+"\nUntung bersih: "+money.format(s-e)+"\n\nLaporan ini berdasarkan rekod hari ini sahaja.";
        new AlertDialog.Builder(this).setTitle("Laporan ringkas").setMessage(msg).setPositiveButton("Tutup",null).show();
    }
    @Override protected void onDestroy(){if(helper!=null)helper.close();super.onDestroy();}
}
