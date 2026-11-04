package br.com.baiapureza.assistentepedagogico;

import android.app.*;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.*;

public class MainActivity extends Activity {
    Database db;
    LinearLayout root;
    final int azul=0xFF2876B8, fundo=0xFFF6F8FA;
    final String escola="ESCOLA MUNICIPAL PAULO FREIRE";
    final String professora="Professora: Marilene Maria da Silva";
    final String[] componentes={
        "Ciências","História","Língua Portuguesa","Geografia","Ensino Religioso",
        "Educação Digital","Atividade Complementar em Ciências Naturais",
        "Atividade Complementar em Ciências Humanas","Cultura, Etnia, Raça e Saberes em Arte",
        "Recomposição da Aprendizagem Língua Portuguesa"
    };
    final String turmaPadrao="Ensino Fundamental de 9 Anos - 2º Ano";

    @Override public void onCreate(Bundle b){ super.onCreate(b); configurarBarrasSistema(); db=new Database(this); inicio(); }
    void configurarBarrasSistema(){
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat controller=new WindowInsetsControllerCompat(getWindow(),getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
    }
    int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    TextView texto(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(0xFF24292F);return t;}
    Button botao(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);return b;}
    void base(String titulo){
        ScrollView sc=new ScrollView(this); root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(20),dp(16),dp(20),dp(24));root.setBackgroundColor(fundo);sc.addView(root);sc.setFillViewport(true);sc.setClipToPadding(true);sc.setBackgroundColor(fundo);
        ViewCompat.setOnApplyWindowInsetsListener(sc,(view,windowInsets)->{
            Insets sistema=windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets teclado=windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(sistema.left,sistema.top,sistema.right,Math.max(sistema.bottom,teclado.bottom));
            if(windowInsets.isVisible(WindowInsetsCompat.Type.ime())){
                view.post(()->{
                    View focado=getCurrentFocus();
                    if(focado!=null){
                        Rect area=new Rect();
                        focado.getDrawingRect(area);
                        sc.offsetDescendantRectToMyCoords(focado,area);
                        int destino=Math.max(0,area.bottom-sc.getHeight()+dp(32));
                        sc.smoothScrollTo(0,destino);
                    }
                });
            }
            return windowInsets;
        });
        setContentView(sc);ViewCompat.requestApplyInsets(sc);
        ImageView logo=new ImageView(this);
        int logoId=getResources().getIdentifier("logo_escola","drawable",getPackageName());
        if(logoId!=0)logo.setImageResource(logoId);
        logo.setAdjustViewBounds(true);logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(104),dp(88));lp.gravity=Gravity.CENTER;root.addView(logo,lp);
        TextView e=texto(escola,18);e.setTypeface(null,1);e.setGravity(Gravity.CENTER);root.addView(e);
        TextView p=texto(professora,14);p.setGravity(Gravity.CENTER);p.setPadding(0,dp(3),0,dp(12));root.addView(p);
        TextView h=texto(titulo,23);h.setTypeface(null,1);h.setTextColor(azul);h.setGravity(Gravity.CENTER);h.setPadding(0,dp(4),0,dp(16));root.addView(h);
    }
    void inicio(){
        base("Assistente Pedagógico");
        Button r=botao("Relatórios individuais");root.addView(r);r.setOnClickListener(v->formRelatorio());
        Button p=botao("Planos de aula");root.addView(p);p.setOnClickListener(v->formPlano());
        Button d=botao("Documentos salvos");root.addView(d);d.setOnClickListener(v->documentos());
        TextView aviso=texto("Os documentos ficam salvos neste aparelho. A geração com inteligência artificial e a exportação serão ativadas nas próximas etapas.",13);aviso.setGravity(Gravity.CENTER);aviso.setPadding(0,dp(18),0,0);root.addView(aviso);
    }
    EditText campo(String hint){EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(false);root.addView(e);return e;}
    void voltar(){Button b=botao("← Voltar ao início");root.addView(b);b.setOnClickListener(v->inicio());}
    String hoje(){return new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(new Date());}
    void formRelatorio(){
        base("Novo relatório individual");
        EditText aluno=campo("Nome completo do aluno");
        EditText serie=campo("Série / turma");
        serie.setText("2º Ano do Ensino Fundamental");
        EditText coord=campo("Nome da coordenadora");
        EditText data=campo("Data (dd/mm/aaaa)");data.setText(hoje());
        TextView rot=texto("Relato da professora",16);rot.setPadding(0,dp(8),0,0);root.addView(rot);
        EditText relato=campo("Descreva o que está acontecendo com o aluno em sala de aula");relato.setMinLines(5);
        Button gerar=botao("Preparar relatório");root.addView(gerar);
        gerar.setOnClickListener(v->{
            if(aluno.getText().toString().trim().isEmpty()||relato.getText().toString().trim().isEmpty()){toast("Informe o nome do aluno e o relato.");return;}
            String conteudo=escola+"\nRELATÓRIO INDIVIDUAL DO ALUNO\n\nAluno: "+aluno.getText()+"\nSérie/turma: "+serie.getText()+"\nProfessora: Marilene Maria da Silva\nCoordenadora: "+coord.getText()+"\nData: "+data.getText()+"\n\nRELATO:\n"+relato.getText()+"\n\nTexto aguardando revisão e aprimoramento com inteligência artificial.";
            long id=db.salvar("RELATORIO",aluno.getText().toString(),serie.getText().toString(),"","",data.getText().toString(),conteudo);
            visualizar(id);
        });
        voltar();
    }
    void formPlano(){
        base("Novo plano de aula");
        EditText data=campo("Data (dd/mm/aaaa)");data.setText(hoje());
        EditText aulas=campo("Quantidade de aulas (ex.: 2 aulas / 120 minutos)");
        Spinner comp=new Spinner(this);comp.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,componentes));root.addView(comp);
        Spinner turmas=new Spinner(this);turmas.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{turmaPadrao}));root.addView(turmas);
        EditText hab=campo("Habilidades BNCC (opcional). Ex.: EF02LP07, EF15LP06");
        Button gerar=botao("Preparar plano de aula");root.addView(gerar);
        gerar.setOnClickListener(v->{
            if(aulas.getText().toString().trim().isEmpty()){toast("Informe a quantidade de aulas.");return;}
            String c=comp.getSelectedItem().toString(),t=turmas.getSelectedItem().toString(),h=hab.getText().toString().trim();
            StringBuilder sb=new StringBuilder(escola+"\nPLANO DE AULA\n\nTurma: "+t+"\nComponente curricular: "+c+"\nData: "+data.getText()+"\nDuração: "+aulas.getText()+"\n");
            if(!h.isEmpty())sb.append("Habilidades BNCC: ").append(h).append("\n");
            sb.append("\nOBJETO DO CONHECIMENTO\nA definir com auxílio da inteligência artificial.\n\nOBJETIVOS\nA definir com auxílio da inteligência artificial.\n\nMETODOLOGIA\nA definir com auxílio da inteligência artificial.\n\nRECURSOS\nA definir com auxílio da inteligência artificial.\n\nAVALIAÇÃO\nA definir com auxílio da inteligência artificial.\n\nREFERÊNCIAS\nA definir com auxílio da inteligência artificial.");
            long id=db.salvar("PLANO","",t,c,h,data.getText().toString(),sb.toString());visualizar(id);
        });
        voltar();
    }
    void documentos(){
        base("Documentos salvos");Cursor c=db.listar();
        while(c.moveToNext()){
            final long id=c.getLong(0);String tipo=c.getString(1),titulo=c.getString(2),data=c.getString(3);
            Button b=botao((tipo.equals("RELATORIO")?"Relatório: ":"Plano: ")+titulo+" • "+data);root.addView(b);b.setOnClickListener(v->visualizar(id));
        }
        if(c.getCount()==0)root.addView(texto("Nenhum documento salvo ainda.",15));c.close();voltar();
    }
    void visualizar(long id){
        Cursor c=db.obter(id);if(!c.moveToFirst()){c.close();return;}
        String tipo=c.getString(1),titulo=c.getString(2),data=c.getString(6),conteudo=c.getString(7);c.close();
        base(tipo.equals("RELATORIO")?"Relatório individual":"Plano de aula");
        TextView corpo=texto(conteudo,15);corpo.setPadding(dp(8),dp(8),dp(8),dp(18));root.addView(corpo);
        Button pdf=botao("Exportar PDF");root.addView(pdf);pdf.setOnClickListener(v->exportarPdf(titulo,conteudo));
        Button word=botao("Exportar Word");root.addView(word);word.setOnClickListener(v->exportarWord(titulo,conteudo));
        Button voltar=botao("← Voltar aos documentos");root.addView(voltar);voltar.setOnClickListener(v->documentos());
    }
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    File pasta(){File f=new File(getFilesDir(),"documentos");if(!f.exists())f.mkdirs();return f;}
    void compartilhar(File f,String mime){
        Uri uri=FileProvider.getUriForFile(this,"br.com.baiapureza.assistentepedagogico.files",f);
        Intent i=new Intent(Intent.ACTION_SEND);i.setType(mime);i.putExtra(Intent.EXTRA_STREAM,uri);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(i,"Compartilhar documento"));
    }
    void exportarPdf(String titulo,String conteudo){
        try{
            PdfDocument pdf=new PdfDocument();Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setTextSize(12);int pageNo=1,y=48;
            PdfDocument.Page page=pdf.startPage(new PdfDocument.PageInfo.Builder(595,842,pageNo).create());Canvas canvas=page.getCanvas();
            for(String linha:conteudo.split("\\n",-1)){
                if(y>800){pdf.finishPage(page);page=pdf.startPage(new PdfDocument.PageInfo.Builder(595,842,++pageNo).create());canvas=page.getCanvas();y=48;}
                for(String pedaco:quebrar(linha,82)){canvas.drawText(pedaco,42,y,paint);y+=18;}
            }
            pdf.finishPage(page);File f=new File(pasta(),nomeArquivo(titulo)+".pdf");FileOutputStream os=new FileOutputStream(f);pdf.writeTo(os);os.close();pdf.close();compartilhar(f,"application/pdf");
        }catch(Exception e){toast("Não foi possível gerar o PDF: "+e.getMessage());}
    }
    List<String> quebrar(String s,int max){List<String> out=new ArrayList<>();if(s.isEmpty()){out.add(" ");return out;}while(s.length()>max){int n=s.lastIndexOf(' ',max);if(n<1)n=max;out.add(s.substring(0,n));s=s.substring(Math.min(n+1,s.length()));}out.add(s);return out;}
    String nomeArquivo(String s){return s.replaceAll("[^a-zA-Z0-9À-ÿ]+","_");}
    void exportarWord(String titulo,String conteudo){
        try{
            File f=new File(pasta(),nomeArquivo(titulo)+".docx");ZipOutputStream z=new ZipOutputStream(new FileOutputStream(f));
            z.putNextEntry(new ZipEntry("[Content_Types].xml"));write(z,"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/></Types>");z.closeEntry();
            z.putNextEntry(new ZipEntry("_rels/.rels"));write(z,"<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>");z.closeEntry();
            z.putNextEntry(new ZipEntry("word/document.xml"));StringBuilder x=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>");
            for(String line:conteudo.split("\\n",-1))x.append("<w:p><w:r><w:t xml:space=\"preserve\">").append(escape(line)).append("</w:t></w:r></w:p>");
            x.append("<w:sectPr/></w:body></w:document>");write(z,x.toString());z.closeEntry();z.close();compartilhar(f,"application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }catch(Exception e){toast("Não foi possível gerar o Word: "+e.getMessage());}
    }
    void write(ZipOutputStream z,String s)throws Exception{z.write(s.getBytes("UTF-8"));}
    String escape(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");}
}