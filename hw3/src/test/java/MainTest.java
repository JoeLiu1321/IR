import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.*;

public class MainTest {
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String fileOutputPath=System.getProperty("user.dir")+"/inverted_index.txt";
    private static final String topicPath=System.getProperty("user.dir")+"/all-topics.txt";
    private static final String docVec=System.getProperty("user.dir")+"/doc_vec/";
    private static final String centroidPath=System.getProperty("user.dir")+"/centroid/";
    @Test
    public void getAllDocVector()throws Exception{
        QueryCalculator queryCalculator=new QueryCalculator(FSDirectory.open(Paths.get(indexPath)),Runtime.getRuntime().availableProcessors()*10);
        queryCalculator.writeAllDocVector();
    }

    @Test
    public void writeAllCentroid()throws Exception{
        QueryCalculator queryCalculator=new QueryCalculator(FSDirectory.open(Paths.get(indexPath)),Runtime.getRuntime().availableProcessors()*10);
        queryCalculator.writeAllCentroid();
    }

    @Test
    public void predictTopic()throws Exception{
        QueryCalculator queryCalculator=new QueryCalculator(FSDirectory.open(Paths.get(indexPath)),Runtime.getRuntime().availableProcessors()*10);
        IndexHelper helper=new IndexHelper(FSDirectory.open(Paths.get(indexPath)));
        List<QueryData> testSet=helper.booleanQueryWithWildCard(new String[]{"set"},new String[]{"*testset"});
        for(QueryData queryData:testSet){
            Score maxRank=queryCalculator.predictTopic(queryData.getNewID());
            System.out.println("\ntest Document:"+queryData.getNewID()+"\nPredict:"+maxRank.getTopic()+"\nActual:"+queryData.getTopicalCategory());
        }
    }
    @Test
    public void buildIndex()throws Exception{
        Main.buildIndex();

    }

}