import org.apache.lucene.store.Directory;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class RankCalculator {
    private Map<String,String> totalToken;
    private Map<String,String> totalDoc;
    private IndexHelper helper;
    private int threadCount;
    public RankCalculator(Directory indexDir,int threadCount)throws Exception{
        this.threadCount=threadCount;
        this.helper=new IndexHelper(indexDir);
        this.totalToken=getTotalResultByField("token",String::compareTo);
        this.totalDoc=getTotalResultByField("docId" , Comparator.comparingInt(Integer::parseInt));
    }

//    public Iterator<Score> calculateRankWithMultiThread(String query,int threadCount)throws Exception{
//        List<Score>rankList=new ArrayList<>();
//        CosineSimilarity similarity=new CosineSimilarity();
//        Map<String,Double>queryVector=this.generateQueryVector(query);
//        Iterator<String> iterator=totalDoc.keySet().iterator();
//        System.out.println("-----------Start Dispatch task to Thread----------");
//        List<Callable<Score>>taskList= new ArrayList<>();
//        ExecutorService executorService= Executors.newFixedThreadPool(threadCount);
//        while(iterator.hasNext()){
//            String docId=iterator.next();
//            taskList.add(() -> {
//                try{
//                    Iterator<Map.Entry<String,Double>>docVector=generateDocVector(docId);
//                    Iterator<Map.Entry<String,Double>>queryVectorTmp=queryVector.entrySet().iterator();
//                    Double score=similarity.cosineSimilarity(docVector,queryVectorTmp);
//                    System.out.println(docId+" : "+score+"\n");
//                    return new Score(docId, score);
//                }
//                catch (Exception e){
//                    e.printStackTrace();
//                    return null;
//                }
//            });
//        }
//        System.out.println("----------------Success Dispath Task To Thread --------------");
//        List<Future<Score>>futures=executorService.invokeAll(taskList);
//        System.out.println("-----------Success Execute all Rank !!!---------\n-------Start to sort--------");
//        Iterator<Future<Score>> futureIterator=futures.iterator();
//        while(iterator.hasNext()){
//            Future<Score> future=futureIterator.next();
//            if(future.get().getScore()!=0)
//                rankList.add(future.get());
//        }
//        rankList.sort((o1, o2) -> -Double.compare(o1.getScore(),o2.getScore()));
//        return rankList.iterator();
//    }

    public Iterator<Score> calculateRank(String query)throws Exception{
        List<Score>rankList=new ArrayList<>();
        CosineSimilarity similarity=new CosineSimilarity();
        Map<String,Double>queryVector=this.generateQueryVector(query);
        Iterator<String> iterator=totalDoc.keySet().iterator();
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
        Iterator<String> iterator=this.totalToken.keySet().iterator();
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

    public Map<String,Double> generateQueryVector(String query)throws Exception{
        List<Callable<Boolean>>taskList= new ArrayList<>();
        ExecutorService executorService= Executors.newFixedThreadPool(this.threadCount);
        ReentrantLock lock=new ReentrantLock();

        List<String> totalQueryResult=this.helper.searchTerms("token",query);
        Map<String,Double> queryWeight=new TreeMap<>();
        Iterator<String> iterator=this.totalToken.keySet().iterator();
        while(iterator.hasNext()){
            String token=iterator.next();
            taskList.add(() -> {
                double weight=getQueryWeight(token,query,totalQueryResult.size());
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
        int termFreq=this.helper.search(condition).size();
        int totalTerms=this.helper.searchTerms("docId",docId).size();
        int documentFreq= getDocumentFreq(token);
        double idf= calculator.idfCalculator(this.totalDoc.size(),documentFreq);
        double tf=calculator.tfCalculator(termFreq,totalTerms);
        return calculator.calculateWeight(tf,idf);
    }

    public double getQueryWeight(String token , String query ,int totalQueryCount) throws Exception{
        Calculator calculator =new Calculator();
        String condition="token:"+query+" AND token:"+token;
        int termFreq=this.helper.search(condition).size();
        int totalTerms=this.helper.searchTerms("token",query).size();
        int documentFreq= getDocumentFreq(token);
        double idf= calculator.idfCalculator(totalQueryCount,documentFreq);
        double tf=calculator.tfCalculator(termFreq,totalTerms);
        return calculator.calculateWeight(tf,idf);
    }

    public int getDocumentFreq(String token) throws Exception{
        Iterator<QueryData> resultIterator=this.helper.search("token",token).iterator();
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
