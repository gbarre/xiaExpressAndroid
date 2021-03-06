package fr.ac_versailles.dane.xiaexpress;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Document;

import java.util.ArrayList;

/**
 *
 *  GridViewAdapter.java
 *  XiaExpress
 *
 *  Created by guillaume on 29/09/2016.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 *  @author : guillaume.barre@ac-versailles.fr
 */

class GridViewAdapter extends ArrayAdapter<PhotoThumbnail> {

    private final Context context;
    private final int layoutResourceId;
    private final String xmlDirectory;
    private ArrayList<PhotoThumbnail> data = new ArrayList<>();
    private Boolean isEmpty = true;

    public GridViewAdapter(Context context, int layoutResourceId, ArrayList<PhotoThumbnail> data, String xmlDir) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.xmlDirectory = xmlDir;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        PhotoThumbnail item = data.get(position);
        String title;
        if (!isEmpty) {
            String filename = item.getFilename().replace(Constants.JPG_EXTENSION, "");
            Document xml = Util.getXMLFromPath((xmlDirectory + filename) + Constants.XML_EXTENSION);
            title = (Util.getNodeValue(xml, "xia/title").equals("")) ? filename : Util.getNodeValue(xml, "xia/title");
            if (title.length() > 35) {
                title = title.substring(0, 35) + "...";
            }
        } else {
            title = context.getResources().getString(R.string.new_resource);
        }

        holder.imageTitle.setText(title);
        holder.image.setImageBitmap(item.getImage());
        return row;
    }

    void deleteItem(int position) {
        data.remove(position);
    }

    Boolean getEmpty() {
        return isEmpty;
    }

    void setEmpty(Boolean value) {
        isEmpty = value;
    }

    int getSize() {
        return data.size();
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }

}
