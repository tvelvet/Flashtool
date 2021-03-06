package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import flashsystem.Bundle;
import flashsystem.BundleEntry;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarFile;

import javax.swing.ListSelectionModel;

import org.lang.Language;
import org.logger.MyLogger;
import org.system.GlobalConfig;
import org.system.OS;
import org.system.PropertiesFile;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JCheckBox;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JList;
import java.awt.GridBagLayout;
import java.awt.GridLayout;


public class firmSelect extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String fsep = OS.getFileSeparator();
	private final JPanel contentPanel = new JPanel();
	private JTable tableFirm;
	private DefaultTableModel modelFirm;
	private DefaultTableModel model_1;
	private XTableColumnModel tableColumnModel = new XTableColumnModel(); 
	private Bundle selected=null;
	private String result;
	private boolean retcode = false;
	JButton okButton;
	private JTable table_1;
	private JTextField folderSource;
	private String filename="";
	private Properties hasCmd25 = new Properties();
	private JPanel panelWipe;
	private JPanel panelExclude;
	private JPanel panelMisc;

	private void dirlist() throws Exception{
		boolean hasElements = false;
		hasCmd25.clear();
		modelFirm = new DefaultTableModel();
		modelFirm.addColumn("File");
		modelFirm.addColumn("Device");
		modelFirm.addColumn("Version");
		modelFirm.addColumn("Branding");
		tableFirm.setModel(modelFirm);
		tableFirm.setColumnModel(tableColumnModel);
		tableFirm.createDefaultColumnsFromModel();
		tableColumnModel.setColumnVisible(tableColumnModel.getColumnByModelIndex(0), false);
    	File dir = new File(folderSource.getText());
	    File[] chld = dir.listFiles(new FtfFilter(filename));
	    for(int i = 0; i < chld.length; i++) {
	    	try {
				hasElements = true;
				JarFile jf = new JarFile(chld[i]);
				modelFirm.addRow(new String[]{chld[i].getName(),jf.getManifest().getMainAttributes().getValue("device"),jf.getManifest().getMainAttributes().getValue("version"),jf.getManifest().getMainAttributes().getValue("branding")});
				String cmd25 = jf.getManifest().getMainAttributes().getValue("cmd25");
				if (cmd25==null) cmd25="false";
				hasCmd25.setProperty(chld[i].getName(), cmd25);
				MyLogger.getLogger().debug("Adding "+chld[i].getName()+" to list of firmwares");
	    	}
	    	catch (Exception e) {
	    	}
	    }
	    if (!hasElements) {
	    	okButton.setEnabled(false);
	    	result=null;
	    }
	    else {
	    	tableFirm.setRowSelectionInterval(0, 0);
	    	result=(String)modelFirm.getValueAt(tableFirm.getSelectedRow(), 0);
	    	okButton.setEnabled(true);
	    }
	}

	public void filelist() throws Exception {
		if (result!=null) {
			selected=new Bundle(folderSource.getText()+fsep+result,Bundle.JARTYPE);
			selected.setDevice((String)modelFirm.getValueAt(tableFirm.getSelectedRow(), 1));
			selected.setVersion((String)modelFirm.getValueAt(tableFirm.getSelectedRow(), 2));
			selected.setBranding((String)modelFirm.getValueAt(tableFirm.getSelectedRow(), 3));
			selected.setCmd25(hasCmd25.getProperty((String)modelFirm.getValueAt(tableFirm.getSelectedRow(), 0)));
		}
		refreshContent();
	}
	
	public void refreshContent() {
		model_1 = new DefaultTableModel();
		model_1.addColumn("File");
		table_1.setModel(model_1);		
		Enumeration<String> e = selected.getMeta().getAllEntries(true);
		while (e.hasMoreElements()) {
			String elem = e.nextElement();
			model_1.addRow(new String[]{elem});
	    	MyLogger.getLogger().debug("Adding "+elem+" to the content of "+result);
	    }
	    if (model_1.getRowCount()>0)
	    	table_1.setRowSelectionInterval(0, 0);
	}
	
	/**
	 * Create the dialog.
	 */
	public firmSelect(String path, String file) {
		filename=file;
		setName("firmSelect");
		setTitle("Firmware Selection");
		setModal(true);
		setBounds(100, 100, 825, 498);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:205dlu"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:102dlu"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:max(117dlu;default)"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:15dlu:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("29dlu"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.GLUE_ROWSPEC,}));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				retcode=false;
				dispose();
			}
		});
		model_1 = new DefaultTableModel();
		model_1.addColumn("File");
			JPanel panelFolder = new JPanel();
			contentPanel.add(panelFolder, "2, 2, 7, 1, fill, fill");
			panelFolder.setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("min:grow"),
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.MIN_COLSPEC,},
				new RowSpec[] {
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,}));
				JLabel lblSelectSourceFolder = new JLabel("Select Source Folder :");
				lblSelectSourceFolder.setName("lblSelectSourceFolder");
				panelFolder.add(lblSelectSourceFolder, "2, 1, fill, fill");
				folderSource = new JTextField();
				folderSource.setEditable(false);
				if (path.length()==0)
					folderSource.setText(OS.getWorkDir()+fsep+"firmwares");
				else
					folderSource.setText(path);
				panelFolder.add(folderSource, "1, 3, 2, 1, fill, fill");
				folderSource.setColumns(10);
				JButton btnNewButton = new JButton("...");
				btnNewButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						doChoose();
					}
				});
				panelFolder.add(btnNewButton, "4, 3, left, default");
			JLabel lblSelectFirmware = new JLabel("Select Firmware");
			lblSelectFirmware.setName(getName()+"_"+"lblSelectFirmware");
			contentPanel.add(lblSelectFirmware, "2, 4, left, fill");
			JLabel lblFilesInThis = new JLabel("Firmware Content :");
			lblFilesInThis.setName(getName()+"_"+"lblFilesInThis");
			contentPanel.add(lblFilesInThis, "4, 4, left, fill");
		
		JScrollPane scrollPaneFolderContent = new JScrollPane();
		contentPanel.add(scrollPaneFolderContent, "2, 6, 1, 11, left, fill");
		tableFirm = new JTable() {
		    /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int rowIndex, int vColIndex) {
		        return false;
		    }
		};
		tableFirm.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				result=(String)modelFirm.getValueAt(tableFirm.getSelectedRow(), 0);
				try {
					filelist();
					addCheckBoxesWipe();
					addCheckBoxesExclupde();
					addCheckBoxesMisc("No final verification",selected.hasCmd25(),new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							selected.setCmd25(selected.hasCmd25()?"false":"true");
						}
					});
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		tableFirm.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				result=(String)modelFirm.getValueAt(tableFirm.getSelectedRow(), 0);
				try {
					filelist();
					addCheckBoxesWipe();
					addCheckBoxesExclupde();
					initMiscCheckBoxes();
					addCheckBoxesMisc("No final verification",selected.hasCmd25(),new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							selected.setCmd25(selected.hasCmd25()?"false":"true");
						}
					});
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		tableFirm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPaneFolderContent.setViewportView(tableFirm);
			JScrollPane scrollPaneFirmwareContent = new JScrollPane();
			contentPanel.add(scrollPaneFirmwareContent, "4, 6, 1, 11, fill, fill");
				table_1 = new JTable(model_1);
				scrollPaneFirmwareContent.setViewportView(table_1);
							
							JPanel panel = new JPanel();
							contentPanel.add(panel, "6, 6, 3, 11, fill, fill");
							panel.setLayout(new FormLayout(new ColumnSpec[] {
									FormFactory.RELATED_GAP_COLSPEC,
									ColumnSpec.decode("73dlu:grow"),
									FormFactory.RELATED_GAP_COLSPEC,},
								new RowSpec[] {
									FormFactory.RELATED_GAP_ROWSPEC,
									FormFactory.DEFAULT_ROWSPEC,
									FormFactory.RELATED_GAP_ROWSPEC,
									RowSpec.decode("top:47dlu"),
									FormFactory.RELATED_GAP_ROWSPEC,
									FormFactory.DEFAULT_ROWSPEC,
									FormFactory.RELATED_GAP_ROWSPEC,
									RowSpec.decode("max(71dlu;default)"),
									FormFactory.RELATED_GAP_ROWSPEC,
									FormFactory.DEFAULT_ROWSPEC,
									FormFactory.RELATED_GAP_ROWSPEC,
									RowSpec.decode("default:grow"),
									FormFactory.RELATED_GAP_ROWSPEC,}));
							
							JLabel lblNewLabel = new JLabel("Wipe :");
							panel.add(lblNewLabel, "2, 2, left, center");
							
							JScrollPane scrollPaneWipe = new JScrollPane();
							panel.add(scrollPaneWipe, "2, 4, fill, fill");
							
							panelWipe = new JPanel();
							scrollPaneWipe.setViewportView(panelWipe);
							
							JLabel lblNewLabel_1 = new JLabel("Exclude :");
							panel.add(lblNewLabel_1, "2, 6");
							JScrollPane scrollPaneExclude = new JScrollPane();
							panel.add(scrollPaneExclude, "2, 8, fill, fill");
							panelExclude = new JPanel();
							scrollPaneExclude.setViewportView(panelExclude);							
							
							JLabel lblNewLabel_2 = new JLabel("Misc :");
							panel.add(lblNewLabel_2, "2, 10");
							
							JScrollPane scrollPaneMisc = new JScrollPane();
							panel.add(scrollPaneMisc, "2, 12, fill, fill");
							
							panelMisc = new JPanel();
							scrollPaneMisc.setViewportView(panelMisc);
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
				okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						retcode=true;
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setName("cancelButton");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						retcode=false;
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
		doRefreshTables();
		setLanguage();
	}
	
	public Bundle getBundle() throws IOException {
		setVisible(true);
		if (retcode) {
			MyLogger.getLogger().debug("Choosed bundle "+result);
			MyLogger.getLogger().info("Selected "+result);
			return selected;
		}
		return null;
	}

	public void setLanguage() {
		Language.translate(this);
	}

	public void doChoose() {
		JFileChooser chooser = new JFileChooser(); 
		if (folderSource.getText().length()==0)
			chooser.setCurrentDirectory(new java.io.File("."));
		else
			chooser.setCurrentDirectory(new java.io.File(folderSource.getText()));
	    chooser.setDialogTitle("Choose a folder");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    //
	    // disable the "All files" option.
	    //
	    chooser.setAcceptAllFileFilterUsed(false);
	    //    
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	folderSource.setText(chooser.getSelectedFile().getAbsolutePath());
	    	doRefreshTables();
	    }
	}

	public void doRefreshTables() {
		try {
			dirlist();
			filelist();
			if (modelFirm.getRowCount()>0) {
				addCheckBoxesWipe();
				addCheckBoxesExclupde();
				initMiscCheckBoxes();
				addCheckBoxesMisc("No final verification",selected.hasCmd25(),new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						selected.setCmd25(selected.hasCmd25()?"false":"true");
					}
				});
			}
		}
		catch (Exception e) {}		
	}
	
	public void setCategEnabled(String categ, boolean enabled) {
		selected.getMeta().setCategEnabled(categ, enabled);
		refreshContent();
	}
	
	public void addCheckBoxesWipe() {
		panelWipe.removeAll();
		FormLayout layout =new FormLayout(
				new ColumnSpec[] {FormFactory.RELATED_GAP_COLSPEC,
						          FormFactory.DEFAULT_COLSPEC,
						          FormFactory.RELATED_GAP_COLSPEC,},
				new RowSpec[] {FormFactory.RELATED_GAP_ROWSPEC});
		panelWipe.setLayout(layout);
		Enumeration<String> wipe = selected.getMeta().getWipe();
		while (wipe.hasMoreElements()) {
			String categ = wipe.nextElement();
			JCheckBox box = new JCheckBox(selected.getMeta().getWipeLabel(categ));
			box.setSelected(true);			
			box.addActionListener(new WipeActionListener(this,box,categ));
			layout.appendRow(FormFactory.DEFAULT_ROWSPEC);
			layout.appendRow(FormFactory.RELATED_GAP_ROWSPEC);
			panelWipe.add(box,"2, "+Integer.toString(layout.getRowCount()-1)+", fill, fill");
		}
		panelWipe.repaint();
		panelWipe.revalidate();
	}

	public void addCheckBoxesExclupde() {
		panelExclude.removeAll();
		FormLayout layout =new FormLayout(
				new ColumnSpec[] {FormFactory.RELATED_GAP_COLSPEC,
						          FormFactory.DEFAULT_COLSPEC,
						          FormFactory.RELATED_GAP_COLSPEC,},
				new RowSpec[] {FormFactory.RELATED_GAP_ROWSPEC});
		panelExclude.setLayout(layout);
		Enumeration<String> exclude = selected.getMeta().getExclude();
		while (exclude.hasMoreElements()) {
			String categ = exclude.nextElement();
			JCheckBox box = new JCheckBox(selected.getMeta().getExcludeLabel(categ));
			box.addActionListener(new ExcludeActionListener(this,box,categ));
			layout.appendRow(FormFactory.DEFAULT_ROWSPEC);
			layout.appendRow(FormFactory.RELATED_GAP_ROWSPEC);
			panelExclude.add(box,"2, "+Integer.toString(layout.getRowCount()-1)+", fill, fill");
		}
		panelExclude.repaint();
		panelExclude.revalidate();
	}

	public void initMiscCheckBoxes() {
		panelMisc.removeAll();
		FormLayout layout =new FormLayout(
				new ColumnSpec[] {FormFactory.RELATED_GAP_COLSPEC,
						          FormFactory.DEFAULT_COLSPEC,
						          FormFactory.RELATED_GAP_COLSPEC,},
				new RowSpec[] {FormFactory.RELATED_GAP_ROWSPEC});
		panelMisc.setLayout(layout);		
	}
	
	public void addCheckBoxesMisc(String label, boolean isSelected, ActionListener al) {
		FormLayout layout = (FormLayout)panelMisc.getLayout();
		JCheckBox box = new JCheckBox(label);
		box.setSelected(isSelected);
		box.addActionListener(al);
		//box.addActionListener(new ExcludeActionListener(this,box,categ));
		layout.appendRow(FormFactory.DEFAULT_ROWSPEC);
		layout.appendRow(FormFactory.RELATED_GAP_ROWSPEC);
		panelMisc.add(box,"2, "+Integer.toString(layout.getRowCount()-1)+", fill, fill");
		panelMisc.repaint();
		panelMisc.revalidate();
	}

}