package brx;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Test {
	
	public static void main(String[] args) {
		
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		Document xml = null;
		try {
			docBuilder = dbfac.newDocumentBuilder();
			xml = docBuilder.parse("C:\\Users\\vlad.dima@metrosystems.net\\Desktop\\CustomerCreditData_09.xml");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Parser brx = new Parser(xml, 34, 34201, "HU", "3480", "HU12345678", "Test");
		
		System.out.println(brx.generateRow4Simulator());
		System.out.println("---");
//		System.out.println(brx.generateSql4MdwOld());
		System.out.println("---");
		System.out.println(brx.generateSql4MdwNew());
	}

}
