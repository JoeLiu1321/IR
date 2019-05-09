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

public class IndexHelper {
    private Directory index_dir;
    public IndexHelper(Directory dir){
        index_dir = dir;
    }

    public  void addDocument(ParagraphInfo tokens) throws IOException {
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(index_dir, iwc);
        Document document;
        // 要改
        document = new Document();
        document.add(new TextField("set", tokens.getCGISPLITCGISPLIT(), Field.Store.YES));
        document.add(new TextField("oldID", tokens.getOldID(), Field.Store.YES));
        document.add(new TextField("newID", tokens.getNewID(), Field.Store.YES));
        document.add(new TextField("TOPICS", tokens.getTopicalCategory(), Field.Store.YES));
        document.add(new TextField("body", tokens.getBody(), Field.Store.YES));
        document.add(new TextField("numOfToken", String.valueOf( tokens.getNumOfToken() ), Field.Store.YES));
//        document.add(new TextField("pos", String.valueOf(i), Field.Store.YES));
//        document.add(new TextField("docId",String.valueOf(docId),Field.Store.YES));
        indexWriter.addDocument(document);
        indexWriter.close();
    }

    public  List<QueryData> search(String key, String token) throws Exception {
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

    public List<QueryData> searchWithWildCard(String field,String value)throws IOException{
        List<QueryData> searchResult=new ArrayList<>();
        IndexReader indexReader = DirectoryReader.open(index_dir);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Query query =new WildcardQuery(new Term(field,value));
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

    public List<QueryData> booleanQueryWithWildCard(String[] wildCardfield,String[] wildCardvalue)throws IOException{
        List<QueryData> searchResult=new ArrayList<>();
        IndexReader indexReader = DirectoryReader.open(index_dir);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        BooleanQuery.Builder builder=new BooleanQuery.Builder();
        for(int i=0;i<wildCardfield.length;i++){
            Query query =new WildcardQuery(new Term(wildCardfield[i],wildCardvalue[i]));
            builder.add(query, BooleanClause.Occur.MUST);
        }
        BooleanQuery booleanQuery=builder.build();
        TotalHitCountCollector collector=new TotalHitCountCollector();
        indexSearcher.search(booleanQuery,collector);
        int upper=(collector.getTotalHits()==0 ? 1 : collector.getTotalHits());
        TopDocs topDocs = indexSearcher.search(booleanQuery,upper);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc doc : scoreDocs) {
            Document document = indexSearcher.doc(doc.doc);
            searchResult.add(new QueryData(document));
        }
        indexReader.close();

        return searchResult;
    }

}
