O-Week v1.0
======
An **Android** app for incoming freshmen to use during their first week as a Cornell student. The **iOS** branch can be found [here](https://github.com/Cornell-SA-Tech/o-week-ios).

<img src="https://raw.githubusercontent.com/Cornell-SA-Tech/o-week-android/master/Screenshots/app-screenshots/Phone%20Screenshot%202.jpg" width="250px">  <img src="https://raw.githubusercontent.com/Cornell-SA-Tech/o-week-android/master/Screenshots/app-screenshots/Phone%20Screenshot%201.jpg" width="250px">  <img src="https://raw.githubusercontent.com/Cornell-SA-Tech/o-week-android/master/Screenshots/app-screenshots/Phone%20Screenshot%203.jpg" width="250px">

Getting Started
------
You will need **Android Studio 2.3.3** to run the latest version of this app, which uses the following SDKs. Last Update **6/26/2017** (remember to check "Show Package Details" in the Android SDK dialog):
 * Android 8.0 (O)
   * Android SDK Platform 26
 * Android 7.1.1 (Nougat) API
   * Android SDK Platform 25, rev 3
   * Sources for Android 25
 * Android SDK Platform-Tools 26.0.0
 * Android SDK Tools 26.0.2
 * Android Support Library 23.2.1 (Windows only)
 * Documentation for Android SDK
 * Google Play Services, rev 41
 * Google USB Driver (Windows only, use newest ver)
 * Support Repository
   * ConstraintLayout for Android 1.0.2
   * Solver for ConstraintLayout 1.0.2
   * Android Support Repository, rev 47
   * Google Repository 54
   
Activity Hierarchy Tree
------
```
MainApplication
  MainActivity
    FeedFragment
      FeedAdapter
        FeedCell
    ScheduleFragment
    DatePickerAdapter
      SpacingItemDecorator
      DateCell
  SettingsActivity
    SettingsFragment
  DetailsActivity
  
```

Design Choices
------
 * All objects are presumed to **not be <code>null</code>** when passed into a method as a parameter. If an object can be null, use the annotation <code>@Nullable</code>.
 * Syntax:
   * Tabs > Spaces
   * Put curly braces on a new line, like so:
   ```java
   if (blah)
   {
      doSomething();
      doSomethingElse();
   }
   ```
   * If a statement is singleLine, don't use brackets at all, like so:
   ```java
   if (blah)
      doSomething();
   ```
   * ClassesShouldBeNamedLikeThis, as should enums and interfaces. (upper camel case)
   * methodsShouldBeNamedLikeThis, as should non-static or non-final variables. (lower camel case)
   * STATIC_VARS_SHOULD_BE_NAMED_LIKE_THIS, as should any final variables (or variables whose values shouldn't be changed).
 * **RecyclerView**s are used instead of ListViews. Each RecyclerView should have a separate **Adapter** class and at least 1 **ViewHolder** class (one for each type of cell supported). RecyclerViews are encouraged over ListViews since we don't need to re-find views' IDs when "recycling" them – creating only the number of cells that will appear on screen and changing the data they show when the user scrolls.
 * <code>TAG</code>s are set on the top of some classes for logging. Set up a shortcut to easily create <code>TAG</code>s for classes by following <a href="https://stackoverflow.com/a/29378779/4028758">this</a> article.
 * An "Event" can refer to 2 things, judging on context:
   1. An <code>Event</code> that will occur during orientation week.
   2. Something to notify listeners of. For example, a click event.
 
Used Libraries
------
 * <a href="https://github.com/google/guava">Guava</a> - a Google Library containing lots of helpful classes for Java. Most notably, immutable data structures (like ImmutableList) and EventBus, which provides a way for classes that do not have references to each other to communicate.
 * <a href="https://github.com/dlew/joda-time-android">JodaTime</a> - a library for immutable time objects, unsupported by Java 7. Includes lots of useful data structures and methods; plus, immutable objects are almost always safer when passing by reference.

Contributors
------
 * **Julia Kruk** - Project Manager
 * **Juhwan Park** - Project Manager
 * **David Chu** - Front-End Developer
 * **Vicente Caycedo** - Front-End Developer
 * **Arnav Ghosh** - Back-End Developer
 
We are a team within **Cornell SA Tech**, a project team affiliated with Cornell Undergraduate Student Assembly led by Alice Pham. For more information, see its website [here](http://www.cornellsatech.org/).
