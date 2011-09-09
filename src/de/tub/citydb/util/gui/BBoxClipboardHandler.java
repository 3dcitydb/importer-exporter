package de.tub.citydb.util.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BBoxClipboardHandler implements ClipboardOwner {
	private static BBoxClipboardHandler instance;

	private BBoxClipboardHandler() {
		// just to thwart instantiation
	}

	public static synchronized BBoxClipboardHandler getInstance() {
		if (instance == null)
			instance = new BBoxClipboardHandler();

		return instance;
	}

	public void putBoundingBox(double minX, double minY, double maxX, double maxY) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringBuilder content = new StringBuilder();
		content.append("BBOX=")
		.append(minX).append(",")
		.append(minY).append(",")
		.append(maxX).append(",")
		.append(maxY);

		clipboard.setContents(new StringSelection(content.toString()), this);
	}

	public double[] getBoundingBox() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
			try {
				Transferable content = clipboard.getContents(null);
				String bbox = (String)content.getTransferData(DataFlavor.stringFlavor);

				double[] result = parseWebServiceRepresentation(bbox);
				if (result != null)
					return result;

				result = parseGMLEnvelopeRepresentation(bbox);
				if (result != null)
					return result;

			} catch (Exception e) {
				//
			}
		}

		return null;
	}


	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// not necessary now
	}

	private double[] parseWebServiceRepresentation(String candidate) {	
		String separators = "(\\s*[,|;|\\s]\\s*?)";
		String value = "([-|\\+]?\\d*?(\\.\\d+?)??)";

		StringBuilder regex = new StringBuilder();
		regex.append("(?i)BBOX(?-i)\\s*=\\s*")		
		.append(value).append(separators)
		.append(value).append(separators)
		.append(value).append(separators)
		.append(value);

		Pattern pattern = Pattern.compile(regex.toString(), Pattern.MULTILINE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(candidate.trim());

		if (matcher.matches()) {
			try {
				double[] bbox = new double[4];
				NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);	

				bbox[0] = format.parse(matcher.group(1)).doubleValue();
				bbox[1] = format.parse(matcher.group(4)).doubleValue();
				bbox[2] = format.parse(matcher.group(7)).doubleValue();
				bbox[3] = format.parse(matcher.group(10)).doubleValue();

				return bbox;
			} catch (Exception e) {
				//
			}
		}

		return null;
	}

	private double[] parseGMLEnvelopeRepresentation(String candidate) {	
		String lowerCorner = "</?(.*?:)?lowerCorner>";
		String upperCorner = "</?(.*?:)?upperCorner>";

		StringBuilder regex = new StringBuilder();
		regex.append(".*?")
		.append(lowerCorner)
		.append("(.*?)")
		.append(lowerCorner)
		.append("(.*?)")
		.append(upperCorner)
		.append("(.*?)")
		.append(upperCorner)
		.append(".*?");

		Pattern pattern = Pattern.compile(regex.toString(), Pattern.MULTILINE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(candidate.trim());

		if (matcher.matches()) {
			try {
				double[] bbox = new double[4];
				NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);	

				String lowerCornerCoords = matcher.group(2);
				String upperCornerCoords = matcher.group(6);

				String value = "([-|\\+]?\\d*?(\\.\\d+?)??)";			

				regex = new StringBuilder();
				regex.append("\\s*").append(value)
				.append("\\s+").append(value)
				.append("(\\s+").append(value).append(")?")
				.append("\\s*");

				pattern = Pattern.compile(regex.toString(), Pattern.MULTILINE | Pattern.DOTALL);
				Matcher lower = pattern.matcher(lowerCornerCoords.trim());
				Matcher upper = pattern.matcher(upperCornerCoords.trim());

				if (lower.matches() && upper.matches()) {
					bbox[0] = format.parse(lower.group(1)).doubleValue();
					bbox[1] = format.parse(lower.group(3)).doubleValue();
					bbox[2] = format.parse(upper.group(1)).doubleValue();
					bbox[3] = format.parse(upper.group(3)).doubleValue();

					return bbox;
				}
			} catch (Exception e) {
				//
			}
		}

		return null;
	}

}
