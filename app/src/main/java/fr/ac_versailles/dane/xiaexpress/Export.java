package fr.ac_versailles.dane.xiaexpress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Export.java
 * XiaExpress
 *
 * Created by guillaume on 09/06/2017.
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

class Export {
    private Document xml;
    private String b64;
    private float imgWidth, imgHeight;


    Export(Document x, String i) {
        xml = x;
        b64 = getB64(i);
    }

    private String getB64(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
        imgWidth = bitmap.getWidth();
        imgHeight = bitmap.getHeight();
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    String xiaTablet() {
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

    String inkscape() {
        String output = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n";
        // randomize svg id
        Random r = new Random();
        int svgID = r.nextInt(9000 - 1) + 1;

        String tmpTitle = Util.cleanInput(Util.getNodeValue(xml, "xia/title"));

        // prepare xml
        output = output + "<svg xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
                "sodipodi:docname=\"" + tmpTitle + "\" " +
                "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
                "xmlns:svg=\"http://www.w3.org/2000/svg\" " +
                "xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                "width=\"" + imgWidth + "\" " +
                "xmlns:cc=\"http://creativecommons.org/ns#\" " +
                "xmlns=\"http://www.w3.org/2000/svg\" " +
                "xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\" " +
                "inkscape:version=\"0.91\" " +
                "height=\"" + imgHeight + "\" " +
                "id=\"svg" + svgID + "\" " +
                "xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\" " +
                "viewBox=\"0 0 " + imgWidth + " " + imgHeight + "\" " +
                "version=\"1.1\">\n";

        output = output + "\t<title id=\"title" + (svgID + 1) + "\">" + tmpTitle + "</title>\n";

        // Metas
        output = output + "\t<metadata id=\"metadata" + (svgID + 2) + "\">\n";
        output = output + "\t\t<rdf:RDF>\n" +
                "\t\t\t<cc:Work rdf:about=\"\">\n" +
                "\t\t\t\t<dc:format>image/svg+xml</dc:format>\n" +
                "\t\t\t\t<dc:type rdf:resource=\"http://purl.org/dc/dcmitype/StillImage\"></dc:type>\n" +
                "\t\t\t\t<dc:title>" + tmpTitle + "</dc:title>\n" +
                "\t\t\t\t<dc:date>" + Util.getNodeValue(xml, "xia/date") + "</dc:date>\n" +
                "\t\t\t\t<dc:creator>\n" +
                "\t\t\t\t\t<cc:Agent>\n" +
                "\t\t\t\t\t\t<dc:title>" + Util.getNodeValue(xml, "xia/creator") + "</dc:title>\n" +
                "\t\t\t\t\t</cc:Agent>\n" +
                "\t\t\t\t</dc:creator>\n" +
                "\t\t\t\t<dc:rights>\n" +
                "\t\t\t\t\t<cc:Agent>\n" +
                "\t\t\t\t\t\t<dc:title>" + Util.getNodeValue(xml, "xia/rights") + "</dc:title>\n" +
                "\t\t\t\t\t</cc:Agent>\n" +
                "\t\t\t\t</dc:rights>\n" +
                "\t\t\t\t<dc:publisher>\n" +
                "\t\t\t\t\t<cc:Agent>\n" +
                "\t\t\t\t\t\t<dc:title>" + Util.getNodeValue(xml, "xia/publisher") + "</dc:title>\n" +
                "\t\t\t\t\t</cc:Agent>\n" +
                "\t\t\t\t</dc:publisher>\n" +
                "\t\t\t\t<dc:identifier>" + Util.getNodeValue(xml, "xia/identifier") + "</dc:identifier>\n" +
                "\t\t\t\t<dc:source>" + Util.getNodeValue(xml, "xia/source") + "</dc:source>\n" +
                "\t\t\t\t<dc:relation>" + Util.getNodeValue(xml, "xia/relation") + "</dc:relation>\n" +
                "\t\t\t\t<dc:language>" + Util.getNodeValue(xml, "xia/language") + "</dc:language>\n" +
                "\t\t\t\t<dc:subject>\n" +
                "\t\t\t\t\t<rdf:Bag>\n" +
                "\t\t\t\t\t\t<rdf:li>" + Util.getNodeValue(xml, "xia/keywords") + "</rdf:li>\n" +
                "\t\t\t\t\t</rdf:Bag>\n" +
                "\t\t\t\t</dc:subject>\n" +
                "\t\t\t\t<dc:coverage>" + Util.getNodeValue(xml, "xia/coverage") + "</dc:coverage>\n" +
                "\t\t\t\t<dc:description>" + Util.getNodeValue(xml, "xia/description") + "</dc:description>\n" +
                "\t\t\t\t<dc:contributor>\n" +
                "\t\t\t\t\t<cc:Agent>\n" +
                "\t\t\t\t\t\t<dc:title>" + Util.getNodeValue(xml, "xia/contributors") + "</dc:title>\n" +
                "\t\t\t\t\t</cc:Agent>\n" +
                "\t\t\t\t</dc:contributor>\n";

        String license = Util.getNodeValue(xml, "xia/license");
        Boolean addPermits;
        Map<String, String> permits = new HashMap<>();
        permits.put("Reproduction", "none");
        permits.put("Distribution", "none");
        permits.put("Notice", "none");
        permits.put("Attribution", "none");
        permits.put("CommercialUse", "none");
        permits.put("DerivativeWorks", "none");
        permits.put("ShareAlike", "none");
        String rdfResource;
        String openFont = "";
        switch (license) {
            case "Proprietary - CC-Zero":
                rdfResource = "";
                addPermits = false;
                break;
            case "CC Attribution - CC-BY":
                rdfResource = "http://creativecommons.org/licenses/by/3.0/";
                addPermits = true;
                permits.put("Reproduction", "permits");
                permits.put("Distribution", "permits");
                permits.put("Notice", "requires");
                permits.put("Attribution", "requires");
                permits.put("DerivativeWorks", "permits");
                break;
            case "CC Attribution-ShareALike - CC-BY-SA":
                rdfResource = "http://creativecommons.org/licenses/by-sa/3.0/";
                addPermits = true;
                permits.put("Reproduction", "permits");
                permits.put("Distribution", "permits");
                permits.put("Notice", "requires");
                permits.put("Attribution", "requires");
                permits.put("DerivativeWorks", "permits");
                permits.put("ShareAlike", "requires");
                break;
            case "CC Attribution-NoDerivs - CC-BY-ND":
                rdfResource = "http://creativecommons.org/licenses/by-nd/3.0/";
                addPermits = true;
                permits.put("Reproduction", "permits");
                permits.put("Distribution", "permits");
                permits.put("Notice", "requires");
                permits.put("Attribution", "requires");
                break;
            case "CC Attribution-NonCommercial - CC-BY-NC":
                rdfResource = "http://creativecommons.org/licenses/by-nc/3.0/";
                addPermits = true;
                permits.put("Reproduction", "permits");
                permits.put("Distribution", "permits");
                permits.put("Notice", "requires");
                permits.put("Attribution", "requires");
                permits.put("CommercialUse", "prohibits");
                permits.put("DerivativeWorks", "permits");
                break;
            case "CC Attribution-NonCommercial-ShareALike - CC-BY-NC-SA":
                rdfResource = "http://creativecommons.org/licenses/by-nc-sa/3.0/";
                addPermits = true;
                permits.put("Reproduction", "permits");
                permits.put("Distribution", "permits");
                permits.put("Notice", "requires");
                permits.put("Attribution", "requires");
                permits.put("CommercialUse", "prohibits");
                permits.put("DerivativeWorks", "permits");
                permits.put("ShareAlike", "requires");
                break;
            case "CC Attribution-NonCommercial-NoDerivs - CC-BY-NC-ND":
                rdfResource = "http://creativecommons.org/licenses/by-nc-nd/3.0/";
                addPermits = true;
                permits.put("Reproduction", "permits");
                permits.put("Distribution", "permits");
                permits.put("Notice", "requires");
                permits.put("Attribution", "requires");
                permits.put("CommercialUse", "prohibits");
                break;
            case "CC0 Public Domain Dedication":
                rdfResource = "http://creativecommons.org/publicdomain/zero/1.0/";
                addPermits = true;
                permits.put("Reproduction", "permits");
                permits.put("Distribution", "permits");
                permits.put("DerivativeWorks", "permits");
                break;
            case "Free Art":
                rdfResource = "http://artlibre.org/licence/lal";
                addPermits = true;
                permits.put("Reproduction", "permits");
                permits.put("Distribution", "permits");
                permits.put("Notice", "requires");
                permits.put("Attribution", "requires");
                permits.put("DerivativeWorks", "permits");
                permits.put("ShareAlike", "requires");
                break;
            case "Open Font License":
                rdfResource = "http://scripts.sil.org/OFL";
                addPermits = false;
                openFont = "\t\t\t<cc:license rdf:about=\"" + rdfResource + "\">\n" +
                        "\t\t\t\t<cc:permits rdf:resource=\"http://scripts.sil.org/pub/OFL/Reproduction\"></cc:permits>\n" +
                        "\t\t\t\t<cc:permits rdf:resource=\"http://scripts.sil.org/pub/OFL/Distribution\"></cc:permits>\n" +
                        "\t\t\t\t<cc:permits rdf:resource=\"http://scripts.sil.org/pub/OFL/Embedding\"></cc:permits>\n" +
                        "\t\t\t\t<cc:permits rdf:resource=\"http://scripts.sil.org/pub/OFL/DerivativeWorks\"></cc:permits>\n" +
                        "\t\t\t\t<cc:permits rdf:resource=\"http://scripts.sil.org/pub/OFL/Notice\"></cc:permits>\n" +
                        "\t\t\t\t<cc:permits rdf:resource=\"http://scripts.sil.org/pub/OFL/Attribution\"></cc:permits>\n" +
                        "\t\t\t\t<cc:permits rdf:resource=\"http://scripts.sil.org/pub/OFL/ShareAlike\"></cc:permits>\n" +
                        "\t\t\t\t<cc:permits rdf:resource=\"http://scripts.sil.org/pub/OFL/DerivativeRenaming\"></cc:permits>\n" +
                        "\t\t\t\t<cc:permits rdf:resource=\"http://scripts.sil.org/pub/OFL/BundlingWhenSelling\"></cc:permits>\n" +
                        "\t\t\t</cc:license>\n";
                break;
            case "Other":
                rdfResource = "";
                addPermits = false;
                break;
            default:
                rdfResource = "";
                addPermits = false;
                break;
        }
        output = output + "\t\t\t\t<cc:license rdf:resource=\"" + rdfResource + "\"></cc:license>\n" +
                "\t\t\t</cc:Work>\n";
        if (addPermits) {
            output = output + "\t\t\t<cc:license rdf:about=\"" + rdfResource + "\">\n";
            for (Map.Entry<String, String> entry : permits.entrySet()) {
                String permit = entry.getKey();
                String state = entry.getValue();
                if (!state.equals("none")) {
                    output = output + "\t\t\t\t<cc:" + state + " rdf:resource=\"http://creativecommons.org/ns#" + permit + "\"></cc:" + state + ">\n";
                }
            }
            output = output + "\t\t\t</cc:license>\n" +
                    "\t\t</rdf:RDF>\n" +
                    "\t</metadata>\n";
        } else if (!openFont.equals("")) {
            output = output + openFont;
        }

        output = output + "\t<defs id=\"defs" + (svgID + 3) + "\"></defs>\n" +
                "\t<sodipodi:namedview " +
                "inkscape:window-width=\"640\" " +
                "inkscape:cy=\"480\" " +
                "inkscape:pageshadow=\"2\" " +
                "showgrid=\"false\" " +
                "guidetolerance=\"10\" " +
                "pagecolor=\"#ffffff\" " +
                "bordercolor=\"#666666\" " +
                "inkscape:zoom=\"0.25\" " +
                "id=\"namedview" + (svgID + 4) + "\" " +
                "inkscape:current-layer=\"svg" + (svgID + 5) + "\" " +
                "inkscape:cx=\"640\" " +
                "inkscape:window-height=\"480\" " +
                "gridtolerance=\"10\" " +
                "objecttolerance=\"10\" " +
                "borderopacity=\"1\" " +
                "inkscape:pageopacity=\"0\"></sodipodi:namedview>\n";

        // Add image
        output = output + "\t<image " +
                "height=\"" + imgHeight + "\" " +
                "xlink:href=\"data:image/jpeg;base64," + b64.replace("\\n", "") + "\" " +
                "preserveAspectRatio=\"none\" " +
                "id=\"image" + (svgID + 6) + "\" " +
                "width=\"" + imgWidth + "\">\n";

        output = output + "\t\t<desc id=\"desc" + (svgID + 7) + "\">" + Util.getNodeAttribute(xml, "image", "description") + "</desc>\n" +
                "\t\t<title id=\"title" + (svgID + 8) + "\">" + Util.getNodeAttribute(xml, "image", "title") + "</title>\n" +
                "\t</image>\n";

        // Add details
        NodeList xmlDetails = xml.getElementsByTagName("detail");
        for (int i = 0; i < xmlDetails.getLength(); i++) {
            Node detail = xmlDetails.item(i);
            NamedNodeMap detailAttr = detail.getAttributes();
            Node pathNode = detailAttr.getNamedItem("path");
            String path = pathNode.getTextContent();
            if (!"".equals(path)) { // we have a path, try to draw it...
                String[] pointsArray = path.split(" ");

                Node tag = detailAttr.getNamedItem("tag");
                int detailTag = Integer.valueOf(tag.getTextContent());

                String detailTitle = detailAttr.getNamedItem("title").getTextContent();
                String detailDescription = detail.getTextContent();
                String detailConstraint = detailAttr.getNamedItem("constraint").getTextContent();
                String detailType;
                String detailAttributes;

                if (detailConstraint.equals(Constants.constraintRectangle) || detailConstraint.equals(Constants.constraintEllipse)) {
                    detailType = (detailConstraint.equals(Constants.constraintRectangle)) ? "rect" : Constants.constraintEllipse;
                    float xMin = 99999999;
                    float xMax = 0;
                    float yMin = 99999999;
                    float yMax = 0;

                    for (String aPointsArray : pointsArray) {
                        String[] coords = aPointsArray.split(";");
                        Float x = Float.parseFloat(coords[0]);
                        Float y = Float.parseFloat(coords[1]);
                        if (x < xMin) {
                            xMin = x;
                        }
                        if (x > xMax) {
                            xMax = x;
                        }
                        if (y < yMin) {
                            yMin = y;
                        }
                        if (y > yMax) {
                            yMax = y;
                        }
                    }
                    float width = xMax - xMin;
                    float height = yMax - yMin;

                    String rectDetail = " style=\"opacity:0.3;fill:#ff0000;stroke:#000000;stroke-width:0.1;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" " +
                            "id=\"rect" + (svgID + detailTag) + "\" " +
                            "width=\"" + width + "\" " +
                            "height=\"" + height + "\" " +
                            "x=\"" + xMin + "\" " +
                            "y=\"" + yMin + "\"";
                    String ellipseDetail = " style=\"opacity:0.3;fill:#ff0000;stroke:#000000;stroke-width:0.1;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" " +
                            "id=\"path" + (svgID + detailTag) + "\" " +
                            "cx=\"" + (xMin + width / 2) + "\" " +
                            "cy=\"" + (yMin + height / 2) + "\" " +
                            "rx=\"" + (width / 2) + "\" " +
                            "ry=\"" + (height / 2) + "\"";

                    detailAttributes = (detailType.equals("rect")) ? rectDetail : ellipseDetail;
                } else {
                    detailType = "path";
                    detailAttributes = " style=\"opacity:0.3;fill:#ff0000;stroke:#000000;stroke-width:0.1;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1\" " +
                            "id=\"path" + (svgID + detailTag) + "\" " +
                            "d=\"M " + path.replace(";", ",") + " Z\" " +
                            "inkscape:connector-curvature=\"0\"";
                }
                output = output + "\t<" + detailType + detailAttributes + ">\n" +
                        "\t\t<desc id=\"" + (svgID + detailTag + 100) + "\">" + detailDescription + "</desc>\n" +
                        "\t\t<title id=\"" + (svgID + detailTag + 200) + "\">" + detailTitle + "</title>\n" +
                        "\t</" + detailType + ">\n";
            }
        }
        output = output + "</svg>";
        return output;
    }
}
