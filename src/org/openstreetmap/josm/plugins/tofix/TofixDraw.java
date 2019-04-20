package org.openstreetmap.josm.plugins.tofix;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;

/**
 *
 * @author ruben
 */
public class TofixDraw {

    public static void draw(final TofixNewLayer tofixNewLayer, DataSet data) {
        if (data == null) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Can not print the layer"));
            return;
        }
        BoundingXYVisitor v = new BoundingXYVisitor();
        data.getDataSourceBounds().forEach(v::visit);

        MainApplication.getMap().mapView.zoomTo(v);
        if (!MainApplication.getLayerManager().containsLayer(tofixNewLayer)) {
            MainApplication.getLayerManager().addLayer(tofixNewLayer);
        }
        tofixNewLayer.setDataSet(data);
    }
}
