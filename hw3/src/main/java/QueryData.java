import org.apache.lucene.document.Document;
import java.util.ArrayList;
import java.util.List;

public class QueryData {
    private Document document;
    private String CGISPLIT;
    private List<String> tokens;
    private String oldID;
    private String newID;
    private String topicalCategory;
    private int numOfToken;

    public QueryData(){
    }

    public QueryData(Document document){
        parseDocument(document);
    }

    private void parseDocument(Document document){
        setDocument(document);
        setCGISPLIT(document.get("set"));
        setOldID(document.get("oldID"));
        setNewID(document.get("newID"));
        setTopicalCategory(document.get("TOPICS"));
        setNumOfToken(Integer.parseInt(document.get("numOfToken")));
        setToken(document.get("body"));

    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getOldID() {
        return this.oldID;
    }

    public String getNewID() {
        return this.newID;
    }

    public String getTopicalCategory() {
        return this.topicalCategory;
    }

    public String getCGISPLITCGISPLIT()
    {
        return this.CGISPLIT;
    }

    public int getNumOfToken() {
        return numOfToken;
    }

    public void setNumOfToken(int _numOfToken) {
        this.numOfToken = _numOfToken;
    }

    public void setCGISPLIT(String _CGISPLIT) {
        this.CGISPLIT = _CGISPLIT;
    }

    public void setOldID(String _oldID) {
        this.oldID = _oldID;
    }

    public void setNewID(String _newID) {
        this.newID = _newID;
    }

    public void setTopicalCategory(String _topicalCategory) {
        this.topicalCategory = _topicalCategory;
    }

    public List<String> getTokens(){
        return tokens;
    }

    public void setToken(String tokens) {
        List<String> result=new ArrayList<>();
        String[]splitTokens=tokens.split(" ");
        for(String string:splitTokens)
            result.add(string);
        this.tokens=result;

    }

    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append("Document ID : ").append(getNewID()).append("\nTopic : ").append(getTopicalCategory()+"\nBody : ");
        for(String token:getTokens())
            builder.append(token).append(" ");
        builder.append("\n");
        return builder.toString();
    }
}
