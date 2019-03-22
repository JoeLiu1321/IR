import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

//
//  use the MainTest.java to run the code
//
public class Main{
    private static final String indexPath=System.getProperty("user.dir")+"/index"; //The index file
    private static final String filePath=System.getProperty("user.dir")+"/html.txt"; //1 million column test data
    private static final String warc_09=System.getProperty("user.dir")+"/09.warc"; //The input file
    private static final String fileOutputPath=System.getProperty("user.dir")+"/output.txt"; //the output file
    public static void main(String[]args) throws Exception{
//        singlethreadMain(filePath); //use the single thread
        multithreadMain(filePath);  //use multi thread
    }
    public static void singlethreadMain(String inputFile)throws Exception{
        FileSlicer get_html = new FileSlicer(inputFile);
        Gettoken gettoken_deal = new Gettoken();
        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
        index_helper index_deal = new index_helper(indexDir);
        int start=0;
        List<String> tmp = get_html.getEachHtml();
        System.out.println("Success Filter All File As Html!!!!");

        Iterator<String>it=tmp.iterator();
        while(it.hasNext())
            it.next();
        for(int i=0;i<tmp.size();i++) {
            if(tmp.get(i).matches("<html>.{0}|<HTML>.{0}"))
                start = i;
            else if(tmp.get(i).matches("</html>|</HTML>")) {

                String temp = get_html.getBlockOfHtml(tmp.subList(start,i));
                index_deal.addDocument(gettoken_deal.gettoken(temp));
            }
        }
        System.out.println("Success Add All Document To The Index!!!");

        List<QueryData> totalResult=index_deal.search("*:*");
        Map<String, String> map=new TreeMap<>();
        for(QueryData data:totalResult)
            map.put(data.getToken(),data.getToken());

        System.out.println("Success Get All Unique Key!!!!");

        System.out.println("Start Output The Result!!!!!");
        FileWriter write=new FileWriter(fileOutputPath);
        Iterator<String> iterator=map.keySet().iterator();
        while(iterator.hasNext()){
            String token=iterator.next();
            List<QueryData> queryResult=index_deal.search(token);
            TokenFrequencyAnalyzer analyzer=new TokenFrequencyAnalyzer(token,queryResult);
            write.write(analyzer.toString()+"\n");
        }
        write.close();
    }

    public static void multithreadMain(String inputFile) throws Exception{
        FileSlicer slicer = new FileSlicer(inputFile);
        Gettoken tokeHelper = new Gettoken();
        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
        index_helper helper = new index_helper(indexDir);

        List<String> tmp=slicer.getEachHtml();
        System.out.println("Success Filter All File As Html!!!!");

        Map<String,String> map=new TreeMap<>();
        int start=0;
        for(int i=0;i<tmp.size();i++){
            if(tmp.get(i).matches("<html>.{0}|<HTML>.{0}"))
                start = i;
            else if(tmp.get(i).matches("</html>|</HTML>")) {
                map.put(String.valueOf(start),String.valueOf(i));
            }
        }
        System.out.println("Success Read All SubHtml Position!!!!!!");

        Iterator<Map.Entry<String,String>>iterator=map.entrySet().iterator();
        List<Callable<Boolean>>taskList=new ArrayList<Callable<Boolean>>();
        ExecutorService executorService= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ReentrantLock lock=new ReentrantLock();

        System.out.println("Start Dispatch To Thread .....Available Thread : "+Runtime.getRuntime().availableProcessors());

        while(iterator.hasNext()){
            Map.Entry<String,String>entry=iterator.next();
            taskList.add((Callable) () -> {
                try{
                    List<String>subData=tmp.subList(Integer.parseInt(entry.getKey()),Integer.parseInt(entry.getValue()));
                    List<String>subDocument=tokeHelper.gettoken(slicer.getBlockOfHtml(subData));
                    lock.lock();
                    helper.addDocument(subDocument);
                    lock.unlock();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    return true;
                }
            });
        }
        System.out.println("Success Dispath All SubHtml To Thread !!!!!");
        List<Future<Boolean>>isFinish=executorService.invokeAll(taskList);
        System.out.println("Success Add All Document To The Index!!!");

        List<QueryData> totalResult=helper.search("*:*");
        Map<String, String> uniqueKey=new TreeMap<>();
        for(QueryData data:totalResult)
            uniqueKey.put(data.getToken(),data.getToken());

        System.out.println("Success Get All Unique Key!!!!");

        Iterator<String> uniqueKeyIterator=uniqueKey.keySet().iterator();
        System.out.println("Start Output The Result!!!!!");
        FileWriter write=new FileWriter(fileOutputPath);
        while(uniqueKeyIterator.hasNext()){
            String token=uniqueKeyIterator.next();
            List<QueryData> queryResult=helper.search(token);
            TokenFrequencyAnalyzer analyzer=new TokenFrequencyAnalyzer(token,queryResult);
            write.write(analyzer.toString()+"\n");
        }
        write.close();
        System.out.println("Finish Output !!!!!");

    }
}
