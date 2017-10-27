package fr.ac_versailles.dane.xiaexpress;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import co.ceryle.segmentedbutton.SegmentedButtonGroup;

import static fr.ac_versailles.dane.xiaexpress.Constants.xmlElementsDict;

/**
 * Metas.java
 * XiaExpress
 *
 * Created by guillaume on 02/06/2017.
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

public class Metas extends AppCompatActivity {

    private final Calendar myCalendar = Calendar.getInstance();
    private Document xml;
    private String fileTitle;
    private String xmlDirectory;
    private TextView title;
    private EditText metasDate;
    final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }
    };
    private Spinner metasLicence;
    private Switch ReadOnly;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metas);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels * 8 / 10;
        int height = metrics.heightPixels * 8 / 10;

        // Gets linearlayout
        LinearLayout layout = findViewById(R.id.activity_metas);
        // Gets the layout params that will allow you to resize the layout
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        // Changes the height and width to the specified *pixels*
        params.height = height;
        params.width = width;

        String rootDirectory = String.valueOf(getExternalFilesDir(null)) + File.separator;
        xmlDirectory = Constants.getXMLFrom(rootDirectory);
        Constants.buildXMLElements(this);

        fileTitle = getIntent().getStringExtra("fileTitle");

        xml = Util.getXMLFromPath(xmlDirectory + fileTitle + Constants.XML_EXTENSION);

        // Load subviews
        final LinearLayout metas1 = findViewById(R.id.metas1);
        final LinearLayout metas2 = findViewById(R.id.metas2);
        final LinearLayout metas3 = findViewById(R.id.metas3);
        final LinearLayout metasimginfos = findViewById(R.id.metasimginfos);
        metas1.setVisibility(View.VISIBLE);
        metas2.setVisibility(View.GONE);
        metas3.setVisibility(View.GONE);
        metasimginfos.setVisibility(View.GONE);

        title = findViewById(R.id.Title);

        // First subview
        final EditText metasTitle = findViewById(R.id.metasTitle);
        final EditText metasDescription = findViewById(R.id.metasDescription);
        setNextFocus(metasTitle, metasDescription);
        ReadOnly = findViewById(R.id.readOnly);
        String roStatus = Util.getNodeValue(xml, "xia/readonly");
        ReadOnly.setChecked(roStatus.equals("true"));
        ReadOnly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showMyDialog(ReadOnly.isChecked(), "");
            }
        });
        final Switch ShowDetails = findViewById(R.id.showDetails);
        String sdStatus = Util.getNodeAttribute(xml, "details", "show");
        ShowDetails.setChecked(sdStatus.equals("true"));

        // Second subview
        final EditText Creator = findViewById(R.id.metasCreator);
        final EditText Rights = findViewById(R.id.metasRights);
        setNextFocus(Creator, Rights);
        final EditText Publisher = findViewById(R.id.metasPublisher);
        setNextFocus(Rights, Publisher);
        final EditText Identifier = findViewById(R.id.metasIdentifier);
        setNextFocus(Publisher, Identifier);
        final EditText Source = findViewById(R.id.metasSource);
        setNextFocus(Identifier, Source);
        metasDate = findViewById(R.id.metasDate);

        metasDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(Metas.this, date, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Third view
        final EditText Language = findViewById(R.id.metasLanguage);
        final EditText Keywords = findViewById(R.id.metasKeywords);
        setNextFocus(Language, Keywords);
        final EditText Contributors = findViewById(R.id.metasContributors);
        setNextFocus(Keywords, Contributors);
        final EditText Relation = findViewById(R.id.metasRelation);
        setNextFocus(Contributors, Relation);
        final EditText Coverage = findViewById(R.id.metasCoverage);
        setNextFocus(Relation, Coverage);
        metasLicence = findViewById(R.id.metasLicense);

        // Fourth view
        final EditText ImgTitle = findViewById(R.id.metasImgTitle);
        ImgTitle.setText(Util.getNodeAttribute(xml, "image", "title"));
        final EditText ImgDescription = findViewById(R.id.metasImgDescription);
        ImgDescription.setText(Util.getNodeAttribute(xml, "image", "description"));
        setNextFocus(ImgTitle, ImgDescription);

        // Get metadatas from xml
        for (Map.Entry<String, String> entry : xmlElementsDict.entrySet()) {
            String key = entry.getKey();
            setStoredText(key);
        }

        SegmentedButtonGroup segmentedButtonGroup = findViewById(R.id.segmentedButtonGroup);
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

        final View.OnClickListener cancelListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
        final View.OnClickListener doneListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the detail in xml
                xml = Util.setNodeValue(xml, "title", metasTitle.getText().toString());
                xml = Util.setNodeValue(xml, "description", metasDescription.getText().toString());
                xml = Util.setNodeValue(xml, "creator", Creator.getText().toString());
                xml = Util.setNodeValue(xml, "rights", Rights.getText().toString());
                xml = Util.setNodeValue(xml, "publisher", Publisher.getText().toString());
                xml = Util.setNodeValue(xml, "identifier", Identifier.getText().toString());
                xml = Util.setNodeValue(xml, "source", Source.getText().toString());
                xml = Util.setNodeValue(xml, "relation", Relation.getText().toString());
                xml = Util.setNodeValue(xml, "language", Language.getText().toString());
                xml = Util.setNodeValue(xml, "keywords", Keywords.getText().toString());
                xml = Util.setNodeValue(xml, "coverage", Coverage.getText().toString());
                xml = Util.setNodeValue(xml, "contributors", Contributors.getText().toString());
                xml = Util.setNodeValue(xml, "date", metasDate.getText().toString());
                xml = Util.setNodeValue(xml, "license", metasLicence.getSelectedItem().toString());
                xml = Util.setNodeAttribute(xml, "details", "show", String.valueOf(ShowDetails.isChecked()));
                xml = Util.setNodeAttribute(xml, "image", "title", ImgTitle.getText().toString());
                xml = Util.setNodeAttribute(xml, "image", "description", ImgDescription.getText().toString());

                Util.writeXML(xml, xmlDirectory + fileTitle + Constants.XML_EXTENSION);
                finish();
            }
        };

        Button btnDone = findViewById(R.id.done);
        Button btnCancel = findViewById(R.id.cancel);

        btnDone.setOnClickListener(doneListener);
        btnCancel.setOnClickListener(cancelListener);

    }

    private void updateLabel() {

        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRENCH);

        metasDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void setNextFocus(View source, View target) {
        source.setNextFocusDownId(target.getId());
        source.setNextFocusRightId(target.getId());
        target.setNextFocusLeftId(source.getId());
    }

    private void setStoredText(String element) {
        String id = "metas" + element.substring(0, 1).toUpperCase() + element.substring(1).toLowerCase();
        String text = Util.getNodeValue(xml, "xia/" + element);

        if (element.equals("license")) {
            if (text.length() > 0) {
                String[] licenses = getResources().getStringArray(R.array.licenses_array);
                for (int i = 0; i < licenses.length; i++) {
                    if (text.equals(licenses[i])) {
                        metasLicence.setSelection(i);
                        break;
                    } else {
                        metasLicence.setSelection(4); // CC Attribution-NonCommercial - CC-BY-NC
                    }
                }
            } else {
                metasLicence.setSelection(4); // CC Attribution-NonCommercial - CC-BY-NC
            }
        } else {
            EditText docElement = findViewById(getResources().getIdentifier(id, "id", getPackageName()));
            if (text.length() > 0) {
                if (element.equals("date")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date mDate = null;
                    try {
                        mDate = sdf.parse(text);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // Set text
                    text = DateFormat.getDateInstance(DateFormat.SHORT).format(mDate);
                    metasDate.setText(text);
                    // prepare datepicker
                    SimpleDateFormat dfy = new SimpleDateFormat("yyyy");
                    int year = Integer.parseInt(dfy.format(mDate));
                    myCalendar.set(Calendar.YEAR, year);
                    SimpleDateFormat dfm = new SimpleDateFormat("MM");
                    int month = Integer.parseInt(dfm.format(mDate)) - 1;
                    myCalendar.set(Calendar.MONTH, month);
                    SimpleDateFormat dfd = new SimpleDateFormat("dd");
                    int day = Integer.parseInt(dfd.format(mDate));
                    myCalendar.set(Calendar.DAY_OF_MONTH, day);

                } else {
                    docElement.setText(text);
                }
                if (element.equals("title")) {
                    title.setText(text);
                }
            }
        }
    }

    private void showMyDialog(final Boolean createPass, final String previousPass) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(Metas.this);
        final View promptView = li.inflate(R.layout.prompt, null);
        final TextView roText = promptView.findViewById(R.id.textView1);
        String roAction;
        if (createPass) {
            roAction = (previousPass.equals("")) ?
                    getResources().getString(R.string.create_code) :
                    getResources().getString(R.string.double_check);
        } else {
            roAction = getResources().getString(R.string.enter_code);
        }

        roText.setText(roAction);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Metas.this);

        // set prompt.xml to alertdialog builder
        alertDialogBuilder.setView(promptView);

        final EditText userInput = promptView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final String currentPass = userInput.getText().toString();
                                if (currentPass.equals("")) {
                                    ReadOnly.setChecked(!ReadOnly.isChecked());
                                    dialog.cancel();
                                } else {
                                    if (!createPass) { // remove the old password
                                        if (currentPass.equals(Util.getNodeAttribute(xml, "readonly", "code"))) { // good password
                                            ReadOnly.setChecked(false); // disable RO
                                            xml = Util.setNodeValue(xml, "readonly", "false");
                                            xml = Util.setNodeAttribute(xml, "readonly", "code", "");
                                            Util.writeXML(xml, xmlDirectory + fileTitle + Constants.XML_EXTENSION);
                                        } else { // Toast error !
                                            Toast.makeText(Metas.this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                                            ReadOnly.setChecked(true);
                                            showMyDialog(false, "");
                                        }
                                    } else { // create password
                                        if (previousPass.equals("")) { // need to check the password
                                            showMyDialog(true, currentPass);
                                        } else {
                                            if (currentPass.equals(previousPass)) { // ok we need to store this password
                                                xml = Util.setNodeValue(xml, "readonly", "true");
                                                xml = Util.setNodeAttribute(xml, "readonly", "code", currentPass);
                                                Util.writeXML(xml, xmlDirectory + fileTitle + Constants.XML_EXTENSION);
                                            }
                                        }
                                    }
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ReadOnly.setChecked(!ReadOnly.isChecked());
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
