package uk.ac.aber.dcs.cs124group.controller;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.util.regex.*;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.*;

import uk.ac.aber.dcs.cs124group.model.*;
import uk.ac.aber.dcs.cs124group.export.*;
import uk.ac.aber.dcs.cs124group.gui.Canvas;
import uk.ac.aber.dcs.cs124group.gui.MainFrame;
import uk.ac.aber.dcs.cs124group.gui.MenuBar;
import uk.ac.aber.dcs.cs124group.gui.SideBar;
import uk.ac.aber.dcs.cs124group.gui.StatusBar;
import uk.ac.aber.dcs.cs124group.gui.ToolBar;

public class Manager extends DiagramListener implements ActionListener,
		ChangeListener {

	private boolean inDebug = true;
	private ListeningMode mode = ListeningMode.LISTEN_TO_ALL;

	public static final String PROGRAM_NAME = "UML2Java";
	public static final String FILE_EXTENSION = "umlj";

	
	private MainFrame window;
	private Canvas canvas;
	private MenuBar menuBar;
	private SideBar sideBar;
	private StatusBar status;
	private ToolBar toolBar;

	private DocumentModel document;

	private boolean edited = false;
	private LabelView currentEdited;

	public Manager() {
		window = new MainFrame(this);
		window.setTitle(PROGRAM_NAME);

		canvas = window.getCanvas();

		menuBar = window.getMenu();
		sideBar = window.getSideBar();
		status = window.getStatus();
		toolBar = window.getToolbar();

		openNewDocument();
		canvas.setPreferredSize(new Dimension(500, 500));
		status.setText("Welcome!");

		changeFont();
	}

	public ArrayList<DocumentElementModel> getDrawableElements() {
		return document.getElements();
	}


	@Override
	public void mousePressed(MouseEvent e) {
		if (mode == ListeningMode.PLACING_CLASS) {
			addNewClass(new Point(
					(int) ((1 / canvas.getZoomFactor()) * e.getX()),
					(int) ((1 / canvas.getZoomFactor()) * e.getY())));
		} else if (mode == ListeningMode.PLACING_TEXT) {
			addNewLabel(new Point(
					(int) ((1 / canvas.getZoomFactor()) * e.getX()),
					(int) ((1 / canvas.getZoomFactor()) * e.getY())));
		} else {
			// Editing text...

		}

	}


	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		status.setMousePos(e.getX(), e.getY());
	}



	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JSlider) {
			changeZoom(((JSlider) e.getSource()).getValue() / 100.0);

		} else if (e.getSource() instanceof JSpinner) {
			changeFont();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String c = e.getActionCommand();
		if (c.equals("Exit")) {
			exit();
		} else if (c.equals("New")) {
			openNewDocument();
		} else if (c.equals("About")) {
			openAboutWindow();
		} else if (c.equals("New Class")) {
			mode = ListeningMode.PLACING_CLASS;
			status.setText("Click on the canvas to place your new class");
		} else if (c.equals("New label")) {
			mode = ListeningMode.PLACING_TEXT;
			status.setText("Click on the canvas to place your label");
		} else if (c.equals("Save")) {
			save();
		} else if (c.equals("Save as...")) {
			saveAs();
		} else if (c.equals("Open...")) {
			openExisting();
		} else if (c.equals("fonts") || c.equals("fontsize")) {
			changeFont();
		} else if (c.equals("Image")) {
			Exporter exp = new Exporter(canvas, this);
			try {
				exp.exportImage();
			} catch (IIOException e1) {
				e1.printStackTrace();
			}
		} else if (c.equals("Code")) {
			document.cleanUp();
			Exporter exp = new Exporter(document, this);
			try {
				exp.exportCode();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	private void openNewDocument() {
		document = new DocumentModel();
		canvas.removeAll();
		canvas.repaint();
		window.setTitle("Unsaved class diagram - " + PROGRAM_NAME);

		DocumentPreferences preferences = document.getPreferences();
		preferences.setFont(new Font(toolBar.getFontName(), Font.PLAIN, toolBar
				.getFontSize()));
		canvas.setFont(new Font(toolBar.getFontName(), Font.PLAIN, toolBar
				.getFontSize()));
		preferences.setCanvasDefaultSize(canvas.getSize());

		status.setText("Opened a brand new class diagram");

	}

	private void addNewClass(Point p) {
		mode = ListeningMode.LISTEN_TO_ALL;
		ClassModel c = new ClassModel(p);
		document.addElement(c);
		status.setText("New class rectangle created at " + p.x + "," + p.y);

		ClassRectangle view = new ClassRectangle(c);
		c.addObserver(view);
		document.getPreferences().addObserver(view);
		canvas.add(view);
		view.repaint();

		canvas.repaint();
		this.edited = true;
	}

	private void addNewLabel(Point p) {
		mode = ListeningMode.LISTEN_TO_ALL;
		TextLabelModel mod = new TextLabelModel(p);
		LabelView l = new LabelView(mod);
		l.setFont(document.getPreferences().getFont());
		l.setName("label");
		mod.addObserver(l);
		document.addElement(mod);
		document.getPreferences().addObserver(l);
		//l.addMouseListener(this);
		//document.addElement(l);
		status.setText("New label created at " + p.x + "," + p.y);

		canvas.add(l);
		l.repaint();

		canvas.repaint();
		this.edited = true;
	}

	private void openAboutWindow() {
		JFrame aboutWindow = new JFrame("About " + PROGRAM_NAME);
		aboutWindow.setSize(450, 250);
		aboutWindow.setMaximumSize(new Dimension(450, 250));
		aboutWindow.setLocationRelativeTo(window);
		aboutWindow.setVisible(true);

		aboutWindow.add(new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawString(
						"This program is being developed by Sam Sherar, Daniel Maly and Lee Smith.",
						50, 150);
				g.drawString(
						"You are advised to stay well away from it until it is finished.",
						50, 165);
				g.setFont(new Font("Arial Black", Font.PLAIN, 50));
				g.drawString("UML2JAVA", 50, 50);
			}
		});

		aboutWindow.setSize(450, 250);
	}

	private void save() {
		if (document.getPreferences().getFilename() == null) {
			saveAs();
		} else {
			serialise(document.getPreferences().getFilename());
		}
	}

	private void saveAs() {
		setWaitCursor(true);
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter(PROGRAM_NAME + " diagram (*." +FILE_EXTENSION+ ")", FILE_EXTENSION));
		int retVal = fc.showSaveDialog(null);
		setWaitCursor(false);

		if (retVal == JFileChooser.APPROVE_OPTION) {
			File saveFile = new File(fc.getSelectedFile().getAbsolutePath() + "." + FILE_EXTENSION);
			try {
				if (saveFile.isFile())
					saveFile.createNewFile();
			} catch (Exception e) {

			}
			document.getPreferences().setFilename(saveFile.getAbsoluteFile().getPath());
			serialise(saveFile.getAbsoluteFile().getPath());
			

		}

	}

	private void serialise(String fileName) {
		document.cleanUp();
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(this.document);
			out.close();
			status.setText("Document saved successfully");
		} catch (Exception e) {
			status.setText("Could not write your document into the file. Sorry!");
			e.printStackTrace();
		}
	}

	private void openExisting() {
		setWaitCursor(true);
		JFileChooser fc = new JFileChooser();

		fc.addChoosableFileFilter(new FileNameExtensionFilter(PROGRAM_NAME + " diagram (*." +FILE_EXTENSION+ ")", FILE_EXTENSION));
		fc.setAcceptAllFileFilterUsed(false);
		
		int retVal = fc.showOpenDialog(null);
		setWaitCursor(false);

		if (retVal == JFileChooser.APPROVE_OPTION) {
			File openFile = fc.getSelectedFile();
			try {
				document = null;
				canvas.removeAll();
				canvas.repaint();
				FileInputStream fos = new FileInputStream(openFile.getPath());
				ObjectInputStream in = new ObjectInputStream(fos);
				document = (DocumentModel) in.readObject();
				for (int i = 0; i < getDrawableElements().size(); i++) {
					DocumentElementModel e = getDrawableElements().get(i);
					e.setPaintState(ElementPaintState.DEFAULT);
					//canvas.add(c);
				}
				canvas.setPreferredSize(document.getPreferences()
						.getCanvasDefaultSize());
				canvas.setFont(document.getPreferences().getFont());
				toolBar.overrideFont(document.getPreferences().getFont());
				canvas.setZoomFactor(document.getPreferences().getZoomLevel()); // TODO:
																				// fixme,
																				// JSlider
																				// will
																				// not
																				// have updated
				
				status.setText("File " + openFile + " opened successfully");
				window.setTitle(openFile + " - " + PROGRAM_NAME);
			} catch (Exception e) {
				status.setText("Could not open file " + openFile);
			}

		}
	}

	private void changeFont() {
		setWaitCursor(true);
		Font font = new Font(toolBar.getFontName(), Font.PLAIN,
				toolBar.getFontSize());
		document.getPreferences().setFont(font);

		canvas.setFont(font);
		canvas.repaint();
		status.setText("Font changed to " + font);
		setWaitCursor(false);
	}

	private void changeZoom(double zoom) {
		canvas.setZoomFactor(zoom);
		status.setText("Zoom factor is " + canvas.getZoomFactor());
		canvas.repaint();

		document.getPreferences().setZoomLevel(zoom);
	}

	public void setWaitCursor(boolean value) {
		if (value) {
			try {
				window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			} catch (Exception e) {
				window.setCursor(Cursor.getDefaultCursor());
			}
		} else
			window.setCursor(Cursor.getDefaultCursor());
	}
	

	public void exit() {

		if (this.edited && !this.inDebug) {
			Object[] options = { "Save", "Don't Save", "Cancel" };
			int n = JOptionPane
					.showOptionDialog(window,"Would you like to save your changes? Any unsaved changes will be lost.","Warning!", JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE, null, options,options[2]);
			if (n != 2 || n != JOptionPane.CANCEL_OPTION) {
				if (n == 0) {
					save();
					System.exit(0);
				} else if (n == 1) {
					System.exit(0);
				}
			}
		} else {
			System.exit(0);
		}
	}


}
