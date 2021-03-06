//
//  GDInterface.java
//  Java Graphics Device
//
//  Created by Simon Urbanek on Thu Aug 05 2004.
//  Copyright (c) 2004-2012 Simon Urbanek. All rights reserved.
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation;
//  version 2.1 of the License.
//  
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//  
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//

package org.rosuda.javaGD;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.lang.reflect.Method;


public class GDContainerGD extends GDInterface {
	
    /** container that will receive all drawing methods. It should be created by subclasses in the {@link #gdOpen} method. */
    public GDContainer c=null;
    
    /** synchronization object for locator calls */
    public LocatorSync ls=null;
	
	
	/** 
	 * Opens the new device with the specified size
	 * @param devNr device number
	 * @param w width of the device
	 * @param h height of the device */
	@Override
	public void     gdOpen(int devNr) {
		super.gdOpen(devNr);
		
		if (c != null) {
			c.setDeviceNumber(devNr);
		}
	}
	
    /** draw a circle
     *  @param x x coordinate of the center
     *  @param y y coordinate of the center
     *  @param r radius */
    @Override
    public void     gdCircle(double x, double y, double r) {
        if (c==null) return;
        c.add(new GDCircle(x,y,r));
    }

    /** clip drawing to the specified region
     *  @param x0 left coordinate
     *  @param x1 right coordinate
     *  @param y0 top coordinate
     *  @param y1 bottom coordinate */
    @Override
    public void     gdClip(double x0, double x1, double y0, double y1) {
        if (c==null) return;
        c.add(new GDClip(x0, y0, x1, y1));
    }

    /** close the display */
    @Override
    public void     gdClose() {
        super.gdClose();
        if (c!=null) c.closeDisplay();
    }

    /** invoke the locator
     *  @return array of indices or <code>null</code> is cancelled */
    @Override
    public double[] gdLocator() {
	if (c==null) return null;
	if (ls==null) ls=new LocatorSync();
	if (!c.prepareLocator(ls)) return null;
        return ls.waitForAction();
    }

    /** draw a line
     *  @param x1 x coordinate of the origin
     *  @param y1 y coordinate of the origin
     *  @param x2 x coordinate of the end
     *  @param y2 y coordinate of the end */
    public void     gdLine(double x1, double y1, double x2, double y2) {
        if (c==null) return;
        c.add(new GDLine(x1, y1, x2, y2));
    }

    /** retrieve font metrics info for the given unicode character
     *  @param ch character (encoding may depend on the font type)
     *  @return an array consisting for three doubles: ascent, descent and width */
    public double[] gdMetricInfo(int ch) {
        double[] res=new double[3];
        double ascent=0.0, descent=0.0, width=8.0;
        if (c!=null) {
            Graphics g=c.getGraphics();
            if (g!=null) {
                Font f=c.getGState().f;
                if (f!=null) {
                    FontMetrics fm=g.getFontMetrics(c.getGState().f);
                    if (fm!=null) {
                        ascent=(double) fm.getAscent();
                        descent=(double) fm.getDescent();
                        width=(double) fm.charWidth((ch==0)?77:ch);
                    }
                }
            }
        }
        res[0]=ascent; res[1]=descent; res[2]=width;
        return res;
    }

    /** R signalled a mode change
     *  @param mode mode as signalled by R (currently 0=R stopped drawing, 1=R started drawing, 2=graphical input exists) */
    public void     gdMode(int mode) {
        if (c!=null) c.syncDisplay(mode==0);
    }

    /** create a new, blank page 
     *  @param devNr device number assigned to this device by R */
    public void     gdNewPage() {
        if (c!=null) {
            c.reset();
        }
    }

    @Override
    public void     gdPath(int nPoly, int[] nPer, double[] x, double[] y, int mode) {
        if (c==null) return;
//        c.add(new GDPolygon(n, x, y, false));
    }

    @Override
    public void     gdPolygon(int n, double[] x, double[] y) {
        if (c==null) return;
        c.add(new GDPolygon(n, x, y, false));
    }

    @Override
    public void     gdPolyline(int n, double[] x, double[] y) {
        if (c==null) return;
        c.add(new GDPolygon(n, x, y, true));
    }

    @Override
    public void     gdRect(double x0, double y0, double x1, double y1) {
        if (c==null) return;
        c.add(new GDRect(x0, y0, x1, y1));
    }

    /** retrieve the current size of the device
     *  @return an array of four doubles: 0, width, height, 0 */
    @Override
    public double[] gdSize() {
        double[] res=new double[4];
        double width=0d, height=0d;
        if (c != null) {
            Dimension d = c.getSize();
            width = d.getWidth();
            height = d.getHeight();
        }
        res[0]=0d;
        res[1]=width - 1;
        res[2]=height - 1;
        res[3]=0;
        return res;
    }

    /** retrieve width of the given text when drawn in the current font
     *  @param str text
     *  @return width of the text */
    @Override
    public double   gdStrWidth(String str) {
        double width=(double)(8*str.length()); // rough estimate
        if (c!=null) { // if canvas is active, we can do better
            Graphics g=c.getGraphics();
            if (g!=null) {
                Font f=c.getGState().f;
                if (f!=null) {
                    FontMetrics fm=g.getFontMetrics(f);
                    if (fm!=null) width=(double)fm.stringWidth(str);
                }
            }
        }
        return width;
    }

    /** draw text
     *  @param x x coordinate of the origin
     *  @param y y coordinate of the origin
     *  @param str text to draw
     *  @param rot rotation (in degrees)
     *  @param hadj horizontal adjustment with respect to the text size (0=left-aligned wrt origin, 0.5=centered, 1=right-aligned wrt origin) */
    @Override
    public void     gdText(double x, double y, String str, double rot, double hadj) {
        if (c==null) return;
        c.add(new GDText(x, y, rot, hadj, str));
    }

    @Override
    public void     gdRaster(byte[] img, boolean imgAlpha, int img_w, int img_h, double x, double y, double w, double h, double rot, boolean interpolate) {
        if (c == null) return;
        c.add(new GDRaster(img, img_w, img_h, x, y, w, h, rot, interpolate));
    }

    @Override
    public byte[] gdCap(int[] dim) {
        return null;
    }
    
    /*-- GDC - manipulation of the current graphics state */
    /** set drawing color
     *  @param cc color */
    @Override
    public void gdcSetColor(int cc) {
        if (c==null) return;
        c.add(new GDColor(cc));
    }

    /** set fill color
     *  @param cc color */
    @Override
    public void gdcSetFill(int cc) {
        if (c==null) return;
        c.add(new GDFill(cc));
    }

    /** set line width and type
     *  @param lwd line width (see <code>lwd</code> parameter in R)
     *  @param lty line type (see <code>lty</code> parameter in R) */
    @Override
    public void gdcSetLine(double lwd, int lty) {
        if (c==null) return;
        c.add(new GDLinePar(lwd, lty));
    }

    /** set current font
     *  @param cex character expansion (see <code>cex</code> parameter in R)
     *  @param ps point size (see <code>ps</code> parameter in R - for all practical purposes the requested font size in points is <code>cex * ps</code>)
     *  @param lineheight line height
     *  @param fontface font face (see <code>font</code> parameter in R: 1=plain, 2=bold, 3=italic, 4=bold-italic, 5=symbol)
     *  @param fontfamily font family (see <code>family</code> parameter in R) */
    @Override
    public void gdcSetFont(double cex, double ps, double lineheight, int fontface, String fontfamily) {
        if (c==null) return;
        GDFont f=new GDFont(cex, ps, lineheight, fontface, fontfamily);
        c.add(f);
        c.getGState().f=f.getFont();
    }
	
    /** close the device in R associted with this instance */
    public void executeDevOff() {
        if (c==null || c.getDeviceNumber()<0) return;
        try { // for now we use no cache - just pure reflection API for: Rengine.getMainEngine().eval("...")
            Class cl=Class.forName("org.rosuda.JRI.Rengine");
            if (cl==null)
                System.out.println(">> can't find Rengine, close function disabled. [c=null]");
            else {
                Method m=cl.getMethod("getMainEngine",null);
                Object o=m.invoke(null,null);
                if (o!=null) {
                    Class[] par=new Class[1];
                    par[0]=Class.forName("java.lang.String");
                    m=cl.getMethod("eval",par);
                    Object[] pars=new Object[1];
                    pars[0]="try({ dev.set("+(c.getDeviceNumber()+1)+"); dev.off()},silent=TRUE)";
                    m.invoke(o, pars);
                }
            }
        } catch (Exception e) {
            System.out.println(">> can't find Rengine, close function disabled. [x:"+e.getMessage()+"]");
        }
    }    
}
