
//import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsoupParser {
    private String filePath;
    private Document fileDocument;
    public JsoupParser(String filePath)throws IOException {
        this.filePath=filePath;
        File f=new File(filePath);

//        this.fileDocument= Jsoup.parse(f,"UTF-8");
    }
    public void getAttr() throws Exception{
        List<String> tmp=new ArrayList<>();
        List<List<String>> result=new ArrayList<>();
        BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        String lineText="";
        while((lineText=br.readLine())!=null)
            tmp.add(lineText);
        int start=0;

        for(int i=0;i<tmp.size();i++) {
            if(tmp.get(i).matches("<html>.{0}|<HTML>.{0}"))
                start = i;
//                System.out.println("Start : "+start);

            else if(tmp.get(i).matches("</html>|</HTML>"))
                result.add(getBlockOfHtml(tmp, start, i));

        }
        for(int i=0;i<result.size();i++)
            for(int j=0;j<result.get(i).size();j++)
                System.out.println(result.get(i).get(j));
        System.out.println(result.size());
    }
    public List<String> getBlockOfHtml(List<String> list,int start,int end){
        List<String> l=new ArrayList<>();
        for(int i=start;i<end+1;i++){
            l.add(list.get(i));
        }
        return l;
    }

}

