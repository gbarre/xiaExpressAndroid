package fr.ac_versailles.dane.xiaexpress;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * import.java
 * XiaExpress
 * <p>
 * Created by guillaume on 04/07/2017.
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

public class importActivity extends AppCompatActivity {

    private String rootDirectory;
    private String imagesDirectory;
    private String xmlDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbg.pt("importActivity", "onCreate", "begin import");

        // TODO check XML structure

        // Prepare directories
        rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        imagesDirectory = Constants.getImagesFrom(rootDirectory);
        xmlDirectory = Constants.getXMLFrom(rootDirectory);

        Util.createDirectory(imagesDirectory);
        Util.createDirectory(xmlDirectory);

        // Get file path
        Intent intent = getIntent();
        String path = intent.getData().getPath();
        dbg.pt("import", "name", path);

        long now = System.currentTimeMillis();

        // Get image base 64
        Document xml = Util.getXMLFromPath(path);
        String imageB64 = Util.getNodeValue(xml, "XiaiPad/image");

        // build image
        byte[] decodedString = Base64.decode(imageB64, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
        try {
            // Store image
            Util.copy(bs, new File(imagesDirectory + now + ".jpg"));
            dbg.pt("import", "image", "imported");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Store xml
        String xmlPath = xmlDirectory + now + ".xml";
        NodeList xiaNodeList = xml.getElementsByTagName("xia");
        Node xia = xiaNodeList.item(0);
        dbg.pt("import", "xia", xia.toString());

        String xmlString = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n";
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(xmlPath));
            outputStreamWriter.write(xmlString);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Document newXML = Util.getXMLFromPath(xmlPath);
        Node copyXia = newXML.importNode(xia, true);
        newXML.appendChild(copyXia);
        Util.writeXML(newXML, xmlPath);

        // TODO launch main activity !

    }


}
