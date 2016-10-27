package fr.ac_versailles.dane.xiaexpress;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import static fr.ac_versailles.dane.xiaexpress.dbg.pt;

/**
 * ShapeView.java
 * XiaExpress
 *
 * Created by guillaume on 27/10/2016.
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

public class ShapeView extends View {

    public Integer currentShapeType = 0;
    public Map<Integer, ImageView> arrayPoints = new HashMap<>();
    public float originX = 0;
    public float originY = 0;
    public int color = R.color.black;
    public Rect frame;
    public Integer shape;

    public ShapeView(Context context, Rect frame, Integer shape, Map<Integer, ImageView> points, int color) {
        super(context);
        this.currentShapeType = shape;
        this.arrayPoints = points;
        this.originX = frame.left;
        this.originY = frame.top;
        this.color = color;
        pt("Shape", "call shapeview", "shape type = " + currentShapeType);
        switch (currentShapeType) {
            case 0:
                drawLines();
                break;
            case 1:
                drawPolygon();
                break;
            case 2:
                drawEllipse();
                break;
            case 3:
                drawEllipseFilled();
                break;
            case 4:
                drawCircle();
                break;
        }
    }

    private void drawLines() {
        pt("Shape", "draw", "lines");
            /*
            let ctx = UIGraphicsGetCurrentContext()!

                    var beginPoint = arrayPoints[0]!.center
            beginPoint.x = beginPoint.x - origin.x
            beginPoint.y = beginPoint.y - origin.y
            let nbPoints = arrayPoints.count

            ctx.beginPath()
            ctx.move(to: CGPoint(x: beginPoint.x, y: beginPoint.y))
            for i in 1 ..< nbPoints {
                var point = arrayPoints[i]!.center
                point.x = point.x - origin.x
                point.y = point.y - origin.y
                ctx.addLine(to: CGPoint(x: point.x, y: point.y))
            }
            ctx.setLineDash(phase: 0, lengths: [5])
            let alphaColor = color.cgColor.copy(alpha: 0.8)
            ctx.setStrokeColor(alphaColor!)
            ctx.setLineWidth(2.5)

            ctx.closePath()
            ctx.strokePath()
            */
    }

    private void drawPolygon() {
        pt("Shape", "draw", "polygon");
            /*
            let ctx = UIGraphicsGetCurrentContext()!

                    var beginPoint = arrayPoints[0]!.center
            beginPoint.x = beginPoint.x - origin.x
            beginPoint.y = beginPoint.y - origin.y
            let nbPoints = arrayPoints.count

            ctx.beginPath()
            ctx.move(to: CGPoint(x: beginPoint.x, y: beginPoint.y))
            for i in 1 ..< nbPoints {
                var point = arrayPoints[i]!.center
                point.x = point.x - origin.x
                point.y = point.y - origin.y
                ctx.addLine(to: CGPoint(x: point.x, y: point.y))
            }
            ctx.setLineWidth(2)

            let semiRed = color.cgColor.copy(alpha: 0.5)
            ctx.setFillColor(semiRed!)
            ctx.fillPath()
            */
    }

    private void drawEllipse() {
        pt("Shape", "draw", "ellipse");
            /*
            let ctx = UIGraphicsGetCurrentContext()!

                    ctx.setLineDash(phase: 0, lengths: [5])
            let alphaColor = color.cgColor.copy(alpha: 0.8)
            ctx.setStrokeColor(alphaColor!)
            ctx.setLineWidth(2.5)

            let size = CGSize(width: arrayPoints[1]!.center.x - arrayPoints[3]!.center.x, height: arrayPoints[2]!.center.y - arrayPoints[0]!.center.y)

            let rectangle = CGRect(x: 5, y: 5, width: abs(size.width), height: abs(size.height))
            ctx.addEllipse(in: rectangle)
            ctx.strokePath()
            */
    }

    private void drawEllipseFilled() {
        pt("Shape", "draw", "ellipse filled");
            /*
            let size = CGSize(width: arrayPoints[1]!.center.x - arrayPoints[3]!.center.x, height: arrayPoints[2]!.center.y - arrayPoints[0]!.center.y)

            let ovalPath = UIBezierPath(ovalIn: CGRect(x: 5, y: 5, width: abs(size.width), height: abs(size.height)))
            color.withAlphaComponent(0.5).setFill()
            ovalPath.fill()
            */
    }

    private void drawCircle() { // not yet used
        pt("Shape", "draw", "circle");
            /*
            let center = CGPoint(x: self.frame.size.width / 2.0, y: self.frame.size.height / 2.0)
            let ctx = UIGraphicsGetCurrentContext()!
                    ctx.beginPath()

            ctx.setLineWidth(1)

            let x:CGFloat = center.x
            let y:CGFloat = center.y
            let radius: CGFloat = 9.0
            let endAngle: CGFloat = CGFloat(2 * M_PI)

            ctx.addArc(center: CGPoint(x: x, y: y), radius: radius, startAngle: 0, endAngle: endAngle, clockwise: false)

            ctx.setFillColor(UIColor.blue.cgColor)

            ctx.strokePath()
            */
    }
}
