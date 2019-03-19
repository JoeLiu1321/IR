import java.util.ArrayList;
import java.util.List;

public class Term {
    String tokenKey;
    List<DocumentFrequency> documentFrequency;
    public Term(QueryData data){
        this.tokenKey=data.getToken();
        documentFrequency=new ArrayList<>();
        documentFrequency.add(new DocumentFrequency());
    }
}
