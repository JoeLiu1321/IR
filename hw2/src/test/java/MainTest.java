import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.*;

public class MainTest {
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String fileOutputPath=System.getProperty("user.dir")+"/inverted_index.txt";

    @Test
    public void buildIndex() throws Exception{
        Main.buildIndex();
    }

    @Test
    public void queryAllDocTf() throws Exception{
        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
        index_helper helper = new index_helper(indexDir);
        TfIdf tfIdf=new TfIdf();
        Iterator<Map.Entry<String,String>> totalToken =Main.getTotalResultByField("docId");
        Iterator<Map.Entry<String,String>> totalDoc =Main.getTotalResultByField("docId");

        Iterator<Map.Entry<String,String>> docIterator=totalDoc;
        while(docIterator.hasNext()){
            String docId=docIterator.next().getKey();
            List<String> totalTerms=helper.searchTerms("docId",docId);
            double tf=tfIdf.tfCalculator(totalTerms.toArray(new String[totalTerms.size()]),"About");
            System.out.println("tf_"+docId+" = "+tf);
        }


        List<String[]> allTerms=new ArrayList<>();

        totalDoc =Main.getTotalResultByField("docId"); //改了重新new iterator
        docIterator=totalDoc;
        while (docIterator.hasNext()){
            String docId=docIterator.next().getKey();
            List<String> docAllTerms=helper.searchTerms("docId",docId);
            allTerms.add(docAllTerms.toArray(new String[docAllTerms.size()]));
        }

        double idf=tfIdf.idfCalculator(allTerms,"About");
        System.out.println("idf = "+idf);

    }

    @Test
    public void multithreadMainTest() throws Exception{
        Main.multithreadMain();
    }

    @Test
    public void singlethreadMainTest()throws Exception{
        Main.singlethreadMain();
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