package com.animalloo.ui.main;

/**
 * MainActivity에서 다른 탭/화면으로 이동하기 위한 인터페이스.
 */
public interface MainNavigator {

    void navigateToTab(int bottomNavItemId);

    void navigateToRescueWithTab(int rescueTabIndex);

    void navigateToMoreSection(int section);
}
