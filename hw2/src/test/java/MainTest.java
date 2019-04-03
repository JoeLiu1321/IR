import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

public class MainTest {
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String fileOutputPath=System.getProperty("user.dir")+"/inverted_index.txt";

    @Test
    public void test()throws Exception {
        Directory indexDir = FSDirectory.open(Paths.get(indexPath));
        RankCalculator rankCalculator = new RankCalculator(indexDir,Runtime.getRuntime().availableProcessors()*10);

        Map<String,String> map=rankCalculator.getTotalResultByField("token",String::compareTo);
        System.out.println(map.size());
        map=rankCalculator.getTotalResultByField("docId",Comparator.comparingInt(Integer::parseInt));
        System.out.println(map.size());
//        while (it.hasNext())
//            System.out.println(it.next());
//        String query = "the";
//        System.out.println(rankCalculator.getDocumentWeight(query, "3"));
    }

    @Test
    public void testMultiThread()throws Exception{
        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
        RankCalculator rankCalculator=new RankCalculator(indexDir,Runtime.getRuntime().availableProcessors()*10);
        String query="the";
        Iterator<Score> iterator=rankCalculator.calculateRank(query);
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

//    @Test
//    public void buildIndex() throws Exception{
//        Main.buildIndex();
//    }
//
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
//
//    @Test
//    public void testSort() throws Exception{
//        String query="About",docId="3";
//        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
//        IndexHelper helper=new IndexHelper(indexDir);
//        List<QueryData> result =helper.search("token:the");
//        for(QueryData data:result)
//            System.out.println(data.toString());
//    }
//
//    @Test
//    public void test()throws Exception{
//        String query="About",docId="3";
//
//        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
//        RankCalculator rankCalculator=new RankCalculator(indexDir);
//        rankCalculator.getDocumentWeight(query,docId,rankCalculator.totalDoc.size());
////        Iterator<Map.Entry<String,Double>> docVec= rankCalculator.generateDocVector(docId);
////        Map<String,Double> queryVec=rankCalculator.generateQueryVector(query);
////        Iterator<Map.Entry<String,Double>> queryIterator=queryVec.entrySet().iterator();
////        while(docVec.hasNext()){
////            Map.Entry<String,Double> doc=docVec.next();
////            Map.Entry<String,Double> que=queryIterator.next();
////            System.out.println(doc.getKey()+ " d = "+doc.getValue().doubleValue()+ "  q = "+que.getValue().doubleValue());
////        }
//
//
//    }

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