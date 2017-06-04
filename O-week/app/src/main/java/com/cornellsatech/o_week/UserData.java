package com.cornellsatech.o_week;

import android.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserData
{
	public static final Map<LocalDate, List<Event>> allEvents;
	public static final Map<LocalDate, List<Event>> selectedEvents;
	public static final List<LocalDate> DATES;
	public static LocalDate selectedDate;
	private static final int YEAR = 2017;
	private static final int MONTH = 8;
	private static final int START_DAY = 19;    //Dates range: [START_DAY, END_DAY], inclusive
	private static final int END_DAY = 24;      //Note: END_DAY must > START_DAY
	public static final String TAG = "UserData";

	//initialize DATES
	static
	{
		ImmutableList.Builder<LocalDate> tempDates = ImmutableList.builder();
		ImmutableMap.Builder<LocalDate, List<Event>> tempAllEvents = ImmutableMap.builder();
		ImmutableMap.Builder<LocalDate, List<Event>> tempSelectedEvents = ImmutableMap.builder();
		LocalDate today = LocalDate.now();
		for (int i = START_DAY; i <= END_DAY; i++)
		{
			LocalDate date = new LocalDate(YEAR, MONTH, i);
			if (date.isEqual(today))
				selectedDate = date;
			tempDates.add(date);
			tempAllEvents.put(date, new ArrayList<Event>());
			tempSelectedEvents.put(date, new ArrayList<Event>());
		}
		DATES = tempDates.build();
		allEvents = tempAllEvents.build();
		selectedEvents = tempSelectedEvents.build();

		if (selectedDate == null)
			selectedDate = DATES.get(0);
	}

	//suppress instantiation
	private UserData(){}
	
	static boolean allEventsContains(Event event)
	{
		LocalDate date = event.startTime.toLocalDate();
		List<Event> eventsForDate = allEvents.get(date);
		return eventsForDate != null && eventsForDate.contains(event);
	}
	static boolean selectedEventsContains(Event event)
	{
		LocalDate date = event.startTime.toLocalDate();
		List<Event> eventsForDate = selectedEvents.get(date);
		return eventsForDate != null && eventsForDate.contains(event);
	}
	static void appendToAllEvents(Event event)
	{
		LocalDate date = event.startTime.toLocalDate();
		List<Event> eventsForDate = allEvents.get(date);
		if (eventsForDate == null)
		{
			Log.e(TAG, "appendToAllEvents: attempted to add event with date outside orientation");
			return;
		}
		eventsForDate.add(event);
	}
	static void insertToSelectedEvents(Event event)
	{
		LocalDate date = event.startTime.toLocalDate();
		List<Event> eventsForDate = selectedEvents.get(date);
		if (eventsForDate == null)
		{
			Log.e(TAG, "insertToSelectedEvents: attempted to add event with date outside orientation");
			return;
		}
		eventsForDate.add(event);
	}
	static void removeFromSelectedEvents(Event event)
	{
		LocalDate date = event.startTime.toLocalDate();
		List<Event> eventsForDate = selectedEvents.get(date);
		if (eventsForDate == null)
			Log.e(TAG, "removeFromSelectedEvents: No selected events for date");
		else
			eventsForDate.remove(event);
	}

	static void loadData()
	{
		//TODO Fix conditional statement, fetch data from DB and compare to Core Data to remove outdated events or add new events. Adding temp data for testing
		Event[] events = new Event[]{

				new Event("Move In", "Multiple locations", "Students should plan to move into their residence halls between 9:00am and 12:00pm on Thursday, January 19. Orientation volunteers will help you move your belongings and answer any questions that you may have. Plan on picking up your key to your room at your service center before heading over to your residence hall. If you are living off campus, we also recommend moving in on Thursday so you can attend First Night at 8:00pm that evening.", null,
						new DateTime(2017, 8, 19, 9, 0), new DateTime(2017, 8, 19, 12, 0), false, 1),
				new Event("New Student Check-In and Welcome Reception", "Willard Straight Hall, 4th Floor Rooms", "You are required to attend New Student Check-In in the Memorial Room to verify your matriculation and registration requirements. Please arrive anytime between 1:00pm and 2:30pm as representatives from across campus will also be available to answer questions and to better acquaint you with university services. Light refreshments will be available for students and parents throughout the fourth floor of Willard Straight Hall.", null,
						new DateTime(2017, 8, 19, 13, 0), new DateTime(2017, 8, 19, 15, 0), false, 2),
				new Event("First Night", "Klarman Atrium, Klarman Hall", "It's your first night at Cornell! Meet your January Orientation Leader (JOL) and then mingle with your classmates and get a taste of what Ithaca has to offer - literally! There will be free food and drinks as well as games and activities. You won't want to miss it!", null,
						new DateTime(2017, 8, 19, 19, 0), new DateTime(2017, 8, 19, 20, 0), false, 3),
				new Event("Cornell Essentials", "Kaufman Auditorium, G64 Goldwin Smith Hall", "Hear from upper-level students and alumni about their own introduction to Cornell. Learn how to navigate the university, deal with setbacks, find balance, and take advantage of the multitude of campus resources available. All new first-year and transfer students must attend this event. First year students will walk to the FYSA Class Photo event from Goldwin Smith Hall.", null,
						new DateTime(2017, 8, 20, 15, 0), new DateTime(2017, 8, 20, 16, 0), false, 4),
				new Event("Welcome Dinner", "Becker House, Robert Purcell Marketplace Eatery", "Join us on West Campus in the Becker House Dining Room or on North Campus in the Robert Purcell Marketplace Eatery. If you don’t have a meal plan, don’t worry, we’ve got you covered at the door. Students living in the Collegetown area and West Campus are encouraged to go to Becker House. FYSAs and students living on North Campus are encouraged to go to RPCC.", null,
						new DateTime(2017, 8, 20, 6, 0), new DateTime(2017, 8, 20, 7, 30), false, 5),
				new Event("Coffee Hour", "Café Jennie, The Cornell Store", "Visit Café Jennie in The Cornell Store for free coffee and hot chocolate! Join in on casual conversation with both new and current students to discuss life at Cornell.", null,
						new DateTime(2017, 8, 21, 10, 0), new DateTime(2017, 8, 21, 11, 0), false, 6),
				new Event("Laser Tag", "2nd Floor, RPCC", "Calling all First Years! You've proven you can handle yourself in a classroom, but how will you fair in a blood pumping, heart racing laser fight? Join your fellow Cornellians at Barskis Xtreme Lazer Tag for an adrenaline-fueled test of agility, precision, and wit.", null,
						new DateTime(2017, 8, 21, 13, 0), new DateTime(2017, 8, 21, 15, 0), false, 7),
				new Event("Study Smarter, Not Harder", "Lewis Auditorium, G76 Goldwin Smith Hall", "Are you ready to conquer procrastination and stress while maximizing your learning experience? Join the Learning Strategies Center's Mile Chen and learn how to make the most of your study skills. Get ahead of the game!", null,
						new DateTime(2017, 8, 22, 11, 0), new DateTime(2017, 8, 22, 12, 0), false, 8),
				new Event("Explore Downtown Ithaca", "Risley or Schwartz Center Bus Stop", "Interested in learning about downtown Ithaca? Want to take advantage of your free bus pass? Come learn about the TCAT bus system and get acquainted with downtown Ithaca through a series of group activities on the Commons. Win free samples and prizes. We will meet at the bus stop in front of Risley Hall or Schwartz Center and take the bus down together, snow or shine.", null,
						new DateTime(2017, 8, 22, 12, 0), new DateTime(2017, 8, 22, 15, 0), false, 9),
				new Event("Cuddles and Chocolate", "Memorial Room, WSH", "Play with puppies from Guiding Eyes for the Blind at Cornell during this afternoon of hot chocolate and cuddles! Guiding Eyes for the Blind at Cornell strives to teach students to learn more about guide dog training and provide support for the Guiding Eyes for the Blind Finger Lakes Region.", null,
						new DateTime(2017, 8, 23, 13, 0), new DateTime(2017, 8, 23, 14, 0), false, 10),
				new Event("Learning Where You Live", "3331 Tatkon Center", "Want to take a small class where you get to know the professor and the other students? Curious to learn about a subject that has nothing to do with your intended major? Want to explore a really interesting subject without the pressure of grades? Come check out a few of the one-credit courses being taught on North Campus this year.", null,
						new DateTime(2017, 8, 23, 16, 0), new DateTime(2017, 8, 23, 17, 0), false, 11),
				new Event("Orientation Finale at the Tatkon Center", "Tatkon Center", "Join us for a celebration! Orientation may be coming to a close, but your first semester at Cornell is just getting started. Mingle with friends, meet current students, and get excited for a great semester. Don’t miss the refreshments and giveaways. JOLs will also introduce you to the Tatkon Center, Cornell’s academic resource center for first-year students.", null,
						new DateTime(2017, 8, 24, 11, 0), new DateTime(2017, 8, 24, 13, 0), false, 12)
		};

		Set<LocalDate> dates = new HashSet<>();
		for (Event event : events)
		{
			appendToAllEvents(event);
			if (!dates.contains(event.startTime.toLocalDate()))
				dates.add(event.startTime.toLocalDate());
		}

		//Telling other classes to reload their data
		NotificationCenter.DEFAULT.post(new NotificationCenter.EventReload());
	}
}
