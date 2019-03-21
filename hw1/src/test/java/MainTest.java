import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Test;

import java.io.FileWriter;
import java.util.concurrent.locks.ReentrantLock;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class MainTest {
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String fileOutputPath=System.getProperty("user.dir")+"/inverted_index.txt";
    @Test
    public void multithreadMainTest() throws Exception{
        Main.multithreadMain();
    }

    @Test
    public void singlethreadMainTest()throws Exception{
        Main.singlethreadMain();
    }

    @After
    public void tearDown(){
        File f=new File(indexPath);
        for(String s:f.list())
            new File(indexPath,s).delete();
        f.delete();
        File file=new File(fileOutputPath);
        file.delete();
    }

}