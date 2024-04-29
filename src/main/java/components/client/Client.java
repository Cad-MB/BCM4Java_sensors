package components.client;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import logger.CustomTraceWindow;
import parsers.ClientParser;
import parsers.TestParser;
import sensor_network.PortName;

import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * This class represents a client component in the sensor network system.
 * It communicates with the registry to discover nodes and sends queries to them periodically.
 * The client component is responsible for gathering data from the sensor nodes.
 */
@OfferedInterfaces(offered={ RequestResultCI.class })
@RequiredInterfaces(required={ ClientCI.class, LookupCI.class, ClocksServerCI.class })
public class Client
    extends AbstractComponent {

    protected static int nth = 0;
    protected final ClientPlugin clientPlugin;

    /**
     * Constructs a new client component.
     * Initializes the ports for node and registry communication and toggles logging and tracing.
     *
     * @throws Exception if an error occurs during initialization
     */
    protected Client(
        ClientParser.Client clientData,
        Map<PortName, String> inboundPortUris,
        Map<PortName, String> outboundPortUris,
        List<TestParser.Test> tests
    ) throws Exception {
        super(8, 8);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        CustomTraceWindow tracerWindow = new CustomTraceWindow(
            "Client",
            0, 0,
            screenSize.width / 2, screenSize.height / 5,
            nth, 4
        );
        tracerWindow.setBackgroundColor(Color.decode("#ef5350"));
        tracerWindow.setForegroundColor(Color.WHITE);
        setTracer(tracerWindow);

        this.toggleLogging();
        this.toggleTracing();
        this.logMessage(clientData.id);
        nth++;

        this.clientPlugin = new ClientPlugin(clientData, inboundPortUris, outboundPortUris, tests);
        this.clientPlugin.setPluginURI(clientData.pluginUri);
        this.installPlugin(clientPlugin);
    }

    /**
     * Executes the client component.
     * Periodically sends gather queries to sensor nodes and handles the results.
     *
     * @throws Exception if an error occurs during execution
     */
    @Override
    public void execute() throws Exception {
        super.execute();
        this.clientPlugin.run();
    }


    /**
     * Finalizes the client component.
     * Disconnects from ports and performs necessary cleanups.
     *
     * @throws Exception if an error occurs during finalization
     */
    @Override
    public synchronized void finalise() throws Exception {
        super.finalise();
    }

    /**
     * Shuts down the client component.
     * Unpublishes ports and shuts down gracefully.
     *
     * @throws ComponentShutdownException if an error occurs during shutdown
     */
    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        super.shutdown();
    }

}
