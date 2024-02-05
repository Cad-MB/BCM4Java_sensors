package executionState;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;

import java.util.ArrayList;
import java.util.Set;

public class ExecutionState implements ExecutionStateI {
    ProcessingNodeI currentNode;
    ArrayList<QueryResultI> results;

    boolean isDirectional;

    double maxDistance;

    int nbHops;

    Set<Direction> directions;

    public ExecutionState(ProcessingNodeI currentNode, ArrayList<QueryResultI> results,
                          boolean isDirectional, double maxDistance, int nbHops, Set<Direction> directions) {
        this.currentNode = currentNode;
        this.results = results;
        this.isDirectional = isDirectional;
        this.maxDistance = maxDistance;
        this.nbHops = nbHops;
        this.directions = directions;
    }

    @Override
    public ProcessingNodeI getProcessingNode() {
        return currentNode;
    }

    @Override
    public void updateProcessingNode(ProcessingNodeI pn) {
        currentNode = pn;
    }

    @Override
    public QueryResultI getCurrentResult() {
        return results.get(0);
    }

    @Override
    public void addToCurrentResult(QueryResultI result) {
        results.add(result);
    }

    @Override
    public boolean isDirectional() {
        return isDirectional;
    }

    public void setDirectional(boolean isDirectional) {
        this.isDirectional = isDirectional;
    }

    @Override
    public Set<Direction> getDirections() {
        return directions;
    }

    public void setDirections(Set<Direction> directions) {
        this.directions = directions;
    }

    @Override
    public boolean noMoreHops() {
        return nbHops == 0;
    }

    @Override
    public void incrementHops() {
        nbHops--;
    }

    @Override
    public boolean isFlooding() {
        return !isDirectional;
    }

    @Override
    public boolean withinMaximalDistance(PositionI p) {
        return currentNode.getPosition().distance(p) < maxDistance;
    }

    public void setFlooding(boolean flooding) {
        isDirectional = !flooding;
    }

    public void setMaxDistance(double md) {
        maxDistance = md;
    }
}
