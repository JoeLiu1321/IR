import java.util.Comparator;

public class Score{
    private String docId;
    private double score;
    public Score(String docId,double score){
        this.docId=docId;
        this.score=score;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
