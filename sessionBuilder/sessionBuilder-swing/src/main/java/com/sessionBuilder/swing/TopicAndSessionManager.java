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
import javax.swing.WindowConstants;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JList;

import java.awt.Font;

public class TopicAndSessionManager extends JFrame implements TopicViewCallback,SessionViewCallback {

	private static final long serialVersionUID = 1L;

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
	
	private JButton completeSessionButton;
	private JButton deleteTopicButton;
	private JButton deleteSessionButton;
	private JButton totalTimeButton;
	private JButton percentageButton;
	
	
	private transient TopicController topicController;
	private transient StudySessionController sessionController;
	
	private static final String FONT = "Dialog";


	public TopicAndSessionManager() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
	
	public void loadInitialData() {
		if (topicController != null && sessionController != null) {
			topicModel.clear();
			studySessionModel.clear();
			List<Topic> topics = topicController.handleGetAllTopics();
			List<StudySession> sessions = sessionController.handleGetAllSessions();
			topics.forEach(topicModel::addElement);
			sessions.forEach(studySessionModel::addElement);
		} 
		else {
			throw new IllegalStateException("i record del db non sono stati caricati correttamente");
		}
	}


	private JPanel createMainView() {
		JPanel mainViewPane = new JPanel(new BorderLayout());
		mainViewPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setupErrorMessageLabel(mainViewPane);
		setupMainContent(mainViewPane);
		setupNavigationButtons(mainViewPane);
		
		setupActionListeners();
		setupListSelectionListeners();

		return mainViewPane;
	}

	private void setupErrorMessageLabel(JPanel parent) {
		lblErrorMessage = new JLabel(" ");
		lblErrorMessage.setForeground(Color.RED);
		lblErrorMessage.setVisible(true);
		lblErrorMessage.setName("errorMessageLabel");
		parent.add(lblErrorMessage, BorderLayout.NORTH);
	}

	private void setupMainContent(JPanel parent) {
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(0.5);
		splitPane.setOneTouchExpandable(true);

		JPanel topicContainerPanel = createTopicPanel();
		JPanel sessionContainerPanel = createSessionPanel();

		splitPane.setLeftComponent(topicContainerPanel);
		splitPane.setRightComponent(sessionContainerPanel);
		
		JPanel mainContent = new JPanel(new GridLayout(0, 1, 0, 0));
		mainContent.add(splitPane);
		parent.add(mainContent, BorderLayout.CENTER);
	}

	private JPanel createTopicPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Topics");
		label.setBorder(new EmptyBorder(0, 5, 0, 0));
		label.setName("topicLabel");
		label.setFont(new Font(FONT, Font.BOLD, 23));
		panel.add(label, BorderLayout.NORTH);

		topicModel = new DefaultListModel<>();
		topicList = new JList<>(topicModel);
		topicList.setName("topicList");
		topicList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		panel.add(new JScrollPane(topicList), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		deleteTopicButton = new JButton("delete");
		deleteTopicButton.setName("deleteTopicButton");
		deleteTopicButton.setFont(new Font(FONT, Font.BOLD, 12));
		deleteTopicButton.setEnabled(false);
		
		totalTimeButton = new JButton("totalTime");
		totalTimeButton.setFont(new Font(FONT, Font.BOLD, 12));
		totalTimeButton.setEnabled(false);

		percentageButton = new JButton("%Completion");
		percentageButton.setFont(new Font(FONT, Font.BOLD, 12));
		percentageButton.setEnabled(false);

		buttonPanel.add(deleteTopicButton);
		buttonPanel.add(totalTimeButton);
		buttonPanel.add(percentageButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createSessionPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Sessions");
		label.setBorder(new EmptyBorder(0, 5, 0, 0));
		label.setName("sessionLabel");
		label.setFont(new Font(FONT, Font.BOLD, 23));
		panel.add(label, BorderLayout.NORTH);

		studySessionModel = new DefaultListModel<>();
		sessionList = new JList<>(studySessionModel);
		sessionList.setName("sessionList");
		sessionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		panel.add(new JScrollPane(sessionList), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		completeSessionButton = new JButton("Complete");
		completeSessionButton.setName("completeSessionButton");
		completeSessionButton.setFont(new Font(FONT, Font.BOLD, 12));
		completeSessionButton.setEnabled(false);

		deleteSessionButton = new JButton("delete");
		deleteSessionButton.setName("deleteSessionButton");
		deleteSessionButton.setFont(new Font(FONT, Font.BOLD, 12));
		deleteSessionButton.setEnabled(false);
		
		buttonPanel.add(completeSessionButton);
		buttonPanel.add(deleteSessionButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		return panel;
	}

	private void setupNavigationButtons(JPanel parent) {
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
		
		toTopicPanel.addActionListener(e -> showCreateTopicView());
		toSessionPanel.addActionListener(e -> showCreateSessionView());
		
		changeViewPanel.add(toTopicPanel);
		changeViewPanel.add(toSessionPanel);
		parent.add(changeViewPanel, BorderLayout.SOUTH);
	}

	private void setupActionListeners() {
		deleteTopicButton.addActionListener(e -> handleDeleteTopicAction());
		deleteSessionButton.addActionListener(e -> handleDeleteSessionAction());
		completeSessionButton.addActionListener(e -> handleCompleteSessionAction());
		totalTimeButton.addActionListener(e -> handleTotalTimeAction());
		percentageButton.addActionListener(e -> handlePercentageAction());
	}
	
	private void setupListSelectionListeners() {
		topicList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				boolean selected = topicList.getSelectedIndex() != -1;
				deleteTopicButton.setEnabled(selected);
				totalTimeButton.setEnabled(selected);
				percentageButton.setEnabled(selected);
			}
		});

		sessionList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				boolean selected = sessionList.getSelectedIndex() != -1;
				deleteSessionButton.setEnabled(selected);
				boolean completable = selected && !sessionList.getSelectedValue().isComplete();
				completeSessionButton.setEnabled(completable);
			}
		});
	}

	private void handleDeleteTopicAction() {
		int selectedIndex = topicList.getSelectedIndex();
		if (selectedIndex != -1 && topicController != null) {
			Topic selectedTopic = topicModel.getElementAt(selectedIndex);
			topicController.handleDeleteTopic(selectedTopic.getId());
		}
	}

	private void handleDeleteSessionAction() {
		int selectedIndex = sessionList.getSelectedIndex();
		if (selectedIndex != -1 && sessionController != null) {
			StudySession selectedSession = studySessionModel.getElementAt(selectedIndex);
			sessionController.handleDeleteSession(selectedSession.getId());
		}
	}

	private void handleCompleteSessionAction() {
		int selectedIndex = sessionList.getSelectedIndex();
		if (selectedIndex != -1 && sessionController != null) {
			StudySession selectedSession = studySessionModel.getElementAt(selectedIndex);
			sessionController.handleCompleteSession(selectedSession.getId());
		}
	}

	private void handleTotalTimeAction() {
		int selectedIndex = topicList.getSelectedIndex();
		if (selectedIndex != -1 && topicController != null) {
			Topic selectedTopic = topicModel.getElementAt(selectedIndex);
			resetErrorLabels();
			topicController.handleTotalTime(selectedTopic.getId());
		}
	}

	private void handlePercentageAction() {
		int selectedIndex = topicList.getSelectedIndex();
		if (selectedIndex != -1 && topicController != null) {
			Topic selectedTopic = topicModel.getElementAt(selectedIndex);
			resetErrorLabels();
			topicController.handlePercentageOfCompletion(selectedTopic.getId());
		}
	}
	
	TopicPanel getTopicPanel() {
		return this.topicPanel;
	}
	
	SessionPanel getSessionPanel() {
		return this.sessionPanel;
	}
	
	public JButton getCompleteSessionButton() {
		return completeSessionButton;
	}
	public JButton getDeleteTopicButton() {
		return deleteTopicButton;
	}
	public JButton getDeleteSessionButton() {
		return deleteSessionButton;
	}
	public JButton getTotalTimeButton() {
		return totalTimeButton;
	}
	public JButton getPercentageButton() {
		return percentageButton;
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
		if (sessionPanel != null && sessionPanel.getTopicModel() != null) {
			sessionPanel.getTopicModel().addElement(topic);
		}
	}

	@Override
	public void onTopicRemoved(Topic topic) {
		topicRemoved(topic);
		if (sessionPanel != null && sessionPanel.getTopicModel() != null) {
			sessionPanel.getTopicModel().removeElement(topic);
		}
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
		if (topicPanel != null && topicPanel.getSessionModel() != null) {
			topicPanel.getSessionModel().addElement(session);
		}
	}
	
	@Override
	public void onSessionUpdated(StudySession updatedSession) {
	   for (int i = 0; i < studySessionModel.getSize(); i++) {
	   	StudySession session = studySessionModel.getElementAt(i);
	   	if (session.getId() == updatedSession.getId()) {
	   		session.setIsComplete(updatedSession.isComplete());
	   		studySessionModel.setElementAt(session, i);
	   		break;
	   	}
	   }
	   
	   if (topicPanel != null && topicPanel.getSessionModel() != null) {
	   	DefaultListModel<StudySession> topicSessionModel = topicPanel.getSessionModel();
	   	for (int i = 0; i < topicSessionModel.getSize(); i++) {
	   		StudySession session = topicSessionModel.getElementAt(i);
	   		if (session.getId() == updatedSession.getId()) {
	   			session.setIsComplete(updatedSession.isComplete());
	   			topicSessionModel.setElementAt(session, i);
	   			break;
	   		}
	   	}
	   }
	}

	@Override
	public void onSessionRemoved(StudySession session) {
		sessionRemoved(session);
		if (topicPanel != null && topicPanel.getSessionModel() != null) {
			topicPanel.getSessionModel().removeElement(session);
		}
	}

	@Override
	public void onSessionError(String message) {
		lblErrorMessage.setText(message);
		
	}

	public StudySessionController getSessionController() {
		return sessionController;
	}
	
	void setTopicPanel(TopicPanel topicPanel) {
		this.topicPanel = topicPanel;
	}
	
	void setSessionPanel(SessionPanel sessionPanel) {
		this.sessionPanel = sessionPanel;
	}

	
	

}
