package br.com.baiapureza.assistentepedagogico;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;

public class Database extends SQLiteOpenHelper {
    public Database(Context c){super(c,"assistente_pedagogico.db",null,2);}
    public void onCreate(SQLiteDatabase d){
        d.execSQL("CREATE TABLE documentos(id INTEGER PRIMARY KEY AUTOINCREMENT,tipo TEXT,titulo TEXT,turma TEXT,componente TEXT,habilidades TEXT,data TEXT,conteudo TEXT,criado_em TEXT DEFAULT CURRENT_TIMESTAMP)");
        criarTabelaVersoes(d);
    }
    public void onUpgrade(SQLiteDatabase d,int oldVersion,int newVersion){
        if(oldVersion<2)criarTabelaVersoes(d);
    }
    void criarTabelaVersoes(SQLiteDatabase d){
        d.execSQL("CREATE TABLE IF NOT EXISTS documento_versoes(id INTEGER PRIMARY KEY AUTOINCREMENT,documento_id INTEGER,tipo TEXT,titulo TEXT,turma TEXT,componente TEXT,habilidades TEXT,data TEXT,conteudo TEXT,salvo_em TEXT DEFAULT CURRENT_TIMESTAMP)");
    }
    public long salvar(String tipo,String titulo,String turma,String componente,String habilidades,String data,String conteudo){
        ContentValues v=new ContentValues();v.put("tipo",tipo);v.put("titulo",titulo);v.put("turma",turma);v.put("componente",componente);v.put("habilidades",habilidades);v.put("data",data);v.put("conteudo",conteudo);
        return getWritableDatabase().insert("documentos",null,v);
    }
    public Cursor listarPorTipo(String tipo,String filtro){
        String termo="%"+(filtro==null?"":filtro.trim())+"%";
        String ordem=tipo.equals("RELATORIO")?"titulo COLLATE NOCASE ASC, id DESC":"turma COLLATE NOCASE ASC, componente COLLATE NOCASE ASC, id DESC";
        return getReadableDatabase().rawQuery(
            "SELECT id,titulo,turma,componente,data FROM documentos WHERE tipo=? AND (titulo LIKE ? OR turma LIKE ? OR componente LIKE ? OR data LIKE ?) ORDER BY "+ordem,
            new String[]{tipo,termo,termo,termo,termo}
        );
    }
    public int atualizar(long id,String titulo,String turma,String componente,String habilidades,String data,String conteudo){
        SQLiteDatabase d=getWritableDatabase();
        d.beginTransaction();
        try{
            d.execSQL("INSERT INTO documento_versoes(documento_id,tipo,titulo,turma,componente,habilidades,data,conteudo) SELECT id,tipo,titulo,turma,componente,habilidades,data,conteudo FROM documentos WHERE id=?",new Object[]{id});
            ContentValues v=new ContentValues();v.put("titulo",titulo);v.put("turma",turma);v.put("componente",componente);v.put("habilidades",habilidades);v.put("data",data);v.put("conteudo",conteudo);
            int n=d.update("documentos",v,"id=?",new String[]{String.valueOf(id)});
            d.setTransactionSuccessful();
            return n;
        }finally{
            d.endTransaction();
        }
    }
    public Cursor listarVersoes(long id){
        return getReadableDatabase().rawQuery("SELECT id,titulo,data,salvo_em FROM documento_versoes WHERE documento_id=? ORDER BY id DESC",new String[]{String.valueOf(id)});
    }
    public Cursor obterVersao(long id){
        return getReadableDatabase().rawQuery("SELECT id,tipo,titulo,turma,componente,habilidades,data,conteudo FROM documento_versoes WHERE id=?",new String[]{String.valueOf(id)});
    }
    public Cursor obter(long id){return getReadableDatabase().rawQuery("SELECT id,tipo,titulo,turma,componente,habilidades,data,conteudo FROM documentos WHERE id=?",new String[]{String.valueOf(id)});}
}
