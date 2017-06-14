package fr.ac_versailles.dane.xiaexpress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;

/**
 * Export.java
 * XiaExpress
 * <p>
 * Created by guillaume on 09/06/2017.
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

public class Export {
    private Document xml;
    private String b64;

    public Export(Document x, String i) {
        xml = x;
        b64 = getB64(i);
    }

    private String getB64(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public String xiaTablet() {
        String output = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n" +
                "<XiaiPad>\n";
        String xmlString = Util.xml2String(xml);
        output = output + xmlString;
        output = output + "\n" +
                "<image>\n" +
                b64 + "\n" +
                "</image>\n" +
                "</XiaiPad>";
        return output;
    }
}
