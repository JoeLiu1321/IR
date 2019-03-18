import org.junit.Test;

import static org.junit.Assert.*;

public class JsoupParserTest {

    @Test
    public void getAttr() {
        try {
            JsoupParser jp = new JsoupParser(System.getProperty("user.dir") + "/test_jsoup.txt");

            jp.getAttr();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Test
    public void get(){
        String s="<html>456</html>";
        System.out.println(s.matches("</html>|</HTML>"));
    }
}