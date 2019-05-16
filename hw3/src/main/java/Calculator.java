import java.text.DecimalFormat;

public class Calculator {
    private DecimalFormat df;
    public Calculator(){
        this.df=new DecimalFormat("#.####");
    }
    public double calculateWeight(double tf, double idf){

//        return (1+Math.log(tf)) * idf;
        return reduceCompute(tf * idf);
    }

    private double reduceCompute(double value){
        if(Double.isInfinite(value) | Double.isNaN(value))
            return 0;
        else{
            return Double.parseDouble(df.format(value));
        }
    }
    public double tfCalculator(double termFreq, double totalTerms) {
        return reduceCompute(termFreq / totalTerms);
    }

    // Calculates idf of term termToCheck
    // allDocuments : total document count
    //documentContainsTerm : count of the total document which contains the term
    // returns idf(inverse document frequency) score
    
    public double idfCalculator(double allDocuments, int documentTimes) {
        return reduceCompute(Math.log(allDocuments / documentTimes));
    }
}
