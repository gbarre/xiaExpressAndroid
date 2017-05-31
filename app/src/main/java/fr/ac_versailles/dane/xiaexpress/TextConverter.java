package fr.ac_versailles.dane.xiaexpress;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TextConverter.java
 * XiaExpress
 *
 * Created by guillaume on 18/05/2017.
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

class TextConverter extends AsyncTask<Void, Void, String> {
    private int videoWidth = 480;
    private int videoHeight = 270;
    private String htmlString;
    private WebView webV;

    TextConverter(String t, WebView wv, int w, int h) {
        htmlString = t;
        webV = wv;
        videoWidth = 480;
        videoHeight = 270;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        htmlString = htmlString.replaceAll("&", "&amp;");
        htmlString = htmlString.replaceAll("<", "&lt;");
        htmlString = htmlString.replaceAll(">", "&gt;");
        htmlString = htmlString.replaceAll("\\n", "<br />");

        htmlString = pikipikiToHTML(htmlString);
        htmlString = htmlString.replace("}}}", "");

        // TODO Test internet connexion
        htmlString = showAudio(htmlString);
        htmlString = showCustomLinks(htmlString);
        htmlString = showPictures(htmlString);
        htmlString = showVideo(htmlString);
    }

    @Override
    protected String doInBackground(Void... arg0) {
        String desc = htmlString;
        // Search http(s) links
        Pattern urls = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        Matcher urlsMatcher = urls.matcher(htmlString);
        while (urlsMatcher.find()) {
            String url = urlsMatcher.group();
            HttpHandler sh = new HttpHandler();
            try {
                // encode url
                String query = URLEncoder.encode(url, "utf-8");
                String fullQuery = "https://coyote.jrmv.net:8443/get?url=" + query;
                // get the json file (as string)
                String jsonStr = sh.makeServiceCall(fullQuery);
                // convert string to json
                JSONObject json = new JSONObject(jsonStr);
                // replace the url by iframe
                String htmlCode = json.getString("html");
                if (!htmlCode.equals("Please insert correct URL")) {
                    htmlCode = htmlCode.replace("src=\"//", "src=\"https://");
                    htmlCode = "<center>" + htmlCode + "</center>"; // so ugly...
                    desc = desc.replace(url, htmlCode);
                }
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        }
        return desc;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onPostExecute(String desc) {
        super.onPostExecute(desc);
        // make other link clickable
        Spannable sp = new SpannableString(desc);
        Linkify.addLinks(sp, Linkify.ALL);
        final String html = "<!DOCTYPE html><html lang=\"fr\"><head><meta charset=\"utf-8\"></head><body>" + sp + "</body></html>";
        // load html in the webview
        webV.loadData(html, "text/html; charset=UTF-8", null);
        // enable javascript
        WebSettings webSettings = webV.getSettings();
        webSettings.setJavaScriptEnabled(true);
        dbg.pt("textConverter", "htmlString", html);
    }

    private String pikipikiToHTML(String t) {
        String output = t;
        // Make bold
        Pattern bold = Pattern.compile("(\\*){3}((?!\\*{3}).)*\\*{3}");
        Matcher boldResults = bold.matcher(output);
        while (boldResults.find()) {
            String result = boldResults.group();
            String boldText = result.replace("***", "");
            output = output.replace(result, "<b>" + boldText + "</b>");
        }

        // Make emphasize
        Pattern emph = Pattern.compile("(\\*){2}((?!\\*{2}).)*\\*{2}");
        Matcher emphResults = emph.matcher(output);
        while (emphResults.find()) {
            String result = emphResults.group();
            String emphText = result.replace("**", "");
            output = output.replace(result, "<em>" + emphText + "</em>");
        }

        // Make pre-formatted
        Pattern pre = Pattern.compile("(\\{){3}((?!\\{{3}).)*\\}{3}");
        Matcher preResults = pre.matcher(output);
        while (preResults.find()) {
            String result = preResults.group();
            String preText = result.replaceAll("\\{{3}", "");
            preText = preText.replace("}}}", "");
            output = output.replace(result, "<pre>" + preText + "</pre>");
        }

        // Make line
        output = output.replaceAll("-----", "<hr size=3/>");
        output = output.replaceAll("----", "<hr/>");

        // Make list line by line
        String[] outputArray = output.split("<br />");
        int nbLines = outputArray.length;
        int onLine = 0;
        Boolean[] levelList = {false, false};
        String previousLine = "";
        int previousClosedLevel = 0;
        for (String line : outputArray) {
            String newLine = line;
            if (line.length() > 3) {
                if (!line.substring(0, 3).equals(" * ")) {
                    if (levelList[1]) {
                        levelList[1] = false;
                        previousClosedLevel = 1;
                        newLine = "<!-- close L1 --></li>\n\t</ul>\n" + line;
                    }
                }
                if (!line.substring(0, 2).equals("* ")) {
                    if (levelList[0] && !levelList[1]) {
                        levelList[0] = false;
                        newLine = "<!-- close L0 --></li></ul>\n" + line;
                    }
                }
                if (line.substring(0, 2).equals("* ")) {
                    if (!levelList[0]) {
                        levelList[0] = true;
                        newLine = "<ul>\n\t<li><!-- new L0 -->";
                    } else {
                        if (previousClosedLevel == 1) {
                            newLine = "<!-- close L1 --></li></ul>" + "<!-- close L0 element --></li>\n<li><!-- and open next L0 -->";
                            previousClosedLevel = 0;
                        } else {
                            newLine = "<!-- close L0 element --></li>\n<li><!-- and open next L0 -->";
                        }
                    }
                    newLine = newLine + line.substring(2);
                }
                if (line.substring(0, 3).equals(" * ")) {
                    if (!levelList[1]) {
                        levelList[1] = true;
                        levelList[0] = true;
                        newLine = "<ul>\n\t<li><!-- new L1 -->";
                    } else {
                        newLine = "<!-- close L1 element --></li>\n\t<li><!-- and open next L1 -->";
                    }
                    newLine = newLine + line.substring(3);
                }
                output = output.replace(line, newLine);
                previousLine = newLine;
            } else {
                if (levelList[1]) {
                    levelList[1] = false;
                    newLine = previousLine + "<!-- close L1 & L0 --></li>\n\t</ul></li>\n</ul>\n" + line;
                    output = output.replace(previousLine, newLine);
                } else if (levelList[0]) {
                    levelList[0] = false;
                    newLine = previousLine + "<!-- close L0 last --></li>\n</ul>\n" + line;
                    output = output.replace(previousLine, newLine);
                }
                previousLine = line;
            }
            onLine = onLine + 1;
        }

        return output;
    }

    private String showAudio(String inText) {
        String output = inText;
        Pattern regex = Pattern.compile("https?:\\/{2}(\\w|\\/|\\.|-|\\%|\\#)*\\.(mp3|ogg)");
        Matcher results = regex.matcher(inText);
        while (results.find()) {
            String result = results.group();
            String mp3Result = result.replaceAll("\\.(mp3|ogg)", ".mp3");
            String oggResult = result.replaceAll("\\.(mp3|ogg)", ".ogg");
            String replaceString = "<center><audio controls>" +
                    "   <source type=\"audio/mpeg\" src=\"" + mp3Result + "\" />" +
                    "   <source type=\"audio/ogg\" src=\"" + oggResult + "\" />" +
                    "</audio></center>";
            output = output.replace(result, replaceString);
        }
        return output;
    }

    private String showCustomLinks(String inText) {
        String output = inText;
        Pattern regex = Pattern.compile("\\[https?:\\/{2}((?! ).)* *((?!\\]).)*\\]");
        Matcher results = regex.matcher(inText);
        while (results.find()) {
            String result = results.group();
            String content = result.replaceAll("\\[|\\]", "");
            String[] contentArray = content.split(" ");
            String url = contentArray[0];
            String linkText = "";
            if (contentArray.length > 1) {
                for (int i = 1; i < contentArray.length; i++) {
                    linkText = linkText + " " + contentArray[i];
                }
            } else {
                linkText = url;
            }
            String replaceString = "<a href=\"" + url + "\">" + linkText + "</a>";
            output = output.replace(result, replaceString);
        }
        return output;
    }

    private String showPictures(String inText) {
        String output = inText;
        Pattern regex = Pattern.compile("https?:\\/{2}(\\w|\\/|\\.|-|\\%|\\#)*\\.(jpg|jpeg|gif|png)");
        Matcher results = regex.matcher(inText);
        while (results.find()) {
            String result = results.group();
            output = output.replace(result, "<img src=\"" + result + "\" alt=\"" + result + "\" style=\"max-width: " + videoWidth + "px;\" />");
        }
        return output;
    }

    private String showVideo(String inText) {
        String output = inText;
        Pattern regex = Pattern.compile("https?:\\/{2}(\\w|\\/|\\.|-|\\%|\\#)*\\.(mp4|ogv|webm)( autostart)?");
        Matcher results = regex.matcher(inText);
        while (results.find()) {
            String result = results.group();
            String mp4Result = result.replaceAll("\\.(mp4|ogv|webm)", ".mp4");
            String ogvResult = result.replaceAll("\\.(mp4|ogv|webm)", ".ogv");
            String webmResult = result.replaceAll("\\.(mp4|ogv|webm)", ".webm");
            String replaceString = "<center><video controls preload=\"none\">" +
                    "   <source type=\"video/mp4\" src=\"" + mp4Result + "\" />" +
                    "   <source type=\"video/ogg\" src=\"" + ogvResult + "\" />" +
                    "   <source type=\"video/webm\" src=\"" + webmResult + "\" />" +
                    "</video></center>";
            output = output.replace(result, replaceString);
        }
        return output;
    }
}
