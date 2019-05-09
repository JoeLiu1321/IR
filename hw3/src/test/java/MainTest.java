import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.*;

public class MainTest {
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String fileOutputPath=System.getProperty("user.dir")+"/inverted_index.txt";
    private static final String topicPath=System.getProperty("user.dir")+"/all-topics.txt";
    private static final String tokenPath=System.getProperty("user.dir")+"/all-tokens.txt";
    private static final String docVec=System.getProperty("user.dir")+"/doc_vec/";
    private static final String centroidPath=System.getProperty("user.dir")+"/centroid/";
    @Test
    public void predictTopic()throws Exception{
        QueryCalculator queryCalculator=new QueryCalculator(FSDirectory.open(Paths.get(indexPath)),Runtime.getRuntime().availableProcessors()*10);
        List<Map.Entry<String,Double>>rank=queryCalculator.predictTopic("120");
        for(Map.Entry<String,Double>entry:rank)
            System.out.println(entry);
    }
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
    public void testAllToken()throws Exception{
        Main.buildIndex();
        writeAllToken();

    }
    public void writeAllToken()throws Exception{
        IndexHelper helper=new IndexHelper(FSDirectory.open(Paths.get(indexPath)));

        List<QueryData> tmp=helper.search("newID","*:*");

        Map<String,String> allTokens=new TreeMap<>();
        FileWriter fw=new FileWriter(tokenPath);

        for(QueryData q:tmp)
            for(String token:q.getTokens())
                allTokens.put(token, token);

        for(String token:allTokens.keySet()){
            fw.write(token+"\n");
            fw.flush();
        }
    }
}