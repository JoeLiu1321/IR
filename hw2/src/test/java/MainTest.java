import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.*;

public class MainTest {
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String fileOutputPath=System.getProperty("user.dir")+"/inverted_index.txt";

    @Test
    public void test()throws Exception{
//       IndexHelper helper=new IndexHelper(FSDirectory.open(Paths.get(indexPath)));
//       for(int i=0;i<100;i++)
//           System.out.println(helper.searchTerms("token:the AND docId:"+i).size());
        QueryCalculator queryCalculator=new QueryCalculator(FSDirectory.open(Paths.get(indexPath)),500,"token:the");
//        Iterator<Map.Entry<String,Double>> iterator=queryCalculator.generateQueryVector().entrySet().iterator();
//        Iterator<Map.Entry<String,Double>> iterator=queryCalculator.generateDocVector("99");
       Iterator<Map.Entry<String,Integer>>iterator= queryCalculator.generateAllTokenDocumentFreq(queryCalculator.getTotalResultByField("token",String::compareTo).keySet().iterator()).entrySet().iterator();
        while (iterator.hasNext())
            System.out.println(iterator.next());
    }

    @Test
    public void test_thread() throws Exception{
//        for (int i = 1 ; i<=1000 ; i++ ) {
//            double start,end;
//            start = System.currentTimeMillis();
//            Directory indexDir= FSDirectory.open(Paths.get(indexPath));
//            String query="the";
//            QueryCalculator queryCalculator =new QueryCalculator(indexDir,Runtime.getRuntime().availableProcessors()*i,query);
//            queryCalculator.generateQueryVector();
//            end = System.currentTimeMillis();
//            System.out.println( "用: "+ 8 * i+ "個Thread"+ "花了：" + (end-start)/(double)1000 + "秒");
//        }
        double start,end;
        start = System.currentTimeMillis();
        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
        String query="the";
        QueryCalculator queryCalculator =new QueryCalculator(indexDir,Runtime.getRuntime().availableProcessors()*10,query);
        queryCalculator.generateQueryVector();
        end = System.currentTimeMillis();
        System.out.println( "用: "+ 8*50 + "個Thread"+ "花了：" + (end-start)/(double)1000 + "秒");
    }
//
    @Test
    public void testSingleThreadRank() throws Exception{
        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
        String query="the";
        QueryCalculator queryCalculator =new QueryCalculator(indexDir,Runtime.getRuntime().availableProcessors()*10,query);
        Iterator<Score> iterator= queryCalculator.calculateRank();
        FileWriter fw=new FileWriter(new File(fileOutputPath));
        fw.write("Query:"+"\""+query+"\""+"\n");
        fw.write("Result:<doc#><similarity score>\n");
        System.out.println("Query:"+"\""+query+"\"");
        System.out.println("Result:<doc#><similarity score>");
        while (iterator.hasNext()){
            Score score=iterator.next();
            fw.write(score.getDocId()+"  "+score.getScore()+"\n");
            System.out.println(score.getDocId()+"  "+score.getScore());
        }
    }

}