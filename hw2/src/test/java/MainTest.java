import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class MainTest {
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String fileOutputPath=System.getProperty("user.dir")+"/inverted_index.txt";
    @Test
    public void buildIndex() throws Exception{
        Main.buildIndex();
    }

    @Test
    public void testRank() throws Exception{
        String query="the";
        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
        RankCalculator rankCalculator=new RankCalculator(indexDir,Runtime.getRuntime().availableProcessors()*10);
        Iterator<Score> iterator=rankCalculator.calculateRank(query);
        System.out.println("Query:"+"\""+query+"\"");
        System.out.println("Result:<doc#><similarity score>");
        while (iterator.hasNext()){
            Score score=iterator.next();
            System.out.println(score.getDocId()+"  "+score.getScore());
        }

    }

    @Test
    public void test_thread() throws Exception{
        long startTime = System.currentTimeMillis();
        List<Callable<Boolean>>taskList= new ArrayList<>();
        ExecutorService executorService= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*100);
        ReentrantLock lock=new ReentrantLock();
        String query="the";
        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
        IndexHelper gp = new IndexHelper(indexDir);
        List<QueryData> searchResult=new ArrayList<>();
        searchResult = gp.search("token","the");
        for( QueryData temp:searchResult){
            taskList.add(() -> {
                lock.lock();
                System.out.println("thread"+ temp.getDocumentId() +  "執行中");
                lock.unlock();
                return true;
            });
        }
        List<Future<Boolean>>futures=executorService.invokeAll(taskList);
        System.out.println("Using Time:" + (System.currentTimeMillis() - startTime) + " ms");


    }
//    @After
//    public void tearDown(){
//        File f=new File(indexPath);
//        for(String s:f.list())
//            new File(indexPath,s).delete();
//        f.delete();
//        File file=new File(fileOutputPath);
//        file.delete();
//    }

}