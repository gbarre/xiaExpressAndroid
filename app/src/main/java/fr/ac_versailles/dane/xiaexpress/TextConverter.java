package fr.ac_versailles.dane.xiaexpress;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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
    private static final List<String> replacedURL = new ArrayList<>();
    private static float videoWidth = 480;
    private static float videoHeight = 270;
    private final WebView webV;
    private final RelativeLayout pBar;
    private String htmlString;
    private Context context;
    private DBAdapter urlDb;

    TextConverter(String t, WebView wv, float w, float h, Context c, RelativeLayout p) {
        htmlString = t;
        webV = wv;
        videoWidth = (w == 0) ? 480 : w;
        videoHeight = (h == 0) ? 270 : h;
        context = c;
        pBar = p;
    }

    static String getAudio(String url) {
        String mp3Result = url.replaceAll("\\.(mp3|ogg)", ".mp3");
        String oggResult = url.replaceAll("\\.(mp3|ogg)", ".ogg");
        return "<center><audio controls>" +
                "   <source type=\"audio/mpeg\" src=\"" + mp3Result + "\" />" +
                "   <source type=\"audio/ogg\" src=\"" + oggResult + "\" />" +
                "</audio></center>";
    }

    static String getvideo(String url) {
        String mp4Result = url.replaceAll("\\.(mp4|ogv|webm)", ".mp4");
        String ogvResult = url.replaceAll("\\.(mp4|ogv|webm)", ".ogv");
        String webmResult = url.replaceAll("\\.(mp4|ogv|webm)", ".webm");
        return "<center><video controls preload=\"none\" width=\"" + videoWidth + "\" height=\"" + videoHeight + ">" +
                "   <source type=\"video/mp4\" src=\"" + mp4Result + "\" />" +
                "   <source type=\"video/ogg\" src=\"" + ogvResult + "\" />" +
                "   <source type=\"video/webm\" src=\"" + webmResult + "\" />" +
                "</video></center>";
    }

    static String pikipikiToHTML(String t) {
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
                        newLine = "</li>\n\t</ul>\n" + line;
                    }
                }
                if (!line.substring(0, 2).equals("* ")) {
                    if (levelList[0] && !levelList[1]) {
                        levelList[0] = false;
                        newLine = "</li></ul>\n" + line;
                    }
                }
                if (line.substring(0, 2).equals("* ")) {
                    if (!levelList[0]) {
                        levelList[0] = true;
                        newLine = "<ul>\n\t<li>";
                    } else {
                        if (previousClosedLevel == 1) {
                            newLine = "</li></ul>" + "</li>\n<li>";
                            previousClosedLevel = 0;
                        } else {
                            newLine = "</li>\n<li>";
                        }
                    }
                    newLine = newLine + line.substring(2);
                }
                if (line.substring(0, 3).equals(" * ")) {
                    if (!levelList[1]) {
                        levelList[1] = true;
                        levelList[0] = true;
                        newLine = "<ul>\n\t<li>";
                    } else {
                        newLine = "</li>\n\t<li>";
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
        }

        return output;
    }

    static String replaceURL(String url, String desc) {
        if (url.matches(".*\\.(mp3|ogg)$")) {
            String audioUrl = getAudio(url);
            desc = desc.replace(url, audioUrl);
        } else if (url.matches(".*\\.(jpg|jpeg|gif|png)$")) {
            desc = desc.replace(url, "<img src=\"" + url + "\" alt=\"" + url + "\" style=\"max-width: " + videoWidth + "px;\" />");
        } else if (url.matches(".*\\.(mp4|ogv|webm)$")) {
            String videoUrl = getvideo(url);
            desc = desc.replace(url, videoUrl);
        } else {
            String[] customLink = showCustomLinks(url, desc);
            desc = desc.replace(customLink[0], customLink[1]);
        }
        return desc;
    }

    static String[] showCustomLinks(String url, String inText) {
        String input = url;
        String replaceString = "<a href=\"" + url + "\">" + url + "</a>";
        if (inText.matches(".*\\[" + url + " *((?!\\]).)*\\].*")) {
            Pattern regex = Pattern.compile("\\[" + url + " *((?!\\]).)*\\]");
            Matcher results = regex.matcher(inText);
            while (results.find()) {
                String result = results.group();
                input = result;
                String content = result.replaceAll("\\[|\\]", "");
                String[] contentArray = content.split(" ");
                String target = contentArray[0];
                String linkText = "";
                if (contentArray.length > 1) {
                    for (int i = 1; i < contentArray.length; i++) {
                        linkText = linkText + " " + contentArray[i];
                    }
                    linkText = linkText.trim();
                } else {
                    linkText = target;
                }
                replaceString = "<a href=\"" + target + "\">" + linkText + "</a>";
            }
        }
        if (replacedURL.contains(url)) {
            replaceString = url;
        } else {
            replacedURL.add(url);
        }
        return new String[]{input, replaceString};
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

        // initiate & open DB
        openDB();
    }

    @Override
    protected String doInBackground(Void... arg0) {
        String desc = htmlString;

        // Search http(s) links
        Pattern urls = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
        Matcher urlsMatcher = urls.matcher(htmlString);
        while (urlsMatcher.find()) {
            String url = urlsMatcher.group();
            if (Util.isOnline(context)) {
                try {
                    Boolean error = false;
                    // Look in DB
                    Cursor cursor = urlDb.getRowURL(url);
                    JSONObject json = null;
                    if (cursor.moveToFirst()) {
                        json = new JSONObject(cursor.getString(DBAdapter.COL_JSON));
                    } else {
                        // encode url
                        String query = URLEncoder.encode(url, "utf-8");
                        String fullQuery = "https://oembedproxy.funraiders.io/?url=" + query;
                        // get the json file (as string)
                        HttpHandler sh = new HttpHandler();
                        String jsonStr = sh.makeServiceCall(fullQuery);
                        // convert string to json
                        if (jsonStr != null) {
                            json = new JSONObject(jsonStr);
                            // Save jsonString to DB
                            urlDb.insertRow(url, jsonStr);
                        } else {
                            error = true;
                        }
                    }
                    // replace the url by iframe
                    String htmlCode = (error) ? "Please insert correct URL" : json.getString("html");
                    if (!htmlCode.equals("Please insert correct URL")) {
                        htmlCode = htmlCode.replace("src=\"//", "src=\"https://");
                        // video / image resizing
                        if (json != null && !json.getString("provider_name").equals("Instagram") && !json.getString("provider_name").equals("Twitter")) {
                            int jsonWidth = json.getInt("width");
                            int jsonHeight = json.getInt("height");
                            float scaleX = videoWidth / jsonWidth;
                            float scaleY = videoHeight / jsonHeight;
                            float scale = Math.min(Math.min(scaleX, scaleY), 1);
                            int newWidth = Math.round(jsonWidth * scale);
                            int newHeight = Math.round(jsonHeight * scale);
                            htmlCode = htmlCode.replace("width=\"" + jsonWidth + "\"", "width=\"" + newWidth + "\"");
                            htmlCode = htmlCode.replace("height=\"" + jsonHeight + "\"", "height=\"" + newHeight + "\"");
                        }
                        // center iframe
                        htmlCode = "<center>" + htmlCode + "</center>"; // so ugly...
                        desc = desc.replace(url, htmlCode);
                    } else {
                        desc = replaceURL(url, desc);
                    }
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            } else {
                desc = replaceURL(url, desc);
            }
        }

        return desc;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onPostExecute(String desc) {
        super.onPostExecute(desc);
        String html = "<!DOCTYPE html><html lang=\"fr\"><head><meta charset=\"utf-8\"></head><body>" + desc + "</body></html>";
        // load html in the webview
        webV.loadData(html, "text/html; charset=UTF-8", null);
        pBar.setVisibility(View.GONE);
        // enable javascript
        WebSettings webSettings = webV.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // close DB
        clodeDB();
    }

    private void clodeDB() {
        urlDb.close();
    }

    private void openDB() {
        urlDb = new DBAdapter(context);
        urlDb.open();
    }

}
