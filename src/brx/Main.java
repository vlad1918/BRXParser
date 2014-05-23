package brx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class Main extends JPanel implements ActionListener {

	private static final long serialVersionUID = 5L;
	private JPanel jpMain;
	private JTextField txStoreNo, txCustNo, txCountryCd, txCountryNb, txComment, txTaxNo;
	private File xmlFile;
	private JLabel lbStatus;
	private JTextArea txMsb, txMdw;
	private JButton btnGenerateOld, btnGenerateNew, btnFile;
	
	/**
	 * 
	 */
	public Main() {
			
		jpMain = this;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		/**
		 * Create and add input panel
		 */
		JPanel jpInput = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel jpInputMatrix = new JPanel(new SpringLayout());
		jpInput.setBorder(BorderFactory.createTitledBorder("Input data"));
		
		JLabel lbFileUp 	= new JLabel("BRM Test XML File");
		JLabel lbStoreNo 	= new JLabel("StoreNo");
		JLabel lbCustNo 	= new JLabel("CustNo");		
		JLabel lbCountryCd  = new JLabel("CountryCdString");
		JLabel lbCountryNb  = new JLabel("CountryCdNumber");
		JLabel lbTaxNo 	    = new JLabel("TaxNo");
		JLabel lbComment    = new JLabel("Comment");

		btnFile		= new JButton("Choose XML file");
		btnFile.addActionListener(this);
		txStoreNo 	= new JTextField(3);
		txCustNo 	= new JTextField(8);
		txCountryCd = new JTextField(2);
		txCountryNb = new JTextField(4);
		txTaxNo 	= new JTextField(10);
		txComment 	= new JTextField(20);
		
		jpInputMatrix.add(lbFileUp); 	
		jpInputMatrix.add(lbStoreNo); 	
		jpInputMatrix.add(lbCustNo); 	
		jpInputMatrix.add(lbCountryCd);
		jpInputMatrix.add(lbCountryNb);
		jpInputMatrix.add(lbTaxNo); 	  
		jpInputMatrix.add(lbComment);  
		jpInputMatrix.add(btnFile);		
		jpInputMatrix.add(txStoreNo); 	
		jpInputMatrix.add(txCustNo); 	
		jpInputMatrix.add(txCountryCd);
		jpInputMatrix.add(txCountryNb);
		jpInputMatrix.add(txTaxNo); 	
		jpInputMatrix.add(txComment);		
		
		SpringUtilities.makeCompactGrid(jpInputMatrix, 2, 7, 0, 0, 7, 3);		
		jpInput.add(jpInputMatrix);
		this.add(jpInput);

		/**
		 * Create and add output panel
		 */
		JPanel jpOutput = new JPanel();
		jpOutput.setLayout(new BoxLayout(jpOutput, BoxLayout.PAGE_AXIS));
				
		txMsb = new JTextArea(15, 50);
		txMdw = new JTextArea(15, 50);
		
		JScrollPane scMsb = new JScrollPane(txMsb);
		JScrollPane scMdw = new JScrollPane(txMdw);
		
		scMsb.setBorder(BorderFactory.createTitledBorder("MSB Simulator row"));
		scMdw.setBorder(BorderFactory.createTitledBorder("MDW SQL query"));		
		
		jpOutput.add(scMsb);
		jpOutput.add(Box.createRigidArea(new Dimension(10, 10)));
		jpOutput.add(scMdw);
		this.add(Box.createRigidArea(new Dimension(10, 10)));
		this.add(jpOutput);

		/**
		 * Create and add status panel
		 */
		JPanel jpStatus = new JPanel();
		lbStatus = new JLabel(" ");
		
		jpStatus.add(lbStatus);
		this.add(jpStatus);
		
		/**
		 * Create and add buttons panel
		 */
		JPanel jpButtons = new JPanel();		
		
		btnGenerateOld = new JButton("Generate for old interface");
		btnGenerateOld.addActionListener(this);
		btnGenerateOld.setEnabled(false);		
		
		btnGenerateNew = new JButton("Generate for new interface");
		btnGenerateNew.addActionListener(this);
		
		jpButtons.add(btnGenerateOld);	
		jpButtons.add(btnGenerateNew);
		this.add(jpButtons);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource().equals(btnFile)) {
	        
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(jpMain);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            xmlFile = fc.getSelectedFile();
	        }
		}
		
		else if (e.getSource().equals(btnGenerateOld) || e.getSource().equals(btnGenerateNew)) {
			
			//Validate the input fields
			if (xmlFile==null) {
				lbStatus.setText("Please choose a test XML file from BRM");
				lbStatus.setForeground(Color.RED);
			}			
			else if (txStoreNo==null || txCustNo==null || txCountryCd==null || txCountryNb==null || txTaxNo==null || txComment==null) {
				lbStatus.setText("Please complete all the input fields");
				lbStatus.setForeground(Color.RED);
			}
			else { //If all inputs are valid then start parsing
				
				DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = null;
				Document xml = null;
				try {
					docBuilder = dbfac.newDocumentBuilder();
					xml = docBuilder.parse(xmlFile);
					
					Parser brx = new Parser(xml, Integer.valueOf(txStoreNo.getText()), Integer.valueOf(txCustNo.getText()), txCountryCd.getText(), txCountryNb.getText(), txTaxNo.getText(), txComment.getText());
					
					if (e.getSource().equals(btnGenerateOld))
					{
						txMsb.setText(brx.generateRow4Simulator());
						txMdw.setText(brx.generateSql4MdwOld());
					}
					else if (e.getSource().equals(btnGenerateNew))
					{
						txMsb.setText(brx.generateRow4Simulator());
						txMdw.setText(brx.generateSql4MdwNew());						
					}					
					
					txMsb.setCaretPosition(0);
					txMdw.setCaretPosition(0);
					
					lbStatus.setText("XML was parsed with success");
					lbStatus.setForeground(Color.BLACK);
					
				} catch (Exception ex) {
					lbStatus.setText(ex.getMessage());
					lbStatus.setForeground(Color.RED);
				}				
			}							
		}		
	}
	
	public static void main(String[] args) {
		
		try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Main content = new Main();
		
		JFrame frame = new JFrame("BRXParser v1."+Main.serialVersionUID);
		frame.setVisible(true);
		frame.setBounds(200, 50, 760, 580);
		frame.getContentPane().add(content);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

}
