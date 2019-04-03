import org.apache.lucene.store.Directory;
import java.util.*;

public class RankCalculator {
    public Map<String,String> totalToken;
    public Map<String,String> totalDoc;
    private Directory indexDir;
    private IndexHelper helper;
    public RankCalculator(Directory indexDir)throws Exception{
        this.indexDir=indexDir;
        this.helper=new IndexHelper(this.indexDir);
        this.totalToken=getTotalResultByField("token",String::compareTo);
        this.totalDoc=getTotalResultByField("docId" , Comparator.comparingInt(Integer::parseInt));

    }

    public Iterator<Score> calculateRank(String query)throws Exception{
        List<Score>rankList=new ArrayList<>();
        System.out.println("-----------calculateRank----------");
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
            if(!score.equals(0.0))
                rankList.add(new Score(docId,score));
        }
        System.out.println("--------Finish Execute-------");
        rankList.sort((o1, o2) -> -Double.compare(o1.getScore(),o2.getScore()));
        return rankList.iterator();
    }

    public Iterator<Map.Entry<String,Double>> generateDocVector(String docId)throws Exception{
        Map<String,Double> tokenWeight=new TreeMap<>();
        Iterator<String> iterator=this.totalToken.keySet().iterator();
        while(iterator.hasNext()){
            String token=iterator.next();
            double weight=getDocumentWeight(token,docId,this.totalDoc.size());
            tokenWeight.put(token,weight);
        }
        return tokenWeight.entrySet().iterator();

    }

    public Map<String,Double> generateQueryVector(String query)throws Exception{
        List<String> totalQueryResult=this.helper.searchTerms("token",query);
        Map<String,Double> queryWeight=new TreeMap<>();
        Iterator<String> iterator=this.totalToken.keySet().iterator();
        while(iterator.hasNext()){
            String token=iterator.next();
            double weight=getQueryWeight(token,query,totalQueryResult.size());
            queryWeight.put(token,weight);
        }
        return queryWeight;
    }

    public double getDocumentWeight(String token , String docId ,int totalDocCount) throws Exception{
        Calculator calculator =new Calculator();
        String condition="docId:"+docId+" AND token:"+token;
        int termFreq=this.helper.search(condition).size();
        int totalTerms=this.helper.searchTerms("docId",docId).size();
        int documentFreq= getDocumentFreq(token);
        double idf= calculator.idfCalculator(totalDocCount,documentFreq);
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
        IndexHelper helper = new IndexHelper(this.indexDir);
        List<QueryData> totalResult=helper.search(field,"*:*");
        Map<String, String> totalKey=new TreeMap<>(comparator);
        for(QueryData data:totalResult)
            totalKey.put(data.getDocument().get(field),data.getDocument().get(field));
        return totalKey;

    }
}
