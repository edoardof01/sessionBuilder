package com.sessionbuilder.swing;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.sessionbuilder.core.StudySession;
import com.sessionbuilder.core.StudySessionController;
import com.sessionbuilder.core.Topic;
import com.toedter.calendar.JDateChooser;

public class SessionPanel extends JPanel {

	private static final long serialVersionUID = 3L;
	private DefaultListModel<Topic> topicModel;
	private JLabel errorLbl;
	private JDateChooser dateChooser;
	private JList<Topic> sessionPaneltopicList;
	private transient StudySessionController sessionController;
	private TopicAndSessionManager managerView;
	
	private static final String FONT = "Dialog";
	
	public SessionPanel (DefaultListModel<Topic> sharedTopicModel) {
		
		setBorder(new EmptyBorder(5,5,5,5));
		setLayout(new BorderLayout());
		
		JPanel formPanel = new JPanel();
		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
		
		JPanel datePanel = new JPanel(new BorderLayout(5,5));
		JLabel dateLbl = new JLabel("Date:");
		dateLbl.setPreferredSize(new Dimension(90, 20));
		dateLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		dateLbl.setFont(new Font(FONT, Font.BOLD, 14));
		dateChooser = new JDateChooser();
		dateChooser.setName("dateChooser");
		datePanel.add(dateLbl, BorderLayout.WEST);
		datePanel.add(dateChooser, BorderLayout.CENTER);
		
		JPanel durationPanel = new JPanel(new BorderLayout(5,0));
		JLabel durationLbl = new JLabel("Duration:");
		
		durationLbl.setPreferredSize(new Dimension(90, 20));
		durationLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		durationLbl.setFont(new Font(FONT, Font.BOLD, 14));
		durationLbl.setName("durationLbl");
		JTextField durationField = new JTextField();
		durationField.setName("durationField");
		durationPanel.add(durationLbl, BorderLayout.WEST);
		durationPanel.add(durationField, BorderLayout.CENTER);
		
		JPanel notePanel = new JPanel(new BorderLayout(5,0));
		JLabel noteLabel = new JLabel("Note:");
		noteLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		noteLabel.setFont(new Font(FONT, Font.BOLD, 14));
		noteLabel.setName("difficultyLbl");
		noteLabel.setPreferredSize(new Dimension(90, 20));
		noteLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		JTextField noteField = new JTextField();
		noteField.setName("noteField");
		notePanel.add(noteLabel, BorderLayout.WEST);
		notePanel.add(noteField, BorderLayout.CENTER);
		
		formPanel.add(datePanel);
		formPanel.add(Box.createVerticalStrut(15));
		formPanel.add(durationPanel);
		formPanel.add(Box.createVerticalStrut(15));
		formPanel.add(notePanel);
		formPanel.add(Box.createVerticalStrut(7));
		
		JLabel topicLabel = new JLabel("Topics:");
		topicLabel.setName("topicLbl");
		topicLabel.setFont(topicLabel.getFont().deriveFont(Font.BOLD, 16f));
		
		this.topicModel = sharedTopicModel;
		sessionPaneltopicList = new JList<>(topicModel);
		sessionPaneltopicList.setName("sessionPanelTopicList");
		
		JScrollPane topics = new JScrollPane(sessionPaneltopicList);
		JPanel topicPanel = new JPanel(new BorderLayout());
		topicPanel.add(topicLabel, BorderLayout.NORTH);
		topicPanel.add(topics, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		JPanel errorPanel = new JPanel();
		errorLbl = new JLabel();
		errorLbl.setForeground(Color.red);
		errorLbl.setName("sessionErrorMessage");
		errorPanel.add(errorLbl);
		
		JButton addSessionButton = new JButton("add");
		addSessionButton.setName("addSessionButton");
		JPanel sessionButtonPanel = new JPanel(new FlowLayout());
		sessionButtonPanel.add(addSessionButton);
		addSessionButton.setEnabled(false);
		
		sessionPaneltopicList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				addSessionButton.setEnabled(dateChooser.getDate() != null &&
					!durationField.getText().trim().isEmpty() && 
					!noteField.getText().trim().isEmpty() && 
					sessionPaneltopicList.getSelectedIndex() != -1);
			}
		});
		
		bottomPanel.add(errorPanel, BorderLayout.NORTH);
		bottomPanel.add(sessionButtonPanel, BorderLayout.CENTER);
		
		JButton backSessionButton = new JButton("Back");
		backSessionButton.setName("backSessionButton");
		backSessionButton.addActionListener(e -> {
			if (managerView != null) {
				managerView.showMainView();
			}
		});
		JPanel backSessionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		backSessionButtonPanel.add(backSessionButton);
		bottomPanel.add(backSessionButtonPanel, BorderLayout.SOUTH);
		
		add(formPanel, BorderLayout.NORTH);
		add(topicPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		
		dateChooser.addPropertyChangeListener("date", e -> {
			if (dateChooser.getDate() != null) {
				addSessionButton.setEnabled(
					!durationField.getText().trim().isEmpty() && 
					!noteField.getText().trim().isEmpty() && 
					sessionPaneltopicList.getSelectedIndex() != -1
				);
			} else {
				addSessionButton.setEnabled(false);
			}
		});

		KeyAdapter btnAddEnabler = new KeyAdapter() {
		    @Override
		    public void keyReleased(KeyEvent e) {
		        addSessionButton.setEnabled(dateChooser.getDate() != null &&
		            !durationField.getText().trim().isEmpty() && 
		            !noteField.getText().trim().isEmpty() && sessionPaneltopicList.getSelectedIndex()!=-1);
		    }
		};

		durationField.addKeyListener(btnAddEnabler);
		noteField.addKeyListener(btnAddEnabler);
		
		addSessionButton.addActionListener(e -> {
		    if (sessionController != null) {
		        Date date = dateChooser.getDate();
		        Instant instant = date.toInstant();
		        ZoneId zoneId = ZoneId.systemDefault();
		        LocalDate localDate = instant.atZone(zoneId).toLocalDate();
		        List<Topic> selectedTopics = sessionPaneltopicList.getSelectedValuesList();
		        try {
			        sessionController.handleCreateSession(localDate, Integer.parseInt(durationField.getText()), 
			                noteField.getText(), new ArrayList<>(selectedTopics));
			        errorLbl.setText("");
		        } catch (Exception ex) {
		        	showGeneralError("Errore nel salvare la sessione: " + ex.getMessage());
		        }
		    }
		});
		
	}
	
	public DefaultListModel<Topic> getTopicModel() {
		return topicModel;
	}
	
	public void setSessionController(StudySessionController sessionController) {
		this.sessionController = sessionController;
	}

	public void showSessionError(String message, StudySession session) {
		errorLbl.setText(message + ": " + session);
	}
	
	public void showGeneralError(String error) {
		errorLbl.setText(error);
	}
	
	public void setManagerView(TopicAndSessionManager managerView) {
		this.managerView = managerView;
	}
	
}
