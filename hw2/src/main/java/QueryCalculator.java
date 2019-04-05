import org.apache.lucene.store.Directory;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class QueryCalculator {
    private Map<String,Integer> totalTokenDocumentFreq;
    private Map<String,String> totalDoc;
    private List<String> queryResult;
    private String query;
    private IndexHelper helper;
    private int threadCount;
    public QueryCalculator(Directory indexDir, int threadCount, String query)throws Exception{
        this.threadCount=threadCount;
        this.helper=new IndexHelper(indexDir);
        this.query=query;
        this.queryResult=helper.searchTerms(this.query);
        this.totalTokenDocumentFreq = generateAllTokenDocumentFreq(getTotalResultByField("token",String::compareTo).keySet().iterator());
        this.totalDoc=getTotalResultByField("docId" , Comparator.comparingInt(Integer::parseInt));
    }


    public Iterator<Score> calculateRank()throws Exception{
        List<Score>rankList=new ArrayList<>();
        CosineSimilarity similarity=new CosineSimilarity();
        Map<String,Double>queryVector=this.generateQueryVector();
        Iterator<String> iterator=this.totalDoc.keySet().iterator();
        System.out.println("-----------Start Execute the Rank----------");
        while(iterator.hasNext()){
            String docId=iterator.next();
            Iterator<Map.Entry<String,Double>>docVector=generateDocVector(docId);
            Iterator<Map.Entry<String,Double>>queryVectorTmp=queryVector.entrySet().iterator();
            Double score=similarity.cosineSimilarity(docVector,queryVectorTmp);
            System.out.println(docId+" : "+score+"\n");
            if(!score.equals(Math.abs(0.0)))
                rankList.add(new Score(docId,score));
        }
        System.out.println("--------Finish Execute-------");
        rankList.sort((o1, o2) -> -Double.compare(o1.getScore(),o2.getScore()));
        return rankList.iterator();
    }

    public Iterator<Map.Entry<String,Double>> generateDocVector(String docId)throws Exception{
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

        return tokenWeight.entrySet().iterator();

    }

    public Map<String,Double> generateQueryVector()throws Exception{
        List<Callable<Boolean>>taskList= new ArrayList<>();
        ExecutorService executorService= Executors.newFixedThreadPool(this.threadCount);
        ReentrantLock lock=new ReentrantLock();
        Map<String,Double> queryWeight=new TreeMap<>();
        Iterator<String> iterator=this.totalTokenDocumentFreq.keySet().iterator();
        while(iterator.hasNext()){
            String token=iterator.next();
            taskList.add(() -> {
                double weight=getQueryWeight(token);
                lock.lock();
                queryWeight.put(token,weight);
                lock.unlock();
                return true;
            });
        }
        List<Future<Boolean>>futures=executorService.invokeAll(taskList);
        return queryWeight;
    }

    public double getDocumentWeight(String token , String docId ) throws Exception{
        Calculator calculator =new Calculator();
        String condition="docId:"+docId+" AND token:"+token;
        int tokenFreqInDocument=this.helper.search(condition).size();
        int totalTokenInDocument=this.helper.searchTerms("docId:"+docId).size();
        int tokenDocumentFreq= this.totalTokenDocumentFreq.get(token);
        double idf= calculator.idfCalculator(this.totalDoc.size(),tokenDocumentFreq);
        double tf=calculator.tfCalculator(tokenFreqInDocument,totalTokenInDocument);
        return calculator.calculateWeight(tf,idf);
    }

    public double getQueryWeight(String token) throws Exception{
        Calculator calculator =new Calculator();
        int tokenFreqInQuery=getQueryFreq(this.queryResult.iterator(),token);
        int totalQueryTerms=this.queryResult.size();
        int documentFreq= this.totalTokenDocumentFreq.get(token);
        double idf= calculator.idfCalculator(this.totalDoc.size(),documentFreq);
        double tf=calculator.tfCalculator(tokenFreqInQuery,totalQueryTerms);
        return calculator.calculateWeight(tf,idf);
    }

    public int getQueryFreq(Iterator<String> queryResult,String token){
        int queryFreq=0;
        while(queryResult.hasNext()) {
            if(queryResult.next().equals(token))
                queryFreq++;
        }
        return queryFreq;
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
        return  allTokenDocumentFreq;
    }

    public int getDocumentFreq(String token) throws Exception{
        Iterator<QueryData> resultIterator=this.helper.search("token:"+token).iterator();
        Map<String ,QueryData> resultWithNoDuplicate=new TreeMap<>();
        while(resultIterator.hasNext()){
            QueryData data=resultIterator.next();
            resultWithNoDuplicate.put(data.getDocumentId(),data);
        }
        return resultWithNoDuplicate.size();
    }

    public Map<String,String> getTotalResultByField(String field ,Comparator<String> comparator) throws Exception{
        List<QueryData> totalResult=this.helper.search(field,"*:*");
        Map<String, String> totalKey=new TreeMap<>(comparator);
        for(QueryData data:totalResult)
            totalKey.put(data.getDocument().get(field),data.getDocument().get(field));
        return totalKey;

    }
}
