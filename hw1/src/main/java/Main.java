import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String filePath=System.getProperty("user.dir")+"/html.txt";
    public static void main(String[]args) throws Exception{
        FileSlicer get_html = new FileSlicer(filePath);
        Gettoken gettoken_deal = new Gettoken();
        Directory indexDir= FSDirectory.open(Paths.get(indexPath));
        index_helper index_deal = new index_helper(indexDir);

        int start=0;
        List<String> tmp = get_html.getEachHtml();
        for(int i=0;i<tmp.size();i++) {
            if(tmp.get(i).matches("<html>.{0}|<HTML>.{0}"))
                start = i;
            else if(tmp.get(i).matches("</html>|</HTML>")) {
                String temp = get_html.getBlockOfHtml(tmp, start, i);
                index_deal.addDocument(gettoken_deal.gettoken(temp));
            }
        }
        List<QueryData> searchResult=index_deal.search("*:*");
        for(QueryData data:searchResult)
            System.out.println(data.toString());

        Map<String,String> outputMap=new HashMap<>() ;

    }



}
