package com.sessionbuilder.core;
import java.util.Map;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EmfFactory {
	private EmfFactory() {}
	public static EntityManagerFactory createEntityManagerFactory(
		String persistenceUnit,
		Map<String,String> properties
	) {
		return Persistence.createEntityManagerFactory(persistenceUnit, properties);
	}
}