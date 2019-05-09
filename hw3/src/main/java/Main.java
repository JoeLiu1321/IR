import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class Main{
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String tarPath=System.getProperty("user.dir")+"/reuters21578.tar";
    private static final String testFile=System.getProperty("user.dir")+"/hw3longtopic.txt";
    private static final String demoPath=testFile;
    public static void main(String[]args)throws Exception {
        buildIndex();
        IndexHelper helper=new IndexHelper(FSDirectory.open(Paths.get(indexPath)));
        List<QueryData>tmp=helper.search("newID","*:*");
        for(QueryData p:tmp)
            System.out.println(p.toString());
    }


    public static void buildIndex() throws Exception{
        FileSlicer slicer = new FileSlicer(demoPath);
        Gettoken tokeHelper = new Gettoken();
        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
        IndexHelper helper = new IndexHelper(indexDir);

        List<String> tmp=slicer.getEachHtml();
        System.out.println("Success Filter All File As Html!!!!");

        Map<String,String> map=new TreeMap<>();
        int start=0;
        for(int i=0;i<tmp.size();i++){
            if(tmp.get(i).matches(".*<REUTERS.*>.*|.*<reuters.*>.*"))
                start = i;
            else if(tmp.get(i).matches(".*</REUTERS.*>.*|.*</reuters.*>.*")) {
                map.put(String.valueOf(start),String.valueOf(i));
            }
        }
        System.out.println("Success Read All SubFile Position!!!!!!");

        System.out.println("SIZE:"+map.size());
        // 錯在上方!!!!!!!!!!!!!!!!
        Iterator<Map.Entry<String,String>>iterator=map.entrySet().iterator();

        List<Callable<Boolean>>taskList=new ArrayList<>();
        ExecutorService executorService= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*10);
        ReentrantLock lock=new ReentrantLock();

        System.out.println("Start Dispatch To Thread .....Available Thread : "+Runtime.getRuntime().availableProcessors()*10);

        while(iterator.hasNext()){
            Map.Entry<String,String>entry=iterator.next();
            taskList.add(() -> {
                try{
                    List<String>subData=tmp.subList(Integer.parseInt(entry.getKey()),Integer.parseInt(entry.getValue()));
                    ParagraphInfo subDocument=tokeHelper.gettoken(slicer.getBlockOfHtml(subData));
                    lock.lock();
                    helper.addDocument(subDocument);
                    lock.unlock();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            });
        }
        System.out.println("Success Dispath All SubFile To Thread !!!!!");
        List<Future<Boolean>>isFinish=executorService.invokeAll(taskList);
        executorService.shutdown();
        System.out.println("Success Add All Document To The Index!!!");
    }

}
