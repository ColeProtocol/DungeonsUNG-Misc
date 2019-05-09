package display;

import java.awt.Color;

public class WordProcessing { // Refactor name if you have something better.

	public static String rainbowfy(String str) { // Makes each color in a string a fading color, randomly.
		String output = "";
		int offset = (int)(Math.random()*255);
		int speed = 5;
		int r, g, b;
		// Process each character.
		for (int i=0; i<str.toCharArray().length; i++) {
			char c = str.toCharArray()[i];
			String hexColor = "";
			/*
			// Use index, + basevalue to get a color.
			r = (int)(64*Math.acos(Math.cos((Math.PI/ 17 ) *(i+offset) ))); //80
			g = (int)(128*Math.sin((Math.PI/ 13 ) *(i+offset) )+127); //255
			b = (int)(64*Math.acos(Math.cos((Math.PI/ 9 ) *(i+offset) ))); //(int)(64*Math.acos(Math.cos((Math.PI/ ((100*Math.random())+155))*(i+offset)))) //(Math.cos((Math.PI*255)*(i+offset)))
			*/
			Color rgb= Color.getHSBColor(i/((float)str.length()), 1, 1);
			//Display.println("" + Integer.toBinaryString(Color.HSBtoRGB(i, 50, 50)));
			//hexColor = Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b);
			//hexColor = Integer.toHexString(rgb.getRed()) + Integer.toHexString(rgb.getGreen()) + Integer.toHexString(rgb.getBlue());
			//System.out.println(i/((float)str.length()) + " " + rgb + "\t" + hexColor);
			
			// Convert color from RGB to hex string.
			String hexR = Integer.toHexString(rgb.getRed());
			if (hexR.length() == 2) 
				hexColor += hexR;
			else if (hexR.length() < 2)
				hexColor += "0" + hexR;
			else 
				hexColor += hexR.substring(0, 1);
			
			String hexG = Integer.toHexString(rgb.getGreen());
			if (hexG.length() == 2) 
				hexColor += hexG;
			else if (hexG.length() < 2)
				hexColor += "0" + hexG;
			else 
				hexColor += hexG.substring(0, 1);
			
			String hexB = Integer.toHexString(rgb.getBlue());
			if (hexB.length() == 2) 
				hexColor += hexB;
			else if (hexB.length() < 2)
				hexColor += "0" + hexB;
			else 
				hexColor += hexB.substring(0, 1);
			
			
			//hexColor
			

			//Display.debug(hexColor);
			
			output += "<font color="+ hexColor+">"+c+"</font>";
		}
		return output;
	}
}
