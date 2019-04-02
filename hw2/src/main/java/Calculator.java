/*******************************************************************************
 * ************************     ADNAN OQUAISH     ******************************
 * *************************     BITS Pilani     *******************************
 *******************************************************************************/
import java.util.List;

public class Calculator {

    public double calculateWeight(double tf, double idf){
        return (1+Math.log(tf)) * idf;
    }

    public double tfCalculator(double termFreq, double totalTerms) {
        return termFreq / totalTerms;
    }

    // Calculates idf of term termToCheck
    // allDocuments : total document count
    //documentContainsTerm : count of the total document which contains the term
    // returns idf(inverse document frequency) score
    
    public double idfCalculator(double allDocuments, int documentTimes) {
        return Math.log(allDocuments / documentTimes);
    }
}
