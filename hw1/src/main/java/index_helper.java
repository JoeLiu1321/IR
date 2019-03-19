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

public class index_helper {
    private Directory index_dir;
    private int docId;
    public index_helper(Directory dir){
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

    public  List<QueryData> search(String token) throws Exception {
        List<QueryData> searchResult=new ArrayList<>();
        IndexReader indexReader = DirectoryReader.open(index_dir);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        QueryParser queryParser = new QueryParser("token", new StandardAnalyzer());
        Query query = queryParser.parse(token);
        TotalHitCountCollector collector=new TotalHitCountCollector();
        indexSearcher.search(query,collector);
        TopDocs topDocs = indexSearcher.search(query,collector.getTotalHits());
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc doc : scoreDocs) {
            Document document = indexSearcher.doc(doc.doc);
            searchResult.add(new QueryData(document));
        }
        indexReader.close();
        return searchResult;
    }
}
