package sensor_network.requests;

import fr.sorbonne_u.cps.sensor_network.interfaces.Direction;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;

import java.util.Collections;
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
    protected QueryResultI currentResult;
    protected boolean isDirectional;
    protected double maxDistance;
    protected int nbHops;
    protected Set<Direction> directions;
    protected boolean isContSet;
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
        this.isContSet = false;
    }

    @Override
    public synchronized ProcessingNodeI getProcessingNode() {
        return currentNode;
    }

    @Override
    public synchronized void updateProcessingNode(ProcessingNodeI pn) {
        this.currentNode = pn;
    }

    @Override
    public synchronized QueryResultI getCurrentResult() {
        return currentResult;
    }

    @Override
    public synchronized void addToCurrentResult(QueryResultI result) {
        if (result == null) return;
        if (this.currentResult == null) {
            this.currentResult = result;
        } else if (result.isBooleanRequest()) {
            this.currentResult.positiveSensorNodes().addAll(result.positiveSensorNodes());
        } else {
            this.currentResult.gatheredSensorsValues().addAll(result.gatheredSensorsValues());
        }
    }

    @Override
    public synchronized boolean isContinuationSet() {
        return isContSet;
    }

    @Override
    public boolean isDirectional() {
        return isDirectional;
    }

    public synchronized void setDirectionalState(int nbHops, Set<Direction> directions) {
        this.directions = directions;
        this.nbHops = nbHops;
        this.isDirectional = true;
        this.isContSet = true;
    }

    public synchronized void setFloodingState(PositionI pos, double distance) {
        this.entryPoint = pos;
        this.maxDistance = distance;
        this.isDirectional = false;
        this.isContSet = true;
    }

    @Override
    public synchronized Set<Direction> getDirections() {
        return directions;
    }

    @Override
    public synchronized boolean noMoreHops() {
        return nbHops == 0;
    }

    @Override
    public synchronized void incrementHops() {
        nbHops--;
    }

    @Override
    public synchronized boolean isFlooding() {
        return !isDirectional;
    }

    @Override
    public boolean withinMaximalDistance(PositionI p) {
        return entryPoint.distance(p) < maxDistance;
    }

    public ExecutionState copyWithDirection(Direction direction) {
        ExecutionState newState = new ExecutionState(this.currentNode);
        newState.setDirectionalState(this.nbHops, Collections.singleton(direction));
        newState.currentResult = this.currentResult;
        return newState;
    }

    public ExecutionState copy() {
        ExecutionState newState = new ExecutionState(this.currentNode);
        if (this.isDirectional) {
            newState.setDirectionalState(this.nbHops, this.directions);
        } else {
            newState.setFloodingState(this.entryPoint, this.maxDistance);
        }
        return newState;
    }
}
