package logger;

import fr.sorbonne_u.components.helpers.TracerI;
import fr.sorbonne_u.exceptions.PreconditionException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Instant;

public class CustomTraceWindow
    extends WindowAdapter
    implements TracerI {
    protected static final String WINDOW_TITLE_PREFIX = "TraceWindow";
    private static boolean decorated;

    /**
     * Width of the screen accessible to the Java AWT toolkit.
     */
    protected int screenWidth;
    /**
     * Height of the screen accessible to the Java AWT toolkit.
     */
    protected int screenHeight;
    /**
     * Frame that will display the tracer on the screen.
     */
    protected JFrame frame;
    /**
     * Title to be displayed by the tracer frame.
     */
    protected String title;
    /**
     * X coordinate of the top left point of the application tracers.
     */
    protected int xOrigin;
    /**
     * Y coordinate of the top left point of the application tracers.
     */
    protected int yOrigin;
    /**
     * Width of the frame in screen coordinates.
     */
    protected int frameWidth;
    /**
     * Height of the frame in screen coordinates.
     */
    protected int frameHeight;
    /**
     * X position of the frame among the application tracers.
     */
    protected int xRelativePos;
    /**
     * Y position of the frame among the application tracers.
     */
    protected int yRelativePos;
    /**
     * True if traces must be output and false otherwise.
     */
    protected boolean tracingStatus;
    /**
     * True if the trace is suspended and false otherwise.
     */
    protected boolean suspendStatus;

    private JPanel panel;
    private Font font;
    private Color backgroundColor;
    private Color foregroundColor;

    // region constructors
    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * create a tracer with default parameters.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	{@code true}	// no precondition.
     * post	{@code !this.isTracing()}
     * post	{@code !this.isSuspended()}
     * </pre>
     */
    public CustomTraceWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenWidth = screenSize.width;
        this.screenHeight = screenSize.height;

        this.title = WINDOW_TITLE_PREFIX;
        this.xOrigin = 0;
        this.yOrigin = 0;
        this.frameWidth = screenSize.width / 4;
        this.frameHeight = screenSize.height / 5;

        // Given that in distributed execution, the global registry uses
        // 0 in standard, put this frame to its right.
        this.xRelativePos = 1;
        this.yRelativePos = 0;

        this.tracingStatus = false;
        this.suspendStatus = false;
    }

    /**
     * create a tracer with the given parameters.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	{@code xRelativePos >= 0}
     * pre	{@code yRelativePos >= 0}
     * post	{@code !this.isTracing()}
     * post	{@code !this.isSuspended()}
     * </pre>
     *
     * @param title        title to put on the frame.
     * @param xRelativePos x position of the frame in the group of frames.
     * @param yRelativePos x position of the frame in the group of frames.
     */
    public CustomTraceWindow(
        String title,
        int xRelativePos,
        int yRelativePos
    ) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenWidth = screenSize.width;
        this.screenHeight = screenSize.height;

        assert xRelativePos >= 0 :
            new PreconditionException(
                "TracerWindow called with "
                + "negative position: x = " + xRelativePos + "!");
        assert yRelativePos >= 0 :
            new PreconditionException(
                "TracerWindow#setRelativePosition called with "
                + "negative position: y = " + yRelativePos + "!");

        this.title = WINDOW_TITLE_PREFIX + ":" + title;
        this.xOrigin = 0;
        this.yOrigin = 0;
        this.frameWidth = screenSize.width / 4;
        this.frameHeight = screenSize.height / 5;
        this.xRelativePos = xRelativePos;
        this.yRelativePos = yRelativePos;

        this.tracingStatus = false;
        this.suspendStatus = false;
    }

    /**
     * create a tracer with the given parameters.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	{@code xOrigin >= 0}
     * pre	{@code xOrigin < this.getScreenWidth()}
     * pre	{@code yOrigin >= 0}
     * pre	{@code yOrigin < this.getScreenHeight()}
     * pre	{@code frameWidth > 0}
     * pre	{@code frameHeight > 0}
     * pre	{@code xRelativePos >= 0}
     * pre	{@code yRelativePos >= 0}
     * post	{@code !this.isTracing()}
     * post	{@code !this.isSuspended()}
     * </pre>
     *
     * @param title        title to put on the frame.
     * @param xOrigin      x origin in screen unit.
     * @param yOrigin      y origin in screen unit.
     * @param frameWidth   width of the tracer frame.
     * @param frameHeight  height of the tracer frame.
     * @param xRelativePos x position of the frame in the group of frames.
     * @param yRelativePos x position of the frame in the group of frames.
     */
    public CustomTraceWindow(
        String title,
        int xOrigin,
        int yOrigin,
        int frameWidth,
        int frameHeight,
        int xRelativePos,
        int yRelativePos
    ) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.screenWidth = screenSize.width;
        this.screenHeight = screenSize.height;

        assert xOrigin >= 0 :
            new PreconditionException(
                "TracerWindow called with negative x origin: "
                + xOrigin + "!");
        assert xOrigin < this.getScreenWidth() :
            new PreconditionException(
                "TracerWindow called with x origin "
                + "outside the screen: " + xOrigin + "!");
        assert yOrigin >= 0 :
            new PreconditionException(
                "TracerWindow called with negative "
                + "y origin: " + yOrigin + "!");
        assert yOrigin < this.screenHeight :
            new PreconditionException(
                "TracerWindow called with y origin "
                + "outside the screen: " + yOrigin + "!");
        assert frameWidth > 0 :
            new PreconditionException(
                "TracerWindow called with non positive frame "
                + "width: " + frameWidth + "!");
        assert frameHeight > 0 :
            new PreconditionException(
                "TracerWindow called with non positive frame "
                + "height: " + frameHeight + "!");
        assert xRelativePos >= 0 :
            new PreconditionException(
                "TracerWindow called with "
                + "negative position: x = " + xRelativePos + "!");
        assert yRelativePos >= 0 :
            new PreconditionException(
                "TracerWindow#setRelativePosition called with "
                + "negative position: y = " + yRelativePos + "!");

        this.title = "Tracer:" + title;
        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.xRelativePos = xRelativePos;
        this.yRelativePos = yRelativePos;

        this.tracingStatus = false;
        this.suspendStatus = false;
    }
    // endregion

    /**
     * initialise the trace window.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	{@code true}	// no precondition.
     * post	{@code true}	// no postcondition.
     * </pre>
     */
    protected synchronized void initialise() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            font = new Font("Consolas", Font.BOLD, 20);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            font = new Font("Droid Sans", Font.BOLD, 15);
        } else {
            font = new Font("SF Pro", Font.BOLD, 20);
        }

        if (this.backgroundColor == null) {
            this.backgroundColor = Color.WHITE;
        }
        decorated = true;
        this.frame = new JFrame(this.title);
        this.frame.setBounds(
            this.xOrigin + this.xRelativePos * this.frameWidth,
            this.yOrigin + (this.yRelativePos * this.frameHeight) + 25,
            this.frameWidth,
            this.frameHeight);

        setDecorated(false);

        this.frame.setUndecorated(!decorated);
        this.panel = new JPanel();
        this.panel.setBackground(this.backgroundColor);

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 15));

        this.frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.frame.addWindowListener(this);
        this.frame.setVisible(true);
    }

    // -------------------------------------------------------------------------
    // TracerWindow specific methods
    // -------------------------------------------------------------------------

    /**
     * @see TracerI#getScreenWidth()
     */
    @Override
    public int getScreenWidth() {
        return this.screenWidth;
    }

    /**
     * @see TracerI#getScreenHeight()
     */
    @Override
    public int getScreenHeight() {
        return this.screenHeight;
    }

    /**
     * @see TracerI#setTitle(String)
     */
    @Override
    public void setTitle(String title) {
        this.title = WINDOW_TITLE_PREFIX + ":" + title;
    }

    /**
     * @see TracerI#setOrigin(int, int)
     */
    @Override
    public void setOrigin(int xOrigin, int yOrigin) {
        assert xOrigin >= 0 :
            new PreconditionException(
                "TracerWindow#setOrigin called with negative "
                + "x origin: " + xOrigin + "!");
        assert xOrigin < this.getScreenWidth() :
            new PreconditionException(
                "TracerWindow#setOrigin called with x origin "
                + "outside the screen: " + xOrigin + "!");
        assert yOrigin >= 0 :
            new PreconditionException(
                "TracerWindow#setOrigin called with negative "
                + "y origin: " + yOrigin + "!");
        assert yOrigin < this.screenHeight :
            new PreconditionException(
                "TracerWindow#setOrigin called with y origin "
                + "outside the screen: " + yOrigin + "!");

        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
    }

    /**
     * @see TracerI#setRelativePosition(int, int)
     */
    @Override
    public void setRelativePosition(int x, int y) {
        assert x >= 0 : new PreconditionException(
            "TracerWindow#setRelativePosition called with "
            + "negative position: x = " + x + "!");
        assert y >= 0 : new PreconditionException(
            "TracerWindow#setRelativePosition called with "
            + "negative position: y = " + y + "!");

        this.xRelativePos = x;
        this.yRelativePos = y;
    }

    /**
     * @see TracerI#isVisible()
     */
    @Override
    public boolean isVisible() {
        return this.frame.isVisible();
    }

    /**
     * @see TracerI#toggleVisible()
     */
    @Override
    public synchronized void toggleVisible() {
        assert this.isTracing();
        this.frame.setVisible(!this.frame.isVisible());
    }

    /**
     * close the window.
     *
     * <p><strong>Contract</strong></p>
     *
     * <pre>
     * pre	{@code true}	// no more preconditions.
     * post	{@code true}	// no more postconditions.
     * </pre>
     *
     * @see WindowAdapter#windowClosing(WindowEvent)
     */
    @Override
    public synchronized void windowClosing(WindowEvent evt) {
        if (this.frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }
    }


    public boolean isDecorated() {
        return decorated;
    }

    public static void setDecorated(boolean d) {
        decorated = d;
    }

    // -------------------------------------------------------------------------
    // Tracer methods
    // -------------------------------------------------------------------------

    /**
     * @see TracerI#toggleTracing()
     */
    @Override
    public synchronized void toggleTracing() {
        this.tracingStatus = !this.tracingStatus;
        if (this.tracingStatus) {
            this.initialise();
            this.suspendStatus = false;
        } else {
            this.frame.setVisible(false);
            this.frame.dispose();
            this.frame = null;
            this.suspendStatus = true;
        }
    }

    /**
     * @see TracerI#toggleSuspend()
     */
    @Override
    public synchronized void toggleSuspend() {
        assert this.isTracing() :
            new PreconditionException(
                "TracerWindow#toggleSuspend called but tracing "
                + "is not activated!");

        this.suspendStatus = !this.suspendStatus;
    }

    /**
     * @see TracerI#isTracing()
     */
    @Override
    public boolean isTracing() {
        return this.tracingStatus;
    }

    /**
     * @see TracerI#isSuspended()
     */
    @Override
    public boolean isSuspended() {
        return this.suspendStatus;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    int messageCounter = 0;

    /**
     * @see TracerI#traceMessage(String)
     */
    @Override
    public synchronized void traceMessage(String message) {
        if (this.tracingStatus && !this.suspendStatus) {
            int sepI = message.indexOf("|");
            Component label;
            if (sepI != -1) {
                label = getPanel(message, sepI);
            } else {
                label = new JLabel(message);
                ((JLabel) label).setOpaque(true);
            }

            int brightness = colorBrightness(this.backgroundColor); // 0-255
            if (messageCounter % 2 == 0) {
                label.setBackground(this.backgroundColor);
            } else {
                if (brightness > 255 / 2) {
                    label.setBackground(this.backgroundColor.darker());
                } else {
                    label.setBackground(this.backgroundColor.brighter());
                }
            }
            label.setFont(this.font);
            label.setForeground(this.foregroundColor);
            label.setMinimumSize(new Dimension(Integer.MAX_VALUE, 50));
            this.panel.add(label);
            this.frame.validate();
            this.frame.repaint();
            messageCounter++;
        }

    }

    public static int colorBrightness(Color color) {
        final double cr = 0.241;
        final double cg = 0.691;
        final double cb = 0.068;

        double r, g, b;
        r = color.getRed();
        g = color.getGreen();
        b = color.getBlue();

        double result = Math.sqrt(cr * r * r + cg * g * g + cb * b * b);

        return (int) result;
    }

    private JPanel getPanel(String message, int separatorIndex) {
        JPanel p = new JPanel();

        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEFT);
        layout.setHgap(10);
        layout.minimumLayoutSize(p);
        p.setLayout(layout);
        String firstPart = message.substring(0, separatorIndex);
        String secondPart = message.substring(separatorIndex + 1);

        Instant instant = Instant.ofEpochMilli(Long.parseLong(firstPart));

        JLabel firstLabel = new JLabel(" " + instant + " ");
        JLabel secondLabel = new JLabel(secondPart);

        firstLabel.setBackground(Color.DARK_GRAY);
        firstLabel.setForeground(Color.WHITE);
        firstLabel.setOpaque(true);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        firstLabel.setFont(this.font);
        secondLabel.setFont(this.font);
        secondLabel.setForeground(this.foregroundColor);
        p.add(firstLabel);
        p.add(secondLabel);
        return p;
    }

}
