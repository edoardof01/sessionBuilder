package com.sessionBuilder.swing;

import java.awt.EventQueue;

public class SessionBuilderApplication {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TopicAndSessionManager frame = new TopicAndSessionManager();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
