//import java.awt.*;
//import javax.swing.*;
//import java.awt.event.*;
//import java.util.*;

import java.io.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Insets;
import java.awt.Container;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.border.*;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import javax.swing.JComponent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import org.mozilla.universalchardet.UniversalDetector;

public class TextEditor extends JFrame implements ActionListener, CaretListener{

	private JTextArea textArea;
	private JTextField textField;
	private JCheckBox exprHandle;
	private JCheckBox upperOrLowerHandle;
	private JDialog searchDialog;
	private int caretPosition = 0;
	private int patternFlags = Pattern.LITERAL;
	private String filePath = new File("").getAbsolutePath();
	private String textTitle = "No title";
	private String textData = "";
	private String saveMode = "save";
	private String updateCheck = "";
	//final String saveCheckMsg = "The contents of the file has changed.\nDo you want to save it?";
	private final String saveCheckMsg = "ファイルの内容が変更されています。\n保存しますか？";
	private final String updateCheckMsg = "そのファイルは存在します。\n上書きしますか？";
	private boolean fileSavedCheck = true;
	private boolean fileOpendCheck = false;
	private Matcher matcher = null;
	private Pattern p = null;

	// Constructor
	TextEditor(){
//		setTitle(textTitle + " - TextEditor");
//		setFrameTitle(textTitle);
		setSize(640, 480);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new MyWindowEvent());

		// 自作関数呼び出し
		setSearchDialog();
		setMenu();
		setTextArea();
		newFile(); // 初期化のため呼び出す
	}

	// create search dialog
	public void setSearchDialog(){
		searchDialog = new JDialog(this, "Search", true);
		searchDialog.setSize(280,180);
		searchDialog.setLocationRelativeTo(null);
		searchDialog.setResizable(false);
		
		JPanel panel = new JPanel();
		//JLabel label = new JLabel("検索文字列 : ");
		textField = new JTextField(20);
		JButton searchButton = new JButton("search");
		JButton cancelButton = new JButton("cancel");
		exprHandle = new JCheckBox("正規表現を使う");
		upperOrLowerHandle = new JCheckBox("大文字・小文字を同一視");

		textField.setActionCommand("Search_run");
		searchButton.setActionCommand("Search_run");
		cancelButton.setActionCommand("SearchDialog_hide");
		
		// add search event
		textField.addActionListener(this);
		searchButton.addActionListener(this);

		// add search cancel event
		cancelButton.addActionListener(this);

		// 正規表現使用判定イベント
		exprHandle.addActionListener(new myExprCheck());
		
		//大文字・小文字の区別処理判定イベント
		upperOrLowerHandle.addActionListener(new myUpperOrLowerCheck());

		panel.add(textField);
		panel.add(searchButton);
		panel.add(cancelButton);
		panel.add(exprHandle);
		panel.add(upperOrLowerHandle);
		
		Container searchContentPane = searchDialog.getContentPane();
		searchContentPane.add(panel);
	}

	public void setTextArea(){
		JPanel textFieldPanel = new JPanel();
		textArea = new JTextArea();
		textArea.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		textFieldPanel.setLayout(new BoxLayout(textFieldPanel, BoxLayout.PAGE_AXIS));

		// border setting
	//	textArea.setBorder(new BevelBorder(BevelBorder.LOWERED));

		// margin setting (top, left, bottom, right)
		textArea.setMargin(new Insets(0, 1, 0, 0));

		// carret setting
		textArea.setCaretPosition(0);
		textArea.addCaretListener(this);

		// set tabSize
		textArea.setTabSize(4);

		// add key event
		textArea.addKeyListener(new MyKeyEvent());

		// D&D event
	//	textArea.setTransferHandler(new MyDropEvent(this));
		textArea.setDropTarget(new MyDropEvent(this));

		// scroll
		JScrollPane scrollPane = new JScrollPane(textArea);
		textFieldPanel.add(scrollPane);
		
		Container contentPane = getContentPane();
		contentPane.add(textFieldPanel, BorderLayout.CENTER);
	}

	public void setMenu(){
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File"); // File menu
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenu editMenu = new JMenu("Edit"); // Edit menu
		editMenu.setMnemonic(KeyEvent.VK_E);
		
		// new
		JMenuItem fileMenuItemNew = new JMenuItem("New");
		fileMenuItemNew.setMnemonic(KeyEvent.VK_N);
		fileMenuItemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		fileMenuItemNew.setActionCommand("New");
		fileMenuItemNew.addActionListener(this);
		
		// open
		JMenuItem fileMenuItemOpen = new JMenuItem("Open");
		fileMenuItemOpen.setMnemonic(KeyEvent.VK_O);
		fileMenuItemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		fileMenuItemOpen.setActionCommand("Open");
		fileMenuItemOpen.addActionListener(this);
		
		// save
		JMenuItem fileMenuItemSave = new JMenuItem("Save");
		fileMenuItemSave.setMnemonic(KeyEvent.VK_S);
		fileMenuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		fileMenuItemSave.setActionCommand("Save");
		fileMenuItemSave.addActionListener(this);
		
		// save as
		JMenuItem fileMenuItemSaveas = new JMenuItem("Save as");
		fileMenuItemSaveas.setMnemonic(KeyEvent.VK_A);
		fileMenuItemSaveas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
		fileMenuItemSaveas.setActionCommand("Save_as");
		fileMenuItemSaveas.addActionListener(this);

		// close
		JMenuItem fileMenuItemClose = new JMenuItem("Close");
		fileMenuItemClose.setMnemonic(KeyEvent.VK_C);
		fileMenuItemClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		fileMenuItemClose.setActionCommand("Close");
		fileMenuItemClose.addActionListener(this);

		// search
		JMenuItem fileMenuItemSearch = new JMenuItem("Search");
		fileMenuItemSearch.setMnemonic(KeyEvent.VK_F);
		fileMenuItemSearch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
		fileMenuItemSearch.setActionCommand("SearchDialog_show");
		fileMenuItemSearch.addActionListener(this);


		// add menuItem
		fileMenu.add(fileMenuItemNew);
		fileMenu.add(fileMenuItemOpen);
		fileMenu.add(fileMenuItemSave);
		fileMenu.add(fileMenuItemSaveas);
		fileMenu.add(fileMenuItemClose);

		editMenu.add(fileMenuItemSearch);
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
	
		this.setJMenuBar(menuBar);
	}

	// menuItem event
	public void actionPerformed(ActionEvent event){
		String cmd = event.getActionCommand();
		switch(cmd){
			case "New" :
				newFile();
				break;
			case "Open" :
				openFile("");
				break;
			case "Save" :
				saveMode = "save";
				saveFile(saveMode);
				break;
			case "Save_as" :
				saveMode = "saveAs";
				saveFile(saveMode);
				break;
			case "Close" :
				closeFrame();
				break;
			case "SearchDialog_show" :
				searchDialog.setVisible(true);
				break;
			case "Search_run" :
				searchRun();
				break;
			case "SearchDialog_hide" :
				searchDialog.setVisible(false);
				break;
		}
	}

	// set fileName
	public void setFrameTitle(String title){
		this.setTitle(title + " - TextEditor");
	}

	// file create
	public void newFile(){

		if(!fileSavedCheck){
			int closeCheck = JOptionPane.showConfirmDialog(this, saveCheckMsg, 
				"caution", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

			if(closeCheck == JOptionPane.YES_OPTION){
				saveFile(saveMode);
			}else if(closeCheck == JOptionPane.CANCEL_OPTION){
				return;
			}
			fileSavedCheck = true;
		}
		
		textTitle = "No title";
		setFrameTitle(textTitle);
		textArea.setText("");
		textData = "";
		filePath = null;
		fileOpendCheck = false;
	}

	// file open
	public void openFile(String fileName){
		String textLine;
		File file = null;
		boolean fileCheck = false;
		String encodingCheck;
	
		if(fileName.equals("")){
			JFileChooser fileChooser = new JFileChooser(filePath);
			fileChooser.setFileFilter(new FileNameExtensionFilter("*.java", "java"));
			fileChooser.setFileFilter(new FileNameExtensionFilter("*.html", "html"));
			fileChooser.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
			
			int selected = fileChooser.showOpenDialog(this);
			if(selected == fileChooser.APPROVE_OPTION){
				file = fileChooser.getSelectedFile();
				fileCheck = true;
			}else{
				return;
			}
		}else{
			file = new File(fileName);
			String[] checkDir = file.list();
			if(checkDir == null){ // file or directory check
				fileCheck = true;
			}
		}
		
		try{
			if(fileCheck){
				BufferedReader br = null;
				textArea.setText("");
				textTitle = file.getName();
				setFrameTitle(textTitle);
				filePath = file.getAbsolutePath();

				encodingCheck = getEncoding(file);
				//System.out.println("encode : " + encodingCheck);

				String[] encodeList = {"UTF-8", "UTF-16LE", "EUC-JP","SHIFT_JIS"};
				
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				for(int i=0;i<encodeList.length;i++){
					if(encodingCheck != null && encodingCheck.equals(encodeList[i])){
						br = new BufferedReader(new InputStreamReader(new FileInputStream(file), encodeList[i]));
						break;
					}
				}	
				while((textLine = br.readLine()) != null){
					textArea.append(textLine);
					textArea.append("\n");
				}
				
				br.close();
				int lastNewLineCount = textArea.getText().length();
				textArea.replaceRange("", lastNewLineCount-1, lastNewLineCount);
				
				fileOpendCheck = true;
				fileSavedCheck = true;
			}else{
				System.out.println("ファイルではないため、開くことができません。");
			}
			textData = textArea.getText();
		}catch(FileNotFoundException error){
			System.out.println("FileNotFoundException");
		}catch(IOException error){
			System.out.println("IOException");
		}catch(NullPointerException error){
			System.out.println("NullPointer");
		}
	}

	// save or saveAs
	public void saveFile(String saveMode){
		JFileChooser fileChooser = new JFileChooser(filePath);
		fileChooser.setFileFilter(new FileNameExtensionFilter("*.java", "java"));
		fileChooser.setFileFilter(new FileNameExtensionFilter("*.html", "html"));
		fileChooser.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));

		File file = null;
		String saveCommand = "save";

		if(fileOpendCheck && saveMode.equals(saveCommand)){ // update
			file = new File(filePath);
			writeTextData(file);
		}else{ // save or saveAs
			int selected = fileChooser.showSaveDialog(this);
			if(selected == fileChooser.APPROVE_OPTION){
				file = fileChooser.getSelectedFile();
				//textTitle = file.getName();
				if(!fileChooser.accept(file)){
					String fileType = new String(fileChooser.getFileFilter().getDescription());
					String[] splitFileType = fileType.split("\\.");
					fileType = splitFileType[1];
					filePath = file.getPath();
					file = new File(filePath +  "." + fileType);
				}

				filePath = file.getAbsolutePath();
				if(file.exists()){
					int fileNameCheck = JOptionPane.showConfirmDialog(this, updateCheckMsg,
						"caution", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

					if(fileNameCheck == JOptionPane.YES_OPTION){
						writeTextData(file);
						setFrameTitle(file.getName());
						fileOpendCheck = true;
					}else if(fileNameCheck == JOptionPane.CANCEL_OPTION){
						return;
					}else{
						saveFile(saveMode);
					}
				}else{
					try{
						file.createNewFile();
						writeTextData(file);
						setFrameTitle(file.getName());
						fileOpendCheck = true;
					}catch(IOException error){
						System.out.println("Faild to create file");
					}
				}
			}
		}
	}

	// word search
	public void searchRun(){
		String textF = textField.getText();
		String textA = textArea.getText();
		
		if(!updateCheck.equals(textF)){
			matcher = null;
		}

		if(matcher == null){
			p = Pattern.compile(textF, patternFlags);
			matcher = p.matcher(textA);
		
			matchCheck(matcher);
		}else{
			matchCheck(matcher);
		}
		updateCheck = textF;
	}

	// file write
	public void writeTextData(File file){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(textArea.getText());
			bw.close();
			fileSavedCheck = true;
			textData = textArea.getText();
		}catch(FileNotFoundException error){
			System.out.println("File NotFound");
		}catch(IOException error){
			System.out.println("Failed to save file");
		}
	}
	
	// file close
	public void closeFrame(){
		if(fileSavedCheck){
			System.exit(0);
		}else{

			int closeCheck = JOptionPane.showConfirmDialog(this, saveCheckMsg, 
				"caution", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

			if(closeCheck == JOptionPane.YES_OPTION){
				saveFile(saveMode);
				System.exit(0);
			}else if(closeCheck == JOptionPane.NO_OPTION){
				System.exit(0);
			}else{
				System.out.println("cancel");
				return;
			}
		}
	}
	// get encode
       public String getEncoding(File file){
		String encoding = "";
		try{
			UniversalDetector detector = new UniversalDetector(null);
			FileInputStream fis = new FileInputStream(file);
			byte[] byteArray = new byte[4096];
			int read;

			while((read = fis.read(byteArray)) > 0 && !detector.isDone()){
				detector.handleData(byteArray, 0, read);
			}
			detector.dataEnd();
			encoding = detector.getDetectedCharset();
		}catch(FileNotFoundException error){
			System.out.println("file not found");	
		}catch(IOException error){
			System.out.println("IO error");
		}
		return encoding;
        }

	// set caretPosition
	public void caretUpdate(CaretEvent event){
		caretPosition = (int)event.getDot();
	}

	public void matchCheck(Matcher m){
		if(m.find(caretPosition)){
			int start = m.start();
			int end = m.end();

			textArea.requestFocusInWindow();
			textArea.select(start, end);
		}else{ //最後まで検索後、最初の行に戻って再検索する
			caretPosition = 0;
			if(m.find(caretPosition)){
				int start = m.start();
				int end = m.end();

				textArea.requestFocusInWindow();
				textArea.select(start, end);
			}else{
				//m.reset();
				//matcher = null;
				System.out.println("not match");
			}
		}
	}

	// main
	public static void main(String[] args){
		TextEditor textEditor = new TextEditor();
		textEditor.setVisible(true);
	}

	// Window close
	public class MyWindowEvent extends WindowAdapter{
		public void windowClosing(WindowEvent event){
			closeFrame();
		}
	}

	// file editing check
	public class MyKeyEvent extends KeyAdapter{
		public void keyReleased(KeyEvent event){
			String text = textArea.getText();

			if(textData.equals(text)){
				fileSavedCheck = true;
				textData = text;
			}else{
				fileSavedCheck = false;
			}
			
		}
	}

	// file drop event
	public class MyDropEvent extends DropTarget{
		TextEditor textEditor;

		MyDropEvent(TextEditor textEditor){
			this.textEditor = textEditor;
		}

		public void drop(DropTargetDropEvent event){
			try{

				Transferable transferble = event.getTransferable();
				if(transferble.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
					event.acceptDrop(DnDConstants.ACTION_REFERENCE);
					if(!fileSavedCheck){
						int closeCheck = JOptionPane.showConfirmDialog(this.textEditor, saveCheckMsg,
							"caution", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

						if(closeCheck == JOptionPane.YES_OPTION){
							saveFile(saveMode);
						}else if(closeCheck == JOptionPane.CANCEL_OPTION){
							return;
						}
						fileSavedCheck = true;
					}
					Object obj = transferble.getTransferData(DataFlavor.javaFileListFlavor);
					List list = (List)obj;
					openFile(list.get(0).toString());
				}
			}catch(UnsupportedFlavorException error){
				System.out.println("Unsupport");
			}catch(IOException error){
				System.out.println("IOExcepton");
			}
		}
	}

	// expr use check
	public class myExprCheck implements ActionListener{
		public void actionPerformed(ActionEvent event){
			if(exprHandle.isSelected()){ // expr use
				patternFlags = patternFlags -  Pattern.LITERAL;
			}else{ // expr not use
				patternFlags = patternFlags +  Pattern.LITERAL;
			}
			matcher = null;

			//System.out.println("exprHandle patternFlags : " + patternFlags);
		}
	}

	// upper or lower word equate setting
	public class myUpperOrLowerCheck implements ActionListener{
		public void actionPerformed(ActionEvent event){
			
			if(upperOrLowerHandle.isSelected()){ // equate
				patternFlags = patternFlags +  Pattern.CASE_INSENSITIVE;
			}else{ // not equate
				patternFlags = patternFlags -  Pattern.CASE_INSENSITIVE;
			}
			matcher = null;

			//System.out.println("upperOrLowerHandle patternFlags : " + patternFlags);
		}
	}
}
