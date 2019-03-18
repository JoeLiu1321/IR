import java.io.*;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

public class main {
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String filePath=System.getProperty("user.dir")+"/test.txt";
    public static void main(String[]args) throws Exception{
        Directory indexDir=FSDirectory.open(Paths.get(indexPath));
        createIndex(indexDir);
        search(indexDir,"abc");
    }
    public static void createIndex(Directory dir) throws IOException {
        IndexWriter indexWriter = null;
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(dir, iwc);

        Document document;
        File file = new File(filePath);
        BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String tmp="";
        while ((tmp=br.readLine())!=null){
            document = new Document();

            document.add(new Field("content", tmp, TextField.TYPE_STORED));
            document.add(new TextField("fileName", file.getName(), Field.Store.YES));
            document.add(new StringField("filePath", file.getAbsolutePath(), Field.Store.YES));

            indexWriter.addDocument(document);
        }
        indexWriter.close();
    }

    public static void search(Directory dir,String token) throws Exception {
        IndexReader indexReader = null;
        indexReader = DirectoryReader.open(dir);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        QueryParser queryParser = new QueryParser("content", new StandardAnalyzer());
        Query query = queryParser.parse(token);
        TopDocs topDocs = indexSearcher.search(query, 10);
        ScoreDoc[] socreDocs = topDocs.scoreDocs;
        for (ScoreDoc doc : socreDocs) {
            Document document = indexSearcher.doc(doc.doc);
            System.out.println("File Path = "+document.get("filePath"));
            System.out.println("File Name = "+document.get("fileName"));
            System.out.println("Content = "+document.get("content"));
        }
        indexReader.close();
    }
}
