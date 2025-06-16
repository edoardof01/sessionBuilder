package com.sessionBuilder.core;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.junit.runners.Parameterized;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Parameterized.class)
public class TopicDecreaseMasteryLevelTest {
	
	@Parameterized.Parameters(name = "initialLevel={0}, decreaseBy={1}, expectedResult={2}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{10, 5, 5},
			{10, 10, 0},
			{10, 11, 0},
			{10, 9, 1},
			{10, 0, 10},
			{0, 10, 0}
		});
	}
	
	@Parameterized.Parameter(0)
	public int initialLevel;
	
	@Parameterized.Parameter(1)
	public int decreaseBy;
	
	@Parameterized.Parameter(2)
	public int expectedResult;
	
	private Topic topic;
	
	@Before
	public void setUp() {
		topic = new Topic("Test", "Test description", 3, new ArrayList<>());
	}
	
	@Test
	public void testDecreaseMasteryLevel() {
		topic.setMasteryLevel(initialLevel);
		topic.decreaseMasteryLevel(decreaseBy);
		assertThat(topic.getMasteryLevel()).isEqualTo(expectedResult);
	}
}
