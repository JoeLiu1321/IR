import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.junit.After;
import org.junit.Test;
import java.io.File;

public class MainTest {
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String fileOutputPath=System.getProperty("user.dir")+"/output.txt";
    private static final String lessDataFile=System.getProperty("user.dir")+"/html.txt"; //1 million column test data
    private static final String warc_09=System.getProperty("user.dir")+"/09.warc"; //The input file

    @Test
    public void multithreadMainTest() throws Exception{
        Main.multithreadMain(lessDataFile);
    }

    @Test
    public void singlethreadMainTest()throws Exception{
        Main.singlethreadMain(lessDataFile);
    }

    @After
    public void tearDown(){
//        File f=new File(indexPath);
//        for(String s:f.list())
//            new File(indexPath,s).delete();
//        f.delete();
//        File file=new File(fileOutputPath);
//        file.delete();
    }

}