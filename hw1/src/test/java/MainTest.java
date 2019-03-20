import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MainTest {
    private final String[]luceneKeywords=new String[]{"NOT","AND","OR"};
    @Test
    public void main() {
        if(Arrays.asList(luceneKeywords).contains("NOT"))
            System.out.println("NOT".toLowerCase());
    }
}