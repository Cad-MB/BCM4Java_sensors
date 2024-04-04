package sensor_network.requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents the state of execution for a query in a sensor network.
 * It contains information about the current processing node, query results, directionality of the query,
 * maximum distance for query propagation, number of hops remaining, set of directions, and executed nodes.
 * <p>
 * This class implements the {@link fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI}
 * interface.
 */
public class ExecutionState
    implements ExecutionStateI {

    protected ProcessingNodeI currentNode;
    protected ArrayList<QueryResultI> results;
    protected boolean isDirectional;
    protected double maxDistance;
    protected int nbHops;
    protected Set<Direction> directions;
    protected boolean hasContinuation;
    protected Set<String> executedNodes;
    protected PositionI entryPoint;

    /**
     * Constructs an {@code ExecutionState} object with the given processing node.
     *
     * @param processingNode the current processing node
     */
    public ExecutionState(ProcessingNodeI processingNode) {
        this.currentNode = processingNode;
        this.directions = new HashSet<>();
        this.nbHops = 0;
        this.maxDistance = 0;
        this.isDirectional = false;
        this.results = new ArrayList<>();
        executedNodes = new HashSet<>();
    }

    @Override
    public synchronized ProcessingNodeI getProcessingNode() {
        return currentNode;
    }

    @Override
    public synchronized void updateProcessingNode(ProcessingNodeI pn) {
        executedNodes.add(currentNode.getNodeIdentifier());
        currentNode = pn;
    }

    @Override
    public synchronized QueryResultI getCurrentResult() {
        return results.get(0);
    }

    @Override
    public synchronized void addToCurrentResult(QueryResultI result) {
        results.add(result);
    }

    @Override
    public synchronized boolean isContinuationSet() {
        return hasContinuation;
    }

    @Override
    public boolean isDirectional() {
        return isDirectional;
    }

    /**
     * Sets whether the query is directional or not.
     */
    public synchronized void setDirectionalState(int nbHops, Set<Direction> directions) {
        this.directions = directions;
        this.nbHops = nbHops;
        this.isDirectional = true;
        this.hasContinuation = nbHops > 0;
    }

    public synchronized void setFloodingState(PositionI pos, double distance) {
        this.entryPoint = pos;
        this.maxDistance = distance;
    }

    @Override
    public synchronized Set<Direction> getDirections() {
        return directions;
    }

    public ExecutionState withDirection(Direction direction) {
        ExecutionState newState = new ExecutionState(this.currentNode);
        newState.directions = new HashSet<>();
        newState.directions.add(direction);
        return newState;
    }

    @Override
    public synchronized boolean noMoreHops() {
        hasContinuation = nbHops > 0;
        return nbHops == 0;
    }

    @Override
    public synchronized void incrementHops() {
        nbHops--;
        if (nbHops == 0) hasContinuation = false;
    }

    @Override
    public synchronized boolean isFlooding() {
        return !isDirectional;
    }

    @Override
    public boolean withinMaximalDistance(PositionI p) {
        return entryPoint.distance(p) < maxDistance;
    }

    /**
     * Checks if a node with the given ID has already been executed.
     *
     * @param nodeId the ID of the node to check
     * @return {@code true} if the node has already been executed, {@code false} otherwise
     */
    public synchronized boolean isNodeNotDone(String nodeId) {
        return !executedNodes.contains(nodeId);
    }


    public synchronized void setEntryPoint(PositionI entryPoint) {
        if (this.entryPoint == null) {
            this.entryPoint = entryPoint;
        }
    }

}
