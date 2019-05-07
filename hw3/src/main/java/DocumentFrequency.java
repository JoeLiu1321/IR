import java.util.*;

public class DocumentFrequency {
    private Map<String, String> position;
    private int documentFrequency;
    private String docId;
    public DocumentFrequency(QueryData data){
        this.position=new HashMap<>();
        this.position.put(data.getPosition(),data.getPosition());
        docId=data.getDocumentId();
        documentFrequency=1;
    }

    public void add(QueryData data){
        this.position.put(data.getPosition(),data.getPosition());
        documentFrequency=this.position.size();
    }

    public int getSize(){
        return position.size();
    }

    public Map<String, String> getPosition() {
        return position;
    }

    public void setPosition(Map<String, String> position) {
        this.position = position;
    }

    public int getDocumentFrequency() {
        return documentFrequency;
    }

    public void setDocumentFrequency(int documentFrequency) {
        this.documentFrequency = documentFrequency;
    }


    @Override
    public String toString() {
        String toString=docId+" , "+documentFrequency+" : <";
        Iterator<String> iterator=position.keySet().iterator();
        while(iterator.hasNext()) {
            toString = toString + iterator.next() ;
            if(iterator.hasNext())
                toString+=" , ";
        }
        return toString+"> ;";
    }
}
