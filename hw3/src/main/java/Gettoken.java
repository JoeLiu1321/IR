import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Gettoken {
    private final String[]luceneKeywords=new String[]{"NOT","AND","OR"};
    public Gettoken(){
    }

    public ParagraphInfo gettoken(String block_html){
        ParagraphInfo info = new ParagraphInfo();
        org.jsoup.nodes.Document docFromFile = Jsoup.parse(block_html, "UTF-8");
        String CGISPLIT = docFromFile.getElementsByTag("REUTERS").attr("CGISPLIT");
        System.out.println(CGISPLIT);
        info.setCGISPLIT(CGISPLIT); //identify set
        String oldID = docFromFile.getElementsByTag("REUTERS").attr("OLDID");
        System.out.println(oldID);
        info.setOldID(oldID);// oldid
        String newID = docFromFile.getElementsByTag("REUTERS").attr("NEWID");
        System.out.println(newID);
        info.setNewID(newID); // newid
        String TOPICS = docFromFile.getElementsByTag("TOPICS").select("D").text();
        System.out.println(TOPICS);
        info.setTopicalCategory(TOPICS); // identify category
        String parse = docFromFile.getElementsByTag("TEXT").text();
//        System.out.println(parse);
//        info.setBody(parse); //body


        String[] parse_list = parse.split(" ");

        String parse_result = "";
        int numOfToken = 0;
        for(String s:parse_list){
            if (s.matches("[a-zA-Z]+[']?[a-zA-Z]")){    // 這樣可以抓到he's didn't這類縮寫 和一般字串
                if(Arrays.asList(luceneKeywords).contains(s))
                    s=s.toLowerCase();
                if ( parse_result != "")
                  parse_result = parse_result + " " ;

                parse_result += s;
                numOfToken++;
            }
        }
        info.setBody(parse_result);
        System.out.println(parse_result);
        info.setNumOfToken(numOfToken);
        System.out.println(numOfToken);

        return info;
    }

}
