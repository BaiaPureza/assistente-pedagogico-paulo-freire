package br.com.baiapureza.assistentepedagogico;

import android.app.*;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.view.*;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.KeyStore;

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
    final String URL_IA = "https://script.google.com/macros/s/AKfycbyvgIfcagzlU34i45EKrynIcxXyCyFQgIBtIt1QSNrQdzWMw-T3dPSElOnf4q3rAWfU/exec";

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
        Button ia=botao("Configurar inteligência artificial");root.addView(ia);
ia.setOnClickListener(v->configurarIA());
        TextView aviso=texto("Os documentos ficam salvos neste aparelho. A geração com inteligência artificial e a exportação serão ativadas nas próximas etapas.",13);aviso.setGravity(Gravity.CENTER);aviso.setPadding(0,dp(18),0,0);root.addView(aviso);
    }
    void configurarIA(){
    base("Configurar inteligência artificial");

    TextView explicacao=texto(
        "Informe a senha de acesso configurada no Google Apps Script. Ela será armazenada de forma criptografada neste aparelho.",
        14
    );
    explicacao.setPadding(0,0,0,dp(12));
    root.addView(explicacao);

    EditText senha=campo("Digite sua senha de acesso");
    senha.setInputType(
        android.text.InputType.TYPE_CLASS_TEXT |
        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
    );

    TextView status=texto("",14);
    status.setPadding(0,dp(10),0,dp(10));
    root.addView(status);

    try{
        if(obterSenhaIA()!=null){
            status.setText("Uma senha já está configurada neste aparelho.");
        }else{
            status.setText("Nenhuma senha configurada ainda.");
        }
    }catch(Exception e){
        status.setText("Não foi possível verificar a configuração.");
    }

    Button salvar=botao("Salvar senha com segurança");
    root.addView(salvar);

    salvar.setOnClickListener(v->{
        String valor=senha.getText().toString().trim();

        if(valor.isEmpty()){
            toast("Digite a senha antes de salvar.");
            return;
        }

        try{
            salvarSenhaIA(valor);
            senha.setText("");
            status.setText("Senha configurada e armazenada com segurança neste aparelho.");
            toast("Configuração salva com sucesso!");
        }catch(Exception e){
            toast("Erro ao salvar a configuração: "+e.getMessage());
        }
    });

    voltar();
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
        base("Documentos salvos");
        EditText pesquisa=campo("Pesquisar por aluno, turma, componente ou data");
        LinearLayout abas=new LinearLayout(this);abas.setOrientation(0);root.addView(abas);
        Button relatorios=botao("Relatórios");abas.addView(relatorios,new LinearLayout.LayoutParams(0,dp(48),1));
        Button planos=botao("Planos de aula");abas.addView(planos,new LinearLayout.LayoutParams(0,dp(48),1));
        LinearLayout lista=new LinearLayout(this);lista.setOrientation(1);root.addView(lista);
        final String[] tipoAtual={"RELATORIO"};
        Runnable atualizar=()->atualizarListaDocumentos(lista,tipoAtual[0],pesquisa.getText().toString());
        relatorios.setOnClickListener(v->{tipoAtual[0]="RELATORIO";atualizar.run();});
        planos.setOnClickListener(v->{tipoAtual[0]="PLANO";atualizar.run();});
        pesquisa.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){atualizar.run();}
            public void afterTextChanged(Editable e){}
        });
        atualizar.run();
        voltar();
    }
    void atualizarListaDocumentos(LinearLayout lista,String tipo,String filtro){
        lista.removeAllViews();
        Cursor c=db.listarPorTipo(tipo,filtro);
        String grupoAnterior="";
        int quantidade=0;
        while(c.moveToNext()){
            quantidade++;
            final long id=c.getLong(0);
            String titulo=c.getString(1),turma=c.getString(2),componente=c.getString(3),data=c.getString(4);
            String grupo=tipo.equals("RELATORIO")?titulo:turma+" • "+componente;
            if(!grupo.equals(grupoAnterior)){
                TextView cabecalho=texto(grupo,17);
                cabecalho.setTypeface(null,1);cabecalho.setTextColor(azul);
                cabecalho.setPadding(0,dp(16),0,dp(5));lista.addView(cabecalho);
                grupoAnterior=grupo;
            }
            Button item=botao((tipo.equals("RELATORIO")?"Relatório de ":"Plano de aula • ")+data);
            lista.addView(item);item.setOnClickListener(v->visualizar(id));
        }
        c.close();
        if(quantidade==0){
            TextView vazio=texto(filtro.trim().isEmpty()?"Nenhum documento salvo nesta categoria.":"Nenhum documento encontrado para essa pesquisa.",15);
            vazio.setPadding(0,dp(18),0,dp(12));lista.addView(vazio);
        }
    }
    void visualizar(long id){
        Cursor c=db.obter(id);if(!c.moveToFirst()){c.close();return;}
        String tipo=c.getString(1),titulo=c.getString(2),turma=c.getString(3),componente=c.getString(4),habilidades=c.getString(5),data=c.getString(6),conteudo=c.getString(7);c.close();
        base(tipo.equals("RELATORIO")?"Relatório individual":"Plano de aula");
        TextView corpo=texto(conteudo,15);corpo.setPadding(dp(8),dp(8),dp(8),dp(18));root.addView(corpo);
        Button editar=botao("Editar documento");root.addView(editar);editar.setOnClickListener(v->editarDocumento(id));
        Button duplicar=botao("Duplicar documento");root.addView(duplicar);duplicar.setOnClickListener(v->confirmarDuplicacao(id));
        Button historico=botao("Ver versões anteriores");root.addView(historico);historico.setOnClickListener(v->historicoVersoes(id));
        Button pdf=botao("Exportar PDF");root.addView(pdf);pdf.setOnClickListener(v->exportarPdf(titulo,conteudo));
        Button word=botao("Exportar Word");root.addView(word);word.setOnClickListener(v->exportarWord(titulo,conteudo));
        Button voltar=botao("← Voltar aos documentos");root.addView(voltar);voltar.setOnClickListener(v->documentos());
    }
    void confirmarDuplicacao(long id){
        new AlertDialog.Builder(this)
            .setTitle("Duplicar documento")
            .setMessage("Será criada uma cópia independente. O documento original continuará intacto. Deseja continuar?")
            .setNegativeButton("Cancelar",null)
            .setPositiveButton("Duplicar",(dialog,which)->{
                long novoId=db.duplicar(id);
                if(novoId>0){toast("Cópia criada. Você já pode editar a nova versão.");visualizar(novoId);}
                else toast("Não foi possível duplicar o documento.");
            }).show();
    }
    void historicoVersoes(long id){
        Cursor c=db.listarVersoes(id);
        base("Versões anteriores");
        int total=0;
        while(c.moveToNext()){
            total++;
            long versaoId=c.getLong(0);
            String titulo=c.getString(1),data=c.getString(2),salvo=c.getString(3);
            Button item=botao("Versão de "+salvo+" • "+data);root.addView(item);
            item.setOnClickListener(v->visualizarVersao(versaoId,id));
        }
        c.close();
        if(total==0){TextView vazio=texto("Este documento ainda não possui versões anteriores. Elas serão guardadas automaticamente sempre que você salvar uma edição.",14);vazio.setPadding(0,dp(16),0,dp(12));root.addView(vazio);}
        Button voltar=botao("← Voltar ao documento");root.addView(voltar);voltar.setOnClickListener(v->visualizar(id));
    }
    void visualizarVersao(long versaoId,long documentoId){
        Cursor c=db.obterVersao(versaoId);if(!c.moveToFirst()){c.close();return;}
        String titulo=c.getString(2),data=c.getString(6),conteudo=c.getString(7);c.close();
        base("Versão anterior");
        TextView info=texto("Registro anterior • "+data,13);root.addView(info);
        TextView corpo=texto(conteudo,15);corpo.setPadding(dp(8),dp(8),dp(8),dp(18));root.addView(corpo);
        Button pdf=botao("Exportar esta versão em PDF");root.addView(pdf);pdf.setOnClickListener(v->exportarPdf(titulo,conteudo));
        Button word=botao("Exportar esta versão em Word");root.addView(word);word.setOnClickListener(v->exportarWord(titulo,conteudo));
        Button voltar=botao("← Voltar às versões");root.addView(voltar);voltar.setOnClickListener(v->historicoVersoes(documentoId));
    }
    void editarDocumento(long id){
        Cursor c=db.obter(id);if(!c.moveToFirst()){c.close();toast("Documento não encontrado.");return;}
        String tipo=c.getString(1),tituloAtual=c.getString(2),turmaAtual=c.getString(3),componenteAtual=c.getString(4),habilidadesAtual=c.getString(5),dataAtual=c.getString(6),conteudoAtual=c.getString(7);c.close();
        base(tipo.equals("RELATORIO")?"Editar relatório":"Editar plano de aula");
        EditText titulo=campo(tipo.equals("RELATORIO")?"Nome do aluno":"Título (opcional)");titulo.setText(tituloAtual);
        EditText turma=campo("Série / turma");turma.setText(turmaAtual);
        EditText componente=campo("Componente curricular");componente.setText(componenteAtual);
        EditText habilidades=campo("Habilidades BNCC");habilidades.setText(habilidadesAtual);
        EditText data=campo("Data (dd/mm/aaaa)");data.setText(dataAtual);
        TextView rotulo=texto("Conteúdo completo do documento",16);rotulo.setPadding(0,dp(10),0,0);root.addView(rotulo);
        EditText conteudo=campo("Edite o texto do documento");conteudo.setText(conteudoAtual);conteudo.setMinLines(12);conteudo.setGravity(Gravity.TOP|Gravity.START);
        TextView dica=texto("As alterações serão salvas neste documento, sem criar uma cópia. Revise o texto antes de salvar.",12);dica.setPadding(0,dp(8),0,dp(8));root.addView(dica);
        Button salvar=botao("Salvar alterações");root.addView(salvar);
        salvar.setOnClickListener(v->{
            if(tipo.equals("RELATORIO")&&titulo.getText().toString().trim().isEmpty()){toast("Informe o nome do aluno.");return;}
            if(conteudo.getText().toString().trim().isEmpty()){toast("O conteúdo não pode ficar vazio.");return;}
            int alterados=db.atualizar(id,titulo.getText().toString().trim(),turma.getText().toString().trim(),componente.getText().toString().trim(),habilidades.getText().toString().trim(),data.getText().toString().trim(),conteudo.getText().toString());
            if(alterados>0){toast("Documento atualizado com sucesso.");visualizar(id);}else toast("Não foi possível salvar as alterações.");
        });
        Button cancelar=botao("Cancelar");root.addView(cancelar);cancelar.setOnClickListener(v->visualizar(id));
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

    // CHAVE DE CRIPTOGRAFIA DO ANDROID
private SecretKey obterOuCriarChaveIA() throws Exception {
    String alias = "AssistentePedagogicoIA";

    KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
    ks.load(null);

    if (ks.containsAlias(alias)) {
        return (SecretKey) ks.getKey(alias, null);
    }

    KeyGenerator gerador = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        "AndroidKeyStore"
    );

    gerador.init(new KeyGenParameterSpec.Builder(
        alias,
        KeyProperties.PURPOSE_ENCRYPT |
        KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setRandomizedEncryptionRequired(true)
        .build()
    );

    return gerador.generateKey();
}

// SALVAR SENHA CRIPTOGRAFADA
private void salvarSenhaIA(String senha) throws Exception {
    SecretKey chave = obterOuCriarChaveIA();

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, chave);

    byte[] criptografado = cipher.doFinal(
        senha.getBytes("UTF-8")
    );

    android.content.SharedPreferences prefs =
        getSharedPreferences("configuracao_ia", MODE_PRIVATE);

    prefs.edit()
        .putString("senha_iv", Base64.encodeToString(
            cipher.getIV(), Base64.NO_WRAP))
        .putString("senha_dados", Base64.encodeToString(
            criptografado, Base64.NO_WRAP))
        .apply();
}

// RECUPERAR SENHA
private String obterSenhaIA() throws Exception {
    android.content.SharedPreferences prefs =
        getSharedPreferences("configuracao_ia", MODE_PRIVATE);

    String ivSalvo = prefs.getString("senha_iv", null);
    String dadosSalvos = prefs.getString("senha_dados", null);

    if (ivSalvo == null || dadosSalvos == null) {
        return null;
    }

    byte[] iv = Base64.decode(ivSalvo, Base64.NO_WRAP);
    byte[] dados = Base64.decode(dadosSalvos, Base64.NO_WRAP);

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

    cipher.init(
        Cipher.DECRYPT_MODE,
        obterOuCriarChaveIA(),
        new GCMParameterSpec(128, iv)
    );

    byte[] original = cipher.doFinal(dados);

    return new String(original, "UTF-8");
    // ENVIAR TEXTO PARA A INTELIGÊNCIA ARTIFICIAL
private String enviarParaIA(String tipo, String textoEntrada) throws Exception {

    String chave = obterSenhaIA();

    if (chave == null || chave.isEmpty()) {
        throw new Exception("Configure a senha da inteligência artificial primeiro.");
    }

    JSONObject dados = new JSONObject();
    dados.put("chave", chave);
    dados.put("tipo", tipo);
    dados.put("texto", textoEntrada);

    HttpURLConnection conexao = (HttpURLConnection)
        new URL(URL_IA).openConnection();

    conexao.setRequestMethod("POST");
    conexao.setConnectTimeout(20000);
    conexao.setReadTimeout(60000);
    conexao.setDoOutput(true);
    conexao.setInstanceFollowRedirects(false);
    conexao.setRequestProperty(
        "Content-Type", "application/json; charset=UTF-8"
    );
    conexao.setRequestProperty("Accept", "application/json");

    OutputStream saida = conexao.getOutputStream();
    saida.write(dados.toString().getBytes("UTF-8"));
    saida.flush();
    saida.close();

    int codigo = conexao.getResponseCode();

    // O Google Apps Script pode redirecionar a resposta.
    if (codigo == 301 || codigo == 302 ||
        codigo == 303 || codigo == 307 || codigo == 308) {

        String destino = conexao.getHeaderField("Location");
        conexao.disconnect();

        if (destino == null) {
            throw new Exception("O Google não informou o endereço da resposta.");
        }

        URL urlResposta = new URL(new URL(URL_IA), destino);

        HttpURLConnection redirecionamento =
            (HttpURLConnection) urlResposta.openConnection();

        redirecionamento.setRequestMethod("GET");
        redirecionamento.setConnectTimeout(20000);
        redirecionamento.setReadTimeout(60000);

        codigo = redirecionamento.getResponseCode();

        InputStream entrada = codigo >= 200 && codigo < 300
            ? redirecionamento.getInputStream()
            : redirecionamento.getErrorStream();

        if (entrada == null) {
            redirecionamento.disconnect();
            throw new Exception("O Google não retornou uma resposta válida.");
        }

        BufferedReader leitor = new BufferedReader(
            new InputStreamReader(entrada, "UTF-8")
        );

        StringBuilder resposta = new StringBuilder();
        String linha;

        while ((linha = leitor.readLine()) != null) {
            resposta.append(linha);
        }

        leitor.close();
        redirecionamento.disconnect();

        if (codigo < 200 || codigo >= 300) {
            throw new Exception("Erro na comunicação com o Google. Código: " + codigo);
        }

        return resposta.toString();
    }

    InputStream entrada = codigo >= 200 && codigo < 300
        ? conexao.getInputStream()
        : conexao.getErrorStream();

    if (entrada == null) {
        conexao.disconnect();
        throw new Exception("O servidor não retornou uma resposta.");
    }

    BufferedReader leitor = new BufferedReader(
        new InputStreamReader(entrada, "UTF-8")
    );

    StringBuilder resposta = new StringBuilder();
    String linha;

    while ((linha = leitor.readLine()) != null) {
        resposta.append(linha);
    }

    leitor.close();
    conexao.disconnect();

    if (codigo < 200 || codigo >= 300) {
        throw new Exception("Erro na comunicação com o Google. Código: " + codigo);
    }

    return resposta.toString();
}
}
}
