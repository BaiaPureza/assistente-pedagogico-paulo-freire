package br.com.baiapureza.assistentepedagogico;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;

public class Database extends SQLiteOpenHelper {
    public Database(Context c){super(c,"assistente_pedagogico.db",null,1);}
    public void onCreate(SQLiteDatabase d){
        d.execSQL("CREATE TABLE documentos(id INTEGER PRIMARY KEY AUTOINCREMENT,tipo TEXT,titulo TEXT,turma TEXT,componente TEXT,habilidades TEXT,data TEXT,conteudo TEXT,criado_em TEXT DEFAULT CURRENT_TIMESTAMP)");
    }
    public void onUpgrade(SQLiteDatabase d,int oldVersion,int newVersion){}
    public long salvar(String tipo,String titulo,String turma,String componente,String habilidades,String data,String conteudo){
        ContentValues v=new ContentValues();v.put("tipo",tipo);v.put("titulo",titulo);v.put("turma",turma);v.put("componente",componente);v.put("habilidades",habilidades);v.put("data",data);v.put("conteudo",conteudo);
        return getWritableDatabase().insert("documentos",null,v);
    }
    public Cursor listar(){return getReadableDatabase().rawQuery("SELECT id,tipo,CASE WHEN tipo='RELATORIO' THEN titulo ELSE componente END,data FROM documentos ORDER BY id DESC",null);}
    public Cursor obter(long id){return getReadableDatabase().rawQuery("SELECT id,tipo,titulo,turma,componente,habilidades,data,conteudo FROM documentos WHERE id=?",new String[]{String.valueOf(id)});}
}