package co.herxun.impp.utils;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Xml;

public class Utils {

	public static int px2Dp(Context ct,int px){
		return (int) (px * ct.getResources().getDisplayMetrics().density);
	}
	
	public AttributeSet getEdittextAttr(Context ct){
		try{
			int res = ct.getResources().getIdentifier("my_edittext", "layout", ct.getPackageName());
			XmlPullParser parser = ct.getResources().getXml(res);
			int state=0;
			do {
			    try {
			        state = parser.next();
			    } catch (Exception e1) {
			        e1.printStackTrace();
			    }       
			    if (state == XmlPullParser.START_TAG) {
			        if (parser.getName().equals("EditText")) {
			            return  Xml.asAttributeSet(parser);
			        }
			    }
			} while(state != XmlPullParser.END_DOCUMENT);
	        return null;
		}catch(Exception e){
			e.printStackTrace();
	        return null;
		}
		
	}
}
