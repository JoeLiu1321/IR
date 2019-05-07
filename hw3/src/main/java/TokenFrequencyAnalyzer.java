import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TokenFrequencyAnalyzer {
    private List<QueryData> dataList;
    private int totalTimes;
    private String term;
    private Map<String,DocumentFrequency> frequencyData;
    public TokenFrequencyAnalyzer(String term,List<QueryData> dataList) {
        this.dataList = dataList;
        frequencyData =new TreeMap<>();
        totalTimes=0;
        this.term=term;
        analyze();
        analyzeTotalTimes();
    }

    public void analyze(){
        Iterator<QueryData>iterator=dataList.iterator();
        while(iterator.hasNext()){
            QueryData data=iterator.next();
            String docId=data.getDocumentId();
            DocumentFrequency frequency;
            if(!frequencyData.containsKey(docId))
                frequency=new DocumentFrequency(data);
            else{
                frequency= frequencyData.get(docId);
                frequency.add(data);
            }
            frequencyData.put(docId, frequency);
        }

    }

    public void analyzeTotalTimes(){
        Iterator<Map.Entry<String,DocumentFrequency>>iterator=frequencyData.entrySet().iterator();
        while(iterator.hasNext()){
            totalTimes+=iterator.next().getValue().getSize();
        }
    }

    public List<QueryData> getDataList() {
        return dataList;
    }

    public void setDataList(List<QueryData> dataList) {
        this.dataList = dataList;
    }

    @Override
    public String toString(){
        String toString=term+" , "+totalTimes+" : \n< ";
        Iterator<Map.Entry<String,DocumentFrequency>>iterator=frequencyData.entrySet().iterator();
        while(iterator.hasNext()) {
            toString +=iterator.next().getValue().toString();
            if (iterator.hasNext())
                toString += "\n";
        }
        return toString+" >\n";
    }
}
