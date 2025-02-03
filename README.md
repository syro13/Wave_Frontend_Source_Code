This project uses Gradle for dependency management.
Here’s a quick guide on how to add or update dependencies.

1. Open the build.gradle File:
   Navigate to the app/build.gradle file if you’re adding a dependency for the app module.
2. In the dependencies block, add the dependency in the following format:
   dependencies {
   implementation 'group:name:version'
   }
3. Sync the Project:
   Go to File > Sync Project with Gradle Files in Android Studio to apply the changes.

Project Dependencies so far:

1. Material Components for Android
   We use Material components like MaterialButton, TextInputLayout, and others to maintain a
   consistent and attractive UI.
2. Lottie for Animations:
   Animations are added by using LottieAnimationView, which references JSON files stored in the
   res/raw folder.
3. Google Fonts
   Fonts are selected through Android Studio’s Resource Manager or directly applied in XML using the
   fontFamily attribute.


Navigation Bar Integration:
The project features a Bottom Navigation Bar for seamless navigation between core activities like
the Dashboard, Calendar, Settings, and Profile. The navigation bar uses BottomNavigationView 
from Material Components, ensuring a consistent look and feel that aligns
with Material Design principles. It supports icon and text customization using 
itemIconTint and itemTextColor attributes.

The navigation bar is implemented as a reusable component, allowing easy integration into new activities. 
Developers can include it in their layouts using the <include> tag and configure navigation actions programmatically.
This design promotes modularity, reduces code duplication, and simplifies future updates to the navigation experience.