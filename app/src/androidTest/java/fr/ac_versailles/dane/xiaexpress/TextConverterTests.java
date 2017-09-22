package fr.ac_versailles.dane.xiaexpress;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * TextConverterTests.java
 * XiaExpress
 * <p>
 * Created by guillaume on 22/09/2017.
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

@RunWith(AndroidJUnit4.class)
public class TextConverterTests {
    public TextConverterTests() {
        // init
    }

    @Test
    public void testTextConverterGetAudio() throws Exception {
        String output1 = TextConverter.getAudio("http://example.com/audio.mp3");

        String expectedOutput1 = "<center><audio controls>" +
                "   <source type=\"audio/mpeg\" src=\"http://example.com/audio.mp3\" />" +
                "   <source type=\"audio/ogg\" src=\"http://example.com/audio.ogg\" />" +
                "</audio></center>";

        assertEquals(expectedOutput1, output1);

        String output2 = TextConverter.getAudio("http://example.com/audio.ogg");

        String expectedOutput2 = "<center><audio controls>" +
                "   <source type=\"audio/mpeg\" src=\"http://example.com/audio.mp3\" />" +
                "   <source type=\"audio/ogg\" src=\"http://example.com/audio.ogg\" />" +
                "</audio></center>";

        assertEquals(expectedOutput2, output2);
    }

    @Test
    public void testTextConverterGetVideo() throws Exception {
        Float width = 480f;
        Float height = 270f;

        String output1 = TextConverter.getvideo("http://example.com/audio.mp4");

        String expectedOutput1 = "<center><video controls preload=\"none\" width=\"" + width + "\" height=\"" + height + ">" +
                "   <source type=\"video/mp4\" src=\"http://example.com/audio.mp4\" />" +
                "   <source type=\"video/ogg\" src=\"http://example.com/audio.ogv\" />" +
                "   <source type=\"video/webm\" src=\"http://example.com/audio.webm\" />" +
                "</video></center>";

        assertEquals(expectedOutput1, output1);

        String output2 = TextConverter.getvideo("http://example.com/audio.ogv");

        String expectedOutput2 = "<center><video controls preload=\"none\" width=\"" + width + "\" height=\"" + height + ">" +
                "   <source type=\"video/mp4\" src=\"http://example.com/audio.mp4\" />" +
                "   <source type=\"video/ogg\" src=\"http://example.com/audio.ogv\" />" +
                "   <source type=\"video/webm\" src=\"http://example.com/audio.webm\" />" +
                "</video></center>";

        assertEquals(expectedOutput2, output2);

        String output3 = TextConverter.getvideo("http://example.com/audio.webm");

        String expectedOutput3 = "<center><video controls preload=\"none\" width=\"" + width + "\" height=\"" + height + ">" +
                "   <source type=\"video/mp4\" src=\"http://example.com/audio.mp4\" />" +
                "   <source type=\"video/ogg\" src=\"http://example.com/audio.ogv\" />" +
                "   <source type=\"video/webm\" src=\"http://example.com/audio.webm\" />" +
                "</video></center>";

        assertEquals(expectedOutput3, output3);
    }

    @Test
    public void testTextConverterShowCustomLinks() throws Exception {
        String desc = "blabla [http://example.com/] bla";
        String url = "http://example.com/";
        String[] customLink = TextConverter.showCustomLinks(url, desc);
        String output1 = desc.replace(customLink[0], customLink[1]);

        String expectedOutput1 = "blabla <a href=\"http://example.com/\">http://example.com/</a> bla";

        assertEquals(expectedOutput1, output1);

        String desc2 = "blabla [http://example.com un lien] bla";
        String url2 = "http://example.com";
        String[] customLink2 = TextConverter.showCustomLinks(url2, desc2);
        String output2 = desc2.replace(customLink2[0], customLink2[1]);

        String expectedOutput2 = "blabla <a href=\"http://example.com\">un lien</a> bla";

        assertEquals(expectedOutput2, output2);
    }

    @Test
    public void testTextConverterImages() throws Exception {
        String[] exts = new String[]{"jpg", "jpeg", "gif", "png"};

        for (String ext : exts) {
            String desc = "blabla http://example.com/photo." + ext + " bla";
            String url = "http://example.com/photo." + ext;
            String output = TextConverter.replaceURL(url, desc);

            String expectedOutput = "blabla <img src=\"" + url + "\" alt=\"" + url + "\" style=\"max-width: 480.0px;\" /> bla";

            assertEquals(expectedOutput, output);
        }
    }

    @Test
    public void testTextConverterPikipiki1() throws Exception {
        String output = TextConverter.pikipikiToHTML("**text**");
        String expectedOutput = "<em>text</em>";

        assertEquals(expectedOutput, output);
    }

    @Test
    public void testTextConverterPikipiki2() throws Exception {
        String output = TextConverter.pikipikiToHTML("***text***");
        String expectedOutput = "<b>text</b>";

        assertEquals(expectedOutput, output);
    }

    @Test
    public void testTextConverterPikipiki3() throws Exception {
        String output = TextConverter.pikipikiToHTML("{{{text}}}");
        String expectedOutput = "<pre>text</pre>";

        assertEquals(expectedOutput, output);
    }

    @Test
    public void testTextConverterPikipiki4() throws Exception {
        String output = TextConverter.pikipikiToHTML("----");
        String expectedOutput = "<hr/>";

        assertEquals(expectedOutput, output);
    }

    @Test
    public void testTextConverterPikipiki5() throws Exception {
        String output = TextConverter.pikipikiToHTML("-----");
        String expectedOutput = "<hr size=3/>";

        assertEquals(expectedOutput, output);
    }
}
