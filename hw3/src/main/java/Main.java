import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
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
    private static final String shortFile=System.getProperty("user.dir")+"/hw3test.txt";
    private static final String thoundsDoc=System.getProperty("user.dir")+"/reut2-000.sgm";
    private static final String demoPath=testFile;
    public static void main(String[]args)throws Exception {
        Scanner s=new Scanner(System.in);
        QueryCalculator queryCalculator=null;
        while(true){
            System.out.println("\n1.build index\n2.testing\n3.training\n4.exit\nPlease input the option\n");
            int option=s.nextInt();
            switch (option){
                case 1:
                    File index=new File(indexPath);
                    if(index.exists()) {
                        for(File f:index.listFiles())
                            f.delete();
                        index.delete();
                    }
                    buildIndex();
                    break;
                case 2:
                    if(queryCalculator==null)
                        queryCalculator=new QueryCalculator(FSDirectory.open(Paths.get(indexPath)),Runtime.getRuntime().availableProcessors()*10);
                    System.out.println("Start Test!!");
                    test(queryCalculator);
                    break;
                case 3:
                    if(queryCalculator==null)
                        queryCalculator=new QueryCalculator(FSDirectory.open(Paths.get(indexPath)),Runtime.getRuntime().availableProcessors()*10);
                    System.out.println("Start training!!");
                    queryCalculator.writeAllDocVector();
                    queryCalculator.writeAllCentroid();
                    System.out.println("Training Successfully!!");
                    break;
                case 4:
                    System.out.println("exit");
                    return;
                default:
                    break;
            }
        }
    }

    public static void test(QueryCalculator queryCalculator)throws Exception{
        IndexHelper helper=new IndexHelper(FSDirectory.open(Paths.get(indexPath)));
        List<QueryData> testSet=helper.booleanQueryWithWildCard(new String[]{"set"},new String[]{"*testset"});
        int successPredict=0;
        int totalTest=testSet.size();
        for(QueryData queryData:testSet){
            Score maxRank=queryCalculator.predictTopic(queryData.getNewID());
            System.out.println("\ntest Document:"+queryData.getNewID()+"\nPredict:"+maxRank.getTopic()+"\nActual:"+queryData.getTopicalCategory()+"\n");
            if(maxRank.getTopic().equals(queryData.getTopicalCategory()))
                successPredict++;
        }
        System.out.println("Total Test :"+totalTest+"\nSuccess : "+successPredict+"\nFail : "+(totalTest-successPredict));
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
