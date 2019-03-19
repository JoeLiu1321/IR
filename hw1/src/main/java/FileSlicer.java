import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileSlicer {
    private String filePath;
    public FileSlicer(String filePath){
        this.filePath=filePath;
    }
    public List<String> getEachHtml()throws Exception{
        List<String> result=new ArrayList<>();
        BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        String lineText;
        while((lineText=br.readLine())!=null)
            result.add(lineText);
        return result;
    }
    public String getBlockOfHtml(List<String> list,int start,int end){
        String temp="";
        for(int i=start;i<end;i++){
            temp+=(list.get(i));
        }
        return temp;
    }

}