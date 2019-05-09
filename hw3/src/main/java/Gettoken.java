import org.jsoup.Jsoup;

import java.util.Arrays;

public class Gettoken {
    private final String[]luceneKeywords=new String[]{"NOT","AND","OR"};
    public Gettoken(){
    }

    public ParagraphInfo gettoken(String block_html){
        ParagraphInfo info = new ParagraphInfo();
        org.jsoup.nodes.Document docFromFile = Jsoup.parse(block_html, "UTF-8");
        String CGISPLIT = docFromFile.getElementsByTag("REUTERS").attr("CGISPLIT");
        info.setCGISPLIT(CGISPLIT.toLowerCase()); //identify set
        String oldID = docFromFile.getElementsByTag("REUTERS").attr("OLDID");
        info.setOldID(oldID.toLowerCase());// oldid
        String newID = docFromFile.getElementsByTag("REUTERS").attr("NEWID");
        info.setNewID(newID.toLowerCase()); // newid
        String TOPICS = docFromFile.getElementsByTag("TOPICS").select("D").text();
        info.setTopicalCategory(TOPICS.toLowerCase()); // identify category
        String parse = docFromFile.getElementsByTag("TEXT").text();
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
        info.setBody(parse_result.toLowerCase());
        info.setNumOfToken(numOfToken);
        System.out.println("Success Read:"+newID);
        return info;
    }

}
