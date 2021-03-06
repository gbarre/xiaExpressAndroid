package fr.ac_versailles.dane.xiaexpress;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

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

    private final View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
    private Integer tag = 0;
    private Boolean zoom = false;
    private Boolean lock = false;
    private String detailTitle = "";
    private String detailDescription = "";
    private Document xml;
    private String fileTitle = "";
    private String xmlDirectory = "";
    private Switch checkBoxZoom = null;
    private Switch checkBoxLocked = null;
    private EditText txtTitle = null;
    private EditText txtDesc = null;
    private final View.OnClickListener doneListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Save the detail in xml
            NodeList xmlDetails = xml.getElementsByTagName("detail");
            for (int i = 0; i < xmlDetails.getLength(); i++) {
                Node detail = xmlDetails.item(i);
                NamedNodeMap detailAttr = detail.getAttributes();
                Node t = detailAttr.getNamedItem("tag");
                Integer detailTag = Integer.valueOf(t.getTextContent());
                if (detailTag.equals(tag)) {
                    Node detailZoom = detailAttr.getNamedItem("zoom");
                    detailZoom.setTextContent(String.valueOf(checkBoxZoom.isChecked()));
                    Node detailLocked = detailAttr.getNamedItem("locked");
                    detailLocked.setTextContent(String.valueOf(checkBoxLocked.isChecked()));
                    Node detailTitle = detailAttr.getNamedItem("title");
                    detailTitle.setTextContent(String.valueOf(txtTitle.getText()));

                    detail.setTextContent(String.valueOf(txtDesc.getText()));

                }
            }
            Util.writeXML(xml, xmlDirectory + fileTitle + Constants.XML_EXTENSION);

            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_infos);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels * 8 / 10;
        int height = metrics.heightPixels * 8 / 10;

        // Gets linearlayout
        LinearLayout layout = (LinearLayout) findViewById(R.id.activity_detail_infos);
        // Gets the layout params that will allow you to resize the layout
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        // Changes the height and width to the specified *pixels*
        params.height = height;
        params.width = width;

        Button btnDone = (Button) findViewById(R.id.done);
        Button btnCancel = (Button) findViewById(R.id.cancel);

        btnDone.setOnClickListener(doneListener);
        btnCancel.setOnClickListener(cancelListener);

        String rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        xmlDirectory = Constants.getXMLFrom(rootDirectory);

        fileTitle = getIntent().getStringExtra("fileTitle");
        tag = Integer.valueOf(getIntent().getStringExtra("tag"));

        xml = Util.getXMLFromPath(xmlDirectory + fileTitle + Constants.XML_EXTENSION);

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

        txtTitle = (EditText) findViewById(R.id.detailTitle);
        checkBoxZoom = (Switch) findViewById(R.id.zoom);
        checkBoxLocked = (Switch) findViewById(R.id.locked);
        txtDesc = (EditText) findViewById(R.id.description);

        txtTitle.setText(detailTitle);
        checkBoxZoom.setChecked(zoom);
        checkBoxLocked.setChecked(lock);
        txtDesc.setText(detailDescription);
        txtDesc.requestFocus();

    }

}
