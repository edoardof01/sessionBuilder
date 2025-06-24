package com.sessionBuilder.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.sessionBuilder.core.StudySession;
import com.sessionBuilder.core.Topic;
import com.sessionBuilder.core.TopicController;

public class TopicPanel extends JPanel {
	
	private static final long serialVersionUID = 2L;
	private DefaultListModel<StudySession> sessionModel;
	private JLabel errorLbl;
	private transient TopicController topicController;
	private TopicAndSessionManager managerView;
	
	private static final String FONT = "Dialog";
	
	public TopicPanel(DefaultListModel<StudySession> sharedSessionModel) {
		setBorder(new EmptyBorder(5,5,5,5));
		setLayout(new BorderLayout());
		
		JPanel formPanel = new JPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
		
		// Name panel
		JPanel namePanel = new JPanel((new BorderLayout(5,0)));
		JLabel nameLabel = new JLabel("Name:");
		nameLabel.setPreferredSize(new Dimension(90, 20));
		nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		nameLabel.setFont(new Font(FONT, Font.BOLD, 14));
		nameLabel.setName("nameLbl");
		JTextField nameField = new JTextField();
		nameField.setName("nameField");
		namePanel.add(nameLabel, BorderLayout.WEST);
		namePanel.add(nameField, BorderLayout.CENTER);
		
		JPanel descriptionPanel = new JPanel(new BorderLayout(5,0));
		JLabel descriptionLabel = new JLabel("Description:");
		
		descriptionLabel.setPreferredSize(new Dimension(90, 20));
		descriptionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		descriptionLabel.setFont(new Font(FONT, Font.BOLD, 14));
		descriptionLabel.setName("descriptionLbl");
		JTextField descriptionField = new JTextField();
		descriptionField.setName("descriptionField");
		descriptionPanel.add(descriptionLabel, BorderLayout.WEST);
		descriptionPanel.add(descriptionField, BorderLayout.CENTER);
		
		JPanel difficultyPanel = new JPanel(new BorderLayout(5,0));
		JLabel difficultyLabel = new JLabel("Difficulty:");
		
		difficultyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		difficultyLabel.setFont(new Font(FONT, Font.BOLD, 14));
		difficultyLabel.setName("difficultyLbl");
		difficultyLabel.setPreferredSize(new Dimension(90, 20));
		difficultyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		JTextField difficultyField = new JTextField();
		difficultyField.setName("difficultyField");
		difficultyPanel.add(difficultyLabel, BorderLayout.WEST);
		difficultyPanel.add(difficultyField, BorderLayout.CENTER);
		
		
		formPanel.add(namePanel);
		formPanel.add(Box.createVerticalStrut(15));
		formPanel.add(descriptionPanel);
		formPanel.add(Box.createVerticalStrut(15));
		formPanel.add(difficultyPanel);
		formPanel.add(Box.createVerticalStrut(7));
		
		// Sessions
		JLabel sessionLabel = new JLabel("Sessions:");
		sessionLabel.setName("sessionLbl");
		sessionLabel.setFont(sessionLabel.getFont().deriveFont(Font.BOLD, 16f));
		
		this.sessionModel = sharedSessionModel;
		JList<StudySession> sessionList = new JList<>(sessionModel);
		sessionList.setName("topicPanelSessionList");
		JScrollPane sessions = new JScrollPane(sessionList);
		
		JPanel sessionPanel = new JPanel(new BorderLayout());
		sessionPanel.add(sessionLabel, BorderLayout.NORTH);
		sessionPanel.add(sessions, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		JButton addTopicButton = new JButton("add");
		addTopicButton.setName("addTopicButton");
		JPanel topicButtonPanel = new JPanel(new FlowLayout());
		topicButtonPanel.add(addTopicButton);
		addTopicButton.setEnabled(false);
		errorLbl = new JLabel();
		errorLbl.setName("errorTopicPanelLbl");
		errorLbl.setForeground(Color.red);
		difficultyLabel.setFont(new Font(FONT, Font.BOLD, 14));
		JPanel errorPanel = new JPanel();
		errorPanel.add(errorLbl);
		bottomPanel.add(topicButtonPanel, BorderLayout.CENTER);
		bottomPanel.add(errorPanel, BorderLayout.NORTH);
		
		JButton backButton = new JButton("Back");
		backButton.setName("backButton");
		backButton.addActionListener(e -> {
			if (managerView != null) {
				managerView.showMainView();
			}
		});
		JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		backButtonPanel.add(backButton);
		bottomPanel.add(backButtonPanel, BorderLayout.SOUTH);
		
		add(formPanel, BorderLayout.NORTH);
		add(sessionPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		
		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				addTopicButton.setEnabled( !nameField.getText().trim().isEmpty() &&
						!descriptionField.getText().trim().isEmpty() && !difficultyField.getText().trim().isEmpty());
			}
		};
		nameField.addKeyListener(btnAddEnabler);
		descriptionField.addKeyListener(btnAddEnabler);
		difficultyField.addKeyListener(btnAddEnabler);
		
		addTopicButton.addActionListener( e -> {
			if (topicController != null) {
				List<StudySession> selectedSessions = sessionList.getSelectedValuesList();
				try {
					topicController.handleCreateTopic(nameField.getText(), descriptionField.getText() , 
							Integer.parseInt(difficultyField.getText()), new ArrayList<>(selectedSessions));
					errorLbl.setText(" ");
				} catch (Exception ex) {
					showGeneralError("Errore nel salvare il topic: " + ex.getMessage());
				}
			}
		});
		
	}
	
	public void setManagerView (TopicAndSessionManager managerView) {
		this.managerView = managerView;
	}
	
	public DefaultListModel<StudySession> getSessionModel(){
		return sessionModel;
	}
	
	public void showTopicError(String message, Topic topic) {
		errorLbl.setText(message + ": " + topic);
	}

	public void showGeneralError(String string) {
		errorLbl.setText(string);
	}
	
	public void setTopicController(TopicController controller) {
		this.topicController = controller;
	}
	
	
	
	
}