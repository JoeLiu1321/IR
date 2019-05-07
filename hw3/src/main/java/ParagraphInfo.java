import org.apache.lucene.document.Document;

public class ParagraphInfo {
    private String CGISPLIT="";
    private String body="";
    private String oldID="";
    private String newID="";
    private String topicalCategory="";
    private int numOfToken=0;

    public ParagraphInfo(){
    }

    public ParagraphInfo(Document document){
        parseDocument(document);
    }
    private void parseDocument(Document document){
        setCGISPLIT(document.get("set"));
        setOldID(document.get("oldID"));
        setNewID(document.get("newID"));
        setTopicalCategory(document.get("TOPICS"));
        setNumOfToken(Integer.parseInt(document.get("numOfToken")));
        setBody(document.get("body"));

    }

    public String getBody() {
        return this.body;
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

    public void setBody(String _body) {
        this.body = _body;
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

    @Override
    public String toString(){
        return getNewID()+":"+getBody();
    }

}
