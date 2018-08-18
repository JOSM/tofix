package org.openstreetmap.josm.plugins.tofix.util;

import static org.openstreetmap.josm.gui.mappaint.mapcss.ExpressionFactory.Functions.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.tofix.TofixDialog;

/**
 *
 * @author ruben
 */
public class Download {

    public static void download(Bounds bounds, final Long osm_obj_id, double downloadSize) {
        DownloadOsmTask task = new DownloadOsmTask();
        ProgressMonitor monitor = null;
        //Fix bbox to download
        if (bounds.getArea() == 0) {
            bounds = new Bounds(new BBox(bounds.getCenter().getX(), bounds.getCenter().getY(), downloadSize).toRectangle());
        } else if (bounds.getArea() < 10) {
            bounds.extend(bounds.getMax().lat() + 0.0001, bounds.getMax().lon() + 0.0001);
            bounds.extend(bounds.getMin().lat() - 0.0001, bounds.getMin().lon() - 0.0001);
        } else if (bounds.getArea() > 10) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("This big area cannot be downloaded!"), tr("Warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        final Future<?> future = task.download(true, bounds, monitor);
        Runnable runAfterTask;

        if (osm_obj_id != null) {
            runAfterTask = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (osm_obj_id != 0) {
                            future.get();
                            //create object
                            if (future.isDone()) {

                                DataSet dataset = MainApplication.getLayerManager().getEditLayer().data;
                                Node node = new Node(osm_obj_id);
                                Relation relation = new Relation(osm_obj_id);
                                Way way = new Way(osm_obj_id);

                                //create list of objects
                                List<OsmPrimitive> selection = new ArrayList<>();

                                if (dataset.allPrimitives().contains(node)) {
                                    selection.add(node);
                                    dataset.setSelected(selection);

                                } else if (dataset.allPrimitives().contains(way)) {
                                    selection.add(way);
                                    dataset.setSelected(selection);

                                } else if (dataset.allPrimitives().contains(relation)) {
                                    selection.add(relation);
                                    dataset.setSelected(selection);
                                }
                                if (!selection.isEmpty()) {
                                    AutoScaleAction.autoScale("selection");
                                }
                            }
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(TofixDialog.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            MainApplication.worker.submit(runAfterTask);
        }

    }
}
