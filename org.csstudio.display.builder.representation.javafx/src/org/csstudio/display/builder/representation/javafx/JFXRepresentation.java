/*******************************************************************************
 * Copyright (c) 2015-2016 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.builder.representation.javafx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.properties.WidgetColor;
import org.csstudio.display.builder.model.widgets.ActionButtonWidget;
import org.csstudio.display.builder.model.widgets.ArcWidget;
import org.csstudio.display.builder.model.widgets.ArrayWidget;
import org.csstudio.display.builder.model.widgets.BoolButtonWidget;
import org.csstudio.display.builder.model.widgets.ByteMonitorWidget;
import org.csstudio.display.builder.model.widgets.CheckBoxWidget;
import org.csstudio.display.builder.model.widgets.ComboWidget;
import org.csstudio.display.builder.model.widgets.EllipseWidget;
import org.csstudio.display.builder.model.widgets.EmbeddedDisplayWidget;
import org.csstudio.display.builder.model.widgets.GroupWidget;
import org.csstudio.display.builder.model.widgets.LEDWidget;
import org.csstudio.display.builder.model.widgets.LabelWidget;
import org.csstudio.display.builder.model.widgets.MultiStateLEDWidget;
import org.csstudio.display.builder.model.widgets.PictureWidget;
import org.csstudio.display.builder.model.widgets.PolygonWidget;
import org.csstudio.display.builder.model.widgets.PolylineWidget;
import org.csstudio.display.builder.model.widgets.ProgressBarWidget;
import org.csstudio.display.builder.model.widgets.RadioWidget;
import org.csstudio.display.builder.model.widgets.RectangleWidget;
import org.csstudio.display.builder.model.widgets.ScaledSliderWidget;
import org.csstudio.display.builder.model.widgets.SpinnerWidget;
import org.csstudio.display.builder.model.widgets.TableWidget;
import org.csstudio.display.builder.model.widgets.TabsWidget;
import org.csstudio.display.builder.model.widgets.TextEntryWidget;
import org.csstudio.display.builder.model.widgets.TextUpdateWidget;
import org.csstudio.display.builder.model.widgets.ThermometerWidget;
import org.csstudio.display.builder.model.widgets.WebBrowserWidget;
import org.csstudio.display.builder.model.widgets.plots.ImageWidget;
import org.csstudio.display.builder.model.widgets.plots.XYPlotWidget;
import org.csstudio.display.builder.representation.ToolkitRepresentation;
import org.csstudio.display.builder.representation.WidgetRepresentation;
import org.csstudio.display.builder.representation.WidgetRepresentationFactory;
import org.csstudio.display.builder.representation.javafx.widgets.ActionButtonRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ArcRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ArrayRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.BoolButtonRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ByteMonitorRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.CheckBoxRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ComboRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.EllipseRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.EmbeddedDisplayRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.GroupRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.JFXBaseRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.LEDRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.LabelRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.MultiStateLEDRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.PictureRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.PolygonRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.PolylineRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ProgressBarRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.RadioRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.RectangleRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ScaledSliderRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.SpinnerRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TableRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TabsRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TextEntryRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.TextUpdateRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.ThermometerRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.WebBrowserRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.plots.ImageRepresentation;
import org.csstudio.display.builder.representation.javafx.widgets.plots.XYPlotRepresentation;
import org.csstudio.javafx.DialogHelper;
import org.csstudio.javafx.Styles;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

/** Represent model items in JavaFX toolkit
 *
 *  <p>The parent of each widget is either a {@link Group} or
 *  a {@link Pane}.
 *  Common ancestor of both is {@link Parent}, but note that other
 *  parent types (Region, ..) are not permitted.
 *
 *  <p>General scene layout:
 *  <pre>
 *  model_root
 *   |
 *  scroll_body
 *   |
 *  model_parent
 *  </pre>
 *
 *  <p>model_parent:
 *  This is where the model items get represented.
 *  Its scaling factors are used to zoom.
 *
 *  <p>scroll_body:
 *  Needed for scroll pane to use visual bounds, i.e. be aware of zoom.
 *  Otherwise scroll bars would enable/disable based on layout bounds,
 *  regardless of zoom.
 *  ( https://pixelduke.wordpress.com/2012/09/16/zooming-inside-a-scrollpane )
 *
 *  <p>model_root:
 *  Scroll pane for viewing a subsection of larger display.
 *  This is also the 'root' of the model-related scene.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class JFXRepresentation extends ToolkitRepresentation<Parent, Node>
{
    public static final String ACTIVE_MODEL = "_active_model";

    /** Zoom to fit display */
    public static final double ZOOM_ALL = -1.0;

    /** Zoom to fit display's width */
    public static final double ZOOM_WIDTH = -3.0;

    /** Zoom to fit display's height */
    public static final double ZOOM_HEIGHT = -2.0;

    /** Constructor
     *  @param edit_mode Edit mode?
     */
    public JFXRepresentation(final boolean edit_mode)
    {
        super(edit_mode);
    }

    @Override
    protected void initialize()
    {
        final Map<String, WidgetRepresentationFactory<Parent, Node>> factories = new HashMap<>();
        registerKnownRepresentations(factories);
        final IExtensionRegistry registry = RegistryFactory.getRegistry();
        if (registry != null)
        {   // Load available representations from registry,
            // which allows other plugins to contribute new widgets.
            for (IConfigurationElement config : registry.getConfigurationElementsFor(WidgetRepresentation.EXTENSION_POINT))
            {
                final String type = config.getAttribute("type");
                final String clazz = config.getAttribute("class");
                logger.log(Level.CONFIG, "{0} contributes {1}", new Object[] { config.getContributor().getName(), clazz });
                factories.put(type, createFactory(config));
            }
        }
        for (Map.Entry<String, WidgetRepresentationFactory<Parent, Node>> entry : factories.entrySet())
            register(entry.getKey(), entry.getValue());
    }

    /** Add known representations as fallback in absence of registry information */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void registerKnownRepresentations(Map<String, WidgetRepresentationFactory<Parent, Node>> factories)
    {
        factories.put(ActionButtonWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new ActionButtonRepresentation());
        factories.put(ArcWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new ArcRepresentation());
        factories.put(ArrayWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation) new ArrayRepresentation());
        factories.put(BoolButtonWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new BoolButtonRepresentation());
        factories.put(ByteMonitorWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new ByteMonitorRepresentation());
        factories.put(CheckBoxWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new CheckBoxRepresentation());
        factories.put(ComboWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new ComboRepresentation());
        factories.put(EmbeddedDisplayWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new EmbeddedDisplayRepresentation());
        factories.put(EllipseWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new EllipseRepresentation());
        factories.put(GroupWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new GroupRepresentation());
        factories.put(ImageWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new ImageRepresentation());
        factories.put(LabelWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new LabelRepresentation());
        factories.put(LEDWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new LEDRepresentation());
        factories.put(MultiStateLEDWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new MultiStateLEDRepresentation());
        factories.put(PictureWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new PictureRepresentation());
        factories.put(PolygonWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new PolygonRepresentation());
        factories.put(PolylineWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new PolylineRepresentation());
        factories.put(ProgressBarWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new ProgressBarRepresentation());
        factories.put(RadioWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation) new RadioRepresentation());
        factories.put(RectangleWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new RectangleRepresentation());
        factories.put(ScaledSliderWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new ScaledSliderRepresentation());
        factories.put(SpinnerWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new SpinnerRepresentation());
        factories.put(TableWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new TableRepresentation());
        factories.put(TabsWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new TabsRepresentation());
        factories.put(TextEntryWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new TextEntryRepresentation());
        factories.put(TextUpdateWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new TextUpdateRepresentation());
        factories.put(ThermometerWidget.WIDGET_DESCRIPTOR.getType(),
                () -> (WidgetRepresentation) new ThermometerRepresentation());
        factories.put(WebBrowserWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new WebBrowserRepresentation());
        factories.put(XYPlotWidget.WIDGET_DESCRIPTOR.getType(), () -> (WidgetRepresentation)new XYPlotRepresentation());
    }

    @SuppressWarnings("unchecked")
    private WidgetRepresentationFactory<Parent, Node> createFactory(final IConfigurationElement config)
    {
        return () -> (WidgetRepresentation<Parent, Node, Widget>) config.createExecutableExtension("class");
    }

    private volatile ScrollPane model_root;
    private volatile Group model_parent;

    /** Create scrollpane etc. for hosting the model
     *  @return ScrollPane
     *  @throws IllegalStateException if had already been called
     */
    final public ScrollPane createModelRoot()
    {
        if (model_root != null)
            throw new IllegalStateException("Already created model root");
        model_parent = new Group();
        final Pane scroll_body = new Pane(model_parent);
        model_root = new ScrollPane(scroll_body);
        return model_root;
    }

    /** @see JFXRepresentation#createScene(DisplayModel)
     *  @param scene Scene created for model
     *  @return Root element
     */
    final public Parent getModelParent()
    {
        return model_parent;
    }

    /** @param scene Scene where style sheet for display builder is added */
    public static void setSceneStyle(final Scene scene)
    {
        // Fetch css relative to JFXRepresentation, not derived class
        final String css = JFXRepresentation.class.getResource("opibuilder.css").toExternalForm();
        scene.getStylesheets().add(css);

        Styles.setSceneStyle(scene);
    }


    /** Set zoom level
     *  @param zoom Zoom level: 1.0 for 100%, 0.5 for 50%, ZOOM_ALL, ZOOM_WIDTH, ZOOM_HEIGHT
     *  @return Zoom level actually used
     */
    final public double setZoom(double zoom)
    {
        if (zoom <= 0.0)
        {   // Determine zoom to fit outline of display into available space
            final Bounds available = model_root.getLayoutBounds();
            final Bounds outline = model_parent.getLayoutBounds();
            final double zoom_x = outline.getWidth()  > 0 ? available.getWidth()  / outline.getWidth() : 1.0;
            final double zoom_y = outline.getHeight() > 0 ? available.getHeight() / outline.getHeight() : 1.0;

            if (zoom == ZOOM_WIDTH)
                zoom = zoom_x;
            else if (zoom == ZOOM_HEIGHT)
                zoom = zoom_y;
            else // Assume ZOOM_ALL
                zoom = Math.min(zoom_x, zoom_y);
        }

        model_parent.setScaleX(zoom);
        model_parent.setScaleY(zoom);

        return zoom;
    }

    /** Obtain the 'children' of a Toolkit widget parent
     *  @param parent Parent that's either Group or Pane
     *  @return Children
     */
    public static ObservableList<Node> getChildren(final Parent parent)
    {
        if (parent instanceof Group)
            return ((Group)parent).getChildren();
        else if (parent instanceof Pane)
            return ((Pane)parent).getChildren();
        throw new IllegalArgumentException("Expecting Group or Pane, got " + parent);
    }

    @Override
    public ToolkitRepresentation<Parent, Node> openNewWindow(final DisplayModel model, Consumer<DisplayModel> close_handler) throws Exception
    {   // Use JFXStageRepresentation or RCP-based implementation
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public void setBackground(final WidgetColor color)
    {
        model_root.setStyle("-fx-background: " + JFXUtil.webRGB(color));
    }

    @Override
    public void representModel(final Parent root, final DisplayModel model) throws Exception
    {
        root.getProperties().put(ACTIVE_MODEL, model);
        super.representModel(root, model);
    }

    @Override
    public Parent disposeRepresentation(final DisplayModel model)
    {
        final Parent root = super.disposeRepresentation(model);
        root.getProperties().remove(ACTIVE_MODEL);
        return root;
    }

    @Override
    public void execute(final Runnable command)
    {   // If already on app thread, execute right away
        if (Platform.isFxApplicationThread())
            command.run();
        else
            Platform.runLater(command);
    }

    @Override
    public void showMessageDialog(final Widget widget, final String message)
    {
        final Node node = JFXBaseRepresentation.getJFXNode(widget);
        final CountDownLatch done = new CountDownLatch(1);
        execute( ()->
        {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION);
            DialogHelper.positionDialog(alert, node, -100, -50);
            alert.setResizable(true);
            alert.setTitle("Message");
            // "header text" allows for larger content than the "content text"
            alert.setContentText(null);
            alert.setHeaderText(message);
            alert.showAndWait();
            done.countDown();
        });
        try
        {
            done.await();
        }
        catch (InterruptedException ex)
        {
            // Ignore
        }
    }

    @Override
    public void showErrorDialog(final Widget widget, final String error)
    {
        final Node node = JFXBaseRepresentation.getJFXNode(widget);
        final CountDownLatch done = new CountDownLatch(1);
        execute( ()->
        {
            final Alert alert = new Alert(Alert.AlertType.WARNING);
            DialogHelper.positionDialog(alert, node, -100, -50);
            alert.setResizable(true);
            alert.setTitle("Error");
            alert.setHeaderText(error);
            alert.showAndWait();
            done.countDown();
        });
        try
        {
            done.await();
        }
        catch (InterruptedException ex)
        {
            // Ignore
        }
    }

    @Override
    public boolean showConfirmationDialog(final Widget widget, final String question)
    {
        final Node node = JFXBaseRepresentation.getJFXNode(widget);
        final CompletableFuture<Boolean> done = new CompletableFuture<>();
        execute( ()->
        {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            DialogHelper.positionDialog(alert, node, -100, -50);
            alert.setResizable(true);
            alert.setTitle("Please Confirm");
            alert.setHeaderText(question);
            // Setting "Yes", "No" buttons
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(ButtonType.YES);
            alert.getButtonTypes().add(ButtonType.NO);
            final Optional<ButtonType> result = alert.showAndWait();
            // NOTE that button type OK/YES/APPLY checked in here must match!
            done.complete(result.isPresent()  &&  result.get() == ButtonType.YES);
        });
        try
        {
            return done.get();
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Confirmation dialog ('" + question + "') failed", ex);
        }
        return false;
    }

    @Override
    public String showSelectionDialog(final Widget widget, final String title, final List<String> options)
    {
        final Node node = JFXBaseRepresentation.getJFXNode(widget);
        final CompletableFuture<String> done = new CompletableFuture<>();
        execute( ()->
        {
            final ChoiceDialog<String> dialog = new ChoiceDialog<>(null, options);
            DialogHelper.positionDialog(dialog, node, -100, -50);

            dialog.setHeaderText(title);
            final Optional<String> result = dialog.showAndWait();
            done.complete(result.orElse(null));
        });
        try
        {
            return done.get();
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Selection dialog ('" + title + ", ..') failed", ex);
        }
        return null;
    }

    @Override
    public String showPasswordDialog(final Widget widget, final String title, final String correct_password)
    {
        final Node node = JFXBaseRepresentation.getJFXNode(widget);
        final CompletableFuture<String> done = new CompletableFuture<>();
        execute( ()->
        {
            final PasswordDialog dialog = new PasswordDialog(title, correct_password);
            DialogHelper.positionDialog(dialog, node, -100, -50);
            final Optional<String> result = dialog.showAndWait();
            done.complete(result.orElse(null));
        });
        try
        {
            return done.get();
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Password dialog ('" + title + ", ..') failed", ex);
        }
        return null;
    }

    @Override
    public String showSaveAsDialog(final Widget widget, final String initial_value)
    {
        // This is implemented in the RCP_JFXRepresentation, using the workspace.
        // Could provide a file-system based dialog here for use without RCP/workspace.
        logger.log(Level.WARNING, "showSaveAsDialog('" + initial_value + "') is not implemented");
        return null;
    }

    // Future for controlling the audio player
    private class AudioFuture implements Future<Boolean>
    {
        private volatile MediaPlayer player;

        AudioFuture(final MediaPlayer player)
        {
            this.player = player;
            // Player by default just stays in "PLAYING" state
            player.setOnEndOfMedia(() -> player.stop());
            player.play();
            logger.log(Level.INFO, "Playing " + this);
        }

        @Override
        public boolean isDone()
        {
            switch (player.getStatus())
            {
            case UNKNOWN:
            case READY:
            case PLAYING:
            case PAUSED:
            case STALLED:
                return false;
            case HALTED:
            case STOPPED:
            case DISPOSED:
            default:
                return true;
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            logger.log(Level.INFO, "Stopping " + this);
            final boolean stopped = !isDone();

            // TODO On Linux, playback of WAV doesn't work. Just stays in PLAYING state.
            // Worse: player.stop() as well as player.dispose() hang
            execute(() ->
            {
                player.stop();
            });

            return stopped;
        }

        @Override
        public boolean isCancelled()
        {
            return player.getStatus() == Status.STOPPED;
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException
        {
            while (! isDone())
            {
                logger.log(Level.FINE, "Awaiting end " + this);
                Thread.sleep(100);
            }
            return !isCancelled();
        }

        @Override
        public Boolean get(final long timeout, final TimeUnit unit)
                throws InterruptedException, ExecutionException,
                TimeoutException
        {
            final long end = System.currentTimeMillis() + unit.toMillis(timeout);
            while (! isDone())
            {
                logger.log(Level.FINE, "Awaiting end " + this);
                Thread.sleep(100);
                if (System.currentTimeMillis() >= end)
                    throw new TimeoutException("Timeout for " + this);
            }
            return !isCancelled();
        }

        @Override
        protected void finalize() throws Throwable
        {
            logger.log(Level.INFO, "Disposing " + this);
            player.dispose();
            player = null;
        }

        @Override
        public String toString()
        {
            final MediaPlayer copy = player;
            if (copy == null)
                return "Disposed autio player";
            return "Audio player for " + player.getMedia().getSource() + " (" + player.getStatus() + ")";
        }
    }

    @Override
    public Future<Boolean> playAudio(final String url)
    {
        final CompletableFuture<AudioFuture> result = new CompletableFuture<>();
        // Create on UI thread
        execute(() ->
        {
            try
            {
                final Media sound = new Media(url);
                final MediaPlayer player = new MediaPlayer(sound);
                result.complete(new AudioFuture(player));
            }
            catch (Exception ex)
            {
                result.completeExceptionally(ex);
            }
        });

        try
        {
            return result.get();
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Audio playback error for " + url, ex);
        }
        return CompletableFuture.completedFuture(false);
    }
}
