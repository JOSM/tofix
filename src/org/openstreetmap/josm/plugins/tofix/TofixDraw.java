package org.openstreetmap.josm.plugins.tofix;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.geojson.DataSetBuilder.BoundedDataSet;

/**
 *
 * @author ruben
 */
public class TofixDraw {

    public static void draw(final TofixNewLayer tofixNewLayer, BoundedDataSet data) {
        if (data == null) {
            JOptionPane.showMessageDialog(Main.parent, tr("Can not print the layer"));
            return;
        }
        BoundingXYVisitor v = new BoundingXYVisitor();
        v.visit(data.getBounds());

        MainApplication.getMap().mapView.zoomTo(v);
        if (!MainApplication.getLayerManager().containsLayer(tofixNewLayer)) {
            MainApplication.getLayerManager().addLayer(tofixNewLayer);
        }
        tofixNewLayer.setBoundedDataSet(data);
    }
}
