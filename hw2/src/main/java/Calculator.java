/*******************************************************************************
 * ************************     ADNAN OQUAISH     ******************************
 * *************************     BITS Pilani     *******************************
 *******************************************************************************/
import java.text.DecimalFormat;

public class Calculator {
    private DecimalFormat df;
    public Calculator(){
        this.df=new DecimalFormat("#.##");
    }
    public Calculator(DecimalFormat df){
        this.df=df;
    }

    public double reduceCompute(double value){
        if(Double.isInfinite(value) | Double.isNaN(value))
            return 0.0;
        else{
            return Double.parseDouble(df.format(value));
        }
    }

    public double calculateWeight(double tf, double idf){
        tf=this.reduceCompute(tf);
        idf=this.reduceCompute(idf);
        double result=(1+Math.log(tf)) * idf;
        return this.reduceCompute(result);
    }

    public double tfCalculator(double termFreq, double totalTerms) {
        double result=termFreq/totalTerms;
        return this.reduceCompute(result);
    }

    // Calculates idf of term termToCheck
    // allDocuments : total document count
    //documentTimes : count of the total document which contains the term
    // returns idf(inverse document frequency) score
    
    public double idfCalculator(double allDocuments, int documentTimes) {
        double result=Math.log(allDocuments / documentTimes);
        return this.reduceCompute(result);
    }
}
