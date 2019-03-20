import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    private static final String indexPath=System.getProperty("user.dir")+"/index";
    private static final String filePath=System.getProperty("user.dir")+"/html.txt";
    private static final String warc_09=System.getProperty("user.dir")+"/09.warc";
    private static final String fileOutputPath=System.getProperty("user.dir")+"/inverted_index.txt";
    public static void main(String[]args) throws Exception{
        FileSlicer get_html = new FileSlicer(warc_09);
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
        List<QueryData> totalResult=index_deal.search("*:*");
        Map<String, String> map=new TreeMap<>();
        for(QueryData data:totalResult)
            map.put(data.getToken(),data.getToken());

        int i=0;
        Iterator<String> iterator=map.keySet().iterator();
        while(iterator.hasNext()){
            i++;
            System.out.println(i+" : "+iterator.next());
        }
        FileWriter write=new FileWriter(fileOutputPath);
        iterator=map.keySet().iterator();
        while(iterator.hasNext()){
            String token=iterator.next();
            List<QueryData> queryResult=index_deal.search(token);
            TokenFrequencyAnalyzer analyzer=new TokenFrequencyAnalyzer(token,queryResult);
            System.out.println(analyzer.toString());
            write.write(analyzer.toString()+"\n");
        }
        write.close();
    }



}
