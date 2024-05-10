package components.node;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import logger.CustomTraceWindow;
import parsers.NodeParser;
import sensor_network.PortName;

import java.awt.*;
import java.util.Map;


/**
 * This class represents a sensor node in the sensor network system.
 * It communicates with the registry for registration and with other nodes for peer-to-peer communication.
 * The node processes queries and executes them either locally or forwards them to neighboring nodes.
 */
public class Node
    extends AbstractComponent {

    protected static int nth = 0;
    protected final NodePlugin nodePlugin;

    /**
     * Constructs a new Node component object.
     *
     * @param nodeData         the data of the node parsed from the config file
     * @param inboundPortUris  the URIs of inbound ports
     * @param outboundPortUris the URIs of outbound ports
     * @throws Exception if an error occurs during construction
     */
    protected Node(
        NodeParser.Node nodeData,
        Map<PortName, String> inboundPortUris,
        Map<PortName, String> outboundPortUris
    ) throws Exception {
        super(nodeData.threads.nbThreads, nodeData.threads.nbScheduleThreads);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        CustomTraceWindow tracerWindow = new CustomTraceWindow(
            nodeData.id, 0, 0,
            screenSize.width / 3, screenSize.height / 5,
            nth % 3, (nth / 3) % 3
        );
        setTracer(tracerWindow);
        this.toggleLogging();
        this.toggleTracing();
        this.logMessage(nodeData.id);
        nth++;

        this.nodePlugin = new NodePlugin(nodeData, inboundPortUris, outboundPortUris);
        this.nodePlugin.setPluginURI(nodeData.pluginUri);
        this.installPlugin(this.nodePlugin);
    }


    /**
     * Executes the sensor node.
     * Registers with the registry and establishes connections with neighboring nodes.
     *
     * @throws Exception if an error occurs during execution
     */
    @Override
    public void execute() throws Exception {
        super.execute();
        this.nodePlugin.run();
    }


    /**
     * Shuts down the sensor node.
     *
     * @throws ComponentShutdownException if an error occurs during shutdown
     */
    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        super.shutdown();
    }

    /**
     * Finalizes the sensor node.
     *
     * @throws Exception if an error occurs during finalization
     */
    @Override
    public synchronized void finalise() throws Exception {
        super.finalise();
    }

}
