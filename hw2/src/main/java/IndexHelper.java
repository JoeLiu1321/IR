import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IndexHelper {
    private Directory index_dir;
    private int docId;
    public IndexHelper(Directory dir){
        index_dir = dir;
        docId=1;
    }

    public  void addDocument(List<String> tokens) throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(index_dir, iwc);
        Document document;
        for(int i=0;i<tokens.size();i++) {
            document = new Document();
            document.add(new Field("token", tokens.get(i), TextField.TYPE_STORED));
            document.add(new TextField("pos", String.valueOf(i), Field.Store.YES));
            document.add(new TextField("docId",String.valueOf(docId),Field.Store.YES));
            indexWriter.addDocument(document);
        }
        docId++;
        indexWriter.close();
    }

    public  List<QueryData> search(String key,String token) throws Exception {
        List<QueryData> searchResult=new ArrayList<>();
        IndexReader indexReader = DirectoryReader.open(index_dir);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        QueryParser queryParser = new QueryParser(key, new StandardAnalyzer());
        Query query = queryParser.parse(token);
        TotalHitCountCollector collector=new TotalHitCountCollector();
        indexSearcher.search(query,collector);
        int upper=(collector.getTotalHits()==0 ? 1 : collector.getTotalHits());
        TopDocs topDocs = indexSearcher.search(query,upper);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc doc : scoreDocs) {
            Document document = indexSearcher.doc(doc.doc);
            searchResult.add(new QueryData(document));
        }
        indexReader.close();
        return searchResult;
    }

    public  List<String> searchTerms(String conditions) throws Exception {
        List<String> searchResult=new ArrayList<>();
        IndexReader indexReader = DirectoryReader.open(index_dir);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        QueryParser queryParser = new QueryParser("<default field>", new StandardAnalyzer());
        Query query = queryParser.parse(conditions);
        TotalHitCountCollector collector=new TotalHitCountCollector();
        indexSearcher.search(query,collector);
        int upper=(collector.getTotalHits()==0 ? 1 : collector.getTotalHits());
        TopDocs topDocs = indexSearcher.search(query,upper);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc doc : scoreDocs) {
            Document document = indexSearcher.doc(doc.doc);
            searchResult.add(document.get("token"));
        }
        indexReader.close();
        return searchResult;
    }

    public List<QueryData> search(String condition)throws Exception{
        List<QueryData> searchResult=new ArrayList<>();
        IndexReader indexReader = DirectoryReader.open(index_dir);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        QueryParser queryParser = new QueryParser("<default field>", new StandardAnalyzer());
        Query query = queryParser.parse(condition);
        TotalHitCountCollector collector=new TotalHitCountCollector();
        indexSearcher.search(query,collector);
        int upper=(collector.getTotalHits()==0 ? 1 : collector.getTotalHits());
        TopDocs topDocs = indexSearcher.search(query,upper);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc doc : scoreDocs) {
            Document document = indexSearcher.doc(doc.doc);
            searchResult.add(new QueryData(document));
        }
        indexReader.close();
        return searchResult;
    }

}
