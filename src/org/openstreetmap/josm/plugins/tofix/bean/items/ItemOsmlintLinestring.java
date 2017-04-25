package org.openstreetmap.josm.plugins.tofix.bean.items;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;

/**
 *
 * @author samely
 */
public class ItemOsmlintLinestring extends AbstractItemOsmlint {

    List<Node> bound = new LinkedList<>();

    public List<List<Node>> get_nodes() {
        String geostring = getCoordinates();
        List<List<Node>> list = new LinkedList<>();
        geostring = geostring.replace("[[", "").replace("]]", "");
        String[] array = geostring.replace("[", "").split("],");
        List<Node> l = new LinkedList<>();

        for (int i = 0; i < array.length; i++) {
            LatLon latLon = new LatLon(Double.parseDouble(array[i].split(",")[1]), Double.parseDouble(array[i].split(",")[0]));
            Node node = new Node(latLon);
            l.add(node);
        }
        bound.addAll(l);
        boundSize(bound);
        list.add(l);

        return list;
    }
}
