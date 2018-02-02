package org.openstreetmap.josm.plugins.tofix;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.openstreetmap.josm.Main;

import org.openstreetmap.josm.actions.UploadAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.io.UploadDialog;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import static org.openstreetmap.josm.gui.mappaint.mapcss.ExpressionFactory.Functions.tr;
import org.openstreetmap.josm.plugins.tofix.bean.AccessToProject;
import org.openstreetmap.josm.plugins.tofix.bean.ListProjectBean;
import org.openstreetmap.josm.plugins.tofix.bean.ProjectBean;
import org.openstreetmap.josm.plugins.tofix.bean.ItemBean;
import org.openstreetmap.josm.plugins.tofix.controller.ItemController;
import org.openstreetmap.josm.plugins.tofix.controller.ItemTrackController;
import org.openstreetmap.josm.plugins.tofix.controller.ListProjectsController;
import org.openstreetmap.josm.plugins.tofix.util.Config;
import org.openstreetmap.josm.plugins.tofix.util.Status;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.OpenBrowser;
import org.openstreetmap.josm.tools.Shortcut;

/**
 *
 * @author ruben
 */
public final class TofixDialog extends ToggleDialog implements ActionListener {

    MapView mv = MainApplication.getMap().mapView;
    //PANELS
    ItemTrackController itemTrackController = new ItemTrackController();
    JTabbedPane TabbedPanel = new javax.swing.JTabbedPane();
    JPanel jContentPanelProjects = new JPanel(new GridLayout(2, 1));
    JPanel jContenActivation = new JPanel(new GridLayout(6, 1));
    JPanel jPanelProjects = new JPanel(new GridLayout(1, 1));
    JPanel jPanelQuery = new JPanel(new GridLayout(2, 1));

    private JDOAuth oauth;
    private JDHost host;

    //OBJECTS FOR EVNETS
    private final JLabel JlabelTitleProject;
    private final SideButton skipButton;
    private final SideButton fixedButton;
    private final SideButton noterrorButton;
    private final JTextField bboxJtextField;
    private final Shortcut skipShortcut;
    private final Shortcut fixedShortcut;
    private final Shortcut noterrorButtonShortcut;
    private final JComboBox<String> jcomboBox;
    private final JCheckBox jCheckBoxToken;
    private final JCheckBox jCheckBoxDeleteLayer;
    private final JCheckBox jCheckBoxSetNewAPI;
    private final JCheckBox jCheckBoxDownloadOSMData;
    private final JCheckBox jCheckBoxSetDataEditable;
    private final JCheckBox jCheckBoxSetBbox;

    //VARS
    double zise = 0.0006; //size to download,per default
    boolean validator;
    private boolean needDeleteLayer;
    private boolean isCheckedDownloadOSMData = true;
    private boolean isCheckedEditableData = false;
    ArrayList<String> listStringsForCombo = new ArrayList<>();

    // LOCAL
    AccessToProject mainAccessToProject = null;
    ProjectBean project = new ProjectBean();
    ListProjectsController listProjectController = new ListProjectsController();
    ListProjectBean projectsList = null;
    ItemBean item = new ItemBean();
    ItemController itemController = new ItemController();
    TofixProject tofixProject = new TofixProject();

    public TofixDialog() {
        super(tr("To-fix"), "icontofix", tr("Open to-fix window."),
                Shortcut.registerShortcut("Tool:To-fix", tr("Toggle: {0}", tr("Tool:To-fix")),
                        KeyEvent.VK_T, Shortcut.ALT_CTRL_SHIFT), 170);

        //jDOAuth = new JDOAuth(Main.parent);
//==============================================================================SETUP LINK TO THE PROJECT        
        JlabelTitleProject = new javax.swing.JLabel();
        JlabelTitleProject.setText(tr("<html><a href=\"\">List of projects</a></html>"));
        JlabelTitleProject.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JlabelTitleProject.addMouseListener(
                new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e
            ) {
                OpenBrowser.displayUrl(Config.URL_TOFIX);
            }
        });
        jContentPanelProjects.add(JlabelTitleProject);

//==============================================================================FILL COMBO
        listStringsForCombo.add(tr("Select a project ..."));
        jcomboBox = new JComboBox<>(listStringsForCombo.toArray(new String[]{}));
        jcomboBox.addActionListener(this);
        jPanelProjects.add(jcomboBox);
        jContentPanelProjects.add(jPanelProjects);
//==============================================================================CONFIG API URL
        jContenActivation.add(new Label(tr("Select the checkbox to:")));

        jCheckBoxSetNewAPI = new JCheckBox(tr("Set default API"));
        jCheckBoxSetNewAPI.setSelected(false);
        jCheckBoxSetNewAPI.addActionListener((ActionEvent e) -> {
            if (jCheckBoxSetNewAPI.isSelected()) {
                host.setVisible(true);
                if(host.getHost()!=null && host.getHost().equals("")){
                    fillCombo();
                }                
            }else{
                Config.preferences(Config.REMOVE, new String[]{"tofix-server.host"},Config.getPluginPreferencesFile().getAbsolutePath());
            }
        });
        jCheckBoxSetNewAPI.setBorderPainted(true);
        jContenActivation.add(jCheckBoxSetNewAPI);

//==============================================================================TOKEN
        jCheckBoxToken = new JCheckBox(tr("Set up My token"));
        jCheckBoxToken.setSelected(false);
        jCheckBoxToken.addActionListener((ActionEvent e) -> {
            if (jCheckBoxToken.isSelected()) {
                SetToken();
            }else{
                Config.preferences(Config.REMOVE, new String[]{"tofix-server.token"},Config.getPluginPreferencesFile().getAbsolutePath());
            }
        });
        jCheckBoxToken.setBorderPainted(true);
        jContenActivation.add(jCheckBoxToken);
//==============================================================================AUTO DELETE LAYER
        jCheckBoxDeleteLayer = new JCheckBox(tr("Auto delete layer"));
        jCheckBoxDeleteLayer.setSelected(true);
        needDeleteLayer = jCheckBoxDeleteLayer.isSelected();
        jCheckBoxDeleteLayer.addItemListener((ItemEvent e) -> {
            needDeleteLayer = e.getStateChange() == ItemEvent.SELECTED;
        });
        jCheckBoxDeleteLayer.setBorderPainted(true);
        jContenActivation.add(jCheckBoxDeleteLayer);
//==============================================================================DOWNLOAD OSM DATA
        jCheckBoxDownloadOSMData = new JCheckBox(tr("Download OSM Data"));
        jCheckBoxDownloadOSMData.setSelected(true);
        jCheckBoxDownloadOSMData.addItemListener((ItemEvent e) -> {
            isCheckedDownloadOSMData = e.getStateChange() == ItemEvent.SELECTED;
        });
        jCheckBoxDownloadOSMData.setBorderPainted(true);
        jContenActivation.add(jCheckBoxDownloadOSMData);
//==============================================================================SET EDITABLE DATA
        jCheckBoxSetDataEditable = new JCheckBox(tr("Set editable layer"));
        jCheckBoxSetDataEditable.setSelected(false);
        jCheckBoxSetDataEditable.addItemListener((ItemEvent e) -> {
            isCheckedEditableData = e.getStateChange() == ItemEvent.SELECTED;
            if (isCheckedEditableData) {
                jCheckBoxDeleteLayer.setSelected(false);
            }
        });
        jCheckBoxSetDataEditable.setBorderPainted(true);
        jContenActivation.add(jCheckBoxSetDataEditable);
//============================================================================== Select bbox button+jtextfield
        jCheckBoxSetBbox = new JCheckBox(tr("Set BBox to request the items"));
        jCheckBoxSetBbox.setSelected(false);
        bboxJtextField = new JTextField();
        jCheckBoxSetBbox.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == 1) {
                Bounds bounds = mv.getRealBounds();
                String bbox = String.valueOf(bounds.getMinLon()) + "," + String.valueOf(bounds.getMinLat()) + "," + String.valueOf(bounds.getMaxLon()) + "," + String.valueOf(bounds.getMaxLat());
                bboxJtextField.setText(bbox);
                Config.setBBOX(bbox);
            } else {
                bboxJtextField.setText("");
                Config.setBBOX("none");
            }
        });
        jPanelQuery.add(jCheckBoxSetBbox);
        jPanelQuery.add(bboxJtextField);

//=============================================================================="Skip" button
        skipButton = new SideButton(new AbstractAction() {
            {
                putValue(NAME, tr("Skip"));
                new ImageProvider("mapmode", "skip").getResource().attachImageIcon(this, true);
                putValue(SHORT_DESCRIPTION, tr("Skip Error"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                skip();
                deleteLayer();

            }
        });
        skipButton.setEnabled(false);
        skipShortcut = Shortcut.registerShortcut("tofix:skip", tr("tofix:Skip item"),
                KeyEvent.VK_S, Shortcut.ALT_SHIFT);
        MainApplication.registerActionShortcut(new skipKeyAction(), skipShortcut);
//=============================================================================="Fixed" button
        fixedButton = new SideButton(new AbstractAction() {
            {
                putValue(NAME, tr("Fixed"));
                new ImageProvider("mapmode", "fixed").getResource().attachImageIcon(this, true);
                putValue(SHORT_DESCRIPTION, tr("Fixed Error"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                eventFixed(e);

            }
        });
        fixedButton.setEnabled(false);
        fixedShortcut = Shortcut.registerShortcut("tofix:fixed", tr("tofix:Fixed item"),
                KeyEvent.VK_F, Shortcut.ALT_SHIFT);
        MainApplication.registerActionShortcut(new fixedKeyAction(), fixedShortcut);
//=============================================================================="Not a error" button
        noterrorButton = new SideButton(new AbstractAction() {
            {
                putValue(NAME, tr("Not an error"));
                new ImageProvider("mapmode", "noterror").getResource().attachImageIcon(this, true);
                putValue(SHORT_DESCRIPTION, tr("Not an error"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                notError();
                deleteLayer();

            }
        });
        noterrorButton.setEnabled(false);
        noterrorButtonShortcut = Shortcut.registerShortcut("tofix:noterror", tr("tofix:Not a Error item"),
                KeyEvent.VK_N, Shortcut.ALT_SHIFT);
        MainApplication.registerActionShortcut(new NotError_key_Action(), noterrorButtonShortcut);
//============================================================================== FILL PANELS
        //PANEL TASKS
        jPanelProjects.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelQuery.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        TabbedPanel.addTab(tr("Projects"), jContentPanelProjects);
        TabbedPanel.addTab(tr("Activation"), jContenActivation);
        TabbedPanel.addTab(tr("Querying"), jPanelQuery);
        //add panel in JOSM
        createLayout(TabbedPanel, false, Arrays.asList(new SideButton[]{
            skipButton, noterrorButton, fixedButton
        }));

        oauth = new JDOAuth(Main.parent);
        host=new JDHost(Main.parent);
        loadOAuthInfo();
        

    }
//==============================================================================OBJECT EVENTS==============================================================================

    public void fillCombo() {
        listStringsForCombo.clear();
        listStringsForCombo.add(tr("Select a project ..."));
        if (Status.isInternetReachable()) {
            if (Status.serverStatus()) {
                projectsList = listProjectController.getListProjects();
                for (int i = 0; i < projectsList.getProjects().size(); i++) {
                    listStringsForCombo.add(projectsList.getProjects().get(i).getName());
                }
                jcomboBox.setModel(new DefaultComboBoxModel<>());
                jcomboBox.setModel(new DefaultComboBoxModel<>(listStringsForCombo.toArray(new String[]{})));

            } else {
                jcomboBox.setModel(new DefaultComboBoxModel<>());
                jcomboBox.setModel(new DefaultComboBoxModel<>(listStringsForCombo.toArray(new String[]{})));
                JOptionPane.showMessageDialog(Main.parent, tr("API did not response! ") + Config.getHOST());
            }
        } else {
            skipButton.setEnabled(false);
            fixedButton.setEnabled(false);
            noterrorButton.setEnabled(false);
        }
    }

    public void SetToken() {
        if (Status.serverStatus()) {
            oauth.setVisible(true);
            if (oauth.getTofixToken() != null && !oauth.getTofixToken().equals("")) {                
                if (Config.preferences(Config.GET, new String[]{"tofix-server.token"},Config.getPluginPreferencesFile().getAbsolutePath()) != null) {
                    Config.preferences(Config.UPDATE, new String[]{"tofix-server.token", oauth.getTofixToken()},Config.getPluginPreferencesFile().getAbsolutePath());
                } else {
                    Config.preferences(Config.ADD, new String[]{"tofix-server.token", oauth.getTofixToken()},Config.getPluginPreferencesFile().getAbsolutePath());
                }
                fillCombo();
            } else {
                fillCombo();
            }
        } else {
            JOptionPane.showMessageDialog(Main.parent, tr("API did not response! ") + Config.getHOST());
        }

    }

// Event select a project, it automatic will get a item and display
    @Override
    public void actionPerformed(ActionEvent e) {
        start();
        JComboBox<?> cb = (JComboBox<?>) e.getSource();
        if (cb.getSelectedIndex() != 0) {
            mainAccessToProject.setProject_id(projectsList.getProjects().get(cb.getSelectedIndex() - 1).getId());
            mainAccessToProject.setProject_name(projectsList.getProjects().get(cb.getSelectedIndex() - 1).getName());
            project = projectsList.getProjects().get(cb.getSelectedIndex() - 1);
            deleteLayer();
            getNewItem();
            skipButton.setEnabled(true);
            fixedButton.setEnabled(true);
            noterrorButton.setEnabled(true);
        } else {
            skipButton.setEnabled(false);
            fixedButton.setEnabled(false);
            noterrorButton.setEnabled(false);
        }
    }

    private void loadOAuthInfo() {
        oauth.setUserInfo(Config.getUserName(), Config.getPassword(), Config.getTOKEN());
        if (oauth.getTofixToken() != null && !oauth.getTofixToken().equals("")) {
            jCheckBoxToken.setSelected(true);
            fillCombo();
        }
        host.setHost(Config.getHOST());
        if(host.getHost()!=null && !host.getHost().equals("")){
            jCheckBoxSetNewAPI.setSelected(true);
        }
    }

    public class skipKeyAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            skip();
            deleteLayer();
        }
    }

    public class fixedKeyAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            eventFixed(e);
        }
    }

    public class NotError_key_Action extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            notError();
            deleteLayer();
        }
    }

    private void eventFixed(ActionEvent e) {

        if (!MainApplication.getLayerManager().getActiveLayer().isSavable() || !MainApplication.getLayerManager().getEditDataSet().isModified()) {
            new Notification(tr("No change to upload!")).show();
            //Be sure you mark as fixed
            fixed();
        } else if (new Bounds(MainApplication.getLayerManager().getEditDataSet().getDataSourceArea().getBounds()).getArea() < 30) {
            validator = false;
            UploadDialog.getUploadDialog().addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    validator = true;
                }
            });
            DataSet data = MainApplication.getLayerManager().getEditLayer().data;
            project.getChangesetComment();
            //Set up the changeset comment and the source
            if (!project.getChangesetComment().equals("none")) {
                data.getChangeSetTags().put("comment", project.getChangesetComment());
            }
            data.getChangeSetTags().put("source", MainApplication.getMap().mapView.getLayerInformationForSourceTag());
            new UploadAction().actionPerformed(e);

            if (validator && !UploadDialog.getUploadDialog().isCanceled() && UploadDialog.getUploadDialog().getChangeset().isNew()) {
                fixed();
                deleteLayer();
            }
        } else {
            new Notification(tr("The bounding box of the edited layer is too big.")).show();
        }
    }

//==============================================================================FUNCTIONS==============================================================================
    private void getNewItem() {
        itemController.setAccessToProject(mainAccessToProject);
        item = itemController.getItem();
        switch (item.getStatusServer()) {
            case 200:
                mainAccessToProject.setAccess(true); //This atribute to access to  the actions
                mainAccessToProject = tofixProject.work(item, mainAccessToProject, zise, isCheckedDownloadOSMData, isCheckedEditableData);
                edit();
                break;
            case 410:
                JOptionPane.showMessageDialog(Main.parent, tr("There are no more items on this Project or Area!"), tr("Warning"), JOptionPane.WARNING_MESSAGE);
                mainAccessToProject.setAccess(false);
                break;
            default:
                mainAccessToProject.setAccess(false);
                new Notification(tr("Something went wrong, try again")).show();
        }
    }

//Actions
    public void edit() {
        itemTrackController.lockItem(item, "locked");
    }

    public void skip() {
        itemTrackController.lockItem(item, "unlocked");
        getNewItem();
    }

    public void fixed() {
        itemTrackController.updateStatusItem(item, "fixed");
        getNewItem();
    }

    public void notError() {
        itemTrackController.updateStatusItem(item, "noterror");
        getNewItem();
    }

    public final void start() {
        mainAccessToProject = new AccessToProject("mixedlayer", false);//start mixedlayer task by default
    }

    public void downloadCancelled() {
        skip();
        deleteLayer();
    }

    public void deleteLayer() {
        if (needDeleteLayer) {
            OsmDataLayer editLayer = MainApplication.getLayerManager().getEditLayer();
            if (editLayer != null) {
                editLayer.data.clear();
                MainApplication.getLayerManager().removeLayer(editLayer);
            }
        }
    }
}
