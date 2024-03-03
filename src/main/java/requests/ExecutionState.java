package requests;

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

    ProcessingNodeI currentNode;
    ArrayList<QueryResultI> results;
    boolean isDirectional;
    double maxDistance;
    int nbHops;
    Set<Direction> directions;
    boolean hasContinuation;
    Set<String> executedNodes;

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
        return hasContinuation;
    }

    @Override
    public boolean isDirectional() {
        return isDirectional;
    }

    /**
     * Sets whether the query is directional or not.
     *
     * @param isDirectional {@code true} if the query is directional, {@code false} otherwise
     */
    public void setDirectional(boolean isDirectional) {
        this.isDirectional = isDirectional;
    }

    @Override
    public Set<Direction> getDirections() {
        return directions;
    }

    /**
     * Sets the directions for the query.
     *
     * @param directions the set of directions for the query
     */
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
        if (nbHops == 0) hasContinuation = false;
    }

    /**
     * Sets the number of hops for the query.
     *
     * @param n the number of hops for the query
     */
    public void setNbHops(int n) {
        nbHops = n;
        hasContinuation = n > 0;
    }

    @Override
    public boolean isFlooding() {
        return !isDirectional;
    }

    /**
     * Sets whether the query is using flooding or not.
     *
     * @param flooding {@code true} if the query is using flooding, {@code false} otherwise
     */
    public void setFlooding(boolean flooding) {
        isDirectional = !flooding;
    }

    @Override
    public boolean withinMaximalDistance(PositionI p) {
        return currentNode.getPosition().distance(p) < maxDistance;
    }

    /**
     * Sets the maximum distance for the query propagation.
     *
     * @param md the maximum distance for the query propagation
     */
    public void setMaxDistance(double md) {
        maxDistance = md;
        if (md > 0) hasContinuation = true;
    }

    /**
     * Checks if a node with the given ID has already been executed.
     *
     * @param nodeId the ID of the node to check
     * @return {@code true} if the node has already been executed, {@code false} otherwise
     */
    public boolean isNodeAlreadyDone(String nodeId) {
        return executedNodes.contains(nodeId);
    }
}
