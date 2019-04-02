import java.util.Iterator;
import java.util.Map;

/*******************************************************************************
 * ************************     ADNAN OQUAISH     ******************************
 * *************************     BITS Pilani     *******************************
 *******************************************************************************/

public class CosineSimilarity 
{

  // Method to calculate cosine similarity between two documents.
  // docVector1 : document vector 1 (a)
  // docVector2 : document vector 2 (b)
	
  public double cosineSimilarity(Iterator<Map.Entry<String,Double>> docVector1 , Iterator<Map.Entry<String,Double>> docVector2){
      double dotProduct = 0.0;
      double magnitude1 = 0.0;
      double magnitude2 = 0.0;
      double cosineSimilarity = 0.0;

      while (docVector1.hasNext()){
          double value1=docVector1.next().getValue().doubleValue();
          double value2=docVector2.next().getValue().doubleValue();
          value1=(Double.isInfinite(value1) ? 0.0 : value1);
          value2=(Double.isInfinite(value2) ? 0.0 : value2);
          dotProduct += value1 * value2;  //a.b
          magnitude1 += Math.pow(value1, 2);  //(a^2)
          magnitude2 += Math.pow(value2, 2); //(b^2)
      }

      magnitude1 = Math.sqrt(magnitude1);//sqrt(a^2)
      magnitude2 = Math.sqrt(magnitude2);//sqrt(b^2)

      if (magnitude1 != 0.0 | magnitude2 != 0.0) {
          cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
      } 
      else {
          cosineSimilarity=0.0;
      }
      return cosineSimilarity;
  }
}
