package fr.ac_versailles.dane.xiaexpress;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Document;

import java.io.File;

import co.ceryle.segmentedbutton.SegmentedButtonGroup;

/**
 * Metas.java
 * XiaExpress
 * <p>
 * Created by guillaume on 02/06/2017.
 * <p>
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

public class Metas extends AppCompatActivity {

    private final View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
    private final View.OnClickListener doneListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Save the detail in xml
            /*NodeList xmlDetails = xml.getElementsByTagName("detail");
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
            Util.writeXML(xml, xmlDirectory + fileTitle + ".xml");
*/
            finish();
        }
    };
    private Document xml;
    private String fileTitle = "";
    private String xmlDirectory = "";
    private EditText txtTitle = null;
    private EditText txtDesc = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metas);

        Button btnDone = (Button) findViewById(R.id.done);
        Button btnCancel = (Button) findViewById(R.id.cancel);

        btnDone.setOnClickListener(doneListener);
        btnCancel.setOnClickListener(cancelListener);

        String rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        xmlDirectory = Constants.getXMLFrom(rootDirectory);

        fileTitle = getIntent().getStringExtra("fileTitle");

        xml = Util.getXMLFromPath(xmlDirectory + fileTitle + ".xml");

        // Load subviews
        final LinearLayout metas1 = (LinearLayout) findViewById(R.id.metas1);
        final LinearLayout metas2 = (LinearLayout) findViewById(R.id.metas2);
        final LinearLayout metas3 = (LinearLayout) findViewById(R.id.metas3);
        final LinearLayout metasimginfos = (LinearLayout) findViewById(R.id.metasimginfos);
        metas1.setVisibility(View.VISIBLE);
        metas2.setVisibility(View.GONE);
        metas3.setVisibility(View.GONE);
        metasimginfos.setVisibility(View.GONE);

        final TextView Title = (TextView) findViewById(R.id.Title);

        // First subview
        final EditText metasTitle = (EditText) findViewById(R.id.metasTitle);
        final EditText metasDescription = (EditText) findViewById(R.id.metasDescription);
        setNextFocus(metasTitle, metasDescription);

        // Second subview
        final EditText Creator = (EditText) findViewById(R.id.metasCreator);
        final EditText Rights = (EditText) findViewById(R.id.metasRights);
        setNextFocus(Creator, Rights);
        final EditText Publisher = (EditText) findViewById(R.id.metasPublisher);
        setNextFocus(Rights, Publisher);
        final EditText Identifier = (EditText) findViewById(R.id.metasIdentifier);
        setNextFocus(Publisher, Identifier);
        final EditText Source = (EditText) findViewById(R.id.metasSource);
        setNextFocus(Identifier, Source);

        // Third view
        final EditText Languages = (EditText) findViewById(R.id.metasLanguages);
        final EditText Keywords = (EditText) findViewById(R.id.metasKeywords);
        setNextFocus(Languages, Keywords);
        final EditText Contributors = (EditText) findViewById(R.id.metasContributors);
        setNextFocus(Keywords, Contributors);
        final EditText Relation = (EditText) findViewById(R.id.metasRelation);
        setNextFocus(Contributors, Relation);
        final EditText Coverage = (EditText) findViewById(R.id.metasCoverage);
        setNextFocus(Relation, Coverage);

        // Fourth view
        final EditText ImgTitle = (EditText) findViewById(R.id.metasImgTitle);
        final EditText ImgDescription = (EditText) findViewById(R.id.metasImgDescription);
        setNextFocus(ImgTitle, ImgDescription);

        // Load detail infos from xml
        /*NodeList xmlDetails = xml.getElementsByTagName("detail");
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
*/
        SegmentedButtonGroup segmentedButtonGroup = (SegmentedButtonGroup) findViewById(R.id.segmentedButtonGroup);
        segmentedButtonGroup.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                switch (position) {
                    default:
                    case 0:
                        metas1.setVisibility(View.VISIBLE);
                        metas2.setVisibility(View.GONE);
                        metas3.setVisibility(View.GONE);
                        metasimginfos.setVisibility(View.GONE);
                        break;
                    case 1:
                        metas1.setVisibility(View.GONE);
                        metas2.setVisibility(View.VISIBLE);
                        metas3.setVisibility(View.GONE);
                        metasimginfos.setVisibility(View.GONE);
                        break;
                    case 2:
                        metas1.setVisibility(View.GONE);
                        metas2.setVisibility(View.GONE);
                        metas3.setVisibility(View.VISIBLE);
                        metasimginfos.setVisibility(View.GONE);
                        break;
                    case 3:
                        metas1.setVisibility(View.GONE);
                        metas2.setVisibility(View.GONE);
                        metas3.setVisibility(View.GONE);
                        metasimginfos.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    private void setNextFocus(View source, View target) {
        source.setNextFocusDownId(target.getId());
        source.setNextFocusRightId(target.getId());
        target.setNextFocusLeftId(source.getId());
    }
}
