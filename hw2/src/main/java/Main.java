import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class Main{
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String filePath=System.getProperty("user.dir")+"/html.txt";
    private static final String warc_09=System.getProperty("user.dir")+"/09.warc";
    private static final String fileOutputPath=System.getProperty("user.dir")+"/hw2_output.txt";
    public static void main(String[]args)throws Exception {
        IndexHelper helper=new IndexHelper(FSDirectory.open(Paths.get(indexPath)));
        Scanner scanner=new Scanner(System.in);
        while(true) {
                System.out.println("\nInput Your Query(exit to quit) : ");
                String query = scanner.nextLine();
                if(query.equals("exit"))
                    break;
                System.out.println("Your query = " + query );
                query=processUserQuery(query);
                int times= helper.search(query).size();
                if(times>0){
                    System.out.println("There are '"+times+"' correspond result for '"+query+"'");
                    rankUserQuery(query);
                }
                else
                    System.out.println("There are no correspond result.......\nPlease Input another query again!!!");
        }
    }

    public static String processUserQuery(String query){
        if(containsBooleanOperation(query))
            return processBooleanQuery(query);
        else
            return processFreeText(query);
    }

    public static String processFreeText(String query){
        String[] tokens =query.split("[ ]+");
        StringBuilder afterProcess= new StringBuilder();
        for(int i = 0; i<tokens.length; i++){
            String token=tokens[i];
            afterProcess.append("token:").append(token);
            if(i!=tokens.length-1)
                afterProcess.append(" OR ");
        }
        return afterProcess.toString();
    }

    public static String processBooleanQuery(String query){
        String[] tokens =query.split("[ ]+");
        StringBuilder afterProcess= new StringBuilder();
        for(int i = 0; i<tokens.length; i++){
            String token=tokens[i];
            if(containsBooleanOperation(token) && i!=0 && i!=tokens.length-1) {
                afterProcess.append(" ").append(token).append(" ");
            }
            else if(!containsBooleanOperation(token)){
                afterProcess.append("token:").append(token);
            }
        }
        return afterProcess.toString();
    }

    public static Boolean containsBooleanOperation(String query){
        return query.contains("AND") || query.contains("OR");
    }

    public static void rankUserQuery(String query) throws Exception{
        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
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
        fw.close();
    }

    public static void buildIndex() throws Exception{
        FileSlicer slicer = new FileSlicer(filePath);
        Gettoken tokeHelper = new Gettoken();
        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
        IndexHelper helper = new IndexHelper(indexDir);

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
        List<Callable<Boolean>>taskList=new ArrayList<>();
        ExecutorService executorService= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*10);
        ReentrantLock lock=new ReentrantLock();

        System.out.println("Start Dispatch To Thread .....Available Thread : "+Runtime.getRuntime().availableProcessors());

        while(iterator.hasNext()){
            Map.Entry<String,String>entry=iterator.next();
            taskList.add(() -> {
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
                return true;
            });
        }
        System.out.println("Success Dispath All SubHtml To Thread !!!!!");
        List<Future<Boolean>>isFinish=executorService.invokeAll(taskList);
        System.out.println("Success Add All Document To The Index!!!");
    }

}
