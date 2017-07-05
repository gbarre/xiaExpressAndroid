package fr.ac_versailles.dane.xiaexpress;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.Toast;

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
 *
 * Created by guillaume on 04/07/2017.
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

public class importActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Boolean errorAtImageImport = true;
        Boolean errorAtXMLImport = true;

        // Prepare directories
        String rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        String imagesDirectory = Constants.getImagesFrom(rootDirectory);
        String xmlDirectory = Constants.getXMLFrom(rootDirectory);
        String cacheDirectory = Constants.getCacheFrom(rootDirectory);

        Util.createDirectory(imagesDirectory);
        Util.createDirectory(xmlDirectory);
        Util.createDirectory(cacheDirectory);

        // Get file path
        Intent intent = getIntent();
        String path;
        Boolean localImport = false;
        if (intent.getStringExtra("fileToImport") != null) {
            path = cacheDirectory + intent.getStringExtra("fileToImport") + Constants.XML_EXTENSION;
            localImport = true;
        } else {
            path = intent.getData().getPath();
        }

        long now = System.currentTimeMillis();

        // Get image base 64
        Document xml = Util.getXMLFromPath(path);
        String imageB64 = Util.getNodeValue(xml, "XiaiPad/image");

        if (!imageB64.equals("")) {
            // build image
            byte[] decodedString = Base64.decode(imageB64, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
            try {
                // Store image
                Util.copy(bs, new File(imagesDirectory + now + Constants.JPG_EXTENSION));
                errorAtImageImport = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!errorAtImageImport) {
            // Store xml
            String xmlPath = xmlDirectory + now + Constants.XML_EXTENSION;
            NodeList xiaNodeList = xml.getElementsByTagName("xia");
            Node xia = xiaNodeList.item(0);
            if (xia != null) {
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
                errorAtXMLImport = false;
                Toast.makeText(this, getResources().getString(R.string.importOK), Toast.LENGTH_LONG).show();
            }
        }

        if (errorAtXMLImport) {
            Util.removeFile(imagesDirectory + now + Constants.JPG_EXTENSION);
        }
        if (errorAtImageImport || errorAtXMLImport) {
            Toast.makeText(this, getResources().getString(R.string.wrongXML), Toast.LENGTH_LONG).show();
        }
        if (localImport) {
            Util.removeFile(path);
        }

        // launch main activity
        Intent start = new Intent(importActivity.this, MainActivity.class);
        startActivity(start);

    }


}
