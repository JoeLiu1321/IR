import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class Gettoken {
    public Gettoken(){
    }

    public List<String> gettoken(String block_html){
        org.jsoup.nodes.Document docFromFile = Jsoup.parse(block_html, "UTF-8");
        Elements elements = docFromFile.select("html");
        String parse = elements.text();

        String[] parse_list = parse.split(" ");

        List<String> parse_result = new ArrayList<>();
        for(String s:parse_list){
            if (s.matches("[a-zA-Z]+[']?[a-zA-Z]")){
                // 這樣可以抓到he's didn't這類縮寫 和一般字串
                parse_result.add(s);
//                System.out.println(s);
            }
        }
        return parse_result;
    }

}
