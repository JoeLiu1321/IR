import org.apache.lucene.document.Document;
public class QueryData {
    private Document document;
    private String documentId,token,position;

    public QueryData(Document document){
        setDocument(document);
        setDocumentId(document.get("docId"));
        setToken(document.get("token"));
        setPosition(document.get("pos"));
    }

    public String getToken() {
        return token;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        String result="Document ID : "+getDocumentId()+"\nToken : "+getToken()+"\nPosition : "+getPosition()+"\n";
        return result;
    }
}
