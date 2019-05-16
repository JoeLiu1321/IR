public class Score {
    private String topic;
    private Double value;
    public Score(String topic,Double value){
        setTopic(topic);
        setValue(value);
    }
    public Score(){

    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getTopic()).append(":").append(getValue());
        return builder.toString();
    }
}
