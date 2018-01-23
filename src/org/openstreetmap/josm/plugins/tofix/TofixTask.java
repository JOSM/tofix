package org.openstreetmap.josm.plugins.tofix;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.GeoJsonObject;
import org.geojson.GeometryCollection;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.io.UploadDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.geojson.DataSetBuilder;
import org.openstreetmap.josm.plugins.geojson.DataSetBuilder.BoundedDataSet;
import org.openstreetmap.josm.plugins.tofix.bean.AccessToProject;
import org.openstreetmap.josm.plugins.tofix.bean.items.Item;
import org.openstreetmap.josm.plugins.tofix.controller.ItemController;
import org.openstreetmap.josm.plugins.tofix.util.Util;
import org.openstreetmap.josm.tools.Logging;

/**
 *
 * @author ruben
 */
public class TofixTask {

    ItemController itemController = new ItemController();
    Bounds bounds = null;
    Bounds bounds_default = null;
    DataSetBuilder dataSetBuilder = new DataSetBuilder();
    MapView mv = null;
    TofixNewLayer tofixLayer = new TofixNewLayer("To-fix-layer", null);

    public AccessToProject work(Item item, AccessToProject accessToTask, double size) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final GeoJsonObject object = mapper.readValue(item.getFeatureCollection().toString(), GeoJsonObject.class);
            GeoJsonObject geoJsonObject = new GeometryCollection();
            final BoundedDataSet data = new DataSetBuilder().build(object);
            checkTofixLayer();
            TofixDraw.draw(tofixLayer, data);
        } catch (final Exception e) {
            Logging.error("Error while reading json file!");
            Logging.error(e);
        }
        UploadDialog.getUploadDialog().getChangeset().getCommentsCount();
        return accessToTask;
    }

    public final void checkTofixLayer() {
        if (!MainApplication.getLayerManager().containsLayer(tofixLayer)) {
            MainApplication.getLayerManager().addLayer(tofixLayer);
        }
    }
}
