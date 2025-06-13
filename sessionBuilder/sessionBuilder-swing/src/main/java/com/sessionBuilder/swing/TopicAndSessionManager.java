package com.sessionBuilder.swing;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import com.sessionBuilder.core.Topic;
import com.sessionBuilder.core.TopicController;
import com.sessionBuilder.core.TopicViewCallback;
import com.sessionBuilder.core.SessionViewCallback;
import com.sessionBuilder.core.StudySession;
import com.sessionBuilder.core.StudySessionController;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JList;

import java.awt.Font;

public class TopicAndSessionManager extends JFrame implements TopicViewCallback,SessionViewCallback {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JList<Topic> topicList;
	private JList<StudySession> sessionList;
	private DefaultListModel<Topic> topicModel;
	private DefaultListModel<StudySession> studySessionModel;
	private CardLayout cardLayout;
	private JPanel mainPanel;
	private JLabel lblErrorMessage;
	private static final String MAIN_VIEW = "MAIN";
	private static final String CREATE_TOPIC_VIEW = "CREATE_TOPIC";
	private static final String CREATE_SESSION_VIEW = "CREATE_SESSION";
	private TopicPanel topicPanel;
	private SessionPanel sessionPanel;
	
	
	private TopicController topicController;
	private StudySessionController sessionController;


	public TopicAndSessionManager() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		setupCardLayout();
	}
	
	private void setupCardLayout() {
		cardLayout = new CardLayout();
		mainPanel = new JPanel(cardLayout);
		JPanel currentMainView = createMainView();
		topicPanel = new TopicPanel();
		sessionPanel = new SessionPanel();
		mainPanel.add(currentMainView, MAIN_VIEW);
		mainPanel.add(topicPanel, CREATE_TOPIC_VIEW);
		mainPanel.add(sessionPanel, CREATE_SESSION_VIEW);
		setContentPane(mainPanel);
	}


	private JPanel createMainView() {
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout());
		lblErrorMessage = new JLabel(" ");
		lblErrorMessage.setForeground(Color.RED);
	    lblErrorMessage.setVisible(true);
	    lblErrorMessage.setName("errorMessageLabel");
	    JPanel changeViewPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	    JButton toSessionPanel = new JButton("sessionPanel");
	    toSessionPanel.setBorderPainted(false);
	    toSessionPanel.setContentAreaFilled(false);
	    toSessionPanel.setFocusPainted(false);
	    toSessionPanel.setName("addSessionNavButton");
	    JButton toTopicPanel = new JButton("topicPanel");
	    toTopicPanel.setBorderPainted(false);
	    toTopicPanel.setContentAreaFilled(false);
	    toTopicPanel.setFocusPainted(false);
	    toTopicPanel.setName("addTopicNavButton");
	    changeViewPanel.add(toTopicPanel);
	    changeViewPanel.add(toSessionPanel);
	    contentPane.add(changeViewPanel,BorderLayout.SOUTH);
	    
	    contentPane.add(lblErrorMessage, BorderLayout.NORTH);
	    
	    JPanel mainContent = new JPanel(new GridLayout(0, 1, 0, 0));
	    mainContent.setLayout(new GridLayout(0, 1, 0, 0));
	    contentPane.add(mainContent, BorderLayout.CENTER);
	    JSplitPane splitPane = new JSplitPane();
	    splitPane.setResizeWeight(0.5);
	    splitPane.setDividerLocation(0.5);
	    splitPane.setOneTouchExpandable(true);
	    mainContent.add(splitPane);
	    
	    JPanel topicPanel = new JPanel();
	    topicPanel.setLayout(new BorderLayout());
	    splitPane.setLeftComponent(topicPanel);
	    
	    JLabel topicLabel = new JLabel("Topics");
	    topicLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
	    topicLabel.setName("topicLabel");
	    topicLabel.setFont(new Font("Dialog", Font.BOLD, 23));
	    topicPanel.add(topicLabel, BorderLayout.NORTH);
	    
	    topicModel = new DefaultListModel<>();
	    topicList = new JList<>(topicModel);
	    topicList.setName("topicList");
	    JScrollPane topicScrollPane = new JScrollPane(topicList);
	    topicPanel.add(topicScrollPane, BorderLayout.CENTER);
	    topicList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    
	    JPanel topicButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
	    JButton deleteTopicButton = new JButton("delete");
	    deleteTopicButton.setFont(new Font("Dialog", Font.BOLD, 12));
	    deleteTopicButton.setName("deleteTopicButton");
	    deleteTopicButton.setEnabled(false);
	    topicButtonPanel.add(deleteTopicButton);
	    JButton totalTimeButton = new JButton("totalTime");
	    totalTimeButton.setEnabled(false);
	    totalTimeButton.setFont(new Font("Dialog", Font.BOLD, 12));
	    topicButtonPanel.add(totalTimeButton);
	    JButton percentageButton = new JButton("%Completion");
	    percentageButton.setFont(new Font("Dialog", Font.BOLD, 12));
	    percentageButton.setEnabled(false);
	    topicButtonPanel.add(percentageButton);
	    
	    topicPanel.add(topicButtonPanel, BorderLayout.SOUTH);
	    
	    
	    
	    JPanel sessionPanel = new JPanel();  
	    sessionPanel.setLayout(new BorderLayout());
	    splitPane.setRightComponent(sessionPanel);
	    
	    JLabel sessionLabel = new JLabel("Sessions");
	    sessionLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
	    sessionLabel.setName("sessionLabel");
	    sessionLabel.setFont(new Font("Dialog", Font.BOLD, 23));
	    sessionPanel.add(sessionLabel, BorderLayout.NORTH);
	    
	    studySessionModel = new DefaultListModel<>();
	    sessionList = new JList<>(studySessionModel);
	    sessionList.setName("sessionList");
	    JScrollPane sessionScrollPane = new JScrollPane(sessionList);
	    sessionPanel.add(sessionScrollPane, BorderLayout.CENTER);
	    sessionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    
	    JPanel sessionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
	    sessionPanel.add(sessionButtonPanel, BorderLayout.SOUTH);
	    
	    JButton completeSessionButton = new JButton("Complete");
	    completeSessionButton.setEnabled(false);
	    completeSessionButton.setName("completeSessionButton");
	    completeSessionButton.setFont(new Font("Dialog", Font.BOLD, 12));
	    JButton deleteSessionButton = new JButton("delete");
	    deleteSessionButton.setName("deleteSessionButton");
	    deleteSessionButton.setEnabled(false);
	    deleteSessionButton.setFont(new Font("Dialog", Font.BOLD, 12));
	    
	    sessionButtonPanel.add(completeSessionButton);
	    sessionButtonPanel.add(deleteSessionButton);
	    
	    toTopicPanel.addActionListener(e -> showCreateTopicView());
	    toSessionPanel.addActionListener(e -> showCreateSessionView());
	    
	    completeSessionButton.addActionListener(e -> {
	    	int selectedIndex = sessionList.getSelectedIndex();
	    	if (selectedIndex != -1 && sessionController != null) { 
	    		StudySession selectedSession = studySessionModel.getElementAt(selectedIndex);
	    		sessionController.handleCompleteSession(selectedSession.getId());
	    	}
	    });
	    
	    deleteTopicButton.addActionListener(e -> {
	    	int selectedIndex = topicList.getSelectedIndex();
	    	if (selectedIndex != -1 && topicController != null) { 
	    		Topic selectedTopic = topicModel.getElementAt(selectedIndex);
	    		topicController.handleDeleteTopic(selectedTopic.getId());
	    	}
	    });
	    
	    deleteSessionButton.addActionListener(e -> {
	    	int selectedIndex = sessionList.getSelectedIndex();
	    	if (selectedIndex != -1 && sessionController != null) { 
	    		StudySession selectedSession = studySessionModel.getElementAt(selectedIndex);
	    		sessionController.handleDeleteSession(selectedSession.getId());
	    	}
	    });
	    
	    totalTimeButton.addActionListener(e ->{
	    	int selectedIndex = topicList.getSelectedIndex();
    		if (selectedIndex != -1 && topicController != null) { 
    			Topic selectedTopic = topicModel.getElementAt(selectedIndex);
	    		topicController.handleTotalTime(selectedTopic.getId());
	    		resetErrorLabels();
    		}
	    });
	    
	    percentageButton.addActionListener(e -> {
	    	int selectedIndex = topicList.getSelectedIndex();
	    	if (selectedIndex != -1 && topicController != null) { 
	    		Topic selectedTopic = topicModel.getElementAt(selectedIndex);
	    		resetErrorLabels();
	    		topicController.handlePercentageOfCompletion(selectedTopic.getId());
	    	}
	    });
	    
	    topicList.addListSelectionListener(new ListSelectionListener() {
	    	@Override
	    	public void valueChanged(ListSelectionEvent e) {
	    		if(!e.getValueIsAdjusting()) {
	    			if(topicList.getSelectedIndex() != -1) {
	    				deleteTopicButton.setEnabled(true);
	    				totalTimeButton.setEnabled(true);
	    				percentageButton.setEnabled(true);
	    			}
	    			else {
	    				deleteTopicButton.setEnabled(false);
	    				totalTimeButton.setEnabled(false);
	    				percentageButton.setEnabled(false);
	    			}
	    		}
	    	}
	    });
	    
	    sessionList.addListSelectionListener(new ListSelectionListener() {
	    	@Override
	    	public void valueChanged(ListSelectionEvent e) {
	    		if(!e.getValueIsAdjusting()) {
	    			if(sessionList.getSelectedIndex() != -1) {
	    				deleteSessionButton.setEnabled(true);
	    				if(sessionList.getSelectedValue().isComplete() == false) {
	    					completeSessionButton.setEnabled(true);
	    				}
	    				else {
	    				}
	    			}
	    			else {
	    				deleteSessionButton.setEnabled(false);
	    				completeSessionButton.setEnabled(false);
	    			}
	    		}
	    	}
	    });
	 
	    return contentPane;
	}
	
	TopicPanel getTopicPanel() {
		return this.topicPanel;
	}
	
	SessionPanel getSessionPanel() {
		return this.sessionPanel;
	}
	
	public TopicController getTopicController() {
		return topicController;
	}
	
	public void setTopicController(TopicController controller) {
		this.topicController = controller;
		if (controller != null) {
			controller.setViewCallback(this);
		}
	}
	
	public void setSessionController(StudySessionController controller) {
		this.sessionController = controller;
		if (controller != null) {
			controller.setViewCallBack(this);
		}
	}
	
	public void sessionAdded(StudySession session) {
		studySessionModel.addElement(session);
		resetErrorLabels();
	}
	
	public void sessionRemoved(StudySession session) {
		studySessionModel.removeElement(session);
		resetErrorLabels();
	}
	
	public void topicAdded(Topic topic) {
		topicModel.addElement(topic);
		resetErrorLabels();
	}
	
	public void topicRemoved(Topic topic) {
		topicModel.removeElement(topic);
		resetErrorLabels();
	}
	
	private void resetErrorLabels() {
		lblErrorMessage.setText(" ");
		lblErrorMessage.setForeground(Color.RED);
	}

	public void showCreateTopicView() {
		resetErrorLabels();
		cardLayout.show(mainPanel, CREATE_TOPIC_VIEW);
	}

	public void showCreateSessionView() {
		resetErrorLabels();
		cardLayout.show(mainPanel, CREATE_SESSION_VIEW);
	}
	
	public void showMainView() {
		resetErrorLabels();
		cardLayout.show(mainPanel, MAIN_VIEW);
	}

	public DefaultListModel<Topic> getTopicModel(){
		return this.topicModel;
	}
	
	public DefaultListModel<StudySession> getStudySessionModel(){
		return this.studySessionModel;
	}


	public void showSessionError(String message, StudySession session) {
		lblErrorMessage.setText(message + ": " + session);
	}


	public void showTopicError(String message, Topic topic) {
		lblErrorMessage.setText(message + ": " + topic);
	}


	public void showGeneralError(String string) {
		lblErrorMessage.setText(string);
	}
	
	
	// IMPLEMENTAZIONE METODI DI INTERFACCIA

	@Override
	public void onTopicAdded(Topic topic) {
		topicAdded(topic);	
	}

	@Override
	public void onTopicRemoved(Topic topic) {
		topicRemoved(topic);
	}

	@Override
	public void onTopicError(String message) {
		lblErrorMessage.setText(message);
		
	}

	@Override
	public void onTotalTimeCalculated(Integer totalTime) {
		lblErrorMessage.setText("Tempo totale: " + totalTime + " minuti");
		lblErrorMessage.setForeground(Color.GREEN);
	}

	@Override
	public void onPercentageCalculated(Integer percentage) {
		lblErrorMessage.setText("Percentuale di completamento: " + percentage + "%");
		lblErrorMessage.setForeground(Color.GREEN);
	}

	@Override
	public void onSessionAdded(StudySession session) {
		sessionAdded(session);
		
	}

	@Override
	public void onSessionRemoved(StudySession session) {
		sessionRemoved(session);
		
	}

	@Override
	public void onSessionError(String message) {
		lblErrorMessage.setText(message);
		
	}

	public StudySessionController getSessionController() {
		return sessionController;
	}
	

}
