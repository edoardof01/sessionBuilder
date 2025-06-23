package com.sessionBuilder.core;

import java.time.LocalDate;
import java.util.List;

public interface StudySessionRepositoryInterface {

	StudySession findById(long id);
	StudySession findByDateDurationAndNote(LocalDate date, int duraiton, String note);
	void save(StudySession session);
	void update(StudySession session);
	void delete(long id);
	List<StudySession> findAll();
}
