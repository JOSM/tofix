package org.openstreetmap.josm.plugins.tofix;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.osm.visitor.paint.MapRendererFactory;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.geojson.DataSetBuilder.BoundedDataSet;
import org.openstreetmap.josm.tools.ImageProvider;

public class TofixNewLayer extends Layer implements ActionListener {

    BoundedDataSet boundedDataSet;

//    DataSet dataset;
    float width;

    public TofixNewLayer(String name) {
        super(name);
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("icontofix_layer");
    }

    @Override
    public String getToolTipText() {
        return tr("Layer to draw OSM error");
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    public void setBoundedDataSet(BoundedDataSet boundedDataSet) {
        this.boundedDataSet = boundedDataSet;
        invalidate();
    }

    @Override
    public void paint(Graphics2D g, final MapView mv, Bounds bounds) {
        DataSet dataset = boundedDataSet.getDataSet();
        Stroke stroke = g.getStroke();
        if (dataset == null) {
            return;
        }
        if (MapRendererFactory.getInstance().isWireframeMapRendererActive()) {
            width = 1f;
        } else {
            width = 3f;
        }
        //Print the objetcs
        g.setColor(new Color(254, 30, 123));
        g.setStroke(new BasicStroke((float) width));
        for (OsmPrimitive primitive : dataset.allPrimitives()) {
            if (primitive instanceof Way) {
                Way way = (Way) primitive;
                List<Node> nodes = way.getNodes();
                if (nodes.size() < 2) {
                    return;
                }
                for (int i = 0; i <= way.getNodes().size() - 2; i++) {
                    Node node1 = way.getNode(i);
                    Node node2 = way.getNode(i + 1);
                    Point pnt1 = mv.getPoint(node1.getCoor());
                    Point pnt2 = mv.getPoint(node2.getCoor());
                    g.draw(new Line2D.Double(pnt1.x, pnt1.y, pnt2.x, pnt2.y));
                }
            } else if (primitive instanceof Node) {
                Node node = (Node) primitive;
                if (node.getParentWays().isEmpty()) {
                    Point pnt = mv.getPoint(node.getCoor());
                    g.drawOval(pnt.x - 20, pnt.y - 20, 40, 40);
                }
            }
        }
        g.setStroke(stroke);
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        // nothing to do here
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[]{
            LayerListDialog.getInstance().createShowHideLayerAction(),
            SeparatorLayerAction.INSTANCE,
            SeparatorLayerAction.INSTANCE,
            new LayerListPopup.InfoAction(this)};
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showConfirmDialog(null, e.getSource());
    }

    @Override
    public void mergeFrom(Layer layer) {

    }
}
