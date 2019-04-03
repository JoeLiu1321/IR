import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;

public class CosineSimilarity {
     DecimalFormat df;
    // Method to calculate cosine similarity between two documents.
    // docVector1 : document vector 1 (a)
    // docVector2 : document vector 2 (b)
      public CosineSimilarity(){
          this.df=new DecimalFormat("#.##");
      }

      public CosineSimilarity(DecimalFormat df){
            this.df=df;
      }

      private double reduceCompute(double value){
          if(Double.isInfinite(value) | Double.isNaN(value))
              return 0.0;
          else{
              return Double.parseDouble(df.format(value));
          }
      }

      public double cosineSimilarity(Iterator<Map.Entry<String,Double>> docVector1 , Iterator<Map.Entry<String,Double>> docVector2){
            double dotProduct = 0.0;
            double magnitude1 = 0.0;
            double magnitude2 = 0.0;
            DecimalFormat df = new DecimalFormat("##.00");
            while (docVector1.hasNext()){
                  double value1=docVector1.next().getValue().doubleValue();
                  double value2=docVector2.next().getValue().doubleValue();
                  value1=this.reduceCompute(value1);
                  value2=this.reduceCompute(value2);
                  dotProduct += value1 * value2;  //a.b
                  magnitude1 += Math.pow(value1, 2);  //(a^2)
                  magnitude2 += Math.pow(value2, 2); //(b^2)
            }
            magnitude1 = Math.sqrt(magnitude1);//sqrt(a^2)
            magnitude2 = Math.sqrt(magnitude2);//sqrt(b^2)

            double cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
            return this.reduceCompute(cosineSimilarity);
      }

}
