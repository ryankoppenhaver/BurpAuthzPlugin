package com.wuntee.burp.authz;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import burp.IBurpExtenderCallbacks;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;
import burp.IResponseInfo;
import burp.ITextEditor;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

public class AuthzContainer extends Container {
	private static final long serialVersionUID = 31337L;
	
	private JTable requestTable;
	private JTable responseTable;
	
	private ITextEditor originalRequestEditor;
	private ITextEditor originalResponseEditor;
	private ITextEditor modifiedRequestEditor;
	private ITextEditor responseEditor;
	private ITextEditor cookieEditor;
	
	private DefaultTableModel requestTableModel;
	private DefaultTableModel responseTableModel;
	
	private IBurpExtenderCallbacks burpCallback;
	
	public static String REQUEST_OBJECT_KEY = "req_obj_key";
	public static String RESPONSE_OBJECT_KEY = "resp_obj_key";
	private static Object[] REQUEST_HEADERS = new Object[]{"Method", "URL", "Parms", "Response Code", REQUEST_OBJECT_KEY};
	private static Object[] RESPONSE_HEADERS = new Object[]{"Method", "URL", "Parms","Orig Response Size", "Response Size", "Orig Return Code", "Return Code", REQUEST_OBJECT_KEY, RESPONSE_OBJECT_KEY};
	
	public AuthzContainer(IBurpExtenderCallbacks burpCallback) {
		this.burpCallback = burpCallback;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0, 1.0, 0};
		setLayout(gridBagLayout);
		
		// Create a model that has the Object reference, but do not show the object reference in the GUI
		requestTableModel = new DefaultTableModel(null, REQUEST_HEADERS);		
		responseTableModel = new DefaultTableModel(null, RESPONSE_HEADERS);
		
		
		// TABBED PANNEL
		originalRequestEditor = burpCallback.createTextEditor();
		originalResponseEditor = burpCallback.createTextEditor();
		modifiedRequestEditor = burpCallback.createTextEditor();
		responseEditor = burpCallback.createTextEditor();
		
		
		// COOKIE EDITOR
		JPanel panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 0;
		add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{0};
		gbl_panel_3.rowHeights = new int[]{0, 100};
		gbl_panel_3.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{0, 1, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		JLabel label = new JLabel("New Cookie", SwingConstants.LEFT);
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		panel_3.add(label, gbc_label);
		
		cookieEditor = burpCallback.createTextEditor();
		cookieEditor.setText("Cookie:".getBytes());
		JScrollPane scrollPane = new JScrollPane(cookieEditor.getComponent());
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panel_3.add(scrollPane, gbc_scrollPane);
		
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.75);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.insets = new Insets(0, 0, 5, 0);
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 1;
		add(splitPane, gbc_splitPane);
		
		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		

		// REQUEST PANNEL
		JLabel label_1 = new JLabel("Requests");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.anchor = GridBagConstraints.WEST;
		gbc_label_1.insets = new Insets(0, 0, 5, 0);
		gbc_label_1.gridx = 0;
		gbc_label_1.gridy = 0;
		panel.add(label_1, gbc_label_1);
		requestTable = new JTable(requestTableModel);
		requestTable.setBorder(new LineBorder(new Color(0, 0, 0)));
		requestTable.addMouseListener(new MouseAdapter(){
		       public void mouseClicked(MouseEvent e) {
		    	   responseTable.clearSelection();
		    	   originalRequestEditor.setText(getRequestObjectByIndex(requestTableModel, requestTable.getSelectedRow()).getRequest());
		    	   originalResponseEditor.setText(getRequestObjectByIndex(requestTableModel, requestTable.getSelectedRow()).getResponse());
		    	   modifiedRequestEditor.setText(new byte[]{});
		    	   responseEditor.setText(new byte[]{});
		       }
		});
		requestTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
	    	   responseTable.clearSelection();
	    	   originalRequestEditor.setText(getRequestObjectByIndex(requestTableModel, requestTable.getSelectedRow()).getRequest());
	    	   originalResponseEditor.setText(getRequestObjectByIndex(requestTableModel, requestTable.getSelectedRow()).getResponse());
	    	   modifiedRequestEditor.setText(new byte[]{});
	    	   responseEditor.setText(new byte[]{});
			}
		});
		requestTable.removeColumn(requestTable.getColumn(REQUEST_OBJECT_KEY));
		JScrollPane scrollPane_1 = new JScrollPane(requestTable);
		scrollPane_1.setEnabled(false);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 1;
		panel.add(scrollPane_1, gbc_scrollPane_1);
		
		
		
		
		
		// RESPONSE PANNEL
		JLabel label_2 = new JLabel("Responses");
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.anchor = GridBagConstraints.WEST;
		gbc_label_2.insets = new Insets(0, 0, 5, 0);
		gbc_label_2.gridx = 0;
		gbc_label_2.gridy = 2;
		panel.add(label_2, gbc_label_2);
		responseTable = new JTable(responseTableModel){
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
				Component c = super.prepareRenderer(renderer, row, column);

				if (!isRowSelected(row)){
					c.setBackground(getBackground());
					int modelRow = convertRowIndexToModel(row);
					Short returnCode = (Short)getModel().getValueAt(modelRow, ((DefaultTableModel)getModel()).findColumn("Return Code"));
					if(returnCode == 200){
						c.setBackground(Color.GREEN);
					}
				}

				return c;
			}
		};
		responseTable.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		responseTable.addMouseListener(new MouseAdapter(){
		       public void mouseClicked(MouseEvent e) {
		    	   requestTable.clearSelection();
		    	   //editor.setText(getRequestObjectByIndex(requestTable.getSelectedRow()).getRequest());
		    	   originalRequestEditor.setText(getRequestObjectByIndex(responseTableModel, responseTable.getSelectedRow()).getRequest());
		    	   originalResponseEditor.setText(getRequestObjectByIndex(responseTableModel, responseTable.getSelectedRow()).getResponse());
		    	   modifiedRequestEditor.setText(getResponseObjectByIndex(responseTableModel, responseTable.getSelectedRow()).getRequest());
		    	   responseEditor.setText(getResponseObjectByIndex(responseTableModel, responseTable.getSelectedRow()).getResponse());
		       }
		});
		responseTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
		    	   requestTable.clearSelection();
		    	   //editor.setText(getRequestObjectByIndex(requestTable.getSelectedRow()).getRequest());
		    	   originalRequestEditor.setText(getRequestObjectByIndex(responseTableModel, responseTable.getSelectedRow()).getRequest());
		    	   originalResponseEditor.setText(getRequestObjectByIndex(responseTableModel, responseTable.getSelectedRow()).getResponse());
		    	   modifiedRequestEditor.setText(getResponseObjectByIndex(responseTableModel, responseTable.getSelectedRow()).getRequest());
		    	   responseEditor.setText(getResponseObjectByIndex(responseTableModel, responseTable.getSelectedRow()).getResponse());
			}
		});
		responseTable.removeColumn(responseTable.getColumn(REQUEST_OBJECT_KEY));
		responseTable.removeColumn(responseTable.getColumn(RESPONSE_OBJECT_KEY));
		JScrollPane scrollPane_2 = new JScrollPane(responseTable);
		scrollPane_2.setEnabled(false);
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 0;
		gbc_scrollPane_2.gridy = 3;
		panel.add(scrollPane_2, gbc_scrollPane_2);
		
		JPanel panel_1 = new JPanel();
		splitPane.setRightComponent(panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		JScrollPane scrollPane_3 = new JScrollPane(originalRequestEditor.getComponent());
		tabbedPane.addTab("Original Request", scrollPane_3);
		
		/*
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(originalResponseEditor.getComponent(), popupMenu);
		JMenuItem mntmSendToRepeater = new JMenuItem("Send to repeater");
		mntmSendToRepeater.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//burpCallback.sendToRepeater(host, port, useHttps, request, tabCaption)
			}
		});
		popupMenu.add(mntmSendToRepeater);
		*/
		
		tabbedPane.addTab("Original Response", new JScrollPane(originalResponseEditor.getComponent()));
		tabbedPane.addTab("Modified Request", new JScrollPane(modifiedRequestEditor.getComponent()));
		tabbedPane.addTab("Response", new JScrollPane(responseEditor.getComponent()));
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		panel_1.add(tabbedPane, gbc_tabbedPane);
		
		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.anchor = GridBagConstraints.SOUTH;
		gbc_panel_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 2;
		add(panel_2, gbc_panel_2);
		
		final JButton btnRun = new JButton("Run");
		btnRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				new Thread(new Runnable(){
					public void run() {
						btnRun.setEnabled(false);
						runRequest();
						btnRun.setEnabled(true);
					}
				}).start();
			}
		});
		GridBagConstraints gbc_btnRun = new GridBagConstraints();
		gbc_btnRun.insets = new Insets(0, 0, 5, 0);
		gbc_btnRun.gridx = 0;
		gbc_btnRun.gridy = 0;
		panel_2.add(btnRun, gbc_btnRun);
		
		JButton btnClearRequests = new JButton("Clear Requests");
		btnClearRequests.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				clearTable(requestTableModel);
			}
		});
		GridBagConstraints gbc_btnClearRequests = new GridBagConstraints();
		gbc_btnClearRequests.insets = new Insets(0, 0, 5, 0);
		gbc_btnClearRequests.gridx = 1;
		gbc_btnClearRequests.gridy = 0;
		panel_2.add(btnClearRequests, gbc_btnClearRequests);
		
		JButton btnClearResponses = new JButton("Clear Responses");
		btnClearResponses.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				clearTable(responseTableModel);
			}
		});
		GridBagConstraints gbc_btnClearResponses = new GridBagConstraints();
		gbc_btnClearResponses.gridx = 2;
		gbc_btnClearResponses.gridy = 0;
		panel_2.add(btnClearResponses, gbc_btnClearResponses);
	}
	
	private void runRequest(){
		try{
			// Clear responses
			clearTable(responseTableModel);
			
			// For each request, grab request object, replace cookie, send request, add response to response table
			for(int i=0; i<requestTableModel.getRowCount(); i++){
				IHttpRequestResponse req = getRequestObjectByIndex(requestTableModel, i);
				byte[] rawRequest = req.getRequest();
				
				IRequestInfo reqInfo = burpCallback.getHelpers().analyzeRequest(rawRequest);
				
				// header of request should be a string
				List<String> headers = reqInfo.getHeaders();
				for(int h=0; h<headers.size(); h++){
					if(headers.get(h).toLowerCase().startsWith("cookie:")){
						headers.set(h, new String(cookieEditor.getText()));
						break;
					}
				}
	
				byte message[] = burpCallback.getHelpers().buildHttpMessage(headers, Arrays.copyOfRange(rawRequest, reqInfo.getBodyOffset(), rawRequest.length));
				IHttpRequestResponse resp = burpCallback.makeHttpRequest(req.getHttpService(), message);
				
				addResponse(req, resp);
			}
		} catch(Throwable e){
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	private void addResponse(IHttpRequestResponse request, IHttpRequestResponse response){
		//{"Method", "URL", "Parms","Orig Response Size", "Response Size", "Orig Return Code", "Return Code", REQUEST_OBJECT_KEY, RESPONSE_OBJECT_KEY};
		IRequestInfo reqInfo = burpCallback.getHelpers().analyzeRequest(response);
		IRequestInfo origReqInfo = burpCallback.getHelpers().analyzeRequest(request);

		IResponseInfo respInfo = burpCallback.getHelpers().analyzeResponse(response.getResponse());
		IResponseInfo origrespInfo = burpCallback.getHelpers().analyzeResponse(request.getResponse());
		
		responseTableModel.addRow(new Object[]{
				reqInfo.getMethod(), 
				origReqInfo.getUrl(), 
				(reqInfo.getParameters().size() > 0), 
				request.getResponse().length, 
				response.getResponse().length,
				origrespInfo.getStatusCode(),
				respInfo.getStatusCode(), 
				request, 
				response
		});
		
	}
		
	private void clearTable(DefaultTableModel model){
		model.getDataVector().removeAllElements();
		model.fireTableDataChanged();
	}
	
	public IHttpRequestResponse getRequestObjectByIndex(DefaultTableModel model, int index){
		return((IHttpRequestResponse)model.getValueAt(index, model.findColumn(REQUEST_OBJECT_KEY)));
	}
	
	public IHttpRequestResponse getResponseObjectByIndex(DefaultTableModel model, int index){
		return((IHttpRequestResponse)model.getValueAt(index, model.findColumn(RESPONSE_OBJECT_KEY)));
	}
	
	public void addRequests(IHttpRequestResponse responses[]){
		for(IHttpRequestResponse response : responses){
			IRequestInfo info = burpCallback.getHelpers().analyzeRequest(response);
			IResponseInfo respInfo = burpCallback.getHelpers().analyzeResponse(response.getResponse());
			//{"Method", "URL", "Parms", "Response Code", OBJECT_KEY}
			requestTableModel.addRow(new Object[]{info.getMethod(), info.getUrl(), (info.getParameters().size() > 0), respInfo.getStatusCode(), response});
		}
	}
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}