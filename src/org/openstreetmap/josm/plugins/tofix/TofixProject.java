package org.openstreetmap.josm.plugins.tofix;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.io.UploadDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.plugins.geojson.GeoJsonLayer;
import org.openstreetmap.josm.plugins.geojson.GeoJsonReader;
import org.openstreetmap.josm.plugins.tofix.bean.AccessToProject;
import org.openstreetmap.josm.plugins.tofix.bean.ItemBean;
import org.openstreetmap.josm.plugins.tofix.util.Download;
import org.openstreetmap.josm.tools.Logging;

/**
 *
 * @author ruben
 */
public class TofixProject {

    TofixNewLayer tofixLayer = new TofixNewLayer(tr("Tofix:<Layer>"));

    public AccessToProject work(ItemBean item, AccessToProject accessToTask, double downloadSize, boolean isCheckedDownloadOSMData, boolean isCheckedEditableData) {
        try (InputStream in = new ByteArrayInputStream(
        		item.getFeatureCollection().toString().getBytes(StandardCharsets.UTF_8))) {
            final DataSet data = GeoJsonReader.parseDataSet(in, NullProgressMonitor.INSTANCE);
            final Bounds bounds = new Bounds();
            data.getDataSourceBounds().forEach(bounds::extend);
            //set layer  name
            if (isCheckedEditableData) {
                final Layer layer = new GeoJsonLayer(tr("Tofix:editable") + accessToTask.getProject_name()+"-" + item.getId(), data, bounds);
                layer.setBackgroundLayer(true);
                MainApplication.getLayerManager().addLayer(layer);
            } else {
                checkTofixLayer();
                tofixLayer.setName(tr("Tofix:" + accessToTask.getProject_name()));
                TofixDraw.draw(tofixLayer, data);
            }
            if (isCheckedDownloadOSMData) {
                Download.download(bounds, 0L, downloadSize);
            }
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
