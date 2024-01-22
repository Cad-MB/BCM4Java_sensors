package ast.gather;

public class RGather extends Gather {
    String sensorId;
    Gather gather;

    public RGather(String sensorId, Gather gather) {
        this.sensorId = sensorId;
        this.gather = gather;
    }
}
