package uz.altemur.crm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private DB db;
    private LinearLayout list;
    private EditText nameInput, phoneInput, boilerInput, noteInput, followInput;
    private Spinner statusInput;
    private final String[] statuses = {"Yangi mijoz", "Qiziqish bildirdi", "O‘ylab ko‘radi", "Qayta qo‘ng‘iroq qilish", "Sotildi", "Javob bermadi"};
    private int green = Color.rgb(7, 134, 75);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); db = new DB(this);
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(28, 24, 28, 32); root.setBackgroundColor(Color.rgb(246,248,247));
        scroll.addView(root); setContentView(scroll);
        TextView title = new TextView(this); title.setText("🔥 AL TEMUR CRM"); title.setTextSize(25); title.setTypeface(null, Typeface.BOLD); title.setTextColor(green); root.addView(title);
        TextView sub = new TextView(this); sub.setText("Katyol mijozlari va qo‘ng‘iroqlar tarixi"); sub.setTextSize(14); sub.setTextColor(Color.DKGRAY); sub.setPadding(0,4,0,18); root.addView(sub);
        TextView formTitle = header("YANGI MIJOZ"); root.addView(formTitle);
        nameInput = field("Ismi (ixtiyoriy)", false); root.addView(nameInput);
        phoneInput = field("Telefon raqami *", false); phoneInput.setInputType(InputType.TYPE_CLASS_PHONE); root.addView(phoneInput);
        boilerInput = field("Qiziqqan katyol (masalan, 50 kW)", false); root.addView(boilerInput);
        noteInput = field("Nima so‘radi? Narx, yetkazish, to‘lov...", true); root.addView(noteInput);
        statusInput = new Spinner(this); statusInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses)); root.addView(statusInput);
        followInput = field("Qayta qo‘ng‘iroq vaqti (ixtiyoriy)", false); followInput.setHint("Masalan: 12.10.2026 10:30"); root.addView(followInput);
        Button save = button("MIJOZNI SAQLASH"); root.addView(save); save.setOnClickListener(v -> saveCustomer());
        Button clear = button("Maydonlarni tozalash"); clear.setBackgroundColor(Color.rgb(90,100,95)); root.addView(clear); clear.setOnClickListener(v -> clearForm());
        root.addView(header("SAQLANGAN MIJOZLAR"));
        EditText search = field("Ism yoki telefon bo‘yicha qidirish", false); root.addView(search);
        list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); root.addView(list);
        search.addTextChangedListener(new android.text.TextWatcher(){ public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int before,int count){ render(s.toString()); } public void afterTextChanged(android.text.Editable e){} });
        render("");
    }
    private TextView header(String text){ TextView t=new TextView(this); t.setText(text); t.setTextSize(16); t.setTypeface(null,Typeface.BOLD); t.setTextColor(Color.rgb(30,45,37)); t.setPadding(0,18,0,8); return t; }
    private EditText field(String hint, boolean multi){ EditText e=new EditText(this); e.setSingleLine(!multi); e.setHint(hint); e.setTextSize(15); e.setPadding(16,10,16,10); e.setBackgroundColor(Color.WHITE); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,multi?110:54); p.setMargins(0,5,0,5); e.setLayoutParams(p); if(multi){e.setGravity( android.view.Gravity.TOP); e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);} return e; }
    private Button button(String text){ Button b=new Button(this); b.setText(text); b.setTextColor(Color.WHITE); b.setAllCaps(false); b.setTextSize(15); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(green)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,54); p.setMargins(0,7,0,5); b.setLayoutParams(p); return b; }
    private void saveCustomer(){
        String phone=phoneInput.getText().toString().trim(); if(phone.isEmpty()){phoneInput.setError("Telefon raqamini kiriting"); return;}
        String now=new SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.getDefault()).format(new Date());
        ContentValues v=new ContentValues(); v.put("name",nameInput.getText().toString().trim()); v.put("phone",phone); v.put("boiler",boilerInput.getText().toString().trim()); v.put("note",noteInput.getText().toString().trim()); v.put("status",statuses[statusInput.getSelectedItemPosition()]); v.put("created",now); v.put("follow",followInput.getText().toString().trim());
        db.getWritableDatabase().insert("customers",null,v); Toast.makeText(this,"Mijoz saqlandi",Toast.LENGTH_SHORT).show(); clearForm(); render("");
    }
    private void clearForm(){ nameInput.setText(""); phoneInput.setText(""); boilerInput.setText(""); noteInput.setText(""); followInput.setText(""); statusInput.setSelection(0); }
    private void render(String query){
        if(list==null)return; list.removeAllViews(); Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name,phone,boiler,note,status,created,follow FROM customers WHERE name LIKE ? OR phone LIKE ? ORDER BY id DESC",new String[]{"%"+query+"%","%"+query+"%"});
        if(c.getCount()==0){TextView empty=new TextView(this);empty.setText("Hozircha mijoz yo‘q. Birinchi mijozni yuqoridan qo‘shing.");empty.setPadding(0,12,0,12);empty.setTextColor(Color.GRAY);list.addView(empty);}
        while(c.moveToNext()){
            final String phone=c.getString(2), name=c.getString(1), boiler=c.getString(3), note=c.getString(4), status=c.getString(5), created=c.getString(6), follow=c.getString(7); final long id=c.getLong(0);
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(16,12,16,12);card.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,6,0,6);card.setLayoutParams(cp);
            TextView nm=new TextView(this);nm.setText((name.isEmpty()?"Nomsiz mijoz":name)+"  •  "+status);nm.setTextSize(16);nm.setTypeface(null,Typeface.BOLD);nm.setTextColor(green);card.addView(nm);
            TextView details=new TextView(this);details.setText("☎ "+phone+"\nKatyol: "+(boiler.isEmpty()?"Ko‘rsatilmagan":boiler)+"\nIzoh: "+(note.isEmpty()?"Yo‘q":note)+"\nKiritilgan: "+created+(follow.isEmpty()?"":"\nQayta qo‘ng‘iroq: "+follow));details.setTextSize(14);details.setTextColor(Color.DKGRAY);details.setPadding(0,6,0,8);card.addView(details);
            LinearLayout actions=new LinearLayout(this); Button call=new Button(this);call.setText("Qo‘ng‘iroq");call.setAllCaps(false);call.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phone));startActivity(i);}); actions.addView(call,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
            Button history=new Button(this);history.setText("Tarix / o‘chirish");history.setAllCaps(false);history.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(name.isEmpty()?phone:name).setMessage("Telefon: "+phone+"\nKiritilgan: "+created+"\nHolat: "+status+"\n\nIzoh: "+note+"\n\nQayta qo‘ng‘iroq: "+(follow.isEmpty()?"Belgilanmagan":follow)).setPositiveButton("Yopish",(d,w)->{}).setNegativeButton("O‘chirish",(d,w)->{db.getWritableDatabase().delete("customers","id=?",new String[]{String.valueOf(id)});render("");}).show());actions.addView(history,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));card.addView(actions);list.addView(card);
        } c.close();
    }
    static class DB extends SQLiteOpenHelper { DB(Activity a){super(a,"al_temur_crm.db",null,1);} public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,phone TEXT,boiler TEXT,note TEXT,status TEXT,created TEXT,follow TEXT)");} public void onUpgrade(SQLiteDatabase d,int o,int n){} }
}
