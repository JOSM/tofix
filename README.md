# Tofix Plugin for JOSM

This plugin makes [To-Fix](http://osmlab.github.io/to-fix) OpenStreetMap microtasking challenges available directly from within the [JOSM](https://josm.openstreetmap.de/) editor.

Instead of picking new tasks and confirming accomplished tasks through the web interface of To-Fix on [https://github.com/osmlab/to-fix](https://github.com/osmlab/to-fix) this plugin allows you to work with To-Fix tasks without ever having to switch out of JOSM.

![tofix-wor2k](https://cloud.githubusercontent.com/assets/1152236/10491901/9b17bf5c-726e-11e5-9575-3c62d0412140.gif)

#### Installation

- Open "Preferences" in your JOSM editor (editor available on [josm.openstreetmap.de](https://josm.openstreetmap.de/))
- Click "Update plugins"
- Select "Plugins" and check "tofix"
- Confirm with "OK" and restart JOSM

![tofix](https://cloud.githubusercontent.com/assets/1152236/10457988/417882b4-718e-11e5-94b5-6ecf4a30eb43.gif)

#### Configuration

- Select "To-fix" from "Windows", the To-fix window will appear as a panel on your screen (at least one layer must be enabled to select options within Windows)
- Select a task from the drop down.

# Working with the plugin

- **Skip:** Click to select a new task at any time.
- **Not an error:** Click if a task does not require any editing.
- **Fixed:** click when you are done editing on a task. This will automatically start the upload process. Fill in a comment like `#to-fix:tigerdelta`. After uploading the plugin will download the next task.

#### Shortcuts

Default shortcuts:

- **Skip button:** ALT + SHIFT + S
- **Not a error button :** ALT + SHIFT + N
- **Fixed button :** ALT + SHIFT + F

Example to configure your own shortcuts:


![screenshot from 2015-06-18 09 30 05](https://cloud.githubusercontent.com/assets/1152236/8237229/6268a12a-15b3-11e5-8496-d67fdc1fc4b8.png)


# Actions

Tofix-plugin has different actions, which you could use for different type of works.

![image](https://user-images.githubusercontent.com/1152236/38279465-0d522eb8-3766-11e8-96c9-dfb7e37b160e.png)


#### Set default API

Tofix-plugin  is using per default the API: [http://tofix.devseed.com:8000](http://tofix.devseed.com:8000), but if you want to set up your own API, you could set up selecting the option, `Set default API`

![image](https://user-images.githubusercontent.com/1152236/38280650-2e8ec6bc-376c-11e8-9e5c-b182b293f2c5.png)


#### Set default Token

Tofix-plugin needs to authenticate with an OSM account in the [to-fix-backend API](https://github.com/osmlab/to-fix-backend), therefore a default user has been created: [tofixjson](https://www.openstreetmap.org/user/tofixjosm), and the token of this users has been established by default in the plugin. If you want to change this configuration, you have to disable the option `Set default Token` and it will show you the option to authenticate with your OSM account. 
 
 ![image](https://user-images.githubusercontent.com/1152236/38279739-53437a5c-3767-11e8-8e18-30a6afcf4ad5.png)
 

#### Auto delete Layer

Tofix-plugin downloads the issues in different layers, we keep this option enabled so that the plugin removes the layers that no longer have changes to upload to OSM.

#### Download OSM Data

Tofix-plugin by default will download OSM data for the work area, but in case you just want to verify the items(issues)  without downloading the OSM data, you can disable the download option. Once the option is disabled the plugin will request only the items.

![get6](https://user-images.githubusercontent.com/1152236/38280298-3a8676e2-376a-11e8-8d1f-4cd17c7690c4.gif)


#### Set editable Layer

Tofix-plugin helps to edit the data that has been uploaded into the [tofix-backend](https://github.com/osmlab/to-fix-backend) [API](http://tofix.devseed.com:8000).  this functionality could help in cases you want to import data into OSM, checking feature by feature.

![get7](https://user-images.githubusercontent.com/1152236/38280393-a2d406ce-376a-11e8-80af-c6155847a840.gif)


#### Set bbox to request the items

This functionality helps to make requests for items(issues) in a certain area.

- If you already have a active layer with the work area, tofix-plugin will take the bbox of this work area.


<img width="806" alt="screen shot 2018-04-03 at 6 16 02 pm" src="https://user-images.githubusercontent.com/1152236/38280510-52025416-376b-11e8-8c09-602f20a27e3f.png">


- If you do not have a active layer, tofix-plugin will set the visible area bbox in JOSM.

This functionality will help the user to work their own areas


![screen shot 2018-04-03 at 6 19 04 pm](https://user-images.githubusercontent.com/1152236/38280570-c6992c8c-376b-11e8-8f1e-39fbbcccad27.png)



# Others

- Tofix-backend: https://github.com/osmlab/to-fix-backend

- Tofix-web: https://github.com/osmlab/to-fix (Currently, nobody maintains this side)

