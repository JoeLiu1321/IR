import org.apache.lucene.store.Directory;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class QueryCalculator {
    private Map<String,Integer> totalTokenDocumentFreq;
    public Map<String,String> totalDoc;
    private List<String> queryResult;
    private String topicPath=System.getProperty("user.dir")+"/all-topics.txt";
    private static final String docVecPath=System.getProperty("user.dir")+"/doc_vec/";
    private static final String centroidPath=System.getProperty("user.dir")+"/centroid/";
    private IndexHelper helper;
    private int threadCount;
    public QueryCalculator(Directory indexDir, int threadCount)throws Exception{
        this.threadCount=threadCount;
        this.helper=new IndexHelper(indexDir);
        this.totalTokenDocumentFreq = generateAllTokenDocumentFreq(getTotalToken(String::compareTo).keySet().iterator());
        this.totalDoc=getTotalResultByField("newID" , Comparator.comparingInt(Integer::parseInt));
    }

    public Score predictTopic(String docId)throws Exception {
        ExecutorService executorService= Executors.newFixedThreadPool(this.threadCount);
        ReentrantLock lock=new ReentrantLock();
        List<Callable<Boolean>>taskList= new ArrayList<>();
        Map<String,Double>testDocVector=generateDocVector(docId);
        CosineSimilarity cosineSimilarity=new CosineSimilarity();
        Iterator<String>topicIterator=getTotalTopic(null).keySet().iterator();
        Score maxScore=new Score("",-5.0);
        while (topicIterator.hasNext()){
            String topic=topicIterator.next();
            taskList.add(() -> {
                Iterator<Map.Entry<String,Double>>centroidIterator=getVector(centroidPath+topic+".txt").entrySet().iterator();
                if(testDocVector.size()>0 && centroidIterator.hasNext()) {
                    Double result = cosineSimilarity.cosineSimilarity(testDocVector.entrySet().iterator(), centroidIterator);
                    lock.lock();
                    if(maxScore.getValue()<result) {
                        maxScore.setTopic(topic);
                        maxScore.setValue(result);
                    }
                    lock.unlock();
                }
                return true;
            });
        }
        List<Future<Boolean>>futures=executorService.invokeAll(taskList);
        executorService.shutdown();
        return maxScore;
    }

    public Map<String,Double> getVector(String filePath)throws IOException{
        BufferedReader br=new BufferedReader(new FileReader(filePath));
        Map<String,Double> vector=new TreeMap<>();
        String line;
        while((line=br.readLine())!=null){
            String[] split=line.split(":");
            String token=split[0];
            Double weight=Double.parseDouble(split[1]);
            vector.put(token,weight);
        }
        br.close();
        return vector;
    }

    public void writeAllCentroid()throws Exception{
        File dir=new File(centroidPath);
        if(dir.exists()) {
            for(File f:dir.listFiles())
                f.delete();
            dir.delete();
        }
        dir.mkdir();
        Iterator<String>topicIterator=this.getTotalTopic(null).keySet().iterator();
        while (topicIterator.hasNext()) {
            String topic=topicIterator.next();
            Iterator<Map.Entry<String, Double>> iterator = calculateCentroidOfClass(topic).entrySet().iterator();
            FileWriter fw=new FileWriter(centroidPath+topic+".txt");
            while(iterator.hasNext()){
                Map.Entry<String,Double> entry=iterator.next();
                String token=entry.getKey(),weight=entry.getValue().toString();
                fw.write(token+":"+weight+"\n");
                fw.flush();
            }
            fw.close();
        }
    }
    public Map<String,Double> calculateCentroidOfClass(String topic)throws Exception{
        Map<String,Double> centroid=new TreeMap<>();
        String[]field=new String[]{"set","TOPICS"};
        String[]value=new String[]{"training",topic};
        List<QueryData> relatedDocument=helper.booleanQueryWithWildCard(field,value);
        for(QueryData queryData:relatedDocument){
            String docId=queryData.getNewID();
            Iterator<Map.Entry<String,Double>>vectorIterator=getVector(docVecPath+docId+".txt").entrySet().iterator();
            while(vectorIterator.hasNext()){
                Map.Entry<String,Double>entry=vectorIterator.next();
                String token=entry.getKey();
                Double weight=entry.getValue();
                if(!centroid.containsKey(token))
                    centroid.put(token,weight);
                else {
                    Double newWeight=centroid.get(token)+ weight;
                    centroid.replace(token,newWeight);
                }
            }
        }
        int size=relatedDocument.size();
        for(Map.Entry<String,Double>entry:centroid.entrySet()){
            Double weight=entry.getValue()/size;
            centroid.replace(entry.getKey(),weight);
            centroid.put(entry.getKey(),weight);
        }
        return centroid;
    }

    public void writeAllDocVector()throws Exception{
        Iterator<String>iterator=totalDoc.keySet().iterator();
        File dir=new File(docVecPath);
        if(dir.exists()) {
            for(File f:dir.listFiles())
                f.delete();
            dir.delete();
        }
        dir.mkdir();
        while (iterator.hasNext()){

            String doc=iterator.next();

            FileWriter fw=new FileWriter(docVecPath+doc+".txt");
            Iterator<Map.Entry<String,Double>> docIterator=generateDocVector(doc).entrySet().iterator();
            while (docIterator.hasNext()) {
                Map.Entry<String,Double>entry=docIterator.next();
                String write=entry.getKey()+":"+entry.getValue();
                fw.write(write+"\n");
                fw.flush();
            }
            fw.close();
        }
    }

    public Map<String,Double> generateDocVector(String docId)throws Exception{
        List<Callable<Boolean>>taskList= new ArrayList<>();
        ExecutorService executorService= Executors.newFixedThreadPool(this.threadCount);
        ReentrantLock lock=new ReentrantLock();

        Map<String,Double> tokenWeight=new TreeMap<>();
        Iterator<String> iterator=this.totalTokenDocumentFreq.keySet().iterator();
        while(iterator.hasNext()){
            String token=iterator.next();
            taskList.add(() -> {
                double weight=getDocumentWeight(token,docId);
                lock.lock();
                tokenWeight.put(token,weight);
                lock.unlock();
                return true;
            });
        }
        List<Future<Boolean>>futures=executorService.invokeAll(taskList);
        executorService.shutdown();
        return tokenWeight;

    }

    public double getDocumentWeight(String token , String docId ) throws Exception{
        Calculator calculator =new Calculator();
        int tokenFreqInDocument=getTokenFreqInDoc(token,docId);
        int totalTokenInDocument=getDocumentTokenSize(docId);
        int tokenDocumentFreq= this.totalTokenDocumentFreq.get(token);
        double idf= calculator.idfCalculator(this.totalDoc.size(),tokenDocumentFreq);
        double tf=calculator.tfCalculator(tokenFreqInDocument,totalTokenInDocument);
        return calculator.calculateWeight(tf,idf);
    }

    public int getTokenFreqInDoc(String token,String docId)throws Exception{
        List<QueryData> docResult=this.helper.search("newID",docId);
        QueryData queryData=docResult.get(0);
        int tokenFreqInDoc=Collections.frequency(queryData.getTokens(),token);
        return tokenFreqInDoc;
    }

    public int getDocumentTokenSize(String docId)throws Exception{
        List<QueryData> docResult=this.helper.search("newID",docId);
        QueryData queryData=docResult.get(0);
        return queryData.getTokens().size();

    }

    public Map<String,Integer> generateAllTokenDocumentFreq(Iterator<String> tokenKeyIterator)throws InterruptedException{
        Map<String,Integer> allTokenDocumentFreq=new TreeMap<>(String::compareTo);
        List<Callable<Boolean>>taskList= new ArrayList<>();
        ExecutorService executorService= Executors.newFixedThreadPool(this.threadCount);
        ReentrantLock lock=new ReentrantLock();
        while(tokenKeyIterator.hasNext()){
            String token=tokenKeyIterator.next();
            taskList.add(()->{
                Integer tokenDocumentFreq=getDocumentFreq(token);
                lock.lock();
                allTokenDocumentFreq.put(token,tokenDocumentFreq);
                lock.unlock();
                return true;
            });
        }
        List<Future<Boolean>> futures=executorService.invokeAll(taskList);
        executorService.shutdown();
        return  allTokenDocumentFreq;
    }

    public int getDocumentFreq(String token) throws Exception{
        String[]fields=new String[]{"set","body"};
        String[]values=new String[]{"training",token.toLowerCase()};
        int documentFreq=this.helper.booleanQueryWithWildCard(fields,values).size();
        return documentFreq;
    }

    public Map<String,String> getTotalResultByField(String field ,Comparator<String> comparator) throws Exception{
        List<QueryData> totalResult=this.helper.searchWithWildCard("set","training");
        Map<String, String> totalKey=new TreeMap<>(comparator);
        for(QueryData data:totalResult)
            totalKey.put(data.getDocument().get(field), data.getDocument().get(field));
        return totalKey;
    }

    public Map<String,String> getTotalToken(Comparator<String> comparator) throws Exception{
        List<QueryData> result=helper.search("newID","*:*");
        Map<String,String> allTokens=new TreeMap<>(comparator);
        for(QueryData q:result)
            for(String token:q.getTokens())
                allTokens.put(token,token);
        return allTokens;
    }

    public Map<String,String> getTotalTopic(Comparator<String> comparator) throws Exception{
        BufferedReader br=new BufferedReader(new FileReader(new File(topicPath)));
        Map<String, String> totalTopic=new TreeMap<>(comparator);
        String topic;
        while ((topic=br.readLine())!=null)
            totalTopic.put(topic,topic);
        return totalTopic;
    }
}
