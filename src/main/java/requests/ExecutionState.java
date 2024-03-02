package requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ExecutionState
    implements ExecutionStateI {

    ProcessingNodeI currentNode;
    ArrayList<QueryResultI> results;
    boolean isDirectional;
    double maxDistance;
    int nbHops;
    Set<Direction> directions;
    Set<String> executedNodes;

    public ExecutionState(ProcessingNodeI currentNodeInfo) {
        this.currentNode = currentNodeInfo;
        this.directions = new HashSet<>();
        this.nbHops = 0;
        this.maxDistance = 0;
        this.isDirectional = false;
        this.results = new ArrayList<>();
        executedNodes = new HashSet<>();
    }

    @Override
    public ProcessingNodeI getProcessingNode() {
        return currentNode;
    }

    @Override
    public void updateProcessingNode(ProcessingNodeI pn) {
        executedNodes.add(currentNode.getNodeIdentifier());
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
    public boolean isContinuationSet() {
        return false;
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

    public void setNbHops(int n) {
        nbHops = n;
    }

    @Override
    public boolean isFlooding() {
        return !isDirectional;
    }

    public void setFlooding(boolean flooding) {
        isDirectional = !flooding;
    }

    @Override
    public boolean withinMaximalDistance(PositionI p) {
        return currentNode.getPosition().distance(p) < maxDistance;
    }

    public void setMaxDistance(double md) {
        maxDistance = md;
    }

    public boolean isNodeAlreadyDone(String nodeId) {
        return executedNodes.contains(nodeId);
    }
}
