package fr.ac_versailles.dane.xiaexpress;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

/**
 * DetailInfos.java
 * XiaExpress
 *
 * Created by guillaume on 14/11/2016.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * @author : guillaume.barre@ac-versailles.fr
 */

public class DetailInfos extends AppCompatActivity {

    private Integer tag = 0;
    private Boolean zoom = false;
    private Boolean lock = false;
    private String detailTitle = "";
    private String detailDescription = "";
    private Document xml;
    private Integer index = 0;
    private String fileTitle = "";
    private String xmlDirectory = "";

    private Button btnDone = null;
    private Button btnCancel = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_infos);

        btnDone = (Button) findViewById(R.id.done);
        btnCancel = (Button) findViewById(R.id.cancel);

        btnDone.setOnClickListener(doneListener);
        btnCancel.setOnClickListener(cancelListener);

        String rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        xmlDirectory = Constants.getXMLFrom(rootDirectory);

        fileTitle = getIntent().getStringExtra("fileTitle");
        tag = Integer.valueOf(getIntent().getStringExtra("tag"));

        xml = Util.getXMLFromPath(xmlDirectory + fileTitle + ".xml");

        // Load detail infos from xml
        NodeList xmlDetails = xml.getElementsByTagName("detail");
        for (int i = 0; i < xmlDetails.getLength(); i++) {
            Node detail = xmlDetails.item(i);
            NamedNodeMap detailAttr = detail.getAttributes();
            Integer thisTag = Integer.valueOf(detailAttr.getNamedItem("tag").getTextContent());
            if (thisTag.equals(tag)) { // we got it !
                zoom = detailAttr.getNamedItem("zoom").getTextContent().equals("true");
                lock = detailAttr.getNamedItem("locked").getTextContent().equals("true");
                detailTitle = detailAttr.getNamedItem("title").getTextContent();
                detailDescription = detail.getTextContent();
                break;
            }
        }

        EditText txtTitle = (EditText) findViewById(R.id.detailTitle);
        CheckBox checkBoxZoom = (CheckBox) findViewById(R.id.zoom);
        CheckBox checkBoxLocked = (CheckBox) findViewById(R.id.locked);
        EditText txtDesc = (EditText) findViewById(R.id.description);

        txtTitle.setText(detailTitle);
        checkBoxZoom.setChecked(zoom);
        checkBoxLocked.setChecked(lock);
        txtDesc.setText(detailDescription);

    }

    private View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private View.OnClickListener doneListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Save the detail in xml
            /* TODO
            if let detail = xml["xia"]["details"]["detail"].allWithAttributes(["tag" : "\(tag)"]) {
                for d in detail {
                    d.attributes["zoom"] = "\(switchZoom.isOn)"
                    d.attributes["locked"] = "\(switchLock.isOn)"
                    d.attributes["title"] = txtTitle.text
                    //d.value = attributedString2pikipiki(txtDesc.attributedText)
                    d.value = txtDesc.text
                }
            }
            let _ = writeXML(xml, path: "\(filePath).xml")
            */

            finish();
        }
    };

}
