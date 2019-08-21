# O-Week v3.3.3

#### Contents
  - [About](#about)
  - [Getting Started](#getting-started)
  - [FAQ](#faq)
  - [Dependencies & Libraries](#dependencies--libraries)
  - [Screenshots](#screenshots)
  - [Contributors](#contributors)
  
## About
An **Android** app for incoming freshmen to use during their first week as a Cornell student. Available on the Play Store [here](https://play.google.com/store/apps/details?id=com.cornellsatech.o_week&hl=en_US). The **iOS** branch can be found [here](https://github.com/cornell-dti/o-week-ios).

## Getting Started
You will need **Android Studio 3.5** to run the latest version of this app, which uses the following SDKs. Remember to check "Show Package Details" on the lower right:

SDK Platforms (tab)
 * Android 10.0 (Q)
   * Android SDK Platform 29, Revision 1

SDK Tools (tab)
 * Android SDK Build-Tools 29.0.2
 * Android Emulator (if you don't have an Android phone)
 * Intel x86 Emulator Accelerator (HAXM installer) (if you don't have an Android phone)
 * Android SDK Platform-Tools 29.0.2
 * Android SDK Tools 26.1.1
 * Documentation for Android SDK 1
 * Google Play Services 49
 * Google USB Driver 11 (if you have an Android phone)
   
_Last updated **8/21/2019**._

## FAQ
#### The project doesn't open correctly.
Make sure Android Studio imports `o-week-android/O-week`, not `o-week-android` itself, which contains folders for other resources.

#### The map isn't working.
Map API keys are managed on <a href="https://console.cloud.google.com/apis/credentials?project=oweek-1496849141291">Google Cloud Platform</a> and access is restricted, which is why the key is exposed in the repository. 

If the map isn't working when you run the app from your local machine, add your development key to the `Android API Key`. You can find instructions by clicking `Android API Key`.

If the map isn't working in production, make sure you're generating the release file with the correct signing key. (Ask developer leads for it if you don't have it.) If you are, then make sure `Android API Key` contains Google's signing key, which can be found in Google Play Console <a href="https://play.google.com/apps/publish/?account=8943927778040647949#KeyManagementPlace:p=com.cornellsatech.o_week&appid=4976206469974389603">here</a>.

## Design Choices
 * All objects are presumed to **not be <code>null</code>** when passed into a method as a parameter. If an object can be null, use the annotation <code>@Nullable</code>.
 * Syntax:
   * Indent with tabs.
   * Put curly braces on a new line, like so:
   ```java
   if (blah)
   {
      doSomething();
      doSomethingElse();
   }
   ```
   * If a statement fits in a single line, don't use brackets at all, like so:
   ```java
   if (blah)
      doSomething();
   ```
   * ClassesShouldBeNamedLikeThis, as should enums and interfaces. (upper camel case)
   * methodsShouldBeNamedLikeThis, as should non-static or non-final variables. (lower camel case)
   * STATIC_VARS_SHOULD_BE_NAMED_LIKE_THIS, as should any final variables (or variables whose values shouldn't be changed).
 * **RecyclerView**s are used instead of ListViews. Each RecyclerView should have a separate **Adapter** class and at least 1 **ViewHolder** class.
 * <code>TAG</code>s are set on the top of some classes for logging. Set up a shortcut to easily create <code>TAG</code>s for classes by following <a href="https://stackoverflow.com/a/29378779/4028758">this</a> article.
 * An "Event" can refer to 2 things, judging on context:
   1. An <code>Event</code> that will occur during orientation week.
   2. Something to notify listeners of. For example, a click event.
 
## Dependencies & Libraries
 * <a href="https://projectlombok.org/features/all">Lombok</a> - a library that provides annotations for creating getters, setters, builders, `toString()`, `hashCode()`, `equals()`, etc in Java. Makes code cleaner and easier to write.
  * <a href="https://github.com/google/gson">GSON</a> - a Google library that reads in JSON objects and wraps them in Java Bean objects automatically, making type-checking and code cleaner.
 * <a href="https://github.com/google/guava">Guava</a> - a Google library containing lots of helpful classes for Java. Most notably, immutable data structures (like ImmutableList) and EventBus, which provides a way for classes that do not have references to each other to communicate.
 * <a href="https://github.com/dlew/joda-time-android">JodaTime</a> - a library for immutable time objects, unsupported by Java 7. Includes lots of useful data structures and methods; plus, immutable objects are almost always safer when passing by reference.

## Screenshots

_Screenshots showing major parts of app_

<img src="https://raw.githubusercontent.com/cornell-dti/o-week-android/master/Screenshots/1.png" width="250px" style="margin: 10px; border: 1px rgba(0,0,0,0.4) solid;"> <img src="https://raw.githubusercontent.com/cornell-dti/o-week-android/master/Screenshots/2.png" width="250px" style="margin: 10px; border: 1px rgba(0,0,0,0.4) solid;"> <img src="https://raw.githubusercontent.com/cornell-dti/o-week-android/master/Screenshots/3.png" width="250px" style="margin: 10px; border: 1px rgba(0,0,0,0.4) solid;">

## Contributors
2019
 * **David Chu** - Developer Lead
 * **Evan Welsh** - Developer Lead

2018
 * **David Chu** - Developer Lead

2017
 * **David Chu** - Product Manager
 * **Amanda Ong** - Front-End Developer
 * **Jagger Brulato** - Front-End Developer
 * **Qichen (Ethan) Hu** - Front-End Developer

2016
 * **David Chu** - Front-End Developer

We are a team within **Cornell Design & Tech Initiative**. For more information, see its website [here](https://cornelldti.org/).
<img src="https://raw.githubusercontent.com/cornell-dti/design/master/Branding/Wordmark/Dark%20Text/Transparent/Wordmark-Dark%20Text-Transparent%403x.png">
